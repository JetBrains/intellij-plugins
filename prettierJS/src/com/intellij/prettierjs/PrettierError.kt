// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

sealed interface PrettierError {
  val message: String

  data class EditSettings(override val message: String = PrettierBundle.message("error.no.valid.package")) : PrettierError
  data class NodeSettings(override val message: String = PrettierBundle.message("error.invalid.interpreter")) : PrettierError
  data class InstallPackage(override val message: String = PrettierBundle.message("error.package.is.not.installed")) : PrettierError
  data class Unsupported(override val message: String) : PrettierError
  data class ShowDetails(override val message: String) : PrettierError
}
