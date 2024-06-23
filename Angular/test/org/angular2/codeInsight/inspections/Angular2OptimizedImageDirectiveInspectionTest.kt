// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import com.intellij.xml.analysis.XmlAnalysisBundle
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.inspections.AngularNgOptimizedImageInspection
import org.angular2.lang.Angular2Bundle

class Angular2OptimizedImageDirectiveInspectionTest : Angular2TestCase("inspections/ngSrc", false) {

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
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_15_1_5, Angular2TestModule.ANGULAR_COMMON_15_1_5, dir = true,
                     configureFileName = "main.component.html", checkResult = true) {
      myFixture.checkHighlighting()
      myFixture.launchAction(myFixture.findSingleIntention(intention))
    }
  }

}