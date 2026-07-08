// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.resolver;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.xml.XmlTokenImpl;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

public class MxmlResolveTest extends BasePlatformTestCase {

  @Override
  protected @NotNull String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("/resolve");
  }

  public void testMxmlTags() {
    // IDEA-139549
    final PsiReference reference = myFixture.getReferenceAtCaretPosition(getTestName(false) + ".mxml");
    final PsiElement resolve = reference.resolve();
    assertTrue(resolve instanceof JSOffsetBasedImplicitElement);
    assertTrue(((JSOffsetBasedImplicitElement)resolve).canNavigate());
    final PsiElement elementAtOffset = ((JSOffsetBasedImplicitElement)resolve).getElementAtOffset();
    assertInstanceOf(elementAtOffset, XmlTokenImpl.class);
    assertEquals("id=\"list\"", elementAtOffset.getParent().getParent().getText());
  }
}
