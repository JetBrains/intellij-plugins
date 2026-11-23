/*
 * Copyright 2026 Google LLC
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
package com.intellij.protobuf.lang.psi

import com.google.common.truth.Truth.assertThat
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.TextRange
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase
import com.intellij.protobuf.lang.PbFileType
import com.intellij.protobuf.lang.PbTextFileType
import com.intellij.psi.ElementManipulators
import com.intellij.psi.util.PsiTreeUtil

class PbTextLanguageInjectionTest : PbCodeInsightFixtureTestCase() {

  private fun parseStringValue(text: String): PbTextStringValue {
    val file = myFixture.configureByText(PbTextFileType.INSTANCE, text)
    return checkNotNull(PsiTreeUtil.findChildOfType(file, PbTextStringValue::class.java))
  }

  fun `test content range excludes outer quotes`() {
    val host = parseStringValue("""field: "hello"""")
    val escaper = host.createLiteralTextEscaper()
    assertThat(escaper.relevantTextRange).isEqualTo(TextRange(1, 6))
    assertThat(ElementManipulators.getValueTextRange(host)).isEqualTo(TextRange(1, 6))
    assertThat(ElementManipulators.getValueText(host)).isEqualTo("hello")

    val out = StringBuilder()
    assertThat(escaper.decode(escaper.relevantTextRange, out)).isTrue()
    assertThat(out.toString()).isEqualTo("hello")
    assertThat(escaper.getOffsetInHost(0, escaper.relevantTextRange)).isEqualTo(1)
    assertThat(escaper.getOffsetInHost(5, escaper.relevantTextRange)).isEqualTo(6)
  }

  fun `test empty single and multipart string ranges`() {
    val singleEmpty = parseStringValue("""field: """"")
    val singleEscaper = singleEmpty.createLiteralTextEscaper()
    assertThat(singleEscaper.relevantTextRange).isEqualTo(TextRange(1, 1))
    assertThat(ElementManipulators.getValueTextRange(singleEmpty)).isEqualTo(TextRange(1, 1))
    val outSingle = StringBuilder()
    assertThat(singleEscaper.decode(singleEscaper.relevantTextRange, outSingle)).isTrue()
    assertThat(outSingle.toString()).isEmpty()
    assertThat(singleEscaper.getOffsetInHost(0, singleEscaper.relevantTextRange)).isEqualTo(1)

    val multiEmpty = parseStringValue("""field: "" """"")
    val multiEscaper = multiEmpty.createLiteralTextEscaper()
    assertThat(multiEscaper.relevantTextRange).isEqualTo(TextRange(1, 1))

    val leadingTrailingEmpty = parseStringValue("""field: "" "hello" """"")
    val ltEscaper = leadingTrailingEmpty.createLiteralTextEscaper()
    assertThat(ltEscaper.relevantTextRange).isEqualTo(TextRange(4, 9))
  }

  fun `test single part edit preserves host and untouched sibling parts`() {
    val host =
      parseStringValue(
        """
        field: "line 1\n"
               "line 2\n"
               "line 3"
        """.trimIndent()
      )
    val part0Before = host.stringParts[0]
    val part2Before = host.stringParts[2]

    val updated =
      WriteCommandAction.runWriteCommandAction<PbTextStringValue>(project) {
        host.updateText("line 1\nline TWO\nline 3") as PbTextStringValue
      }

    assertThat(updated).isSameInstanceAs(host)
    assertThat(updated.isValid).isTrue()
    assertThat(updated.stringParts[0]).isSameInstanceAs(part0Before)
    assertThat(updated.stringParts[2]).isSameInstanceAs(part2Before)
    assertThat(updated.value).isEqualTo("line 1\nline TWO\nline 3")
  }

  fun `test full part deletion with shared suffix drops only target part`() {
    val host =
      parseStringValue(
        """
        sql: "  bar\n"
             "SELECT   bar\n"
             "    baz\n"
        """.trimIndent()
      )

    WriteCommandAction.runWriteCommandAction(project) {
      host.updateText("  bar\n    baz\n")
    }

    assertThat(host.stringParts.map { it.text }).containsExactly("\"  bar\\n\"", "\"    baz\\n\"").inOrder()
    assertThat(host.value).isEqualTo("  bar\n    baz\n")
  }

  fun `test cross part edit with mixed quotes produces valid literal`() {
    val host =
      parseStringValue(
        """
        field: 'hello '
               "world's"
        """.trimIndent()
      )

    WriteCommandAction.runWriteCommandAction(project) {
      host.updateText("hellO World's")
    }

    assertThat(host.isValidHost).isTrue()
    assertThat(host.value).isEqualTo("hellO World's")
    assertThat(host.text).isEqualTo("'hellO World\\'s'")
  }

  fun `test subrange handleContentChange updates only subrange`() {
    val host = parseStringValue("""path: "dir/file.txt"""")

    WriteCommandAction.runWriteCommandAction(project) {
      ElementManipulators.handleContentChange(host, TextRange(5, 13), "renamed.txt")
    }

    assertThat(host.value).isEqualTo("dir/renamed.txt")
    assertThat(host.text).isEqualTo(""""dir/renamed.txt"""")
  }

  fun `test interpart comment is not duplicated on newline split`() {
    val host =
      parseStringValue(
        """
        field: "line 1\n"
               # keep once
               "line 2"
        """.trimIndent()
      )

    WriteCommandAction.runWriteCommandAction(project) {
      host.updateText("line 1\ninserted a\ninserted b\nline 2")
    }

    assertThat(host.value).isEqualTo("line 1\ninserted a\ninserted b\nline 2")
    val commentOccurrences = Regex("# keep once").findAll(host.containingFile.text).count()
    assertThat(commentOccurrences).isEqualTo(1)
  }

  fun `test proto file aggregate value preserves c style comments between parts`() {
    val file =
      myFixture.configureByText(
        PbFileType.INSTANCE,
        """
        syntax = "proto3";
        import "google/protobuf/descriptor.proto";
        extend google.protobuf.MessageOptions {
          MyOpt opt = 1000;
        }
        message MyOpt {
          string str = 1;
        }
        message Msg {
          option (opt) = {
            str: "part 1\n"
                 // c-style comment
                 "part 2"
          };
        }
        """.trimIndent()
      )
    val host = checkNotNull(PsiTreeUtil.findChildOfType(file, PbTextStringValue::class.java))

    WriteCommandAction.runWriteCommandAction(project) {
      host.updateText("part ONE\npart 2")
    }

    assertThat(host.value).isEqualTo("part ONE\npart 2")
    assertThat(host.text).contains("// c-style comment")
  }

  fun `test surrogate pair escape replacement does not split escape`() {
    val host = parseStringValue("""emoji: "\U0001f600"""")
    assertThat(host.value).isEqualTo("\uD83D\uDE00")

    WriteCommandAction.runWriteCommandAction(project) {
      host.updateText("\uD83D\uDE0E")
    }

    assertThat(host.isValidHost).isTrue()
    assertThat(host.value).isEqualTo("\uD83D\uDE0E")
  }

  fun `test short octal escape followed by octal digit is padded`() {
    val host = parseStringValue("""data: "\0abc"""")
    assertThat(host.value).isEqualTo("\u0000abc")

    WriteCommandAction.runWriteCommandAction(project) {
      host.updateText("\u00007")
    }

    assertThat(host.isValidHost).isTrue()
    assertThat(host.value).isEqualTo("\u00007")
    assertThat(host.text).isEqualTo(""""\0007"""")
  }

  fun `test tab indentation preserved when splitting single part`() {
    val host = parseStringValue("\t\tfield: \"single\"")

    WriteCommandAction.runWriteCommandAction(project) {
      host.updateText("a\nb\nc\nd")
    }

    assertThat(host.value).isEqualTo("a\nb\nc\nd")
    assertThat(host.text).isEqualTo("\"a\\n\"\n\t\t       \"b\\n\"\n\t\t       \"c\\n\"\n\t\t       \"d\"")
  }
}
