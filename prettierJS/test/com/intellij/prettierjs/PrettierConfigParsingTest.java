package com.intellij.prettierjs;

import com.intellij.lang.javascript.service.JSLanguageServiceQueue;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.LineSeparator;

public class PrettierConfigParsingTest extends BasePlatformTestCase {

  public void testDefaultConfigs() {
    doTest(PrettierConfig.DEFAULT, "package.json", "{\n" +
                                                   "  \"devDependencies\":{\n" +
                                                   "    \"prettier\":\"latest\"\n" +
                                                   "  },\n" +
                                                   "  \"prettier\": {}\n" +
                                                   "}");
    doTest(PrettierConfig.DEFAULT, ".prettierrc.json", "{}");
    doTest(PrettierConfig.DEFAULT, ".prettierrc.yml", "#comment");
  }

  public void testNoConfigIfPackageNotInDependencies() {
    doTest(null, "package.json", "{}");
  }

  public void testJsonConfig() {
    doTest(new PrettierConfig(true, false, 120, false, true,
                              3, PrettierConfig.TrailingCommaOption.all, true,
                              LineSeparator.CRLF.getSeparatorString(), true),
           ".prettierrc.json",
           "{\n" +
           "  \"semi\": false,\n" +
           "  \"bracketSpacing\": false,\n" +
           "  \"jsxBracketSameLine\": true,\n" +
           "  \"printWidth\": 120,\n" +
           "  \"singleQuote\": true,\n" +
           "  \"tabWidth\": 3,\n" +
           "  \"useTabs\": true,\n" +
           "  \"trailingComma\": \"all\",\n" +
           "  \"parser\": \"babylon\",\n" +
           "  \"endOfLine\": \"crlf\",\n" +
           "  \"vueIndentScriptAndStyle\": true\n" +
           "}");
  }

  public void testJsonWithAutoLineSeparator() {
    PrettierConfig parsed = doParse(".prettierrc.json",
                                    "{\n" +
                                    "  \"printWidth\": 113,\n" +
                                    "  \"endOfLine\": \"auto\"\n" +
                                    "}");
    assertEquals(113, parsed.printWidth);
    assertNull(parsed.lineSeparator);
  }

  public void testPackageJsonConfig() {
    doTest(new PrettierConfig(true, false, 120, false, true,
                              3, PrettierConfig.TrailingCommaOption.all, true, null, false),
           "package.json",
           "{\n" +
           "  \"devDependencies\": {\n" +
           "    \"prettier\": \"latest\"\n" +
           "  },\n" +
           "  \"prettier\": {\n" +
           "    \"semi\": false,\n" +
           "    \"bracketSpacing\": false,\n" +
           "    \"jsxBracketSameLine\": true,\n" +
           "    \"printWidth\": 120,\n" +
           "    \"singleQuote\": true,\n" +
           "    \"tabWidth\": 3,\n" +
           "    \"useTabs\": true,\n" +
           "    \"trailingComma\": \"all\",\n" +
           "    \"parser\": \"babylon\"\n" +
           "  }\n" +
           "}");
  }

  public void testYamlConfig() {
    doTest(new PrettierConfig(true, false, 120, false, true, 3, PrettierConfig.TrailingCommaOption.es5, true,
                              LineSeparator.CRLF.getSeparatorString(), true),
           ".prettierrc.yml",
           "semi: false\n" +
           "bracketSpacing: false\n" +
           "jsxBracketSameLine: true\n" +
           "printWidth: 120\n" +
           "singleQuote: true\n" +
           "tabWidth: 3\n" +
           "useTabs: true\n" +
           "trailingComma: es5\n" +
           "parser: babylon\n" +
           "endOfLine: crlf\n" +
           "vueIndentScriptAndStyle: true"
    );
  }

  private static void assertSameConfig(PrettierConfig expected, PrettierConfig actual) {
    if (expected == null || actual == null) {
      assertEquals(expected, actual);
      return;
    }
    assertEquals(JSLanguageServiceQueue.GSON.toJson(expected), JSLanguageServiceQueue.GSON.toJson(actual));
  }

  private void doTest(PrettierConfig expected, String fileName, String fileContent) {
    assertSameConfig(expected, doParse(fileName, fileContent));
  }

  private PrettierConfig doParse(String fileName, String fileContent) {
    PsiFile psiFile = myFixture.configureByText(fileName, fileContent);
    return PrettierUtil.parseConfig(psiFile.getProject(), psiFile.getVirtualFile());
  }
}
