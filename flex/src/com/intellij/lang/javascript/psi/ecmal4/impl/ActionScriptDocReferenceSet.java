package com.intellij.lang.javascript.psi.ecmal4.impl;

import com.intellij.lang.javascript.documentation.JSDocumentationUtils;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ActionScriptDocReferenceSet extends ActionScriptReferenceSet {

  private static final String GLOBAL_PREFIX = "global#";
  private boolean myOnlyDefaultPackage;

  public ActionScriptDocReferenceSet(@NotNull PsiElement element, String text, int offset, boolean soft) {
    super(element, soft);
    myReferenceText = text;
    myOffset = offset;
    myReferences = reparse(text, offset);
  }

  public boolean isOnlyDefaultPackage() {
    return myOnlyDefaultPackage;
  }

  @Override
  protected PsiReference @NotNull [] reparse(@NotNull String value, int offset) {
    if (value.startsWith(GLOBAL_PREFIX)) {
      value = value.substring(GLOBAL_PREFIX.length());
      offset += GLOBAL_PREFIX.length();
      myOnlyDefaultPackage = true;
    }
    List<PsiReference> refs = new SmartList<>();

    int dotPos = -1;
    int lastPos = 0;
    int lastLength = -1;

    while (dotPos < value.length()) {
      if (dotPos == -1) {
        if (!value.isEmpty() && StringUtil.containsChar(JSDocumentationUtils.NAMEPATH_SEPARATORS, value.charAt(0))) {
          dotPos++;
        }
      }
      else if (!StringUtil.containsChar(JSDocumentationUtils.NAMEPATH_SEPARATORS, value.charAt(dotPos))) {
        return PsiReference.EMPTY_ARRAY;
      }
      int startOffset = dotPos + 1;
      int nextDotPos = JSDocumentationUtils.findNextNamepathSeparator(value, startOffset);
      if (nextDotPos == -1) break;

      if (lastLength > 0) {
        refs.add(new ActionScriptTextReference(this, value.substring(lastPos, lastPos + lastLength), offset + lastPos));
      }

      lastPos = startOffset;
      lastLength = nextDotPos - startOffset;
      dotPos = nextDotPos;
    }

    if (lastLength > 0) {
      String s = value.substring(lastPos, lastPos + lastLength);
      int index = 0;
      refs.add(new ActionScriptTextReference(this, s, offset + lastPos + index));
    }
    return !refs.isEmpty() ? refs.toArray(PsiReference.EMPTY_ARRAY) : PsiReference.EMPTY_ARRAY;
  }
}
