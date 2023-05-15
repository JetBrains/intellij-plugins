package org.jetbrains.webstorm.lang.editor

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.webstorm.lang.psi.WebAssemblyNamedElement
import org.jetbrains.webstorm.lang.psi.WebAssemblyTypes
import org.junit.Test

class WebAssemblyReferenceTest : BasePlatformTestCase() {
    private val testReferenceDir: String = "reference/"

    override fun getTestDataPath(): String = "src/test/resources/editor"

    @Test
    fun testBulkMemoryDisabled() {
        testReference(getTestName(false), arrayOf(
                Pair(WebAssemblyTypes.DATA,  null),
                Pair(WebAssemblyTypes.DATA,  "\$data"),
                Pair(WebAssemblyTypes.DATA,  null),
                Pair(WebAssemblyTypes.DATA,  "\$data"),
                Pair(WebAssemblyTypes.FUNC,  null),
                Pair(WebAssemblyTypes.ELEM,  null),
                Pair(WebAssemblyTypes.TABLE, "\$t1"),
                Pair(WebAssemblyTypes.ELEM,  null),
                Pair(WebAssemblyTypes.ELEM,  null),
                Pair(WebAssemblyTypes.ELEM,  "\$elem"),
                Pair(WebAssemblyTypes.TABLE, "\$t1"),
                Pair(WebAssemblyTypes.TABLE, "\$t2")
        ))
    }

    @Test
    fun testBulkMemoryNamed() {
        testReference(getTestName(false), arrayOf(
                Pair(WebAssemblyTypes.DATA,  "\$data"),
                Pair(WebAssemblyTypes.DATA,  "\$data"),
                Pair(WebAssemblyTypes.FUNC,  null),
                Pair(WebAssemblyTypes.FUNC,  null),
                Pair(WebAssemblyTypes.ELEM,  "\$elem"),
                Pair(WebAssemblyTypes.ELEM,  "\$elem")
        ))
    }

    @Test
    fun testCall() {
        testReference(getTestName(false), arrayOf(
                Pair(WebAssemblyTypes.FUNC, null),
                Pair(WebAssemblyTypes.FUNC, null),
                Pair(WebAssemblyTypes.FUNC, null),
                Pair(WebAssemblyTypes.FUNC, "\$f")
        ))
    }

    @Test
    fun testExportGlobal() {
        testReference(getTestName(false), arrayOf(
                Pair(WebAssemblyTypes.GLOBAL, null),
                Pair(WebAssemblyTypes.MEM,    null),
                Pair(WebAssemblyTypes.MEM,    null),
                Pair(WebAssemblyTypes.TABLE,  null),
                Pair(WebAssemblyTypes.FUNC,   "\$n"),
                Pair(WebAssemblyTypes.FUNC,   null),
                Pair(WebAssemblyTypes.FUNC,   null)
        ))
    }

    @Test
    fun testHighlighting() {
        testReference(getTestName(false), arrayOf(
                Pair(WebAssemblyTypes.FUNC,   "\$getHeight"),
                Pair(WebAssemblyTypes.FUNC,   "\$getWidth"),
                Pair(WebAssemblyTypes.MEM,    "\$mem"),
                Pair(WebAssemblyTypes.FUNC,   "\$setDimensions"),
                Pair(WebAssemblyTypes.TYPE,   "\$getI32"),
                Pair(WebAssemblyTypes.GLOBAL, "\$height"),
                Pair(WebAssemblyTypes.TYPE,   "\$getI32"),
                Pair(WebAssemblyTypes.GLOBAL, "\$width"),
                Pair(WebAssemblyTypes.LOCAL,  "\$success"),
                Pair(WebAssemblyTypes.GLOBAL, "\$true"),
                Pair(WebAssemblyTypes.LOCAL,  "\$pixels"),
                Pair(WebAssemblyTypes.PARAM,  "\$width"),
                Pair(WebAssemblyTypes.PARAM,  "\$height")
        ))
    }

