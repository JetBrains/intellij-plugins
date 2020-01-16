// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.openapi.application.PathManager
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.usageView.UsageInfo

class VueFindUsagesTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/findUsages"

  fun testPrivateComponentGetter() {
    myFixture.configureByFiles("privateFields.vue")
    checkUsages("get f<caret>oo",
                "12 + foo <privateFields.vue.int:(5,8):(0,3)>",
                "this.foo + 12 <privateFields.vue:(385,393):(5,8)>")
  }

  fun testPrivateComponentSetter() {
    myFixture.configureByFiles("privateFields.vue")
    checkUsages("set f<caret>oo",
                "foo <privateFields.vue:(44,47):(0,3)>",
                "this.foo <privateFields.vue:(374,382):(5,8)>")
  }

  fun testPrivateComponentMethod() {
    myFixture.configureByFiles("privateFields.vue")
    checkUsages("private on<caret>Scrolled",
                "onScrolled <privateFields.vue:(95,105):(0,10)>")
  }

  fun testPrivateConstructorField() {
    myFixture.configureByFiles("privateFields.vue")
    checkUsages("private ba<caret>r",
                "bar <privateFields.vue.int:(0,3):(0,3)>",
                "this.foo + 12 + this.bar <privateFields.vue:(401,409):(5,8)>")
  }

  private fun checkUsages(signature: String, vararg usages: String) {
    myFixture.moveToOffsetBySignature(signature)
    assertEquals(usages.asSequence().sorted().toList(),
                 myFixture.findUsages(myFixture.elementAtCaret).asSequence()
                   .map { usage: UsageInfo ->
                     getElementText(usage.element!!) +
                     " <" + usage.file!!.name +
                     ":" + usage.element!!.textRange +
                     ":" + usage.rangeInElement +
                     ">"
                   }
                   .sorted()
                   .toList()
    )
  }

  private fun getElementText(element: PsiElement): String {
    if (element is XmlTag) {
      return element.name
    }
    else if (element is XmlAttribute) {
      return element.getText()
    }
    return element.parent.text
  }

}
