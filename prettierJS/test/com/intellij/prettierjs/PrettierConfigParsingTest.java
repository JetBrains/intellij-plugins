package com.intellij.prettierjs;

import com.intellij.lang.javascript.service.JSLanguageServiceQueue;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.LineSeparator;

public class PrettierConfigParsingTest extends BasePlatformTestCase {

  public void testDefaultConfigs() {
    doTest(PrettierConfig.DEFAULT, "package.json", """
      {
        "devDependencies":{
          "prettier":"latest"
        },
        "prettier": {}
      }""");
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
           """
             {
               "semi": false,
               "bracketSpacing": false,
               "jsxBracketSameLine": true,
               "printWidth": 120,
               "singleQuote": true,
               "tabWidth": 3,
               "useTabs": true,
               "trailingComma": "all",
               "parser": "babylon",
               "endOfLine": "crlf",
               "vueIndentScriptAndStyle": true
             }""");
  }

  public void testJsonWithAutoLineSeparator() {
    PrettierConfig parsed = doParse(".prettierrc.json",
                                    """
                                      {
                                        "printWidth": 113,
                                        "endOfLine": "auto"
                                      }""");
    assertEquals(113, parsed.printWidth);
    assertNull(parsed.lineSeparator);
  }

  public void testPackageJsonConfig() {
    doTest(new PrettierConfig(true, false, 120, false, true,
                              3, PrettierConfig.TrailingCommaOption.all, true, null, false),
           "package.json",
           """
             {
               "devDependencies": {
                 "prettier": "latest"
               },
               "prettier": {
                 "semi": false,
                 "bracketSpacing": false,
                 "jsxBracketSameLine": true,
                 "printWidth": 120,
                 "singleQuote": true,
                 "tabWidth": 3,
                 "useTabs": true,
                 "trailingComma": "all",
                 "parser": "babylon"
               }
             }""");
  }

  public void testYamlConfig() {
    doTest(new PrettierConfig(true, false, 120, false, true, 3, PrettierConfig.TrailingCommaOption.es5, true,
                              LineSeparator.CRLF.getSeparatorString(), true),
           ".prettierrc.yml",
           """
             semi: false
             bracketSpacing: false
             jsxBracketSameLine: true
             printWidth: 120
             singleQuote: true
             tabWidth: 3
             useTabs: true
             trailingComma: es5
             parser: babylon
             endOfLine: crlf
             vueIndentScriptAndStyle: true"""
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
