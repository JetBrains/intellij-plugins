package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import junit.framework.TestCase;
import org.jetbrains.plugins.cucumber.psi.GherkinLexer;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;
import org.jetbrains.plugins.cucumber.psi.PlainGherkinKeywordProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author yole
 */
public class GherkinLexerTest extends TestCase {
  public void testComment() throws Exception {
    doTest("# foo", "COMMENT:0-5");
  }

  public void testWhitespaceComment() throws Exception {
    doTest("   # foo", "WHITE_SPACE:0-3", "COMMENT:3-8");
  }

  public void testKeyword() throws Exception {
    doTest("Feature", "FEATURE_KEYWORD:0-7");
  }

  public void testTextAfterKeyword() throws Exception {
    doTest("Feature: Feature", "FEATURE_KEYWORD:0-7", "COLON:7-8", "WHITE_SPACE:8-9", "TEXT:9-16");
  }

  public void testNoSpaceAfterKeyword() throws Exception {
    doTest("Featureee", "TEXT:0-9");
  }

  public void testSpaceAfterKeyword() throws Exception {
    doTest("Given foo", "STEP_KEYWORD:0-5", "WHITE_SPACE:5-6", "TEXT:6-9");
  }

  public void testSpaceAtEndOfLine() throws Exception {
    doTest("Given foo   \n", "STEP_KEYWORD:0-5", "WHITE_SPACE:5-6", "TEXT:6-9", "WHITE_SPACE:9-13");
  }

  public void testTag() throws Exception {
    doTest("@foo", "TAG:0-4");
  }

  public void testPyString() throws Exception {
    doTest("\"\"\"\n bar \"\"\"", "PYSTRING_QUOTES:0-3", "PYSTRING_TEXT:3-9", "PYSTRING_QUOTES:9-12");
  }

  public void testTable() throws Exception {
    doTest("|a|", "PIPE:0-1", "TABLE_CELL:1-2", "PIPE:2-3");
  }

  public void testTable_EscapedBackslash() throws Exception {
    doTest("|a\\|a|", "PIPE:0-1", "TABLE_CELL:1-5", "PIPE:5-6");
  }

  public void testTable_EscapedBar() throws Exception {
    doTest("|a\\\\|", "PIPE:0-1", "TABLE_CELL:1-4", "PIPE:4-5");
  }

  public void testTableWhitespace() throws Exception {
    doTest("|a  |", "PIPE:0-1", "TABLE_CELL:1-2", "WHITE_SPACE:2-4", "PIPE:4-5");
  }

  public void testLongKeywordsFirst() throws Exception {
    doTest("Scenario Outline:", "SCENARIO_OUTLINE_KEYWORD:0-16", "COLON:16-17");
  }

  public void testLanguage() throws Exception {
    doTest("# language: en-lol\nOH HAI: STUFFING", "COMMENT:0-18", "WHITE_SPACE:18-19", "FEATURE_KEYWORD:19-25", "COLON:25-26", "WHITE_SPACE:26-27", "TEXT:27-35");
  }

  public void testKeywordAtEndOfLine() throws Exception {
    doTest("Feature:\n  Background:\n    When foo",
           "FEATURE_KEYWORD:0-7", "COLON:7-8", "WHITE_SPACE:8-11", "BACKGROUND_KEYWORD:11-21", "COLON:21-22", "WHITE_SPACE:22-27", "STEP_KEYWORD:27-31", "WHITE_SPACE:31-32", "TEXT:32-35");
  }

  public void testAsteriskKeyword() throws Exception {
    doTest("*", "STEP_KEYWORD:0-1");
  }

  public void testNewStyleSpaceAfterKeyword() throws Exception {
    doTest("Lorsqu'foo", "STEP_KEYWORD:0-7", "TEXT:7-10");
  }

  public void testStepParameter() throws Exception {
    doTest("Given test step <param01>", "STEP_KEYWORD:0-5", "WHITE_SPACE:5-6", "TEXT:6-15", "WHITE_SPACE:15-16", "STEP_PARAMETER_BRACE:16-17", "STEP_PARAMETER_TEXT:17-24", "STEP_PARAMETER_BRACE:24-25");
  }

  public void testStepParameterList() throws Exception {
    doTest("Given test step <param01> word <param02>", "STEP_KEYWORD:0-5", "WHITE_SPACE:5-6", "TEXT:6-15", "WHITE_SPACE:15-16", "STEP_PARAMETER_BRACE:16-17", "STEP_PARAMETER_TEXT:17-24", "STEP_PARAMETER_BRACE:24-25", "WHITE_SPACE:25-26", "TEXT:26-30", "WHITE_SPACE:30-31", "STEP_PARAMETER_BRACE:31-32", "STEP_PARAMETER_TEXT:32-39", "STEP_PARAMETER_BRACE:39-40");
  }

