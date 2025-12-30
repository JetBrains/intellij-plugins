// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor

import com.intellij.codeInsight.lookup.CharFilter
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.psi.xml.XmlFile
import org.angular2.lang.Angular2LangUtil

/**
 * @author Dennis.Ushakov
 */
class Angular2LookupCharFilter : CharFilter() {
  override fun acceptChar(c: Char, prefixLength: Int, lookup: Lookup): Result? {
    if (c == '(' || c == ')' || c == '[' || c == ']' || c == '*' || c == '#' || c == '@' || c == '.') {
      val file = lookup.psiFile
      if (file is XmlFile && Angular2LangUtil.isAngular2Context(file)) {
        return Result.ADD_TO_PREFIX
      }
    }
    return null
  }
}
