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

import com.intellij.openapi.application.EDT
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class VueParameterInfoTest {
  private val hybrid by hybridFixture("")
  private val myFixture get() = hybrid.fixture

  @Test
  fun testTypeScriptParametersHint(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies()
    myFixture.addFileToProject("api.vue", "<script lang='ts'>\n" +
                                          "    export function isApiResponse(s:String,b:boolean){}\n" +
                                          "</script>")
    myFixture.configureByText("a.vue", "<script lang='ts'>\n" +
                                       "    import {isApiResponse} from 'api'\n" +
                                       "    isApiResponse(<caret>)\n" +
                                       "</script>")
    assertEquals("<b>s: String,</b> b: boolean", myFixture.parameterInfoAtCaret)
  }
}
