package com.intellij.grazie.detection

import com.intellij.grazie.jlanguage.Lang
import tanvd.grazie.langdetect.model.Language

fun Lang.toLanguage() = Language.values().find { it.iso.name.toLowerCase() == this.shortCode }!!

/** Note that it will return SOME dialect */
fun Language.toLang() = Lang.values().find { it.shortCode == this.iso.name.toLowerCase() }!!

val Language.displayName: String
  get() = this.name.toLowerCase().capitalize()