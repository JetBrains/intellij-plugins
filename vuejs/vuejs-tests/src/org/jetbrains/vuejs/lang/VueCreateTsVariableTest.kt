// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class VueCreateTsVariableTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/intentions/createVariable")
  private val myFixture get() = hybrid.fixture

  @BeforeEach
  fun setUp() {
    myFixture.enableInspections(JSUnresolvedReferenceInspection(),
                                TypeScriptUnresolvedReferenceInspection())
  }

  @Test
  fun testCreateVariableWorksInVueTs() {
    myFixture.configureByFile("before.vue")
    myFixture.launchAction(myFixture.findSingleIntention(
      JavaScriptBundle.message("javascript.create.field.intention.name", "name2")))
    myFixture.checkResultByFile("after.vue")
  }
}
