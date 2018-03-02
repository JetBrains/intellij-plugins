package com.intellij.prettierjs;

import com.intellij.lang.javascript.service.JSLanguageServiceQueue;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public class PrettierConfigParsingTest extends LightPlatformCodeInsightFixtureTestCase {
  
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
    doTest(new PrettierUtil.Config(true, false, 120, false, true, 3, PrettierUtil.TrailingCommaOption.all, true),
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
           "  \"parser\": \"babylon\"\n" +
           "}");
  }

  public void testPackageJsonConfig() {
    doTest(new PrettierUtil.Config(true, false, 120, false, true, 3, PrettierUtil.TrailingCommaOption.all, true),
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
    doTest(new PrettierUtil.Config(true, false, 120, false, true, 3, PrettierUtil.TrailingCommaOption.es5, true),
           ".prettierrc.yml",
           "semi: false\n" +
           "bracketSpacing: false\n" +
           "jsxBracketSameLine: true\n" +
           "printWidth: 120\n" +
           "singleQuote: true\n" +
           "tabWidth: 3\n" +
           "useTabs: true\n" +
           "trailingComma: es5\n" +
           "parser: babylon");

  }

  private static void assertSameConfig(PrettierUtil.Config expected, PrettierUtil.Config actual) {
    if (expected == null || actual == null) {
      assertEquals(expected, actual);
      return;
    }
    assertEquals(JSLanguageServiceQueue.GSON.toJson(expected), JSLanguageServiceQueue.GSON.toJson(actual));
  }

  private void doTest(PrettierUtil.Config expected, String fileName, String fileContent) {
    PsiFile psiFile = myFixture.configureByText(fileName, fileContent);
    assertSameConfig(expected, PrettierUtil.parseConfig(psiFile.getProject(), psiFile.getVirtualFile()));
  }
}
