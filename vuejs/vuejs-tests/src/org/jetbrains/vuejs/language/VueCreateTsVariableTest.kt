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
package org.jetbrains.vuejs.language

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.lang.javascript.BaseJSIntentionTestCase
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedVariableInspection
import com.intellij.openapi.application.PathManager

class VueCreateTsVariableTest : BaseJSIntentionTestCase() {
  override fun getBasePath(): String {
    return "" // not used
  }

  override fun configureLocalInspectionTools(): Array<LocalInspectionTool> {
    return arrayOf(JSUnresolvedVariableInspection(), TypeScriptUnresolvedVariableInspection())
  }

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/intentions/createVariable"

  fun testAll() {
    doTestAll()
  }

}