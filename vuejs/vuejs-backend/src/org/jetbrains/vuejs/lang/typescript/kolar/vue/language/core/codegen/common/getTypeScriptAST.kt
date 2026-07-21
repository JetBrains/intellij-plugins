// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.common

import com.intellij.openapi.project.Project
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.FakeSourceFile
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.getTypeScriptAST

// manual adapter
fun getTypeScriptAST(
  text: String,
  options: CommonCodegenOptions,
): FakeSourceFile =
  getTypeScriptAST(
    text = text,
    project = getProject(options),
  )

private fun getProject(
  options: CommonCodegenOptions,
): Project {
  TODO("not implemented")
}
