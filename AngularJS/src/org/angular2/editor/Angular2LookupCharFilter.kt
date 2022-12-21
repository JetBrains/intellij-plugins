// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.codeInsight.lookup.CharFilter;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class Angular2LookupCharFilter extends CharFilter {
  @Override
  public @Nullable Result acceptChar(char c, int prefixLength, Lookup lookup) {
    if (c == '(' || c == ')' || c == '[' || c == ']' || c == '*' || c == '#' || c == '@' || c == '.') {
      final PsiFile file = lookup.getPsiFile();
      if (file instanceof XmlFile && Angular2LangUtil.isAngular2Context(file)) {
        return Result.ADD_TO_PREFIX;
      }
    }
    return null;
  }
}
