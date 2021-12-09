// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.refactoring.JSMoveModuleMembersLightTestBase

class VueMoveModuleMemberTest: JSMoveModuleMembersLightTestBase() {
  override fun getTestDataPath(): String = getVueTestDataPath() + "/refactoring/moveModuleMember/"
  
  override fun getTestRoot(): String {
    return ""
  }
  
  fun testMoveBetweenVueFiles() {
    doTest("from.vue", "to.vue", "foo")
  }
}