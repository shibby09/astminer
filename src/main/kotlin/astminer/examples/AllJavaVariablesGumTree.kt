package astminer.examples

import astminer.common.getNormalizedToken
import astminer.common.model.LabeledPathContexts
import astminer.parse.java.NamedGumTreeJavaParser
import astminer.paths.CsvPathStorage
import astminer.paths.PathMiner
import astminer.paths.PathRetrievalSettings
import astminer.paths.toPathContext
import java.io.File

//Retrieve paths from Java files, using a GumTree parser.
fun allJavaVariablesGumTree() {
    val inputDir = "./testData/gumTreeMethodSplitter/"
    val maxPathContexts = 200;
    val miner = PathMiner(PathRetrievalSettings(4, 4))
    val outputDir = "out_examples/allJavaFilesGumTree"
    val storage = CsvPathStorage(outputDir)
    File(inputDir).forFilesWithSuffix(".java") { file ->
        val node = NamedGumTreeJavaParser().parse(file.inputStream()) ?: return@forFilesWithSuffix
        val paths = miner.retrieveVariablePaths(node, file.absolutePath)

        for ((key, value) in paths) {
            storage.store(LabeledPathContexts(key, value.take(maxPathContexts).map { astPath ->
                toPathContext(astPath) { node ->
                    node.getNormalizedToken()
                }
            }))
        }
    }
    storage.save()
}
