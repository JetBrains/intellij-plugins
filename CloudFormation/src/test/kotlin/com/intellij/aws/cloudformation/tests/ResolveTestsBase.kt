package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.references.CloudFormationReferenceBase
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import com.intellij.testFramework.ResolveTestCase
import org.junit.Assert
import java.io.File
import java.util.ArrayList

abstract class ResolveTestsBase protected constructor(private val myReferenceClass: Class<*>) : ResolveTestCase() {
  protected val NotResolved = Object()

  protected fun assertEntityResolve(testName: String, vararg entityNames: Any) {
    val templateFileName = testName + ".template"

    FileUtil.copy(File(testDataPath, templateFileName), File(myProject.basePath, templateFileName))

    val vFile = myProject.baseDir.findChild(templateFileName)!!

    var fileText = StringUtil.convertLineSeparators(VfsUtil.loadText(vFile))

    val fileName = vFile.name

    val offsets = ArrayList<Int>()
    while (true) {
      val offset = fileText.indexOf(ResolveTestCase.MARKER)
      if (offset < 0) {
        break
      }

      fileText = fileText.substring(0, offset) + fileText.substring(offset + ResolveTestCase.MARKER.length)
      offsets.add(offset)
    }

    Assert.assertTrue("Test input must contain one or more " + ResolveTestCase.MARKER + "markers", offsets.size > 0)
    Assert.assertEquals("Number of assertions and markers differs", entityNames.size.toLong(), offsets.size.toLong())

    myFile = createFile(myModule, fileName, fileText)
    Assert.assertFalse(
        "File " + myFile.name + " contains error(s)",
        ErrorUtil.containsError(myFile))

    for (i in offsets.indices) {
      val rawRef = myFile.findReferenceAt(offsets[i]) ?: throw AssertionError("Reference not found at marker #${i + 1}")

      val ref: PsiReference
      if (rawRef is PsiMultiReference) {
        ref = rawRef.references.filter { it is CloudFormationReferenceBase }.single()
      } else {
        ref = rawRef
      }

      Assert.assertTrue("Ref should be " + myReferenceClass.name +
          ", but got " + ref.javaClass.name,
          myReferenceClass.isAssignableFrom(ref.javaClass))

      val resolved = ref.resolve()
      val expectedEntity = entityNames[i]

      if (expectedEntity === NotResolved) {
        if (resolved != null) {
          Assert.fail("Ref #" + (i + 1) + " should be unresolved, but resolved to " + resolved.text)
        }
      } else {
        if (resolved == null) {
          Assert.fail("Ref #" + (i + 1) + " is unresolved")
        }

        val namedResolved = resolved as? PsiNamedElement
        if (namedResolved == null) {
          Assert.fail("Ref #" + (i + 1) + " should be named element")
        }

        if (expectedEntity != namedResolved!!.name) {
          Assert.fail("Wrong resolve result, should resolve to " + expectedEntity + ", but got " + namedResolved.name)
        }
      }
    }

    val visitor = ReferencesCollectorElementVisitor()
    myFile.accept(visitor)

    if (visitor.myResult.size > entityNames.size) {
      Assert.fail("More references found in file")
    } else if (visitor.myResult.size < entityNames.size) {
      Assert.fail("Less references found in file?")
    }
  }

  private inner class ReferencesCollectorElementVisitor : PsiRecursiveElementVisitor() {
    val myResult = ArrayList<PsiReference>()

    override fun visitElement(element: PsiElement) {
      super.visitElement(element)

      for (reference in element.references) {
        if (!myReferenceClass.isAssignableFrom(reference.javaClass)) {
          continue
        }

        myResult.add(reference)
      }
    }
  }
}
