package com.jetbrains.lang.dart.dart_style;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterTestCase;
import com.intellij.util.ArrayUtil;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.util.DartTestUtils;
import junit.framework.ComparisonFailure;

import java.io.File;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DartStyleTest extends FormatterTestCase {

  protected String getFileExtension() {
    return DartFileType.DEFAULT_EXTENSION;
  }

  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH;
  }

  protected String getBasePath() {
    return "dart_style";
  }

  public void testClasses() throws Exception {
    runTestInDirectory("comments");
  }

  public void testEnums() throws Exception {
    runTestInDirectory("comments");
  }

  public void testExpressions() throws Exception {
    runTestInDirectory("comments");
  }

  public void testFunctions() throws Exception {
    runTestInDirectory("comments");
  }

  public void testLists() throws Exception {
    runTestInDirectory("comments");
  }

  public void testMaps() throws Exception {
    runTestInDirectory("comments");
  }

  public void testMixed() throws Exception {
    runTestInDirectory("comments");
  }

  public void testStatements() throws Exception {
    runTestInDirectory("comments");
  }

  public void testTop_level() throws Exception {
    runTestInDirectory("comments");
  }

  public void testSelections() throws Exception {
    runTestInDirectory("selections");
  }

  public void testArguments() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testArrows() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testAssignments() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testClasses2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testConstructors2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testEnums2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testExports() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testExpressions2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testFunction_arguments() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testImports() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testInvocations() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testLists2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testList_arguments() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testLoops() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testMaps2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testMap_arguments() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testMembers() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testMixed2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testParameters() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testStatements2() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testStrings() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testType_arguments() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testType_parameters() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testVariables() throws Exception {
    runTestInDirectory("splitting");
  }

  public void testBlocks() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testCascades() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testClasses3() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testCompilation_unit() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testConstructors() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testDirectives() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testDo() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testEnums3() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testExpressions3() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testFor() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testFunctions3() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testIf() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testMetadata() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testMethods() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testScript() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testSwitch() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testTry() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testType_arguments2() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testType_parameters2() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void testWhile() throws Exception {
    runTestInDirectory("whitespace");
  }

  public void test0000() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0005() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0006() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0009() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0013() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0014() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0019() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0021() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0022() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0023() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0025() throws Exception {
    // Verify leadingIndent is working.
    runTestInDirectory("regression/0000");
  }

  public void test0026() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0027() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0028() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0029() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0031() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0033() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0036() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0037() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0038() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0039() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0040() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0041() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0042() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0044() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0045() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0046() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0047() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0049() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0050() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0054() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0055() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0056() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0057() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0058() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0060() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0061() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0066() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0068() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0069() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0070() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0071() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0072() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0075() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0076() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0077() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0080() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0081() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0082() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0083() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0084() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0085() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0086() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0087() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0089() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0090() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0091() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0095() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0096() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0098() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0099() throws Exception {
    runTestInDirectory("regression/0000");
  }

  public void test0100() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0102() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0108() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0109() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0110() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0111() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0112() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0113() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0114() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0115() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0119() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0121() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0122() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0130() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0132() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0135() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0137() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0139() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0140() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0141() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0142() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0144() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0146() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0151() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0152() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0154() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0155() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0156() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0158() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0161() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0162() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0168() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0170() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0171() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0176() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0177() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0178() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0184() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0185() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0186() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0187() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0189() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0192() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0197() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0198() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0199() throws Exception {
    runTestInDirectory("regression/0100");
  }

  public void test0200() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0203() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0204() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0205() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0206() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0211() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0212() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0217() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0218() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0221() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0222() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0223() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0224() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0228() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0229() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0232() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0235() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0236() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0237() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0238() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0241() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0242() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0243() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0247() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0249() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0250() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0256() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0257() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0258() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0259() throws Exception {
    runTestInDirectory("regression/0200");
  }

  public void test0361() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0364() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0368() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0370() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void test0384() throws Exception {
    runTestInDirectory("regression/0300");
  }

  public void testAnalysis_server() throws Exception {
    runTestInDirectory("regression/other");
  }

  public void testDart2js() throws Exception {
    runTestInDirectory("regression/other");
  }

  public void testPub() throws Exception {
    runTestInDirectory("regression/other");
  }

  /**
   * Run a test defined in "*.unit" or "*.stmt" file inside directory <code>dirName</code>.
   */
  protected abstract void runTestInDirectory(String dirName) throws Exception;

  /**
   * Run a test defined in "*.unit" or "*.stmt" file inside directory <code>dirName</code>.
   * Only signal failures for tests that fail and are not listed in <code>knownFailures.</code>
   */
  protected void runTestInDirectory(String dirName, Set knownFailures) throws Exception {
    Pattern indentPattern = Pattern.compile("^.*\\s\\(indent (\\d+)\\)\\s*");

    String testName = getTestName(true);
    if (Character.isLetter(testName.charAt(0)) && Character.isDigit(testName.charAt(testName.length() - 1))) {
      testName = testName.substring(0, testName.length() - 1);
    }

    File dir = new File(new File(getTestDataPath(), getBasePath()), dirName);
    boolean found = false;

    final StringBuilder combinedActualResult = new StringBuilder();
    final StringBuilder combinedExpectedResult = new StringBuilder();

    for (String ext : new String[]{".stmt", ".unit"}) {
      String testFileName = testName + ext;
      File entry = new File(dir, testFileName);
      if (!entry.exists()) {
        continue;
      }

      found = true;
      String[] lines = ArrayUtil.toStringArray(FileUtil.loadLines(entry, "UTF-8"));
      boolean isCompilationUnit = entry.getName().endsWith(".unit");

      // The first line may have a "|" to indicate the page width.
      int pageWidth = 80;
      int i = 0;

      if (lines[0].endsWith("|")) {
        // As it happens, this is always 40 except for some files in 'regression'
        pageWidth = lines[0].indexOf("|");
        i = 1;
      }
      if (!isCompilationUnit) pageWidth += 2; // Adjust for indent in case test is near margin.

      System.out.println("\nTest: " + dirName + "/" + testFileName + ", Right margin: " + pageWidth);
      final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
      settings.RIGHT_MARGIN = pageWidth;
      settings.KEEP_LINE_BREAKS = false; // TODO Decide whether this should be the default -- risky!
      settings.KEEP_BLANK_LINES_IN_CODE = 1;

      while (i < lines.length) {
        String description = (dirName + "/" + testFileName + ":" + (i + 1) + " " + lines[i++].replaceAll(">>>", "")).trim();

        // Let the test specify a leading indentation. This is handy for
        // regression tests which often come from a chunk of nested code.
        int leadingIndent = 0;
        Matcher matcher = indentPattern.matcher(description);

        if (matcher.matches()) {
          // The leadingIndent is only used by some tests in 'regression'.
          leadingIndent = Integer.parseInt(matcher.group(1));
          settings.RIGHT_MARGIN = pageWidth - leadingIndent;
        }

        String input = "";
        // If the input isn't a top-level form, wrap everything in a function.
        // The formatter fails horribly otherwise.
        if (!isCompilationUnit) input += "m() {\n";

        while (!lines[i].startsWith("<<<")) {
          String line = lines[i++];
          if (leadingIndent > 0 && leadingIndent < line.length()) line = line.substring(leadingIndent);
          if (!isCompilationUnit && !line.isEmpty()) line = "  " + line;
          input += line + "\n";
        }

        if (!isCompilationUnit) input += "}\n";

        String expectedOutput = "";
        if (!isCompilationUnit) expectedOutput += "m() {\n";

        i++;

        while (i < lines.length && !lines[i].startsWith(">>>")) {
          String line = lines[i++];
          if (leadingIndent > 0 && leadingIndent < line.length()) line = line.substring(leadingIndent);
          if (!isCompilationUnit && !line.isEmpty()) line = "  " + line;
          expectedOutput += line + "\n";
        }

        if (!isCompilationUnit) expectedOutput += "}\n";

        SourceCode inputCode = extractSourceSelection(input, expectedOutput, isCompilationUnit);
        SourceCode expected = extractSelection(expectedOutput, isCompilationUnit);

        myTextRange = new TextRange(inputCode.selectionStart, inputCode.selectionEnd());

        try {
          doTextTest(inputCode.text, expected.text);
          if (knownFailures.contains(description)) {
            fail("The test passed, but was expected to fail: " + description);
          }
          System.out.println("TEST PASSED: " + (description.isEmpty() ? "(unnamed)" : description));
        }
        catch (ComparisonFailure failure) {
          if (!knownFailures.contains(description.replace('"', '\''))) {
            combinedExpectedResult.append("TEST: ").append(description).append("\n").append(failure.getExpected()).append("\n");
            combinedActualResult.append("TEST: ").append(description).append("\n").append(failure.getActual()).append("\n");
          }
        }
      }
    }

    if (!found) {
      fail("No test data for " + testName);
    }

    assertEquals(combinedExpectedResult.toString(), combinedActualResult.toString());
  }

  protected abstract SourceCode extractSourceSelection(String input, String expectedOutput, boolean isCompilationUnit);

  /*
   * Given a source string that contains ‹ and › to indicate a selection, returns
   * a <code>SourceCode</code> with the text (with the selection markers removed)
   * and the correct selection range.
   */
  protected static SourceCode extractSelection(String source, boolean isCompilationUnit) {
    int start = source.indexOf("‹");
    source = source.replaceAll("‹", "");

    int end = source.indexOf("›");
    source = source.replaceAll("›", "");

    return new SourceCode(source, isCompilationUnit, start == -1 ? 0 : start, end == -1 ? source.length() : end - start);
  }

  protected static class SourceCode {
    String text;
    boolean isCompilationUnit;
    int selectionStart, selectionLength;

    SourceCode(String content, boolean isCU, int start, int len) {
      this.text = content;
      this.isCompilationUnit = isCU;
      this.selectionStart = start;
      this.selectionLength = len;
    }

    int selectionEnd() {
      return selectionStart + selectionLength;
    }
  }
}
