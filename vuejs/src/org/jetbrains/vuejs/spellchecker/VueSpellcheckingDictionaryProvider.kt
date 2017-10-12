package org.jetbrains.vuejs.spellchecker

import com.intellij.spellchecker.BundledDictionaryProvider

/**
 * @author Irina.Chernushina on 10/12/2017.
 */
class VueSpellcheckingDictionaryProvider : BundledDictionaryProvider {
  override fun getBundledDictionaries(): Array<String> = arrayOf("vue.dic")
}