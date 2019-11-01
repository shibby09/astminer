package astminer.parse.java

import astminer.common.model.Parser
import com.github.gumtreediff.client.Run
import com.github.gumtreediff.gen.jdt.AbstractJdtTreeGenerator
import com.github.gumtreediff.gen.jdt.AbstractJdtVisitor
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator
import com.github.gumtreediff.gen.jdt.JdtVisitor
import com.github.gumtreediff.tree.TreeContext
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit
import java.io.*

class GumTreeJavaParser : Parser<GumTreeJavaNode> {
    init {
        Run.initGenerators()
    }

    override fun parse(content: InputStream): GumTreeJavaNode? {
        val treeContext = JdtTreeGenerator().generate(InputStreamReader(content))
        return wrapGumTreeNode(treeContext)
    }
}


class NamedGumTreeJavaParser : Parser<NamedGumTreeJavaNode> {
    init {
        Run.initGenerators()
    }

    override fun parse(content: InputStream): NamedGumTreeJavaNode? {
        val treeContext = LineAwareTreeGenerator().generate(InputStreamReader(content))
        return wrapNamedGumTreeNode(treeContext)
    }
}

fun wrapGumTreeNode(treeContext: TreeContext): GumTreeJavaNode {
    return GumTreeJavaNode(treeContext.root, treeContext, null)
}

fun wrapNamedGumTreeNode(treeContext: TreeContext): NamedGumTreeJavaNode {
    return NamedGumTreeJavaNode(treeContext.root, treeContext, null)
}

class LineAwareTreeGenerator() : AbstractJdtTreeGenerator() {
    lateinit var compilationUnit: CompilationUnit
    lateinit var filePath: String

    @Throws(IOException::class)
    private fun readerToCharArray(r: Reader): CharArray {
        val fileData = StringBuilder()
        BufferedReader(r).use { br ->
            var buf = CharArray(10)
            var numRead = br.read(buf)
            while (numRead != -1) {
                val readData = String(buf, 0, numRead)
                fileData.append(readData)
                buf = CharArray(1024)
                numRead = br.read(buf)
            }
        }
        return fileData.toString().toCharArray()
    }

    @Throws(IOException::class)
    override fun generate(r: Reader): TreeContext {
        val parser = ASTParser.newParser(AST.JLS8)
        parser.setKind(ASTParser.K_COMPILATION_UNIT)
        val pOptions = JavaCore.getOptions()
        pOptions[JavaCore.COMPILER_COMPLIANCE] = JavaCore.VERSION_1_8
        pOptions[JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM] = JavaCore.VERSION_1_8
        pOptions[JavaCore.COMPILER_SOURCE] = JavaCore.VERSION_1_8
        pOptions[JavaCore.COMPILER_DOC_COMMENT_SUPPORT] = JavaCore.ENABLED
        parser.setCompilerOptions(pOptions)
        parser.setSource(readerToCharArray(r))
        compilationUnit = parser.createAST(null) as CompilationUnit
        val v = createVisitor()
        compilationUnit.accept(v)
        return v.treeContext
    }

    override fun createVisitor(): AbstractJdtVisitor {
        return LineAwareVisitor(compilationUnit)
    }
}

class LineAwareVisitor(val compilationUnit: CompilationUnit) : JdtVisitor() {
    companion object {
        const val LINE = "line"
    }

    override fun pushNode(n: ASTNode?, label: String?) {
        super.pushNode(n, label)
        n?.let {
            val line = compilationUnit.getLineNumber(it.startPosition)
            currentParent.setMetadata(LINE, line)
        }
    }
}