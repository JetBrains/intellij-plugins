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

import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;
import com.intellij.protobuf.lang.PbFileType;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.psi.PbStatement;

import java.util.Arrays;

/** Tests for {@link PbDocumentationProvider}. */
public class PbDocumentationProviderTest extends PbCodeInsightFixtureTestCase {

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
    String doc = new PbDocumentationProvider().generateDoc(msg, null);
    assertEquals(
        "<pre>Comments for Msg. Example:\n  Msg.abcd = 5\n&lt;escaped-tag/&gt;\n</pre>", doc);
  }

  public void testGenerateDocWithNoCommentsReturnsNull() {
    PbStatement msg = loadSymbol("Msg", "syntax = 'proto2';", "", "message Msg {", "}");
    String doc = new PbDocumentationProvider().generateDoc(msg, null);
    assertNull(doc);
  }

  private PbStatement loadSymbol(String symbol, String... lines) {
    String text = String.join("\n", Arrays.asList(lines));
    PbFile file = (PbFile) myFixture.configureByText(PbFileType.INSTANCE, text);
    return file.findSymbols(QualifiedName.fromDottedString(symbol), PbStatement.class)
        .iterator()
        .next();
  }
}
