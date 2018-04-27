// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.lang.javascript.linter.tslint;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.linter.tslint.config.style.rules.TsLintConfigWrapper;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.Consumer;
import org.intellij.lang.annotations.Language;
import org.junit.Assert;

public class TslintCodeStyleImportTest extends LightPlatformCodeInsightFixtureTestCase {
  public void testSimpleStringValue() throws Exception {
    doTestJson("{\"rules\": {\"semicolon\": [true,\"never\"]}}", (settings) -> {
      Assert.assertFalse(settings.getCustomSettings(TypeScriptCodeStyleSettings.class).USE_SEMICOLON_AFTER_STATEMENT);
    });
  }

  public void testSimpleNumberValue() throws Exception {
    doTestJson("{\"rules\": {\"max-line-length\": [true, 12]}}", (settings) -> {
      CommonCodeStyleSettings commonSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
      Assert.assertEquals(12, commonSettings.RIGHT_MARGIN);
    });
  }

  public void testDisabledRule() throws Exception {
    doTestJson("{\"rules\": {\"semicolon\": false}}", (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      Assert.assertTrue(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      Assert.assertFalse(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  public void testDisabledRuleWithOptions() throws Exception {
    doTestJson("{\"rules\": {\"semicolon\": [false,\"never\"]}}", (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      Assert.assertTrue(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      Assert.assertFalse(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  public void testSeverityOff() throws Exception {
    doTestJson("{\n" +
               "  \"rules\": {\n" +
               "    \"semicolon\": {\n" +
               "      \"options\": [\"never\"],\n" +
               "      \"severity\": \"off\"\n" +
               "    }\n" +
               "  }\n" +
               "}", (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      Assert.assertTrue(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      Assert.assertFalse(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  public void testSeverityNone() throws Exception {
    doTestJson("{\n" +
               "  \"rules\": {\n" +
               "    \"semicolon\": {\n" +
               "      \"options\": [\"never\"],\n" +
               "      \"severity\": \"none\"\n" +
               "    }\n" +
               "  }\n" +
               "}", (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      Assert.assertTrue(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      Assert.assertFalse(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  public void testWithSeverityAndSingleStringOption() throws Exception {
    doTestJson("{\n" +
               "  \"rules\": {\n" +
               "    \"semicolon\": {\n" +
               "      \"options\": \"never\",\n" +
               "      \"severity\": \"error\"\n" +
               "    }\n" +
               "  }\n" +
               "}", (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      Assert.assertFalse(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      Assert.assertTrue(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }
  
  public void testWithSeverityAndStringArrayOption() throws Exception {
    doTestJson("{\n" +
               "  \"rules\": {\n" +
               "    \"semicolon\": {\n" +
               "      \"options\": [\"never\"],\n" +
               "      \"severity\": \"error\"\n" +
               "    }\n" +
               "  }\n" +
               "}", (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      Assert.assertFalse(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      Assert.assertTrue(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  public void testWithSeverityAndSingleNumberOption() throws Exception {
    doTestJson("{\n" +
               "  \"rules\": {\n" +
               "    \"max-line-length\": {\n" +
               "      \"severity\": \"error\",\n" +
               "      \"options\": 12\n" +
               "    }\n" +
               "  }\n" +
               "}", (settings) -> {
      CommonCodeStyleSettings tsSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
      assertEquals(12, tsSettings.RIGHT_MARGIN);
    });
  }

  public void testWithSeverityAndArrayNumberOption() throws Exception {
    doTestJson("{\n" +
               "  \"rules\": {\n" +
               "    \"max-line-length\": {\n" +
               "      \"severity\": \"error\",\n" +
               "      \"options\": [12]\n" +
               "    }\n" +
               "  }\n" +
               "}", (settings) -> {
      CommonCodeStyleSettings tsSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
      assertEquals(12, tsSettings.RIGHT_MARGIN);
    });
  }

  public void testStringListRule() throws Exception {
    doTestJson("{\n" +
               "  \"rules\": {\n" +
               "    \"import-blacklist\": [\n" +
               "      true,\n" +
               "      \"foojs\",\n" +
               "      \"barjs\"\n" +
               "    ]\n" +
               "  }\n" +
               "}\n", (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      assertEquals("foojs,barjs", tsSettings.BLACKLIST_IMPORTS);
    });
  }

  public void testSimpleStringValueYaml() throws Exception {
    doTestYaml("rules:\n" +
               "    semicolon: [true, \"never\"]", settings -> {
      Assert.assertFalse(settings.getCustomSettings(TypeScriptCodeStyleSettings.class).USE_SEMICOLON_AFTER_STATEMENT);
    });
  }

  public void testSimpleNumberValueYaml() throws Exception {
    doTestYaml("rules:\n" +
               "    semicolon: [true, \"never\"]\n" +
               "    max-line-length: [true, 12]", settings -> {
      CommonCodeStyleSettings tsSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
      assertEquals(12, tsSettings.RIGHT_MARGIN);
    });
  }

  public void testStringValueFromOptionsYaml() throws Exception {
    doTestYaml("rules:\n" +
               "    semicolon:\n" +
               "        options:\n" +
               "            - never", settings -> {
      Assert.assertFalse(settings.getCustomSettings(TypeScriptCodeStyleSettings.class).USE_SEMICOLON_AFTER_STATEMENT);
    });
  }

  public void testNumberValueFromOptionsYaml() throws Exception {
    doTestYaml("rules:\n" +
               "    max-line-length:\n" +
               "        options: 12", settings -> {
      CommonCodeStyleSettings tsSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
      assertEquals(12, tsSettings.RIGHT_MARGIN);
    }); 
  }

  public void testNoneSeverityYaml() throws Exception {
    doTestYaml("rules:\n" +
               "    max-line-length:\n" +
               "        severity: \"none\"\n" +
               "        options: 12", settings -> {
      CommonCodeStyleSettings tsSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
      assertEquals(-1, tsSettings.RIGHT_MARGIN);
    });
  }

  public void testStringListRuleYaml() throws Exception {
    doTestYaml("rules:\n" +
               "    import-blacklist:\n" +
               "        options:\n" +
               "            - foojs\n" +
               "            - barjs\n", settings -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      assertEquals("foojs,barjs", tsSettings.BLACKLIST_IMPORTS);
    });
  }

  private void doTestJson(@Language("JSON") String configText, Consumer<CodeStyleSettings> test) throws Exception {
    doTest("tslint.json", configText, test);
  }

  private void doTestYaml(@Language("YAML") String configText, Consumer<CodeStyleSettings> test) throws Exception {
    doTest("tslint.yaml", configText, test);
  }

  private void doTest(String configFileName, String configText, Consumer<CodeStyleSettings> test)
    throws Exception {
    JSTestUtils.testWithTempCodeStyleSettings(getProject(), settings -> {
      PsiFile configFile = myFixture.configureByText(configFileName, configText);
      TsLintConfigWrapper config = TsLintConfigWrapper.Companion.getConfigForFile(configFile);
      config.applyRules(getProject(), config.getRulesToApply(getProject()));
      test.consume(CodeStyle.getSettings(configFile));
    });
  }
}
