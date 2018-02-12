package com.intellij.prettierjs;

import com.intellij.lang.javascript.service.JSLanguageServiceQueue;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public class PrettierConfigParsingTest extends LightPlatformCodeInsightFixtureTestCase {
  
  public void testEmptyConfigs() {
    doTest(PrettierUtil.Config.DEFAULT, "package.json", "{\"prettier\":{}}");
    doTest(PrettierUtil.Config.DEFAULT, ".prettierrc.json", "{}");
    doTest(PrettierUtil.Config.DEFAULT, ".prettierrc.yaml", "");
  }

  public void testJsonConfig() {
    doTest(new PrettierUtil.Config(true, false, 120, false, true, 3, "all", true),
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
    doTest(new PrettierUtil.Config(true, false, 120, false, true, 3, "all", true),
           "package.json",
           "{\n" +
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
    doTest(new PrettierUtil.Config(true, false, 120, false, true, 3, "all", true),
           ".prettierrc.yml",
           "semi: false\n" +
           "bracketSpacing: false\n" +
           "jsxBracketSameLine: true\n" +
           "printWidth: 120\n" +
           "singleQuote: true\n" +
           "tabWidth: 3\n" +
           "useTabs: true\n" +
           "trailingComma: all\n" +
           "parser: babylon");

  }

  private static void assertSameConfig(PrettierUtil.Config expected, PrettierUtil.Config actual) {
    assertEquals(JSLanguageServiceQueue.GSON.toJson(expected), JSLanguageServiceQueue.GSON.toJson(actual));
  }

  private void doTest(PrettierUtil.Config expected, String fileName, String fileContent) {
    PsiFile psiFile = myFixture.configureByText(fileName, fileContent);
    assertSameConfig(expected, PrettierUtil.parseConfig(psiFile.getProject(), psiFile.getVirtualFile()));
  }
}
