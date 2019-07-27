package com.intellij.prettierjs;

import com.intellij.lang.javascript.service.JSLanguageServiceQueue;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.LineSeparator;

public class PrettierConfigParsingTest extends BasePlatformTestCase {
  
  public void testDefaultConfigs() {
    doTest(PrettierUtil.Config.DEFAULT, "package.json", "{\n" +
                                                        "  \"devDependencies\":{\n" +
                                                        "    \"prettier\":\"latest\"\n" +
                                                        "  },\n" +
                                                        "  \"prettier\": {}\n" +
                                                        "}");
    doTest(PrettierUtil.Config.DEFAULT, ".prettierrc.json", "{}");
    doTest(PrettierUtil.Config.DEFAULT, ".prettierrc.yml", "#comment");
  }

  public void testNoConfigIfPackageNotInDependencies() {
    doTest(null, "package.json", "{}");
  }

  public void testJsonConfig() {
    doTest(new PrettierUtil.Config(true, false, 120, false, true, 3, PrettierUtil.TrailingCommaOption.all, true, 
                                   LineSeparator.CRLF.getSeparatorString()),
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
           "  \"endOfLine\": \"crlf\"\n" +
           "}");
  }

  public void testJsonWithAutoLineSeparator() {
    PrettierUtil.Config parsed = doParse(".prettierrc.json",
                                         "{\n" +
                                         "  \"printWidth\": 113,\n" +
                                         "  \"endOfLine\": \"auto\"\n" +
                                         "}");
    assertEquals(113, parsed.printWidth);
    assertNull(parsed.lineSeparator);
  }

  public void testPackageJsonConfig() {
    doTest(new PrettierUtil.Config(true, false, 120, false, true, 3, PrettierUtil.TrailingCommaOption.all, true, null),
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
    doTest(new PrettierUtil.Config(true, false, 120, false, true, 3, PrettierUtil.TrailingCommaOption.es5, true,
                                   LineSeparator.CRLF.getSeparatorString()),
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
           "endOfLine: crlf"
    );
  }

  private static void assertSameConfig(PrettierUtil.Config expected, PrettierUtil.Config actual) {
    if (expected == null || actual == null) {
      assertEquals(expected, actual);
      return;
    }
    assertEquals(JSLanguageServiceQueue.GSON.toJson(expected), JSLanguageServiceQueue.GSON.toJson(actual));
  }

  private void doTest(PrettierUtil.Config expected, String fileName, String fileContent) {
    assertSameConfig(expected, doParse(fileName, fileContent));
  }

  private PrettierUtil.Config doParse(String fileName, String fileContent) {
    PsiFile psiFile = myFixture.configureByText(fileName, fileContent);
    return PrettierUtil.parseConfig(psiFile.getProject(), psiFile.getVirtualFile());
  }
}
