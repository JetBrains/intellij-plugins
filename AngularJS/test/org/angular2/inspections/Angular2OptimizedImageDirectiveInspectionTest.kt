// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections

import com.intellij.xml.analysis.XmlAnalysisBundle
import org.angular2.Angular2MultiFileFixtureTestCase
import org.angular2.Angular2TestModule
import org.angular2.lang.Angular2Bundle
import org.angularjs.AngularTestUtil

class Angular2OptimizedImageDirectiveInspectionTest : Angular2MultiFileFixtureTestCase() {

  override fun getTestDataPath(): String? {
    return AngularTestUtil.getBaseTestDataPath() + "inspections/"
  }

  override fun getTestRoot(): String {
    return "ngSrc/"
  }

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(AngularNgOptimizedImageInspection())
  }

  fun testConvert() {
    doMultiFileTest(Angular2Bundle.message("angular.quickfix.template.covert-to-ng-src.family"))
  }

  fun testAddWidthHeight() {
    doMultiFileTest(Angular2Bundle.message("angular.quickfix.template.create-height-width-attributes.name"))
  }

  fun testAddFill() {
    doMultiFileTest(Angular2Bundle.message("angular.quickfix.template.create-attribute.name", AngularNgOptimizedImageInspection.FILL_ATTR))
  }

  fun testAddWidth() {
    doMultiFileTest(Angular2Bundle.message("angular.quickfix.template.create-attribute.name", AngularNgOptimizedImageInspection.WIDTH_ATTR))
  }

  fun testRemoveWidth() {
    doMultiFileTest(XmlAnalysisBundle.message("xml.quickfix.remove.attribute.text", AngularNgOptimizedImageInspection.WIDTH_ATTR))
  }

  private fun doMultiFileTest(intention: String) {
    doTest { _, _ ->
      Angular2TestModule.configureLink(myFixture, Angular2TestModule.ANGULAR_CORE_15_1_5, Angular2TestModule.ANGULAR_COMMON_15_1_5)
      myFixture.configureFromTempProjectFile("main.component.html")
      myFixture.checkHighlighting()
      myFixture.launchAction(myFixture.findSingleIntention(intention))
    }
  }

}