    @Test
    fun testLoopMultiNamed() {
        testReference(getTestName(false), arrayOf(
                Pair(WebAssemblyTypes.TYPE, "\$v_v"),
                Pair(WebAssemblyTypes.TYPE, "\$v_ii"),
                Pair(WebAssemblyTypes.TYPE, "\$ii_v"),
                Pair(WebAssemblyTypes.TYPE, "\$ff_ff")
        ))
    }

    @Test
    fun testMemory() {
        testReference(getTestName(false), arrayOf(
                Pair(WebAssemblyTypes.LOCAL, "\$end"),
                Pair(WebAssemblyTypes.PARAM, "\$ptr"),
                Pair(WebAssemblyTypes.PARAM, "\$len"),
                Pair(WebAssemblyTypes.PARAM, "\$ptr"),
                Pair(WebAssemblyTypes.LOCAL, "\$end"),
                Pair(WebAssemblyTypes.LOCAL, "\$sum"),
                Pair(WebAssemblyTypes.LOCAL, "\$sum"),
                Pair(WebAssemblyTypes.PARAM, "\$ptr"),
                Pair(WebAssemblyTypes.PARAM, "\$ptr"),
                Pair(WebAssemblyTypes.PARAM, "\$ptr"),
                Pair(WebAssemblyTypes.LOCAL, "\$sum")
        ))
    }

    @Test
    fun testReferenceTypesCallIndirect() {
        testReference(getTestName(false), arrayOf(
                Pair(WebAssemblyTypes.TABLE, "\$foo"),
                Pair(WebAssemblyTypes.TYPE, null),
                Pair(WebAssemblyTypes.TABLE, "\$bar"),
                Pair(WebAssemblyTypes.TYPE, null)
        ))
    }

    @Test
    fun testReferenceTypesNamed() {
        testReference(getTestName(false), arrayOf(
                Pair(WebAssemblyTypes.TABLE, "\$foo"),
                Pair(WebAssemblyTypes.PARAM, null),
                Pair(WebAssemblyTypes.TABLE, "\$foo"),
                Pair(WebAssemblyTypes.TABLE, null)
        ))
    }

    @Test
    fun testWasmTable() {
        testReference(getTestName(false), arrayOf(
                Pair(WebAssemblyTypes.FUNC,   "\$f2"),
                Pair(WebAssemblyTypes.IMPORT, "\$i64"),
                Pair(WebAssemblyTypes.TABLE,  "\$namedTable"),
                Pair(WebAssemblyTypes.FUNC,   "\$f1"),
                Pair(WebAssemblyTypes.FUNC,   "\$f2"),
                Pair(WebAssemblyTypes.IMPORT, "\$namedImportedTable"),
                Pair(WebAssemblyTypes.FUNC,   null),
                Pair(WebAssemblyTypes.IMPORT, null),
                Pair(WebAssemblyTypes.PARAM,  "\$i"),
                Pair(WebAssemblyTypes.TYPE,   "\$return_i32"),
                Pair(WebAssemblyTypes.MEM,    null),
                Pair(WebAssemblyTypes.IMPORT, null)
        ))
    }

    private fun testReference(testName: String, testData: Array<Pair<IElementType, String?>>) {
        myFixture.configureByFiles("${testReferenceDir}${testName}.wat")

        val references: MutableList<PsiReference> = ArrayList()
        getAllReferences(myFixture.file, references)

        if (references.size != testData.size) {
            fail("Error number of references: expected ${testData.size}, but got ${references.size}.")
        }

        references
                .zip(testData)
                .map {
                    val element: WebAssemblyNamedElement? = it.first.resolve() as WebAssemblyNamedElement?

                    if (element == null) {
                        fail("Null reference for ${it.first.canonicalText} in ${it.first}")
                    } else {
                        val (type, name) = it.second
                        assertEquals(type, element.elementType)
                        name?.let {
                            assertEquals(name, element.nameIdentifier?.text)
                        }
                    }
                }
    }

    private fun getAllReferences(element: PsiElement, result: MutableList<PsiReference>) {
        element
                .children
                .map {
                    result.addAll(it.references)
                    getAllReferences(it, result)
                }
    }
}