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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;
import com.intellij.protobuf.lang.psi.*;

/** Test cases for {@link SchemaProvider}. */
public class SchemaProviderTest extends PbCodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    TestUtils.registerTestdataFileExtension();
  }

  public void testRootMessageProvider() {
    final PbFile messageProto =
        (PbFile) myFixture.configureByFile("lang/resolve/root_message.proto");
    final PbMessageType message =
        messageProto
            .findSymbols(QualifiedName.fromDottedString("foo.bar.Message"), PbMessageType.class)
            .stream()
            .findFirst()
            .orElse(null);
    assertNotNull(message);

    // Register a simple RootMessageProvider extension which returns the message found above.
    // Needs to be unregistered afterwards to not interfere with subsequent tests.
    SchemaProvider extension =
        textFile -> {
          assertEquals("root_data.pb", textFile.getVirtualFile().getName());
          return SchemaInfo.create(message);
        };

    Disposable disposable = new TestDisposable();
    getProject().getExtensionArea().getExtensionPoint(SchemaProvider.EP_NAME).registerExtension(extension, disposable);
    try {
      PbTextFile textFile = (PbTextFile) myFixture.configureByFile("lang/resolve/root_data.pb");
      PbTextMessage rootMessage = textFile.getRootMessage();
      assertNotNull(rootMessage);

      PbMessageType declaredMessage = rootMessage.getDeclaredMessage();
      assertEquals(message, declaredMessage);

      PbTextField field = rootMessage.getFields().get(0);
      assertNotNull(field.getFieldName().getDeclaredField());
    } finally {
      Disposer.dispose(disposable);
    }
  }
}
