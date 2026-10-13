// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.EqualsToFile;
import com.intellij.testFramework.ParsingTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestApplication
public class JdlParserTest {
  private static final TestFixture<Project> projectFixture = projectFixture();

  @Test
  public void testApplication(TestInfo testInfo) throws IOException {
    doTest(testInfo);
  }

  @Test
  public void testBlog(TestInfo testInfo) throws IOException {
    doTest(testInfo);
  }

  private static void doTest(TestInfo testInfo) throws IOException {
    String name = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), false);
    Path testDataDir = Path.of(PathManager.getHomePath(), "contrib/jhipster/testData/parser");
    String text = StringUtil.convertLineSeparators(Files.readString(testDataDir.resolve(name + ".jdl"))).trim();

    String tree = ReadAction.computeBlocking(() -> {
      PsiFile file = PsiFileFactory.getInstance(projectFixture.get()).createFileFromText(name + ".jdl", JdlLanguage.INSTANCE, text);
      ParsingTestUtil.ensureParsed(file);
      assertEquals(text, file.getText(), "psi text mismatch");
      return DebugUtil.psiToString(file, true, false);
    });
    EqualsToFile.assertEqualsToFile("PSI tree", testDataDir.resolve(name + ".txt").toFile(), tree);
  }
}
