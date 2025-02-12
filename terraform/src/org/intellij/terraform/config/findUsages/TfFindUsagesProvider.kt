// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.findUsages

import com.intellij.lang.cacheBuilder.WordsScanner
import org.intellij.terraform.hcl.findUsages.HCLFindUsagesProvider
import org.intellij.terraform.hcl.findUsages.HCLWordsScanner
import org.intellij.terraform.config.TfParserDefinition

class TfFindUsagesProvider : HCLFindUsagesProvider() {
  override fun getWordsScanner(): WordsScanner {
    return HCLWordsScanner(TfParserDefinition.createLexer())
  }
}
