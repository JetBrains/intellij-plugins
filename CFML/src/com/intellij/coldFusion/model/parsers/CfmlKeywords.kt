// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.parsers

import java.util.Locale

enum class CfmlKeywords(val keyword: String, val omitCodeBlock: Boolean = false) {
  PARAM("param"),
  THREAD("thread", true),
  LOCK ("lock"),
  TRANSACTION("transaction", true),
  WRITELOG("writelog"),
  SAVECONTENT("savecontent")
}

fun isKeyword(actionName: String): Boolean {
  return parseKeyword(actionName) != null
}

fun parseKeyword(keywordName: String): CfmlKeywords? {
  return CfmlKeywords.values().firstOrNull { it.keyword.lowercase(Locale.getDefault()) == keywordName.lowercase(Locale.getDefault()) }
}