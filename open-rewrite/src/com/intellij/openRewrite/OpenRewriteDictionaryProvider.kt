package com.intellij.openRewrite

import com.intellij.spellchecker.BundledDictionaryProvider

internal class OpenRewriteDictionaryProvider : BundledDictionaryProvider {
  override fun getBundledDictionaries(): Array<String> {
    return arrayOf("/dictionaries/openRewrite.dic")
  }
}