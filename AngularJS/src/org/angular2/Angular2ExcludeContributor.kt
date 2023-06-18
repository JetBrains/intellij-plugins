// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2

import com.intellij.javascript.library.exclude.JsExcludeContributor

class Angular2ExcludeContributor: JsExcludeContributor() {
  override val excludeFileOrDirName: String
    get() = ".angular"
  override val isDirectory: Boolean
    get() = true
}