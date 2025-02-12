// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform

import com.intellij.spellchecker.BundledDictionaryProvider

internal class TfBundledDictionaryProvider : BundledDictionaryProvider {
  override fun getBundledDictionaries(): Array<String> {
    return arrayOf("/org/intellij/terraform/intellij-terraform.dic")
  }
}
