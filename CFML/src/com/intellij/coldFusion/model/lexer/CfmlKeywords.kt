// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.lexer

enum class CfmlKeywords(val keyword: String, val omitCodeBlock: Boolean = false) {
  PARAM("param"),
  THREAD("thread", true),
  LOCK ("lock"),
  TRANSACTION("transaction", true),
  WRITELOG("writelog"),
  SAVECONTENT("savecontent")
}