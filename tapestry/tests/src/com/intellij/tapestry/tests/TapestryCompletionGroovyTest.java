/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.tapestry.tests;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * @author Alexey Chmutov
 */
public class TapestryCompletionGroovyTest extends TapestryCompletionTest {
  private static final Set<String> ourTestsWithExtraLibraryComponents = ContainerUtil.newHashSet("CompleteComponentFromLibrary");
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
  public void testIdAttrValue() {
    addComponentToProject("Count");
    initByComponent();
    doTestBasicCompletionVariants("link2", "link3");
  }

  @Override
  protected void addTapestryLibraries(JavaModuleFixtureBuilder moduleBuilder) {
    super.addTapestryLibraries(moduleBuilder);
    if (ourTestsWithExtraLibraryComponents.contains(getTestName(false))) {
      moduleBuilder.addLibraryJars("tapestry_5.1.0.5_additional", Util.getCommonTestDataPath() + "libs", "tapestry-upload-5.1.0.5.jar");
    }
  }

  public void testCompleteComponentFromLibrary() {
    addComponentToProject("Count3");
    initByComponent();
    doTestBasicCompletionVariants("wf.upload", "addrowlink", "gridrows", "outputraw", "passwordfield", "removerowlink");
  }
}