  public void testStepParameterBeforeText() throws Exception {
    doTest("Given <param01> test <param02>", "STEP_KEYWORD:0-5", "WHITE_SPACE:5-6", "STEP_PARAMETER_BRACE:6-7", "STEP_PARAMETER_TEXT:7-14", "STEP_PARAMETER_BRACE:14-15", "WHITE_SPACE:15-16", "TEXT:16-20", "WHITE_SPACE:20-21", "STEP_PARAMETER_BRACE:21-22", "STEP_PARAMETER_TEXT:22-29", "STEP_PARAMETER_BRACE:29-30");
  }

  public void testStepParameters() {
    doTest("Given test step   <par01>  <par01> test", "STEP_KEYWORD:0-5", "WHITE_SPACE:5-6", "TEXT:6-15", "WHITE_SPACE:15-18", "STEP_PARAMETER_BRACE:18-19", "STEP_PARAMETER_TEXT:19-24", "STEP_PARAMETER_BRACE:24-25", "WHITE_SPACE:25-27", "STEP_PARAMETER_BRACE:27-28", "STEP_PARAMETER_TEXT:28-33", "STEP_PARAMETER_BRACE:33-34", "WHITE_SPACE:34-35", "TEXT:35-39");
  }

  public void testPyStringParameters() {
    doTest("\"\"\"text <param> text\"\"\"", "PYSTRING_QUOTES:0-3", "PYSTRING_TEXT:3-8", "STEP_PARAMETER_BRACE:8-9", "STEP_PARAMETER_TEXT:9-14", "STEP_PARAMETER_BRACE:14-15", "PYSTRING_TEXT:15-20", "PYSTRING_QUOTES:20-23");
  }

  public void testPyStringParametersAndExample() {
    doTest("\"\"\"text\"\"\" Examples", "PYSTRING_QUOTES:0-3", "PYSTRING_TEXT:3-7", "PYSTRING_QUOTES:7-10", "WHITE_SPACE:10-11", "EXAMPLES_KEYWORD:11-19");
  }

  public void testPyStringWithoutParameters() {
    doTest("\"\"\"\n" +
           "<\n" +
           "\"\"\"\n" +
           "And step", "PYSTRING_QUOTES:0-3", "PYSTRING_TEXT:3-6", "PYSTRING_QUOTES:6-9", "WHITE_SPACE:9-10", "STEP_KEYWORD:10-13", "WHITE_SPACE:13-14", "TEXT:14-18");
  }

  public void testPyStringWithParameter() {
    doTest("\"\"\"<>|\"\"\"", "PYSTRING_QUOTES:0-3", "STEP_PARAMETER_BRACE:3-4", "STEP_PARAMETER_BRACE:4-5", "PYSTRING_TEXT:5-6", "PYSTRING_QUOTES:6-9");
  }

  public void testScenarioWithParameter() {
    doTest(
      "Feature: test\n" +
      "  Scenario Outline: Opening <scan> Scan Settings",
      "FEATURE_KEYWORD:0-7", "COLON:7-8", "WHITE_SPACE:8-9", "TEXT:9-13", "WHITE_SPACE:13-16", "SCENARIO_OUTLINE_KEYWORD:16-32",
      "COLON:32-33", "WHITE_SPACE:33-34", "TEXT:34-41", "WHITE_SPACE:41-42", "STEP_PARAMETER_BRACE:42-43", "STEP_PARAMETER_TEXT:43-47",
      "STEP_PARAMETER_BRACE:47-48", "WHITE_SPACE:48-49", "TEXT:49-62"
    );
  }

  public void testUnicodeWhitespace() {
    doTest("|\u3000\n", "PIPE:0-1", "WHITE_SPACE:1-3");
  }

  private static void doTest(String text, String... expectedTokens) {
    Lexer lexer = new GherkinLexer(new MockGherkinKeywordProvider());
    lexer.start(text);
    int idx = 0;
    int tokenPos = 0;
    while (lexer.getTokenType() != null) {
      if (idx > expectedTokens.length) fail("Too many tokens");
      assertEquals("Token offset mismatch at position " + idx, tokenPos, lexer.getTokenStart());
      String tokenName = lexer.getTokenType().toString() + ":" + lexer.getTokenStart() + "-" + lexer.getTokenEnd();
      assertEquals("Token mismatch at position " + idx, expectedTokens[idx], tokenName);
      idx++;
      tokenPos = lexer.getTokenEnd();
      lexer.advance();
    }
    if (idx < expectedTokens.length) fail("Not enough tokens");
  }

  private static class MockGherkinKeywordProvider extends PlainGherkinKeywordProvider {
    private List<String> myLolcatKeywords = Arrays.asList("OH HAI", "I CAN HAZ", "MISHUN", "MISHUN SRSLY");

    private MockGherkinKeywordProvider() {
      super();
      //TODO add custom langs
    }

    public Collection<String> getAllKeywords(String language) {
      return language.equals("en-lol") ? myLolcatKeywords : super.getAllKeywords(language);
    }

    @Override
    public IElementType getTokenType(String language, String keyword) {
      return language.equals("en-lol") ? GherkinTokenTypes.FEATURE_KEYWORD : super.getTokenType(language, keyword);
    }

    public String getBaseKeyword(String language, String keyword) {
      return language.equals("en-lol") ? "Feature" : super.getBaseKeyword(language, keyword);
    }
  }
}
