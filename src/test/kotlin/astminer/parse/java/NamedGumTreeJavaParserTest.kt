package astminer.parse.java

import org.junit.Assert
import org.junit.Test
import java.io.File
import java.io.FileInputStream

class NamedGumTreeJavaParserTest {
    @Test
    fun testNodeIsNotNull() {
        val parser = NamedGumTreeJavaParser()
        val file = File("testData/examples/1.java")
        val node = parser.parse(FileInputStream(file))
        Assert.assertNotNull("Parse tree for a valid file should not be null", node)
    }

    @Test
    fun testProjectParsing() {
        val parser = NamedGumTreeJavaParser()
        val projectRoot = File("testData/examples")
        val trees = parser.parseWithExtension(projectRoot, "java")
        Assert.assertEquals("There is only 2 file with .java extension in 'testData/examples' folder", 2, trees.size)
        trees.forEach { Assert.assertNotNull("Parse tree for a valid file should not be null", it) }
    }
}