// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.typing;

import com.intellij.application.options.CodeStyle;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.impl.TrailingSpacesStripper;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartLanguage;
import org.jetbrains.annotations.NotNull;

public class DartTypingTest extends DartCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return "/typing";
  }

  protected void doTest(char charToType) {
    myFixture.configureByFiles(getTestName(false) + ".dart");
    myFixture.type(charToType);
    myFixture.checkResultByFile(getTestName(false) + "_after.dart");
  }

  private void doTypingTest(final char charToType, @NotNull final String textBefore, @NotNull final String textAfter) {
    doTypingTest(DartFileType.INSTANCE, charToType, textBefore, textAfter);
  }

  private void doTypingTest(@NotNull final LanguageFileType fileType,
                            final char charToType,
                            @NotNull final String textBefore,
                            @NotNull final String textAfter) {
    myFixture.configureByText(fileType, textBefore);
    myFixture.type(charToType);
    myFixture.checkResult(textAfter);
  }

  private void doBackspaceTest(final @NotNull String textBefore, final @NotNull String textAfter) {
    myFixture.configureByText(DartFileType.INSTANCE, textBefore);
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_BACKSPACE);
    myFixture.checkResult(textAfter);
  }

  public void testDocComment() {
    doTest('\n');
  }

  public void testDocComment2() {
    doTest('\n');
  }

  public void testDocComment3() {
    doTest('\n');
  }

  public void testGenericBraceWithMultiCaret() {
    doTest('<');
  }

  public void testGenericBraceWithMultiCaretInDifferentContexts() {
    doTest('<');
  }

  public void testGenericBrace1() {
    doTest('<');
  }

  public void testGenericBrace2() {
    doTest('<');
  }

  public void testGenericBrace3() {
    doTest('<');
  }

  public void testLess() {
    doTest('<');
  }

  public void testStringWithMultiCaret() {
    doTest('{');
  }

  public void testStringWithMultiCaretInDifferentContexts() {
    doTest('{');
  }

  public void testString1() {
    doTest('{');
  }

  public void testString2() {
    doTest('{');
  }

  public void testString3() {
    doTest('{');
  }

  public void testQuote() {
    doTypingTest('\'', "var foo = <caret>", "var foo = '<caret>'");
    doTypingTest('"', "var foo = <caret>", "var foo = \"<caret>\"");
    doTypingTest('"', "var foo = '<caret>'", "var foo = '\"<caret>'");
    doTypingTest('\'', "var foo = \"<caret>\"", "var foo = \"'<caret>\"");
    doTypingTest('\'', "var foo = \"bar<caret>\"", "var foo = \"bar'<caret>\"");
    doTypingTest('\'', "import <caret>", "import '<caret>'");
    doTypingTest('"', "import <caret>", "import \"<caret>\"");
    doTypingTest('\'', "var foo = '<caret>'", "var foo = ''<caret>");
    doTypingTest('\"', "var foo = \"<caret>\"", "var foo = \"\"<caret>");
    doTypingTest('\'', "var foo = 'bar<caret>'", "var foo = 'bar'<caret>");
    doTypingTest('\"', "var foo = \"bar<caret>\"", "var foo = \"bar\"<caret>");
    doTypingTest('\'', "var foo = 'bar' <caret>", "var foo = 'bar' '<caret>'");
    doTypingTest('\"', "var foo = \"\" <caret>", "var foo = \"\" \"<caret>\"");
    doTypingTest('"', "var foo = r<caret>", "var foo = r\"<caret>\"");
    doTypingTest('\'', "var foo = r<caret>", "var foo = r'<caret>'");
    doTypingTest('"', "var foo = bar(r<caret>)", "var foo = bar(r\"<caret>\")");
    doTypingTest('\'', "var foo = bar(r<caret>)", "var foo = bar(r'<caret>')");
    doTypingTest('"', "var foo = r'<caret>", "var foo = r'\"<caret>");
    doTypingTest('\'', "var foo = r'<caret>", "var foo = r''<caret>");
    doTypingTest('\'', "var foo = r'<caret>'", "var foo = r''<caret>");
    doTypingTest('"', "var foo = r\"foo<caret>\"", "var foo = r\"foo\"<caret>");
    doTypingTest('\'', "var foo = r'<caret>'\"", "var foo = r''<caret>\"");
  }

  public void testBackspace() {
    doBackspaceTest("var foo = \"<caret> \"", "var foo = <caret> \"");
    doBackspaceTest("var foo = \"<caret>\"", "var foo = <caret>");
    doBackspaceTest("var foo = '<caret>a'", "var foo = <caret>a'");
    doBackspaceTest("import '<caret>'", "import <caret>");
    doBackspaceTest("var foo = \"\"<caret>", "var foo = \"<caret>");
    doBackspaceTest("var foo = \" '<caret>' \"", "var foo = \" <caret>' \"");
    doBackspaceTest("var foo = '\"<caret>\"'", "var foo = '<caret>\"'");
  }

  public void testWEB_8315() {
    doTypingTest('\n',
                 "class X {\n" +
                 "  num x;<caret>\n" +
                 "}",
                 "class X {\n" +
                 "  num x;\n" +
                 "  <caret>\n" +
                 "}");
  }

  public void testCaseAlignAfterColon1() {
    doTypingTest(':',
                 "class X {\n" +
                 "  void doit(x) {\n" +
                 "    switch (x) {\n" +
                 "      case 1<caret>\n" +
                 "    }\n" +
                 "  }\n" +
                 "}",
                 "class X {\n" +
                 "  void doit(x) {\n" +
                 "    switch (x) {\n" +
                 "      case 1:<caret>\n" +
                 "    }\n" +
                 "  }\n" +
                 "}");
  }

  public void testCaseAlignAfterColon2() {
    doTypingTest(':',
                 "class X {\n" +
                 "  void doit(x) {\n" +
                 "    switch (x) {\n" +
                 "      case 1:\n" +
                 "    case 2<caret>\n" +
                 "    }\n" +
                 "  }\n" +
                 "}",
                 "class X {\n" +
                 "  void doit(x) {\n" +
                 "    switch (x) {\n" +
                 "      case 1:\n" +
                 "      case 2:<caret>\n" +
                 "    }\n" +
                 "  }\n" +
                 "}");
  }

  public void testDefaultAlignAfterColon() {
    doTypingTest(':',
                 "class X {\n" +
                 "  void doit(x) {\n" +
                 "    switch (x) {\n" +
                 "      case 1:\n" +
                 "    default<caret>\n" +
                 "    }\n" +
                 "  }\n" +
                 "}",
                 "class X {\n" +
                 "  void doit(x) {\n" +
                 "    switch (x) {\n" +
                 "      case 1:\n" +
                 "      default:<caret>\n" +
                 "    }\n" +
                 "  }\n" +
                 "}");
  }

  public void testCaseStringAlignAfterColon() {
    doTypingTest(':',
                 "class X {\n" +
                 "  void doit(x) {\n" +
                 "    switch (x) {\n" +
                 "      case 1:\n" +
                 "    case '<caret>'\n" +
                 "    }\n" +
                 "  }\n" +
                 "}",
                 "class X {\n" +
                 "  void doit(x) {\n" +
                 "    switch (x) {\n" +
                 "      case 1:\n" +
                 "    case ':<caret>'\n" +
                 "    }\n" +
                 "  }\n" +
                 "}");
  }

  public void testEnterInSwitch() {
    doTypingTest('\n',
                 "void bar() {\n" +
                 "  switch (1) {<caret>\n" +
                 "}",
                 "void bar() {\n" +
                 "  switch (1) {\n" +
                 "    <caret>\n" +
                 "  }\n" +
                 "}");
  }

  public void testEnterAfterCase() {
    doTypingTest('\n',
                 "void bar() {\n" +
                 "  switch (1) {\n" +
                 "    case 1+1: <caret>\n" +
                 "      a;\n" +
                 "    case 2:\n" +
                 "  }\n" +
                 "}",
                 "void bar() {\n" +
                 "  switch (1) {\n" +
                 "    case 1+1: \n" +
                 "      <caret>\n" +
                 "      a;\n" +
                 "    case 2:\n" +
                 "  }\n" +
                 "}");
  }

  public void testEnterAfterDefault() {
    doTypingTest('\n',
                 "void bar() {\n" +
                 "  switch (1) {\n" +
                 "    case 1:\n" +
                 "    default:<caret>\n" +
                 "  }\n" +
                 "}",
                 "void bar() {\n" +
                 "  switch (1) {\n" +
                 "    case 1:\n" +
                 "    default:\n" +
                 "      <caret>\n" +
                 "  }\n" +
                 "}");
  }

  public void testEnterAfterBreakInCase() {
    final String textBefore = "void bar() {\n" +
                              "  switch (1) {\n" +
                              "    case 1:\n" +
                              "      break;<caret>\n" +
                              "  }\n" +
                              "}";
    final String textAfter = "void bar() {\n" +
                             "  switch (1) {\n" +
                             "    case 1:\n" +
                             "      break;\n" +
                             "      <caret>\n" +
                             "  }\n" +
                             "}";

    doTypingTest('\n', textBefore, textAfter);
    doTypingTest('\n', StringUtil.replace(textBefore, "break;", "continue;"), StringUtil.replace(textAfter, "break;", "continue;"));
    doTypingTest('\n', StringUtil.replace(textBefore, "break;", "return 1+1;"), StringUtil.replace(textAfter, "break;", "return 1+1;"));
    doTypingTest('\n', StringUtil.replace(textBefore, "break;", "throw '';"), StringUtil.replace(textAfter, "break;", "throw '';"));
    doTypingTest('\n', StringUtil.replace(textBefore, "break;", "foo;"),
                 StringUtil.replace(textAfter, "break;\n      <caret>", "foo;\n      <caret>"));
  }

  public void testEnterInMapLiteral() {
    doTypingTest('\n', "var data = {<caret>};", "var data = {\n" +
                                                "  <caret>\n" +
                                                "};");
    doTypingTest('\n',
                 "var data = {\n" +
                 "  1:1,<caret>\n" +
                 "};",
                 "var data = {\n" +
                 "  1:1,\n" +
                 "  <caret>\n" +
                 "};");
  }

  public void testEnterInListLiteral() {
    doTypingTest('\n',
                 "var data = [<caret>\n" +
                 "];",
                 "var data = [\n" +
                 "  <caret>\n" +
                 "];");
    doTypingTest('\n',
                 "var data = [\n" +
                 "  1,<caret>\n" +
                 "];",
                 "var data = [\n" +
                 "  1,\n" +
                 "  <caret>\n" +
                 "];");
  }

  public void testLt() {
    doTypingTest('<', "Map<List<caret>>", "Map<List<<caret>>>");
    doTypingTest('<', "class A<caret>", "class A<<caret>>");
  }

  public void testGt() {
    doTypingTest('>', "foo () {Map<List<<caret>>}", "foo () {Map<List<><caret>>}");
    doTypingTest('>', "Map<List<<caret>>>", "Map<List<><caret>>");
    doTypingTest('>', "Map<List<><caret>>", "Map<List<>><caret>");
    doTypingTest('>', "Map<List<><caret>", "Map<List<>><caret>");
    doTypingTest('>', "Map<List<>><caret>", "Map<List<>>><caret>");
    doTypingTest('>', "Map<List<A>, B <caret>", "Map<List<A>, B ><caret>");
    doTypingTest('>', "class A<T, E <caret>", "class A<T, E ><caret>");
    doTypingTest('>', "class A<T, E <caret>>", "class A<T, E ><caret>");
  }

  public void testLBraceInString() {
    doTypingTest('{', "var a = 'xx$<caret>xx'", "var a = 'xx${<caret>}xx'");
    doTypingTest('{', "foo () {var a = 'xx$<caret>xx';\n}", "foo () {var a = 'xx${<caret>}xx';\n}");
    doTypingTest('{', "var a = \"$<caret>\";", "var a = \"${<caret>}\";");
    doTypingTest('{', "var a = r'$<caret>'", "var a = r'${<caret>'");
    doTypingTest('{', "var a = '''$<caret>'''", "var a = '''${<caret>}'''");
    doTypingTest('{', "var a = '${}<caret>'", "var a = '${}{<caret>'");
    doTypingTest('{', "<caret>", "{<caret>}");
    doTypingTest('{', "foo(){if(a) <caret> return; }", "foo(){if(a) {<caret> return; }");
    doTypingTest('{', "foo()<caret> => 1;", "foo(){<caret> => 1;");
    doTypingTest('\n', "foo(){<caret> => 1;", "foo(){\n  <caret>=> 1;\n}");
  }

  public void testRBraceInString() {
    doTypingTest('}', "var a = 'xx${<caret>}xx'", "var a = 'xx${}<caret>xx'");
    doTypingTest('}', "var a = 'xx${<caret>xx'", "var a = 'xx${}<caret>xx'");
    doTypingTest('}', "var a = \"${1 + 2 <caret>}\"", "var a = \"${1 + 2 }<caret>\"");
    doTypingTest('}', "var a = r'${<caret>}'", "var a = r'${}<caret>}'");
    doTypingTest('}', "var a = '''${<caret>}'''", "var a = '''${}<caret>'''");
    doTypingTest('}', "var a = '${{<caret>}}'", "var a = '${{}<caret>}'");
    doTypingTest('}', "var a = '${{a<caret>}'", "var a = '${{a}<caret>'");
    doTypingTest('}', "var a = '${{1+1;}<caret>}'", "var a = '${{1+1;}}<caret>'");
    doTypingTest('}', "var a = '${{}<caret>'", "var a = '${{}}<caret>'");
    doTypingTest('}', "var a = '${{}}<caret>}'", "var a = '${{}}}<caret>}'");
  }

  public void testEnterInSingleLineDocComment() {
    doTypingTest('\n', "///<caret>", "///\n/// <caret>");
    doTypingTest('\n', "///<caret>\nvar a;", "///\n/// <caret>\nvar a;");
    doTypingTest('\n', "///\n<caret>\nvar a;", "///\n\n<caret>\nvar a;");
    doTypingTest('\n', "///     q<caret>", "///     q\n///     <caret>");
    doTypingTest('\n', "///     <caret>q", "///     \n///     <caret>q");
    doTypingTest('\n', "/// Hello<caret>Dart", "/// Hello\n/// <caret>Dart");
    doTypingTest('\n', "///   q  <caret>", "///   q  \n///   <caret>");
    doTypingTest('\n', "///   q  <caret>z", "///   q  \n///   <caret>z");
    doTypingTest('\n', "///   q  <caret>  z", "///   q  \n///   <caret>z");
    doTypingTest('\n', "///   q<caret>   z", "///   q\n///   <caret>z");
    doTypingTest('\n', "  ///   q  <caret>    z", "  ///   q  \n  ///   <caret> z");
    doTypingTest('\n', "///q<caret>z", "///q\n///<caret>z");
    doTypingTest('\n', " ///q<caret> \t ///z", " ///q \t \n ///<caret>z");

    doTypingTest(HtmlFileType.INSTANCE, '\n',
                 "<script type=\"application/dart\">\n" +
                 "///   q<caret>   z\n" +
                 "</script>",
                 "<script type=\"application/dart\">\n" +
                 "///   q\n///   <caret>z\n" +
                 "</script>");
  }

  public void testEnterAfterSingleLineComment() {
    doTypingTest('\n',
                 "Future main() async {\n" +
                 "  Directory systemTempDir = Directory.systemTemp;\n" +
                 "  // comment\n" +
                 "  File file = await new File('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa')\n" +
                 "      .create(recursive: true);<caret>\n" +
                 "  print(file.path);\n" +
                 "}\n",
                 "Future main() async {\n" +
                 "  Directory systemTempDir = Directory.systemTemp;\n" +
                 "  // comment\n" +
                 "  File file = await new File('aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa')\n" +
                 "      .create(recursive: true);\n" +
                 "  <caret>\n" +
                 "  print(file.path);\n" +
                 "}\n");
  }

  public void testEnterAfterIncompleteStatement() {
    doTypingTest('\n',
                 "class T {\n" +
                 "  void r() {\n" +
                 "    int criticalPathAB = overall.inMilliseconds - slowestRequest.inMilliseconds<caret>\n" +
                 "  }\n" +
                 "}",
                 "class T {\n" +
                 "  void r() {\n" +
                 "    int criticalPathAB = overall.inMilliseconds - slowestRequest.inMilliseconds\n" +
                 "        <caret>\n" +
                 "  }\n" +
                 "}");
  }

  public void testEnterBeforeIncompleteStatement() {
    doTypingTest('\n',
                 "void _advanceParagraph(_currentParagraph, _currentIndentation, _currentListId) {\n" +
                 "  _currentIndentation = null;<caret>\n" +
                 "  _currentListId = _currentParagraph is bool ?\n" +
                 "}",
                 "void _advanceParagraph(_currentParagraph, _currentIndentation, _currentListId) {\n" +
                 "  _currentIndentation = null;\n" +
                 "  <caret>\n" +
                 "  _currentListId = _currentParagraph is bool ?\n" +
                 "}");
  }

  public void testEnterAfterCompleteStatement() {
    doTypingTest('\n',
                 "class T {\n" +
                 "  void r() {\n" +
                 "    int criticalPathAB = overall.inMilliseconds - slowestRequest.inMilliseconds;<caret>\n" +
                 "  }\n" +
                 "}",
                 "class T {\n" +
                 "  void r() {\n" +
                 "    int criticalPathAB = overall.inMilliseconds - slowestRequest.inMilliseconds;\n" +
                 "    <caret>\n" +
                 "  }\n" +
                 "}");
  }

  public void testEnterAfterBlankLine() {
    doTypingTest('\n',
                 "class T {\n" +
                 "  void r() {\n" +
                 "    int criticalPathAB = overall.inMilliseconds - slowestRequest.inMilliseconds;\n" +
                 "    <caret>\n" +
                 "  }\n" +
                 "}",
                 "class T {\n" +
                 "  void r() {\n" +
                 "    int criticalPathAB = overall.inMilliseconds - slowestRequest.inMilliseconds;\n" +
                 "    \n" +
                 "    <caret>\n" +
                 "  }\n" +
                 "}");
  }

  public void testEnterAfterCompleteStatementInIncompleteBlock() {
    doTypingTest('\n',
                 "class T {\n" +
                 "  void r() {\n" +
                 "    int criticalPathAB = overall.inMilliseconds - slowestRequest.inMilliseconds;<caret>\n" +
                 "  }\n" +
                 "",
                 "class T {\n" +
                 "  void r() {\n" +
                 "    int criticalPathAB = overall.inMilliseconds - slowestRequest.inMilliseconds;\n" +
                 "    <caret>\n" +
                 "  }\n" +
                 "");
  }

  public void testAutoWrapString() {
    CommonCodeStyleSettings settings = CodeStyle.getSettings(getProject()).getCommonSettings(DartLanguage.INSTANCE);
    settings.WRAP_ON_TYPING = CommonCodeStyleSettings.WrapOnTyping.WRAP.intValue;
    settings.RIGHT_MARGIN = 42;
    doTypingTest('3',
                 "var x = '12345678901234567890123456789012<caret>'",
                 "var x = '123456789012345678901234567890'\n" +
                 "    '123<caret>'");
  }

  public void testAutoWrapCascade() {
    CommonCodeStyleSettings settings = CodeStyle.getSettings(getProject()).getCommonSettings(DartLanguage.INSTANCE);
    settings.WRAP_ON_TYPING = CommonCodeStyleSettings.WrapOnTyping.WRAP.intValue;
    settings.RIGHT_MARGIN = 42;
    doTypingTest('3',
                 "var x = a123456789012345..b890123456789012<caret>",
                 "var x = a123456789012345\n" +
                 "  ..b8901234567890123");
  }

  public void testAutoWrapStringEscape() {
    CommonCodeStyleSettings settings = CodeStyle.getSettings(getProject()).getCommonSettings(DartLanguage.INSTANCE);
    settings.WRAP_ON_TYPING = CommonCodeStyleSettings.WrapOnTyping.WRAP.intValue;
    settings.RIGHT_MARGIN = 42;
    doTypingTest('3',
                 "var x = '123456789012345\\t890123456789012<caret>'",
                 "var x = '123456789012345'\n" +
                 "    '\\t8901234567890123'");
  }

  public void testEnterBetweenInterpolationsHtml() {
    doTypingTest(HtmlFileType.INSTANCE, '\n',
                 "<script type=\"application/dart\">\n" +
                 "var a = '$x and <caret> also $y';\n" +
                 "</script>",
                 "<script type=\"application/dart\">\n" +
                 "var a = '$x and '\n" +
                 // 8 spaces continuation indent is taken from HTML language instead of Dart's 4 spaces. Fix expected result when it is fixed in Platform
                 "        '<caret> also $y';\n" +
                 "</script>");
  }

  public void testEnterAfterEqualsVar() {
    doTypingTest('\n',
                 "o =<caret>",
                 "o =\n" +
                 "    <caret>");
  }

  public void testEnterAfterEqualsProperty() {
    doTypingTest('\n',
                 "o.x =<caret>",
                 "o.x =\n" +
                 "    <caret>");
  }

  public void testEnterBeforeStringParam() {
    doTypingTest('\n',
                 "foo() {return comment.replaceAll(<caret>'foo', 'bar');}",
                 "foo() {return comment.replaceAll(\n" +
                 "    <caret>'foo', 'bar');}");
  }

  public void testEnterInEmptyMetadataArgList() {
    doTypingTest('\n',
                 "@Component(<caret>)",
                 "@Component(\n" +
                 "  <caret>\n" +
                 ")");
  }

  public void testEnterBeforeMetadataNamedArg() {
    doTypingTest('\n',
                 "@Component(<caret>selector: 'something')",
                 "@Component(\n" +
                 "    <caret>selector: 'something')");
  }

  public void testEnterAfterEQ() {
    doTypingTest('\n',
                 "ms(toto)\n" +
                 "  bool x =<caret> toto;\n" +
                 "  return;\n" +
                 "}",
                 "ms(toto)\n" +
                 "  bool x =\n" +
                 "      <caret>toto;\n" +
                 "  return;\n" +
                 "}");
  }

  public void testEnterAfterLastArg() {
    doTypingTest('\n',
                 "m(l) {\n" +
                 "  List p = new List(l,<caret>);\n" +
                 "}",
                 "m(l) {\n" +
                 "  List p = new List(l,\n" +
                 "  <caret>);\n" + // not sure it's the best possible behavior
                 "}");
  }

  public void testEnterInEmptyArg() {
    doTypingTest('\n',
                 "m(l) {\n" +
                 "  List p = new List(<caret>);\n" +
                 "}",
                 "m(l) {\n" +
                 "  List p = new List(\n" +
                 "    <caret>\n" +
                 "  );\n" +
                 "}");
  }

  public void testEnterInMultiLineArg() {
    doTypingTest('\n',
                 "m(l) {\n" +
                 "  return new X(<caret>1,\n" +
                 "    2,\n" +
                 "  );\n" +
                 "}",
                 "m(l) {\n" +
                 "  return new X(\n" +
                 "    <caret>1,\n" +
                 "    2,\n" +
                 "  );\n" +
                 "}");
    doTypingTest('\n',
                 "m(l) {\n" +
                 "  return new X(\n" +
                 "    1,\n" +
                 "    2,<caret>\n" +
                 "  );\n" +
                 "}",
                 "m(l) {\n" +
                 "  return new X(\n" +
                 "    1,\n" +
                 "    2,\n" +
                 "    <caret>\n" +
                 "  );\n" +
                 "}");
    doTypingTest('\n',
                 "m(l) {\n" +
                 "  return new X(\n" +
                 "    1,\n" +
                 "    2,<caret>);\n" +
                 "}",
                 "m(l) {\n" +
                 "  return new X(\n" +
                 "    1,\n" +
                 "    2,\n" +
                 "  <caret>);\n" +
                 "}");
    doTypingTest('\n',
                 "main() {\n" +
                 "  new X(\n" +
                 "    1,<caret>\n" +
                 "    2,\n" +
                 "  );\n" +
                 "}",
                 "main() {\n" +
                 "  new X(\n" +
                 "    1,\n" +
                 "    <caret>\n" +
                 "    2,\n" +
                 "  );\n" +
                 "}");
    doTypingTest('\n',
                 "main() {\n" +
                 "  new X(\n" +
                 "      1,<caret>\n" +
                 "      2\n" +
                 "  );\n" +
                 "}",
                 "main() {\n" +
                 "  new X(\n" +
                 "      1,\n" +
                 "      <caret>\n" +
                 "      2\n" +
                 "  );\n" +
                 "}");
  }

  public void testEnterInMultilineString() {
    doTypingTest('\n', "var a = <caret>r''' ''';", "var a = \n<caret>r''' ''';"); // indent should be here, need to fix formatter
    doTypingTest('\n', "var a = r<caret>''' ''';", "var a = r\n<caret>''' ''';"); // indent should be here, need to fix formatter
    doTypingTest('\n', "var a = r'<caret>'' ''';", "var a = r'\n    <caret>'' ''';");
    doTypingTest('\n', "var a = r''<caret>' ''';", "var a = r''\n    <caret>' ''';");
    doTypingTest('\n', "var a = r'''<caret>''';", "var a = r'''\n<caret>''';");
    doTypingTest('\n', "var a = '''<caret>''';", "var a = '''\n<caret>''';");
    doTypingTest('\n', "var a = \"\" r\"\"\"x<caret>\"\"\";", "var a = \"\" r\"\"\"x\n<caret>\"\"\";");
    doTypingTest('\n', "var a = '' \"\"\"<caret>y\"\"\";", "var a = '' \"\"\"\n<caret>y\"\"\";");
  }

  public void testEnterInRawString() {
    doTypingTest('\n', "var a = '''x''' <caret>r'a';", "var a = '''x''' \n    r'a';");
    doTypingTest('\n', "var a = \"\" r<caret>'a'\"\";", "var a = \"\" r\n<caret>'a'\"\";");
    doTypingTest('\n', "var a = r\"<caret>\";", "var a = r\"\"\n    r\"<caret>\";");
    doTypingTest('\n', "var a = r'<caret>$a", "var a = r''\n    r'<caret>$a");
    doTypingTest('\n', "var a = r\"$<caret>a", "var a = r\"$\"\n    r\"<caret>a");
    doTypingTest('\n', "var a = r''<caret>r'';", "var a = r''\n    <caret>r'';");
  }

  public void testEnterInString() {
    doTypingTest('\n', "var a = ''<caret>\"\";", "var a = ''\n    <caret>\"\";");
    doTypingTest('\n', "var a = \"\"'<caret>'\"\";", "var a = \"\"''\n    '<caret>'\"\";");
    doTypingTest('\n', "var a = \"<caret>$a\"", "var a = \"\"\n    \"<caret>$a\"");
    doTypingTest('\n', "var a = '$<caret>a", "var a = '$\n<caret>a");
    doTypingTest('\n', "var a = '$a<caret>';", "var a = '$a'\n    '<caret>';");
    doTypingTest('\n', "var a = '$x a<caret>b$x${y}';", "var a = '$x a'\n    '<caret>b$x${y}';");
    doTypingTest('\n', "var a = \"<caret>${ab}\";", "var a = \"\"\n    \"<caret>${ab}\";");
    doTypingTest('\n', "var a = '$<caret>{ab}';", "var a = '$\n<caret>{ab}';");
    doTypingTest('\n', "var a = '${<caret>ab}';", "var a = '${\n    <caret>ab}';");
    doTypingTest('\n', "var a = '${a<caret>b}';", "var a = '${a\n<caret>b}';");
    doTypingTest('\n', "var a = '${ab<caret>}';", "var a = '${ab\n<caret>}';");
    doTypingTest('\n', "var a = '${ab}<caret>';", "var a = '${ab}'\n    '<caret>';");
    doTypingTest('\n', "var a = '$a<caret>$b';", "var a = '$a'\n    '<caret>$b';");
    doTypingTest('\n', "var a = '$a i <caret> i $b';", "var a = '$a i '\n    '<caret> i $b';");
    doTypingTest('\n', "var a = '${a}<caret>${b}';", "var a = '${a}'\n    '<caret>${b}';");
  }

  public void testBracket() {
    doTypingTest('[', "var a = new RegExp(<caret>)", "var a = new RegExp([<caret>])");
    doTypingTest('[', "var a = new RegExp(r'<caret>)", "var a = new RegExp(r'[<caret>)");
    doTypingTest('[', "var a = ", "var a = [<caret>]");
    doTypingTest('\n', "var a = [<caret>]", "var a = [\n  <caret>\n]");
    doTypingTest('\n', "g() {\n  var a = [<caret>]", "g() {\n  var a = [\n    <caret>\n  ]");
  }

  public void testTrailingSpaces() {
    myFixture.configureByText("foo.dart",
                              "var a = r'''   \n" +
                              "trailing spaces     \n" +
                              "''';   \n" +
                              "var b = \"\"\"   \n" +
                              "  ${''' \n" +
                              "     '''  \n" +
                              "}\"\"\";  \n");
    WriteAction.run(() -> TrailingSpacesStripper.strip(myFixture.getDocument(myFixture.getFile()), false, false));
    myFixture.checkResult("var a = r'''   \n" +
                          "trailing spaces     \n" +
                          "''';\n" +
                          "var b = \"\"\"   \n" +
                          "  ${''' \n" +
                          "     '''\n" +
                          "}\"\"\";\n", false);
  }

  public void testLineCommentIndent() {
    myFixture.configureByText("foo.dart", "main() {\n  foo();<caret>\n}");
    myFixture.performEditorAction(IdeActions.ACTION_COMMENT_LINE);
    myFixture.checkResult("main() {\n  // foo();\n}");
  }
}
