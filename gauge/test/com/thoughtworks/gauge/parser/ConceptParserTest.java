/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.parser;

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
import com.thoughtworks.gauge.language.Concept;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestApplication
public class ConceptParserTest {
  private static final TestFixture<Project> projectFixture = projectFixture();

  @Test
  public void testSimpleConcept(TestInfo testInfo) throws IOException {
    doTest(testInfo);
  }

  private static void doTest(TestInfo testInfo) throws IOException {
    String name = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), false);
    Path testDataDir = Path.of(PathManager.getHomePath(), "contrib/gauge/testData/conceptParser");
    String text = StringUtil.convertLineSeparators(Files.readString(testDataDir.resolve(name + ".cpt"))).trim();

    String tree = ReadAction.computeBlocking(() -> {
      PsiFile file = PsiFileFactory.getInstance(projectFixture.get()).createFileFromText(name + ".cpt", Concept.INSTANCE, text);
      ParsingTestUtil.ensureParsed(file);
      assertEquals(text, file.getText(), "psi text mismatch");
      return DebugUtil.psiToString(file, true, true);
    });
    EqualsToFile.assertEqualsToFile("PSI tree", testDataDir.resolve(name + ".txt").toFile(), tree);
  }
}