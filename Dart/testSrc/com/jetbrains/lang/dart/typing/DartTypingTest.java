package com.jetbrains.lang.dart.typing;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
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

  protected void doTest(char charToType) throws Throwable {
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

  public void testDocComment() throws Throwable {
    doTest('\n');
  }

  public void testDocComment2() throws Throwable {
    doTest('\n');
  }

  public void testDocComment3() throws Throwable {
    doTest('\n');
  }

  public void testGenericBraceWithMultiCaret() throws Throwable {
    doTest('<');
  }

  public void testGenericBraceWithMultiCaretInDifferentContexts() throws Throwable {
    doTest('<');
  }

  public void testGenericBrace1() throws Throwable {
    doTest('<');
  }

  public void testGenericBrace2() throws Throwable {
    doTest('<');
  }

  public void testGenericBrace3() throws Throwable {
    doTest('<');
  }

  public void testLess() throws Throwable {
    doTest('<');
  }

  public void testStringWithMultiCaret() throws Throwable {
    doTest('{');
  }

  public void testStringWithMultiCaretInDifferentContexts() throws Throwable {
    doTest('{');
  }

  public void testString1() throws Throwable {
    doTest('{');
  }

  public void testString2() throws Throwable {
    doTest('{');
  }

  public void testString3() throws Throwable {
    doTest('{');
  }

  public void testQuote() throws Throwable {
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
  }

  public void testBackspace() throws Throwable {
    doBackspaceTest("var foo = \"<caret> \"", "var foo = <caret> \"");
    doBackspaceTest("var foo = \"<caret>\"", "var foo = <caret>");
    doBackspaceTest("var foo = '<caret>a'", "var foo = <caret>a'");
    doBackspaceTest("import '<caret>'", "import <caret>");
    doBackspaceTest("var foo = \"\"<caret>", "var foo = \"<caret>");
    doBackspaceTest("var foo = \" '<caret>' \"", "var foo = \" <caret>' \"");
    doBackspaceTest("var foo = '\"<caret>\"'", "var foo = '<caret>\"'");
  }

  public void testWEB_8315() throws Throwable {
    doTypingTest('\n',
                 "class X {\n" +
                 "  num x;<caret>\n" +
                 "}",
                 "class X {\n" +
                 "  num x;\n" +
                 "  <caret>\n" +
                 "}");
  }

  public void testCaseAlignAfterColon1() throws Throwable {
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

  public void testCaseAlignAfterColon2() throws Throwable {
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

  public void testDefaultAlignAfterColon() throws Throwable {
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

  public void testCaseStringAlignAfterColon() throws Throwable {
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

  public void testEnterInSwitch() throws Throwable {
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

  public void testEnterAfterCase() throws Throwable {
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

  public void testEnterAfterDefault() throws Throwable {
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

  public void testEnterAfterBreakInCase() throws Throwable {
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
                             "    <caret>\n" +
                             "  }\n" +
                             "}";

    doTypingTest('\n', textBefore, textAfter);
    doTypingTest('\n', StringUtil.replace(textBefore, "break;", "continue;"), StringUtil.replace(textAfter, "break;", "continue;"));
    doTypingTest('\n', StringUtil.replace(textBefore, "break;", "return 1+1;"), StringUtil.replace(textAfter, "break;", "return 1+1;"));
    doTypingTest('\n', StringUtil.replace(textBefore, "break;", "throw '';"), StringUtil.replace(textAfter, "break;", "throw '';"));
    doTypingTest('\n', StringUtil.replace(textBefore, "break;", "foo;"),
                 StringUtil.replace(textAfter, "break;\n    <caret>", "foo;\n      <caret>"));
  }

  public void testEnterInMapLiteral() throws Throwable {
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

  public void testEnterInListLiteral() throws Throwable {
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

  public void testEnterBetweenEmptyStringQuotes() {
    // Checks single quotes.
    doTypingTest('\n',
                 "var x = '<caret>'",
                 "var x = ''\n" +
                 "    '<caret>'");
  }

  public void testEnterBetweenStringQuotes() {
    // Checks single quotes.
    doTypingTest('\n',
                 "var x = 'content<caret>'",
                 "var x = 'content'\n" +
                 "    '<caret>'");
  }

  public void testEnterMidString() {
    doTypingTest('\n',
                 "var x = 'content<caret>and stuff'",
                 "var x = 'content'\n" +
                 "    '<caret>and stuff'");
  }

  public void testAutoWrapString() {
    CommonCodeStyleSettings settings = CodeStyleSettingsManager.getSettings(getProject()).getCommonSettings(DartLanguage.INSTANCE);
    settings.WRAP_ON_TYPING = CommonCodeStyleSettings.WrapOnTyping.WRAP.intValue;
    settings.RIGHT_MARGIN = 42;
    doTypingTest('3',
                 "var x = '12345678901234567890123456789012<caret>'",
                 "var x = '123456789012345678901234567890'\n" +
                 "    '123<caret>'");
  }

  public void testAutoWrapCascade() {
    CommonCodeStyleSettings settings = CodeStyleSettingsManager.getSettings(getProject()).getCommonSettings(DartLanguage.INSTANCE);
    settings.WRAP_ON_TYPING = CommonCodeStyleSettings.WrapOnTyping.WRAP.intValue;
    settings.RIGHT_MARGIN = 42;
    doTypingTest('3',
                 "var x = a123456789012345..b890123456789012<caret>",
                 "var x = a123456789012345\n" +
                 "  ..b8901234567890123");
  }

  public void testAutoWrapStringEscape() {
    CommonCodeStyleSettings settings = CodeStyleSettingsManager.getSettings(getProject()).getCommonSettings(DartLanguage.INSTANCE);
    settings.WRAP_ON_TYPING = CommonCodeStyleSettings.WrapOnTyping.WRAP.intValue;
    settings.RIGHT_MARGIN = 42;
    doTypingTest('3',
                 "var x = '123456789012345\\t890123456789012<caret>'",
                 "var x = '123456789012345'\n" +
                 "    '\\t8901234567890123'");
  }

  public void testAutoIndentMultilineString() {
    doTypingTest('\n',
                 "var q = '''<caret>",
                 "var q = '''\n" +
                 "<caret>");
  }

  public void testEnterMidRawString() {
    doTypingTest('\n',
                 "var x = r'content<caret>and stuff'",
                 "var x = r'content'\n" +
                 "    r'<caret>and stuff'");
  }

  public void testEnterMidInterpolatedString() {
    doTypingTest('\n',
                 "var x = 'content<caret>and $some stuff'",
                 "var x = 'content'\n" +
                 "    '<caret>and $some stuff'");
  }

  public void testEnterBetweenInterpolations() {
    doTypingTest('\n',
                 "var a = '$x and <caret> also $y';",
                 "var a = '$x and '\n" +
                 "    '<caret> also $y';");
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

  public void testEnterBeforeInterpolation() {
    doTypingTest('\n',
                 "var a = \"see <caret>$y\";",
                 "var a = \"see \"\n" +
                 "    \"<caret>$y\";");
  }

  public void testEnterBeforeInterpolationSequence() {
    doTypingTest('\n',
                 "var a = \"see <caret>$y${z}\";",
                 "var a = \"see \"\n" +
                 "    \"<caret>$y${z}\";");
  }

  public void testEnterBeforeInterpolatedExpr() {
    doTypingTest('\n',
                 "var a = 'see <caret> also ${y}';",
                 "var a = 'see '\n" +
                 "    '<caret> also ${y}';");
  }

  public void testEnterInMultilineString() {
    doTypingTest('\n',
                 "var a = '''some<caret>content''';",
                 "var a = '''some\n" +
                 "<caret>content''';");
  }

  public void testEnterInRawMultilineString() {
    doTypingTest('\n',
                 "var a = r'''some<caret>content''';",
                 "var a = r'''some\n" +
                 "<caret>content''';");
  }

  public void testEnterBeforeEmbeddedInterpolation() {
    // We are not going to address the whole issue of
    // "how do I format an entire functional program written in an interpolation expression"
    doTypingTest('\n',
                 "var b = \"see ${'${<caret>a}'} and more\";",
                 "var b = \"see ${'${\n" +
                 "    a}'} and more\";");
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
                 "    <caret>\n" +
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
                 "      <caret>);\n" +
                 "}");
  }

  public void testEnterInEmptyArg() {
    doTypingTest('\n',
                 "m(l) {\n" +
                 "  List p = new List(<caret>);\n" +
                 "}",
                 "m(l) {\n" +
                 "  List p = new List(\n" +
                 "      <caret>\n" +
                 "  );\n" +
                 "}");
  }

  public void testRawStringAfterR() {
    doTypingTest('\n',
                 "var a = r<caret>'a';",
                 "var a = r\n" +
                 "'a';");
  }
}
