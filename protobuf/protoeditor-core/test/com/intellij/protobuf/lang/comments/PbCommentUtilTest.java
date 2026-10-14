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

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.protobuf.lang.PbFileType;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.psi.PbMessageType;
import com.intellij.protobuf.lang.psi.PbOptionStatement;
import com.intellij.protobuf.lang.psi.PbStatement;
import com.intellij.protobuf.lang.psi.PbSyntaxStatement;
import com.intellij.protobuf.lang.psi.util.PbCommentUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.util.QualifiedName;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests for {@link PbCommentUtil}. */
@TestApplication
public class PbCommentUtilTest {
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
  public void testSingleLeadingLineComment() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "// A comment", "option foo=true;");
    checkLeading(element, "A comment");
  }

  @Test
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

  @Test
  public void testSingleLeadingBlockComment() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "/* A comment */", "option foo=true;");
    checkLeading(element, "A comment");
  }

  @Test
  public void testSingleLeadingBlockCommentSameLine() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "/* A comment */ option foo=true;");
    checkLeading(element, "A comment");
  }

  @Test
  public void testSingleLeadingBlockCommentSameLineNoSpace() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "/* A comment */option foo=true;");

    checkLeading(element, "A comment");
  }

  @Test
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

  @Test
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

  @Test
  public void testOnlyOneLeadingBlockCommentIsCollectedSameLine() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "/* Ignored comment */",
            "/* Comment */ option foo=true;");
    checkLeading(element, "Comment");
  }

  @Test
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

  @Test
  public void testBlankLinePreventsLeadingLineCommentCollection() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "// Comment1", "", "option foo=true;");
    checkLeading(element);
  }

  @Test
  public void testBlankLinePreventsLeadingBlockCommentCollection() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "/* Comment1 */", "", "option foo=true;");
    checkLeading(element);
  }

  @Test
  public void testLeadingLineCommentAtBeginningOfFile() {
    PbStatement element = loadStatement(PbSyntaxStatement.class, "// Comment1", "syntax='proto2';");
    checkLeading(element, "Comment1");
  }

  @Test
  public void testLeadingLineCommentAfterOnlyWhitespace() {
    PbStatement element =
        loadStatement(PbSyntaxStatement.class, "", "// Comment1", "syntax='proto2';");
    checkLeading(element, "Comment1");
  }

  @Test
  public void testLeadingBlockCommentAtBeginningOfFile() {
    PbStatement element =
        loadStatement(PbSyntaxStatement.class, "/* Comment1 */", "syntax='proto2';");
    checkLeading(element, "Comment1");
  }

  @Test
  public void testLeadingBlockCommentAfterOnlyWhitespace() {
    PbStatement element =
        loadStatement(PbSyntaxStatement.class, "", "/* Comment1 */", "syntax='proto2';");
    checkLeading(element, "Comment1");
  }

  @Test
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

  @Test
  public void testSingleTrailingLineCommentSameLine() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true; // A comment",
            "message A {}");
    checkTrailing(element, "A comment");
  }

  @Test
  public void testSingleTrailingLineCommentSameLineNoSpace() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true;// A comment",
            "message A {}");
    checkTrailing(element, "A comment");
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testSingleTrailingBlockCommentSameLine() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true; /* A comment */",
            "message A {}");
    checkTrailing(element, "A comment");
  }

  @Test
  public void testSingleTrailingBlockCommentSameLineNoSpace() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class,
            "syntax='proto2';",
            "option foo=true;/* A comment */",
            "message A {}");
    checkTrailing(element, "A comment");
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testTrailingLineCommentAtEndOfFile() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "option foo=true;", "// Comment1");
    checkTrailing(element, "Comment1");
  }

  @Test
  public void testTrailingLineCommentBeforeOnlyWhitespace() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "option foo=true;", "// Comment1", "");
    checkTrailing(element, "Comment1");
  }

  @Test
  public void testTrailingBlockCommentAtEndOfFile() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "option foo=true;", "/* Comment1 */");
    checkTrailing(element, "Comment1");
  }

  @Test
  public void testTrailingBlockCommentBeforeOnlyWhitespace() {
    PbStatement element =
        loadStatement(
            PbOptionStatement.class, "syntax='proto2';", "option foo=true;", "/* Comment1 */", "");
    checkTrailing(element, "Comment1");
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testCommentSurroundedByStatementsOnSameLineBelongsToNeither() {
    PbFile file =
        loadFile("syntax='proto2';", "message A {", "  bool X = 1; /* Ignored */ bool Y = 2;", "}");
    PbStatement x = findSymbol("A.X", file);
    PbStatement y = findSymbol("A.Y", file);

    checkTrailing(x);
    checkLeading(y);
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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
    return ReadAction.computeBlocking(() -> file.getStatements()
        .stream()
        .filter(type::isInstance)
        .map(type::cast)
        .findFirst()
        .orElse(null));
  }

  private PbStatement loadSymbol(String symbol, String... lines) {
    PbFile file = loadFile(lines);
    return findSymbol(symbol, file);
  }

  private static PbStatement findSymbol(String symbol, PbFile file) {
    return ReadAction.computeBlocking(() -> file.findSymbols(QualifiedName.fromDottedString(symbol), PbStatement.class)
        .iterator()
        .next());
  }

  private static void checkLines(List<PsiComment> comments, String... expected) {
    List<String> actual = ReadAction.computeBlocking(() -> PbCommentUtil.extractText(comments));

    String expectedString = String.join("\n", Arrays.asList(expected));
    String actualString = String.join("\n", actual);

    assertEquals(expectedString, actualString);
  }

  private static void checkLeading(PbStatement element, String... expected) {
    checkLines(ReadAction.computeBlocking(() -> element.getLeadingComments()), expected);
  }

  private static void checkTrailing(PbStatement element, String... expected) {
    checkLines(ReadAction.computeBlocking(() -> element.getTrailingComments()), expected);
  }
}
