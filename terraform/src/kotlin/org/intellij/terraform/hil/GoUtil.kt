// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil

object GoUtil {
  /*
  func ParseBool(str string) (bool, error) {
    switch str {
    case "1", "t", "T", "true", "TRUE", "True":
      return true, nil
    case "0", "f", "F", "false", "FALSE", "False":
      return false, nil
    }
    return false, syntaxError("ParseBool", str)
  }
   */
  private val Boolean_Pattern = "[01tfTF]|true|True|TRUE|false|False|FALSE".toPattern()
  fun isBoolean(text: String): Boolean {
    return Boolean_Pattern.matcher(text).matches()
  }
}