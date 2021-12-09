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
package com.intellij.protobuf.lang.comments;

import com.intellij.psi.PsiComment;
import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;
import com.intellij.protobuf.lang.PbFileType;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbCommentUtil;

import java.util.Arrays;
import java.util.List;

/** Tests for {@link PbCommentUtil}. */
public class PbCommentUtilTest extends PbCodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    TestUtils.registerTestdataFileExtension();
  }

  public void testSingleLeadingLineComment() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "// A comment", "option foo=true;");
    checkLeading(element, "A comment");
  }

  public void testManyLeadingLineComments() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "// A comment",
            "// Another comment",
            "option foo=true;");
    checkLeading(element, "A comment", "Another comment");
  }

  public void testSingleLeadingBlockComment() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "/* A comment */", "option foo=true;");
    checkLeading(element, "A comment");
  }

  public void testSingleLeadingBlockCommentSameLine() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "/* A comment */ option foo=true;");
    checkLeading(element, "A comment");
  }

  public void testSingleLeadingBlockCommentSameLineNoSpace() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "/* A comment */option foo=true;");

    checkLeading(element, "A comment");
  }

  public void testBlockCommentBeforeLeadingLineCommentIsIgnored() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "/* Ignored comment */",
            "// Comment",
            "option foo=true;");
    checkLeading(element, "Comment");
  }

  public void testOnlyOneLeadingBlockCommentIsCollected() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "/* Ignored comment */",
            "/* Comment */",
            "option foo=true;");
    checkLeading(element, "Comment");
  }

  public void testOnlyOneLeadingBlockCommentIsCollectedSameLine() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "/* Ignored comment */",
            "/* Comment */ option foo=true;");
    checkLeading(element, "Comment");
  }

  public void testBlankLineStopsLeadingLineCommentCollection() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "// Comment1",
            "",
            "// Comment2",
            "// Comment3",
            "option foo=true;");
    checkLeading(element, "Comment2", "Comment3");
  }

  public void testBlankLinePreventsLeadingLineCommentCollection() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "// Comment1", "", "option foo=true;");
    checkLeading(element);
  }

  public void testBlankLinePreventsLeadingBlockCommentCollection() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "/* Comment1 */", "", "option foo=true;");
    checkLeading(element);
  }

  public void testLeadingLineCommentAtBeginningOfFile() {
    PbStatement element = loadStatement(PbSyntaxStatement.class, "// Comment1", "syntax='proto2';");
    checkLeading(element, "Comment1");
  }

  public void testLeadingLineCommentAfterOnlyWhitespace() {
    PbStatement element =
        loadStatement(PbSyntaxStatement.class, "", "// Comment1", "syntax='proto2';");
    checkLeading(element, "Comment1");
  }

  public void testLeadingBlockCommentAtBeginningOfFile() {
    PbStatement element =
        loadStatement(PbSyntaxStatement.class, "/* Comment1 */", "syntax='proto2';");
    checkLeading(element, "Comment1");
  }

  public void testLeadingBlockCommentAfterOnlyWhitespace() {
    PbStatement element =
        loadStatement(PbSyntaxStatement.class, "", "/* Comment1 */", "syntax='proto2';");
    checkLeading(element, "Comment1");
  }

  public void testSingleTrailingLineComment() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true;",
            "// A comment",
            "",
            "message A {}");
    checkTrailing(element, "A comment");
  }

  public void testSingleTrailingLineCommentSameLine() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true; // A comment",
            "message A {}");
    checkTrailing(element, "A comment");
  }

  public void testSingleTrailingLineCommentSameLineNoSpace() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true;// A comment",
            "message A {}");
    checkTrailing(element, "A comment");
  }

  public void testSingleTrailingLineCommentSameLineStopsCollection() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true; // A comment",
            "// Not collected.",
            "",
            "message A {}");
    checkTrailing(element, "A comment");
  }

  public void testSingleTrailingLineCommentSameLineStopsBlockCommentCollection() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true; // A comment",
            "/* Not collected. */",
            "",
            "message A {}");
    checkTrailing(element, "A comment");
  }

  public void testManyTrailingLineComments() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true;",
            "// A comment",
            "// Another comment",
            "",
            "message A {}");
    checkTrailing(element, "A comment", "Another comment");
  }

  public void testSingleTrailingBlockComment() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true;",
            "/* A comment */",
            "",
            "message A {}");
    checkTrailing(element, "A comment");
  }

  public void testSingleTrailingBlockCommentSameLine() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true; /* A comment */",
            "message A {}");
    checkTrailing(element, "A comment");
  }

  public void testSingleTrailingBlockCommentSameLineNoSpace() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true;/* A comment */",
            "message A {}");
    checkTrailing(element, "A comment");
  }

  public void testBlockCommentAfterTrailingLineCommentIsIgnored() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true;",
            "// Comment",
            "/* Ignored */",
            "",
            "message A {}");
    checkTrailing(element, "Comment");
  }

  public void testOnlyOneTrailingBlockCommentIsCollected() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true;",
            "/* Comment */",
            "/* Ignored */",
            "",
            "message A {}");
    checkTrailing(element, "Comment");
  }

  public void testOnlyOneTrailingBlockCommentIsCollectedSameLine() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true; /* Comment */",
            "/* Ignored */",
            "",
            "message A {}");
    checkTrailing(element, "Comment");
  }

  public void testBlankLineStopsTrailingLineCommentCollection() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true;",
            "// Comment1",
            "// Comment2",
            "",
            "// Comment3",
            "",
            "message A {}");
    checkTrailing(element, "Comment1", "Comment2");
  }

  public void testBlankLinePreventsTrailingLineCommentCollection() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true;",
            "",
            "// Comment1",
            "// Comment2",
            "",
            "message A {}");
    checkTrailing(element);
  }

  public void testBlankLinePreventsTrailingBlockCommentCollection() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true;",
            "",
            "/* Comment1 */",
            "",
            "message A {}");
    checkTrailing(element);
  }

  public void testTrailingLineCommentAtEndOfFile() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "option foo=true;", "// Comment1");
    checkTrailing(element, "Comment1");
  }

  public void testTrailingLineCommentBeforeOnlyWhitespace() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "option foo=true;", "// Comment1", "");
    checkTrailing(element, "Comment1");
  }

  public void testTrailingBlockCommentAtEndOfFile() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "option foo=true;", "/* Comment1 */");
    checkTrailing(element, "Comment1");
  }

  public void testTrailingBlockCommentBeforeOnlyWhitespace() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "option foo=true;", "/* Comment1 */", "");
    checkTrailing(element, "Comment1");
  }

  public void testTrailingLineCommentAfterBlockOpenSameLine() {
    PbStatement element =
        loadStatement(
            PbMessageType.class,
            "syntax='proto2';",
            "message A {  // This comment belongs to A.",
            "  // Ignored.",
            "}");
    checkTrailing(element, "This comment belongs to A.");
  }

  public void testTrailingLineCommentsAfterBlockOpen() {
    PbStatement element =
        loadStatement(
            PbMessageType.class,
            "syntax='proto2';",
            "message A {",
            "  // This comment belongs to A.",
            "  // And this one.",
            "}");
    checkTrailing(element, "This comment belongs to A.", "And this one.");
  }

  public void testTrailingBlockCommentAfterBlockOpenSameLine() {
    PbStatement element =
        loadStatement(
            PbMessageType.class,
            "syntax='proto2';",
            "message A {  /* This comment belongs to A. */",
            "  // Ignored.",
            "}");
    checkTrailing(element, "This comment belongs to A.");
  }

  public void testTrailingBlockCommentAfterBlockOpen() {
    PbStatement element =
        loadStatement(
            PbMessageType.class,
            "syntax='proto2';",
            "message A {",
            "  /* This comment belongs to A. */",
            "  /* Ignored. */",
            "}");
    checkTrailing(element, "This comment belongs to A.");
  }

  public void testCommentSurroundedByStatementsOnSameLineBelongsToNeither() {
    PbFile file =
        loadFile("syntax='proto2';", "message A {", "  bool X = 1; /* Ignored */ bool Y = 2;", "}");
    PbStatement x = findSymbol("A.X", file);
    PbStatement y = findSymbol("A.Y", file);

    checkTrailing(x);
    checkLeading(y);
  }

  public void testTwoCommentsSurroundedByStatementsOnSameLineBelongToNeither() {
    PbFile file =
        loadFile(
            "syntax='proto2';",
            "message A {",
            "  bool X = 1; /* Ignored */ /* Ignored again */ bool Y = 2;",
            "}");
    PbStatement x = findSymbol("A.X", file);
    PbStatement y = findSymbol("A.Y", file);

    checkTrailing(x);
    checkLeading(y);
  }

  public void testTwoCommentsOnFirstLineBelongToNeither() {
    PbFile file =
        loadFile(
            "syntax='proto2';",
            "message A {",
            "  bool X = 1; /* Ignored */ /* Ignored again */",
            "  bool Y = 2;",
            "}");
    PbStatement x = findSymbol("A.X", file);
    PbStatement y = findSymbol("A.Y", file);

    checkTrailing(x);
    checkLeading(y);
  }

  public void testTwoCommentsOnFirstLinePreventLeadingCollectionForNextToken() {
    // TODO(b/33539835): This is probably a bug in protoc's comment collection.
    PbFile file =
        loadFile(
            "syntax='proto2';",
            "message A {",
            "  bool X = 1; /* Ignored */ /* Ignored again */",
            "",
            "  // This is also ignored",
            "  bool Y = 2;",
            "}");
    PbStatement x = findSymbol("A.X", file);
    PbStatement y = findSymbol("A.Y", file);

    checkTrailing(x);
    checkLeading(y);
  }

  public void testTwoCommentsOnSecondLineBelongToEach() {
    PbFile file =
        loadFile(
            "syntax='proto2';",
            "message A {",
            "  bool X = 1;",
            "   /* X comment */ /* Y comment */ bool Y = 2;",
            "}");
    PbStatement x = findSymbol("A.X", file);
    PbStatement y = findSymbol("A.Y", file);

    checkTrailing(x, "X comment");
    checkLeading(y, "Y comment");
  }

  public void testSingleBlockCommentBetweenTwoStatementsBelongsToSecond() {
    PbFile file =
        loadFile(
            "syntax='proto2';",
            "message A {",
            "  bool X = 1;",
            "  /* Comment */",
            "  bool Y = 2;",
            "}");
    PbStatement x = findSymbol("A.X", file);
    PbStatement y = findSymbol("A.Y", file);

    checkTrailing(x);
    checkLeading(y, "Comment");
  }

  public void testSingleLineCommentBetweenTwoStatementsBelongsToSecond() {
    PbFile file =
        loadFile(
            "syntax='proto2';",
            "message A {",
            "  bool X = 1;",
            "  // Comment",
            "  bool Y = 2;",
            "}");
    PbStatement x = findSymbol("A.X", file);
    PbStatement y = findSymbol("A.Y", file);

    checkTrailing(x);
    checkLeading(y, "Comment");
  }

  public void testTwoBlockCommentsBetweenTwoStatementsBelongToEach() {
    PbFile file =
        loadFile(
            "syntax='proto2';",
            "message A {",
            "  bool X = 1;",
            "  /* X comment */",
            "  /* Y comment */",
            "  bool Y = 2;",
            "}");
    PbStatement x = findSymbol("A.X", file);
    PbStatement y = findSymbol("A.Y", file);

    checkTrailing(x, "X comment");
    checkLeading(y, "Y comment");
  }

  public void testTwoBlockCommentsOnSameLineBetweenTwoStatementsBelongToEach() {
    PbFile file =
        loadFile(
            "syntax='proto2';",
            "message A {",
            "  bool X = 1;",
            "  /* X comment */ /* Y comment */",
            "  bool Y = 2;",
            "}");
    PbStatement x = findSymbol("A.X", file);
    PbStatement y = findSymbol("A.Y", file);

    checkTrailing(x, "X comment");
    checkLeading(y, "Y comment");
  }

  public void testTwoLineCommentsBetweenTwoStatementsBelongToSecond() {
    PbFile file =
        loadFile(
            "syntax='proto2';",
            "message A {",
            "  bool X = 1;",
            "  // Y comment 1",
            "  // Y comment 2",
            "  bool Y = 2;",
            "}");
    PbStatement x = findSymbol("A.X", file);
    PbStatement y = findSymbol("A.Y", file);

    checkTrailing(x);
    checkLeading(y, "Y comment 1", "Y comment 2");
  }

  public void testBlockAndLineCommentsBetweenTwoStatementsBelongToEach() {
    PbFile file =
        loadFile(
            "syntax='proto2';",
            "message A {",
            "  bool X = 1;",
            "  /* X comment */",
            "  // Y comment",
            "  bool Y = 2;",
            "}");
    PbStatement x = findSymbol("A.X", file);
    PbStatement y = findSymbol("A.Y", file);

    checkTrailing(x, "X comment");
    checkLeading(y, "Y comment");
  }

  public void testLineAndBlockCommentsBetweenTwoStatementsBelongToEach() {
    PbFile file =
        loadFile(
            "syntax='proto2';",
            "message A {",
            "  bool X = 1;",
            "  // X comment",
            "  /* Y comment */",
            "  bool Y = 2;",
            "}");
    PbStatement x = findSymbol("A.X", file);
    PbStatement y = findSymbol("A.Y", file);

    checkTrailing(x, "X comment");
    checkLeading(y, "Y comment");
  }

  public void testLineCommentTextExtraction() {
    PbStatement statement =
        loadSymbol(
            "A.Y",
            "syntax='proto2';",
            "message A {",
            "  //  This line should have 1 leading space and no trailing spaces.   ",
            "  // This line should have 0.  ",
            "  //   This line should have 2. ",
            "  bool Y = 2;",
            "}");
    checkLeading(
        statement,
        " This line should have 1 leading space and no trailing spaces.",
        "This line should have 0.",
        "  This line should have 2.");
  }

  public void testLineCommentTextExtractionLineWithNoLeadingSpaces() {
    PbStatement statement =
        loadSymbol(
            "A.Y",
            "syntax='proto2';",
            "message A {",
            "  //This line should have 0 leading spaces.",
            "  // This line should have 1.",
            "  bool Y = 2;",
            "}");
    checkLeading(statement, "This line should have 0 leading spaces.", " This line should have 1.");
  }

  public void testBlockCommentTextExtraction() {
    PbStatement statement =
        loadSymbol(
            "A.Y",
            "syntax='proto2';",
            "message A {",
            "  /*  This line should have 1 leading space and no trailing spaces.   ",
            "   * This line should have 0.  ",
            "   *   This line should have 2. ",
            "   */",
            "  bool Y = 2;",
            "}");
    checkLeading(
        statement,
        " This line should have 1 leading space and no trailing spaces.",
        "This line should have 0.",
        "  This line should have 2.");
  }

  public void testBlockCommentTextExtractionLineWithNoLeadingSpaces() {
    PbStatement statement =
        loadSymbol(
            "A.Y",
            "syntax='proto2';",
            "message A {",
            "  /*This line should have 0 leading spaces.",
            "   * This line should have 1.",
            "   */",
            "  bool Y = 2;",
            "}");
    checkLeading(statement, "This line should have 0 leading spaces.", " This line should have 1.");
  }

  public void testBlockCommentFirstAndLastBlankLinesAreOmitted() {
    PbStatement statement =
        loadSymbol(
            "A.Y",
            "syntax='proto2';",
            "message A {",
            "  /*",
            "   * The first and last lines should be ignored.",
            "   */",
            "  bool Y = 2;",
            "}");
    checkLeading(statement, "The first and last lines should be ignored.");

    statement =
        loadSymbol(
            "A.Y",
            "syntax='proto2';",
            "message A {",
            "  /* The last line should be ignored.",
            "   */",
            "  bool Y = 2;",
            "}");
    checkLeading(statement, "The last line should be ignored.");

    statement =
        loadSymbol(
            "A.Y",
            "syntax='proto2';",
            "message A {",
            "  /*",
            "   * The first line should be ignored. */",
            "  bool Y = 2;",
            "}");
    checkLeading(statement, "The first line should be ignored.");

    statement =
        loadSymbol(
            "A.Y",
            "syntax='proto2';",
            "message A {",
            "  /* One line block comment. */",
            "  bool Y = 2;",
            "}");
    checkLeading(statement, "One line block comment.");
  }

  private PbFile loadFile(String... lines) {
    String text = String.join("\n", Arrays.asList(lines));
    return (PbFile) myFixture.configureByText(PbFileType.INSTANCE, text);
  }

  private <T extends PbStatement> T loadStatement(Class<T> type, String... lines) {
    PbFile file = loadFile(lines);
    return file.getStatements()
        .stream()
        .filter(type::isInstance)
        .map(type::cast)
        .findFirst()
        .orElse(null);
  }

  private PbStatement loadSymbol(String symbol, String... lines) {
    PbFile file = loadFile(lines);
    return findSymbol(symbol, file);
  }

  private static PbStatement findSymbol(String symbol, PbFile file) {
    return file.findSymbols(QualifiedName.fromDottedString(symbol), PbStatement.class)
        .iterator()
        .next();
  }

  private static void checkLines(List<PsiComment> comments, String... expected) {
    List<String> actual = PbCommentUtil.extractText(comments);

    String expectedString = String.join("\n", Arrays.asList(expected));
    String actualString = String.join("\n", actual);

    assertEquals(expectedString, actualString);
  }

  private static void checkLeading(PbStatement element, String... expected) {
    checkLines(element.getLeadingComments(), expected);
  }

  private static void checkTrailing(PbStatement element, String... expected) {
    checkLines(element.getTrailingComments(), expected);
  }
}
