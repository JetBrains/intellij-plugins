/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.osgi.highlighting

import org.osmorc.LightOsgiFixtureTestCase
import org.osmorc.inspection.ClassInDefaultPackageInspection

class ClassInDefaultPackageInspectionTest : LightOsgiFixtureTestCase() {
  fun testNegative() {
    doTest("package pkg;\npublic class C { }")
  }

  fun testPositive() {
    doTest("public class <error descr=\"Class is in the default package\">C</error> { }")
  }

  fun testGroovy() {
    doTest("class <error descr=\"Class is in the default package\">C</error> { }", "C.groovy")
  }

  private fun doTest(text: String, fileName: String = "C.java") {
    myFixture.enableInspections(ClassInDefaultPackageInspection())
    myFixture.configureByText(fileName, text)
    myFixture.checkHighlighting()
  }
}