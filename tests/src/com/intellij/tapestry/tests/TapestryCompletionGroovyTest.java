/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.tapestry.tests;

import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class TapestryCompletionGroovyTest extends TapestryCompletionTest {

  @Override
  protected String getComponentClassExtension() {
    return Util.DOT_GROOVY;
  }

  @Override
  @Nullable
  protected String getExistingComponentClassFileName() {
    String fileName = super.getExistingComponentClassFileName();
    return fileName != null ? fileName : checkTestDataFileExists(getElementName() + super.getComponentClassExtension());
  }

  @Override
  public void testIdAttrValue() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    doTestBasicCompletionVariants("link2", "link3");
  }

}
