// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.jhipster.psi.JdlParserDefinition;
import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.ParsingTestCase;

public class JdlParserTest extends ParsingTestCase {
  public JdlParserTest() {
    super("", "jdl", new JdlParserDefinition());
  }

  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + "/contrib/jhipster/testData/parser";
  }

  public void testApplication() {
    doTest(true);
  }

  public void testBlog() {
    doTest(true);
  }
}
