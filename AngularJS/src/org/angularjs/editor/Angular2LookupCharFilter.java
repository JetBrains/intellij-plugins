package org.angularjs.editor;

import com.intellij.codeInsight.lookup.CharFilter;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class Angular2LookupCharFilter extends CharFilter {
  @Nullable
  @Override
  public Result acceptChar(char c, int prefixLength, Lookup lookup) {
    if (c == '(' || c == ')' || c == '[' || c == ']' || c == '*') {
      final PsiFile file = lookup.getPsiFile();
      if (file instanceof XmlFile && AngularIndexUtil.hasAngularJS2(file.getProject())) {
        return Result.ADD_TO_PREFIX;
      }
    }
    return null;
  }
}
