package astminer.parse.java

import astminer.common.model.Node
import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext

class NamedGumTreeJavaNode(val wrappedNode: ITree, val context: TreeContext, val parent: NamedGumTreeJavaNode?) : Node {

    private val metadata: MutableMap<String, Any> = HashMap()

    public companion object {
        const val SIMPLE_NAME = "SIMPLE_NAME"
        const val LINE = "LINE"

        public object TypeLabels {
            const val fieldDeclaration = "FieldDeclaration"
            const val singleVariableDeclaration = "SingleVariableDeclaration"
            const val variableDeclaration = "VariableDeclaration"
            const val simpleName = "SimpleName"
            const val typeDeclaration = "TypeDeclaration"
            const val variableDeclarationFragment = "VariableDeclarationFragment"

            val allVarDeclarations = setOf(fieldDeclaration, variableDeclaration, variableDeclarationFragment, singleVariableDeclaration)
        }
    }

    init {
        setMetadata(SIMPLE_NAME, getSimpleName())
        setMetadata(LINE, getLine())
    }

    override fun getMetadata(key: String): Any? {
        return metadata[key]
    }

    override fun setMetadata(key: String, value: Any) {
        metadata[key] = value
    }

    override fun isLeaf(): Boolean {
        return childrenList.isEmpty()
    }

    private val childrenList: List<NamedGumTreeJavaNode> by lazy {
        wrappedNode.children.map { NamedGumTreeJavaNode(it, context, this) }
    }

    override fun getTypeLabel(): String {
        return context.getTypeLabel(wrappedNode)
    }

    override fun getChildren(): List<Node> {
        return childrenList
    }

    override fun getParent(): Node? {
        return parent
    }

    override fun getToken(): String {
        return wrappedNode.label
    }

    override fun toString(): String {
        return "${getTypeLabel()}"
    }

    private fun getSimpleName(): String {
        val nameNode = wrappedNode.children.firstOrNull { context.getTypeLabel(it.type) == TypeLabels.simpleName }
        return nameNode?.label ?: ""
    }

    private fun getLine(): Int {
        return wrappedNode.getMetadata(LineAwareVisitor.LINE) as Int
    }


}