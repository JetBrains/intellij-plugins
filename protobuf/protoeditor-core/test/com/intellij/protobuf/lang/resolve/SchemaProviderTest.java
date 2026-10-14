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
package com.intellij.protobuf.lang.resolve;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.psi.PbMessageType;
import com.intellij.protobuf.lang.psi.PbTextField;
import com.intellij.protobuf.lang.psi.PbTextFile;
import com.intellij.protobuf.lang.psi.PbTextMessage;
import com.intellij.psi.util.QualifiedName;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Test cases for {@link SchemaProvider}. */
@TestApplication
public class SchemaProviderTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  private CodeInsightTestFixture myFixture;

  @BeforeEach
  void setUp() {
    myFixture = codeInsightFixture.get();
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture);
    EdtTestUtil.runInEdtAndWait(TestUtils::registerTestdataFileExtension);
  }

  @Test
  public void testRootMessageProvider() {
    final PbFile messageProto =
        (PbFile) myFixture.configureByFile("lang/resolve/root_message.proto");
    final PbMessageType message = ReadAction.computeBlocking(() ->
        messageProto
            .findSymbols(QualifiedName.fromDottedString("foo.bar.Message"), PbMessageType.class)
            .stream()
            .findFirst()
            .orElse(null));
    assertNotNull(message);

    // Register a simple RootMessageProvider extension which returns the message found above.
    // Needs to be unregistered afterwards to not interfere with subsequent tests.
    SchemaProvider extension =
        textFile -> {
          assertEquals("root_data.pb", textFile.getVirtualFile().getName());
          return SchemaInfo.create(message);
        };

    Disposable disposable = Disposer.newDisposable();
    myFixture.getProject().getExtensionArea().getExtensionPoint(SchemaProvider.EP_NAME).registerExtension(extension, disposable);
    try {
      PbTextFile textFile = (PbTextFile) myFixture.configureByFile("lang/resolve/root_data.pb");
      ReadAction.runBlocking(() -> {
        PbTextMessage rootMessage = textFile.getRootMessage();
        assertNotNull(rootMessage);

        PbMessageType declaredMessage = rootMessage.getDeclaredMessage();
        assertEquals(message, declaredMessage);

        PbTextField field = rootMessage.getFields().get(0);
        assertNotNull(field.getFieldName().getDeclaredField());
      });
    } finally {
      Disposer.dispose(disposable);
    }
  }
}
