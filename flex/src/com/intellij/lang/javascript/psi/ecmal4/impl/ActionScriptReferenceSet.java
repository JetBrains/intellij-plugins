// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.psi.ecmal4.impl;

import com.intellij.lang.javascript.JSStringUtil;
import com.intellij.lang.javascript.psi.impl.JSReferenceSet;
import com.intellij.lang.javascript.psi.impl.JSReferenceSetElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class ActionScriptReferenceSet extends JSReferenceSet {

  @Nullable Collection<String> myBaseClassFqns;

  final boolean onlyFqns;

  protected String myReferenceText;

  public ActionScriptReferenceSet(@NotNull PsiElement element, String text, int offset, boolean soft) {
    super(element, text, offset, soft);
    myReferenceText = text;
    onlyFqns = false;
  }

  public ActionScriptReferenceSet(@NotNull PsiElement element, String text, int offset, boolean soft, boolean _onlyFqns) {
    super(element, text, offset, soft);
    myReferenceText = text;
    onlyFqns = _onlyFqns;
  }

  public ActionScriptReferenceSet(@NotNull PsiElement element, boolean soft) {
    super(element, soft);
    onlyFqns = false;
  }

  public void setBaseClassFqns(final @NotNull Collection<String> baseClassFqns) {
    myBaseClassFqns = new ArrayList<>(baseClassFqns);
  }

  public String @NotNull [] getBaseClassFqns() {
    return myBaseClassFqns == null ? ArrayUtilRt.EMPTY_STRING_ARRAY : ArrayUtilRt.toStringArray(myBaseClassFqns);
  }

  public boolean isOnlyFqns() {
    return onlyFqns;
  }

  public @Nullable String getReferenceText() {
    return myReferenceText;
  }

  @Override
  public synchronized void update(String text, int offset) {
    if (myReferences != null &&
        myReferenceText != null &&
        myReferenceText.equals(text) &&
        myOffset == offset) {
      return;
    }

    myReferenceText = text;
    myOffset = offset;
    myReferences = JSStringUtil.isStartedWithQuote(text)
                   ? reparse(JSStringUtil.unquoteAndUnescapeStringLiteralValue(text), offset + 1)
                   : PsiReference.EMPTY_ARRAY;
  }

  @Override
  protected JSReferenceSetElement createTextReference(String s, int offset) {
    return new ActionScriptTextReference(this, s, offset);
  }
}
