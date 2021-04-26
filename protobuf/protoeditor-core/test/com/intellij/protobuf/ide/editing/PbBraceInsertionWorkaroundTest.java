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
package com.intellij.protobuf.ide.editing;

import com.intellij.injected.editor.EditorWindow;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;

import static com.google.common.truth.Truth.assertThat;

/**
 * These tests exercised workarounds required to make language injection work for prototext within
 * protobufs. They're likely unnecessary now that injection is no longer used, but remain to catch
 * regressions.
 */
public class PbBraceInsertionWorkaroundTest extends PbCodeInsightFixtureTestCase {

  public void testIndentOnClosingCurlyBrace() {
    String before =
        String.join(
            "\n",
            "foo {",
            "  bar {",
            "  foo: 10",
            "<caret>", // <- we'll type a '}' here, ending the block
            "}");

    String after =
        String.join(
            "\n",
            "foo {",
            "  bar {",
            "    foo: 10", // <- newly indented.
            "  }<caret>",
            "}");

    doBraceInsertionTest("test.pb", before, after, '}');
  }

  public void testIndentOnClosingPointyBrace() {
    String before =
        String.join(
            "\n",
            "foo {",
            "  bar <",
            "  foo: 10",
            "<caret>", // <- we'll type a '>' here, ending the block
            "}");

    String after =
        String.join(
            "\n",
            "foo {",
            "  bar <",
            "    foo: 10", // <- newly indented.
            "  ><caret>",
            "}");

    doBraceInsertionTest("test.pb", before, after, '>');
  }

  public void testInjectedIndentOnClosingCurlyBrace() {
    String before =
        String.join(
            "\n",
            "option foo = {",
            "  bar {",
            "  foo: 10",
            "<caret>", // <- we'll type a '}' here, ending the block
            "};");

    String after =
        String.join(
            "\n",
            "option foo = {",
            "  bar {",
            "    foo: 10", // <- newly indented.
            "  }<caret>",
            "};");

    doBraceInsertionTest("test.proto", before, after, '}');
  }

  public void testInjectedMoreIndentOnClosingCurlyBrace() {
    String before =
        String.join(
            "\n",
            "option foo = {",
            "bar {",
            " foo: 10",
            "}",
            "<caret>;"); // <- we'll type a '}' here, ending the block

    String after =
        String.join(
            "\n",
            "option foo = {",
            "  bar {",
            "    foo: 10", // <- newly indented.
            "  }",
            "}<caret>;");

    doBraceInsertionTest("test.proto", before, after, '}');
  }

  public void testInjectedIndentOnClosingPointyBrace() {
    String before =
        String.join(
            "\n",
            "option foo = {",
            "  bar <",
            "  foo: 10",
            "<caret>", // <- we'll type a '>' here, ending the block
            "};");

    String after =
        String.join(
            "\n",
            "option foo = {",
            "  bar <",
            "    foo: 10", // <- newly indented.
            "  ><caret>",
            "};");

    doBraceInsertionTest("test.proto", before, after, '>');
  }

  public void testInjectedIndentOnSelectedClosingCurlyBrace() {
    String before =
        String.join(
            "\n",
            "option foo = {",
            "  bar {",
            "  foo: 10",
            "<selection>}</selection><caret>", // <- we'll type a '}' here replacing the original.
            "};");

    String after =
        String.join(
            "\n",
            "option foo = {",
            "  bar {",
            "    foo: 10", // <- newly indented.
            "  }<caret>",
            "};");

    doBraceInsertionTest("test.proto", before, after, '}');
  }

  public void testInjectedIndentOnPrecedingNewline() {
    String before =
        String.join(
            "\n",
            "message Foo {",
            "  option foo = <caret>{",
            "    bar {",
            "      foo: 10",
            "    }",
            "  };",
            "}");

    String after =
        String.join(
            "\n",
            "message Foo {",
            "  option foo = ",
            "  <caret>{",
            "    bar {",
            "      foo: 10",
            "    }",
            "  };",
            "}");

    doBraceInsertionTest("test.proto", before, after, '\n');
  }

  public void testInjectedIndentOnMultiplePrecedingNewlines() {
    String before =
        String.join(
            "\n",
            "message Foo {",
            "  option foo =",
            "  <caret>{",
            "    bar {",
            "      foo: 10",
            "    }",
            "  };",
            "}");

    String after =
        String.join(
            "\n",
            "message Foo {",
            "  option foo =",
            "  ",
            "  <caret>{",
            "    bar {",
            "      foo: 10",
            "    }",
            "  };",
            "}");

    doBraceInsertionTest("test.proto", before, after, '\n');
  }

