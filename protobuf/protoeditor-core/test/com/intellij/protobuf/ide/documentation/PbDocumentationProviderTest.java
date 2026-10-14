/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.documentation;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.protobuf.lang.PbFileType;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.psi.PbStatement;
import com.intellij.psi.util.QualifiedName;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Tests for {@link PbDocumentationProvider}. */
@TestApplication
public class PbDocumentationProviderTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  private CodeInsightTestFixture myFixture;

  @BeforeEach
  void setUp() {
    myFixture = codeInsightFixture.get();
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture);
  }

  @Test
  public void testGenerateDocReturnsFormattedComments() {
    PbStatement msg =
        loadSymbol(
            "Msg",
            "syntax = 'proto2';",
            "",
            "// Comments for Msg. Example:",
            "//   Msg.abcd = 5",
            "// <escaped-tag/>",
            "message Msg {",
            "}");
    String doc = ReadAction.computeBlocking(() -> new PbDocumentationProvider().generateDoc(msg, null));
    assertEquals(
        "<pre>Comments for Msg. Example:\n  Msg.abcd = 5\n&lt;escaped-tag/&gt;\n</pre>", doc);
  }

  @Test
  public void testGenerateDocWithNoCommentsReturnsNull() {
    PbStatement msg = loadSymbol("Msg", "syntax = 'proto2';", "", "message Msg {", "}");
    String doc = ReadAction.computeBlocking(() -> new PbDocumentationProvider().generateDoc(msg, null));
    assertNull(doc);
  }

  private PbStatement loadSymbol(String symbol, String... lines) {
    String text = String.join("\n", Arrays.asList(lines));
    PbFile file = (PbFile) myFixture.configureByText(PbFileType.INSTANCE, text);
    return ReadAction.computeBlocking(() -> file.findSymbols(QualifiedName.fromDottedString(symbol), PbStatement.class)
        .iterator()
        .next());
  }
}
