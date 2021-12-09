// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

public final class HbPsiUtil {

  /**
   * Used to determine if an element is part of an "open tag" (i.e. "{{#open}}" or "{{^openInverse}}")
   * <p/>
   * If the given element is the descendant of an {@link HbOpenBlockMustache}, this method returns
   * that parent.
   * <p/>
   * Otherwise, returns null.
   *
   * @param element The element whose ancestors will be searched
   * @return An ancestor of type {@link HbOpenBlockMustache} or null if none exists
   */
  public static HbOpenBlockMustache findParentOpenTagElement(PsiElement element) {
    return (HbOpenBlockMustache)PsiTreeUtil.findFirstParent(element, true, element1 -> element1 instanceof HbOpenBlockMustache);
  }

  /**
   * Used to determine if an element is part of a "close tag" (i.e. "{{/closer}}")
   * <p/>
   * If the given element is the descendant of an {@link HbCloseBlockMustache}, this method returns that parent.
   * <p/>
   * Otherwise, returns null.
   * <p/>
   *
   * @param element The element whose ancestors will be searched
   * @return An ancestor of type {@link HbCloseBlockMustache} or null if none exists
   */
  public static HbCloseBlockMustache findParentCloseTagElement(PsiElement element) {
    return (HbCloseBlockMustache)PsiTreeUtil.findFirstParent(element, true, element1 -> element1 instanceof HbCloseBlockMustache);
  }

  /**
   * Tests to see if the given element is not the "root" statements expression of the grammar
   */
  public static boolean isNonRootStatementsElement(PsiElement element) {
    PsiElement statementsParent = PsiTreeUtil.findFirstParent(element, true, element1 -> element1 instanceof HbStatements);

    // we're a non-root statements if we're of type statements, and we have a statements parent
    return element instanceof HbStatements
           && statementsParent != null;
  }
}
