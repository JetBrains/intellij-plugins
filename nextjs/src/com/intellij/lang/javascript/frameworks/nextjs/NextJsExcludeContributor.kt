// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.javascript.library.exclude.JsExcludeContributor

class NextJsExcludeContributor: JsExcludeContributor() {
  override val excludeFileOrDirName: String
    get() = ".next"
  override val isDirectory: Boolean
    get() = true
}