// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.lang.Language
import org.intellij.terraform.config.CompletionTestCase
import org.intellij.terraform.config.TerraformLanguage
import org.intellij.terraform.config.model.TfTypeModel

internal abstract class TfBaseCompletionTestCase : CompletionTestCase() {

  val commonResourceProperties: List<String> = TfTypeModel.AbstractResource.properties.values
    .filter { it.configurable }
    .map { it.name }

  val commonDataSourceProperties: List<String> = TfTypeModel.AbstractDataSource.properties.values
    .filter { it.configurable }
    .map { it.name }

  override fun getTestDataPath(): String = "tests/data"

  override fun getFileName(): String = "a.tf"

  override fun getExpectedLanguage(): Language = TerraformLanguage
}