  public void testInjectedIndentOnPrecedingInnerNewline() {
    String before =
        String.join(
            "\n",
            "message Foo {",
            "  option foo = {",
            "    bar <caret>{",
            "      foo: 10",
            "    }",
            "  };",
            "}");

    String after =
        String.join(
            "\n",
            "message Foo {",
            "  option foo = {",
            "    bar ",
            "    <caret>{",
            "      foo: 10",
            "    }",
            "  };",
            "}");

    doBraceInsertionTest("test.proto", before, after, '\n');
  }

  public void testInjectedEnterAfterOpenBrace() {
    String before =
        String.join(
            "\n",
            "message Foo {",
            "  option foo = {<caret>",
            "    bar {",
            "      foo: 10",
            "    }",
            "  };",
            "}");

    String after =
        String.join(
            "\n",
            "message Foo {",
            "  option foo = {",
            "    <caret>",
            "    bar {",
            "      foo: 10",
            "    }",
            "  };",
            "}");

    doBraceInsertionTest("test.proto", before, after, '\n');
  }

  public void testInjectedEnterOnNewlineBeforeFirstElement() {
    String before =
        String.join(
            "\n",
            "message Foo {",
            "  option foo = {",
            "    <caret>",
            "    bar {",
            "      foo: 10",
            "    }",
            "  };",
            "}");

    String after =
        String.join(
            "\n",
            "message Foo {",
            "  option foo = {",
            "    ",
            "    <caret>",
            "    bar {",
            "      foo: 10",
            "    }",
            "  };",
            "}");

    doBraceInsertionTest("test.proto", before, after, '\n');
  }

  public void testInjectedEnterBeforeFirstElement() {
    String before =
        String.join(
            "\n",
            "message Foo {",
            "  option foo = {",
            "    <caret>bar {",
            "      foo: 10",
            "    }",
            "  };",
            "}");

    String after =
        String.join(
            "\n",
            "message Foo {",
            "  option foo = {",
            "    ",
            "    <caret>bar {",
            "      foo: 10",
            "    }",
            "  };",
            "}");

    doBraceInsertionTest("test.proto", before, after, '\n');
  }

  public void testInjectedEnterBeforeLastElement() {
    String before =
        String.join(
            "\n",
            "message Foo {",
            "  option foo = {",
            "    bar {",
            "      foo: 10",
            "    <caret>}",
            "  };",
            "}");

    String after =
        String.join(
            "\n",
            "message Foo {",
            "  option foo = {",
            "    bar {",
            "      foo: 10",
            "    ",
            "    <caret>}",
            "  };",
            "}");

    doBraceInsertionTest("test.proto", before, after, '\n');
  }

  public void testInjectedEnterAfterLastElement() {
    String before =
        String.join(
            "\n",
            "message Foo {",
            "  option foo = {",
            "    bar {",
            "      foo: 10",
            "    }<caret>",
            "  };",
            "}");

    String after =
        String.join(
            "\n",
            "message Foo {",
            "  option foo = {",
            "    bar {",
            "      foo: 10",
            "    }",
            "    <caret>",
            "  };",
            "}");

    doBraceInsertionTest("test.proto", before, after, '\n');
  }

  public void testNewlineInsertionWorkaround() {
    String input = String.join("\n", "option foo = <caret>{", "  foo: 10", "", "");
    String output = String.join("\n", "option foo =", "{", "  foo: 10", "}", "");

    // Disable fixture's injection support so that the configured file and editor are for the host
    // .proto file.
    myFixture.setCaresAboutInjection(false);
    myFixture.configureByText("test.proto", input);

    // Place a newline before the opening brace
    myFixture.type('\n');

    PsiDocumentManager.getInstance(getProject()).commitAllDocuments();

    // Move the caret to the end of the file.
    getEditor().getCaretModel().moveToOffset(getEditor().getDocument().getTextLength() - 1);

    // Type the closing brace
    myFixture.type('}');

    // At this point, without the prefix hack, there are additional spaces inserted above the
    // opening brace. With the hack, only the newline that we inserted originally is there.
    assertThat(getEditor().getDocument().getText()).isEqualTo(output);
  }

  private void doBraceInsertionTest(String filename, String before, String after, char c) {
    myFixture.configureByText(filename, before);
    Editor editor = myFixture.getEditor();
    if (editor instanceof EditorWindow) {
      editor = ((EditorWindow) editor).getDelegate();
    }

    // Type the closing brace.
    myFixture.type(c);

    int finalCaretPos = after.indexOf("<caret>");
    after = after.replace("<caret>", "");

    // The result should include an auto-indented foo variable.
    assertThat(editor.getDocument().getText()).isEqualTo(after);
    assertThat(editor.getCaretModel().getOffset()).isEqualTo(finalCaretPos);
  }
}
