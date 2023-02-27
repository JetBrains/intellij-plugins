/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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