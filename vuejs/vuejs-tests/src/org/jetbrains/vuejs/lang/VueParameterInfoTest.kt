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

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.impl.NonBlockingReadActionImpl
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.EditorHintFixture
import com.intellij.util.ui.UIUtil

class VueParameterInfoTest : BasePlatformTestCase() {

  fun testTypeScriptParametersHint() {
    myFixture.configureVueDependencies()
    myFixture.addFileToProject("api.vue", "<script lang='ts'>\n" +
                                          "    export function isApiResponse(s:String,b:boolean){}\n" +
                                          "</script>")
    myFixture.configureByText("a.vue", "<script lang='ts'>\n" +
                                       "    import {isApiResponse} from 'api'\n" +
                                       "    isApiResponse(<caret>)\n" +
                                       "</script>")
    val hintFixture = EditorHintFixture(testRootDisposable)
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_SHOW_PARAMETER_INFO)

    // effective there is a chain of 3 nonBlockingRead actions
    for (i in 0..2) {
      UIUtil.dispatchAllInvocationEvents()
      NonBlockingReadActionImpl.waitForAsyncTaskCompletion()
    }

    assertEquals("<html><b>s: String</b>, b: boolean</html>", hintFixture.currentHintText?.replace(Regex("</?span[^>]*>"), ""))
  }
}
