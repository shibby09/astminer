package cli

import astminer.common.getNormalizedToken
import astminer.common.model.LabeledPathContexts
import astminer.common.model.Node
import astminer.common.model.Parser
import astminer.common.preOrder
import astminer.common.setNormalizedToken
import astminer.parse.java.NamedGumTreeJavaParser
import astminer.paths.CsvPathStorage
import astminer.paths.PathMiner
import astminer.paths.PathRetrievalSettings
import astminer.paths.toPathContext
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import java.io.File

class VariablePathContextsExtractor : CliktCommand() {

    /**
     * @param parser class that implements parsing
     * @param extension file extension to choose files for parsing
     */
    private data class SupportedLanguage(val parser: Parser<out Node>, val extension: String)

    /**
     * List of supported language extensions and corresponding parsers.
     */
    private val supportedLanguages = listOf(
        SupportedLanguage(NamedGumTreeJavaParser(), "java")
    )

    val extensions: List<String> by option(
        "--lang",
        help = "File extensions that will be parsed"
    ).split(",").default(supportedLanguages.map { it.extension })

    val projectRoot: String by option(
        "--project",
        help = "Path to the project that will be parsed"
    ).required()

    val outputDirName: String by option(
        "--output",
        help = "Path to directory where the output will be stored"
    ).required()

    val maxPathHeight: Int by option(
        "--maxH",
        help = "Maximum height of path for code2vec"
    ).int().default(8)

    val maxPathWidth: Int by option(
        "--maxW",
        help = "Maximum width of path. " +
                "Note, that here width is the difference between token indices in contrast to the original code2vec."
    ).int().default(3)

    val maxPathContexts: Int by option(
        "--maxContexts",
        help = "Number of path contexts to keep from each method."
    ).int().default(500)

    val maxTokens: Long by option(
        "--maxTokens",
        help = "Keep only contexts with maxTokens most popular tokens."
    ).long().default(Long.MAX_VALUE)

    val maxPaths: Long by option(
        "--maxPaths",
        help = "Keep only contexts with maxTokens most popular paths."
    ).long().default(Long.MAX_VALUE)

    private fun getParser(extension: String): Parser<out Node> {
        for (language in supportedLanguages) {
            if (extension == language.extension) {
                return language.parser
            }
        }
        throw UnsupportedOperationException("Unsupported extension $extension")
    }

    fun extractVariablePathContexts() {
        val outputDir = File(outputDirName)
        for (extension in extensions) {
            val miner = PathMiner(PathRetrievalSettings(maxPathHeight, maxPathWidth))
            val storage = CsvPathStorage(outputDirName)
            val parser = getParser(extension)
            val parsedFiles = parser.parseWithExtension(File(projectRoot), extension)

            parsedFiles.forEach { parseResult ->
                val root = parseResult.root ?: return@forEach
                val filePath = parseResult.filePath

                root.preOrder().forEach { node -> node.setNormalizedToken() }

                val paths = miner.retrieveVariablePaths(root, filePath)

                for ((key, value) in paths) {
                    storage.store(LabeledPathContexts(key, value.take(maxPathContexts).map { astPath ->
                        toPathContext(astPath) { node ->
                            node.getNormalizedToken()
                        }
                    }))
                }
            }

            val outputDirForLanguage = outputDir.resolve(extension)
            outputDirForLanguage.mkdir()
            // Save stored data on disk
            storage.save(maxPaths, maxTokens)
        }
    }

    override fun run() {
        extractVariablePathContexts()
    }
}