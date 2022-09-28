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
import com.intellij.lang.javascript.linter.tslint.codestyle.rules.TsLintConfigWrapper;
import com.intellij.lang.javascript.linter.tslint.codestyle.rules.TsLintRule;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.Consumer;
import com.intellij.util.EmptyConsumer;
import org.intellij.lang.annotations.Language;
import org.junit.Assert;

import java.util.Collection;

public class TsLintCodeStyleImportBasicTest extends BasePlatformTestCase {
  public void testSimpleStringValue() {
    doTestJson("{\"rules\": {\"semicolon\": [true,\"never\"]}}", (settings) -> Assert.assertFalse(settings.getCustomSettings(TypeScriptCodeStyleSettings.class).USE_SEMICOLON_AFTER_STATEMENT));
  }

  public void testSimpleNumberValue() {
    doTestJson("{\"rules\": {\"max-line-length\": [true, 12]}}", (settings) -> {
      CommonCodeStyleSettings commonSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
      Assert.assertEquals(12, commonSettings.RIGHT_MARGIN);
    });
  }

  public void testDisabledRule() {
    doTestJson("{\"rules\": {\"semicolon\": false}}", (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      Assert.assertTrue(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      Assert.assertFalse(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  public void testDisabledRuleWithOptions() {
    doTestJson("{\"rules\": {\"semicolon\": [false,\"never\"]}}", (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      Assert.assertTrue(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      Assert.assertFalse(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  public void testSeverityOff() {
    doTestJson("""
                 {
                   "rules": {
                     "semicolon": {
                       "options": ["never"],
                       "severity": "off"
                     }
                   }
                 }""", (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      Assert.assertTrue(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      Assert.assertFalse(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  public void testSeverityNone() {
    doTestJson("""
                 {
                   "rules": {
                     "semicolon": {
                       "options": ["never"],
                       "severity": "none"
                     }
                   }
                 }""", (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      Assert.assertTrue(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      Assert.assertFalse(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  public void testWithSeverityAndSingleStringOption() {
    doTestJson("""
                 {
                   "rules": {
                     "semicolon": {
                       "options": "never",
                       "severity": "error"
                     }
                   }
                 }""", (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      Assert.assertFalse(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      Assert.assertTrue(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  public void testWithOptionsObject() {
    doTestJson("""
                 {
                   "rules": {
                     "space-before-function-paren": [true, {"anonymous": "never"}]
                   }
                 }
                 """, (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      Assert.assertFalse(tsSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH);
    });
  }

  public void testWithSeverityAndOptionsObject() {
    doTestJson("""
                 {
                   "rules": {
                     "space-before-function-paren": {
                       "severity": "error",
                       "options": { "anonymous": "never" }
                     }
                   }
                 }
                 """, (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      Assert.assertFalse(tsSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH);
    });
  }

  public void testWithSeverityAndOptionsObjectInArray() {
    doTestJson("""
                 {
                   "rules": {
                     "space-before-function-paren": {
                       "severity": "error",
                       "options": [{ "anonymous": "never" }
                       ]
                     }
                   }
                 }
                 """, (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      Assert.assertFalse(tsSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH);
    });
  }
  
  public void testWithSeverityAndStringArrayOption() {
    doTestJson("""
                 {
                   "rules": {
                     "semicolon": {
                       "options": ["never"],
                       "severity": "error"
                     }
                   }
                 }""", (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      Assert.assertFalse(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      Assert.assertTrue(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  public void testWithSeverityAndSingleNumberOption() {
    doTestJson("""
                 {
                   "rules": {
                     "max-line-length": {
                       "severity": "error",
                       "options": 12
                     }
                   }
                 }""", (settings) -> {
      CommonCodeStyleSettings tsSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
      assertEquals(12, tsSettings.RIGHT_MARGIN);
    });
  }

  public void testWithSeverityAndArrayNumberOption() {
    doTestJson("""
                 {
                   "rules": {
                     "max-line-length": {
                       "severity": "error",
                       "options": [12]
                     }
                   }
                 }""", (settings) -> {
      CommonCodeStyleSettings tsSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
      assertEquals(12, tsSettings.RIGHT_MARGIN);
    });
  }

  public void testStringListRule() {
    doTestJson("""
                 {
                   "rules": {
                     "import-blacklist": [
                       true,
                       "foojs",
                       "barjs"
                     ]
                   }
                 }
                 """, (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      assertEquals("foojs,barjs", tsSettings.BLACKLIST_IMPORTS);
    });
  }

  public void testSimpleStringValueYaml() {
    doTestYaml("rules:\n" +
               "    semicolon: [true, \"never\"]", settings -> Assert.assertFalse(settings.getCustomSettings(TypeScriptCodeStyleSettings.class).USE_SEMICOLON_AFTER_STATEMENT));
  }

  public void testSimpleNumberValueYaml() {
    doTestYaml("""
                 rules:
                     semicolon: [true, "never"]
                     max-line-length: [true, 12]""", settings -> {
      CommonCodeStyleSettings tsSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
      assertEquals(12, tsSettings.RIGHT_MARGIN);
    });
  }

  public void testStringValueFromOptionsYaml() {
    doTestYaml("""
                 rules:
                     semicolon:
                         options:
                             - never""", settings -> Assert.assertFalse(settings.getCustomSettings(TypeScriptCodeStyleSettings.class).USE_SEMICOLON_AFTER_STATEMENT));
  }

  public void testNumberValueFromOptionsYaml() {
    doTestYaml("""
                 rules:
                     max-line-length:
                         options: 12""", settings -> {
      CommonCodeStyleSettings tsSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
      assertEquals(12, tsSettings.RIGHT_MARGIN);
    }); 
  }

  public void testNoneSeverityYaml() {
    doTestYaml("""
                 rules:
                     max-line-length:
                         severity: "none"
                         options: 12""", settings -> {
      CommonCodeStyleSettings tsSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
      assertEquals(-1, tsSettings.RIGHT_MARGIN);
    });
  }

  public void testStringListRuleYaml() {
    doTestYaml("""
                 rules:
                     import-blacklist:
                         options:
                             - foojs
                             - barjs
                 """, settings -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      assertEquals("foojs,barjs", tsSettings.BLACKLIST_IMPORTS);
    });
  }

  public void testIndentWithTabs() {
    int indentBefore = 7;
    doTestJson("""
                 {
                   "rules": {
                     "indent": [true, "tabs"]
                   }
                 }""", (settings) -> {
                 CommonCodeStyleSettings tsSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
                 CommonCodeStyleSettings.IndentOptions indentOptions = tsSettings.getIndentOptions();
                 indentOptions.INDENT_SIZE = indentBefore;
                 indentOptions.CONTINUATION_INDENT_SIZE = indentBefore;
                 indentOptions.TAB_SIZE = indentBefore;
               },
               settings -> {
                 CommonCodeStyleSettings tsSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
                 CommonCodeStyleSettings.IndentOptions indentOptions = tsSettings.getIndentOptions();
                 assertTrue(indentOptions.USE_TAB_CHARACTER);
                 assertEquals(indentBefore, indentOptions.INDENT_SIZE);
                 assertEquals(indentBefore, indentOptions.CONTINUATION_INDENT_SIZE);
                 assertEquals(indentBefore, indentOptions.TAB_SIZE);
               });
  }

  public void testIndentWithTabsInOptions() {
    doTestJson("""
                 {
                   "rules": {
                     "indent": {
                       "options": "tabs"
                     }
                   }
                 }""", settings -> {
      CommonCodeStyleSettings.IndentOptions indentOptions =
        settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT).getIndentOptions();
      assertTrue(indentOptions.USE_TAB_CHARACTER);
    });
  }

  public void testIndentWithSize() {
    doTestJson("""
                 {
                   "rules": {
                     "indent": [true, "spaces", 2]
                   }
                 }""", settings -> {
      CommonCodeStyleSettings.IndentOptions indentOptions = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT).getIndentOptions();
      assertFalse(indentOptions.USE_TAB_CHARACTER);
      assertEquals(2, indentOptions.INDENT_SIZE);
      assertEquals(2, indentOptions.CONTINUATION_INDENT_SIZE);
    });
  }

  public void testIndentWithSizeInOptions() {
    doTestJson("""
                 {
                   "rules": {
                     "indent": {
                       "options": ["spaces", 2]
                     }
                   }
                 }""", settings -> {
      CommonCodeStyleSettings.IndentOptions indentOptions =
        settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT).getIndentOptions();
      assertFalse(indentOptions.USE_TAB_CHARACTER);
      assertEquals(2, indentOptions.INDENT_SIZE);
      assertEquals(2, indentOptions.CONTINUATION_INDENT_SIZE);
    });
  }

  private void doTestJson(@Language("JSON") String configText, Consumer<CodeStyleSettings> test) {
    doTestJson(configText, EmptyConsumer.getInstance(), test);
  }

  private void doTestJson(@Language("JSON") String configText, Consumer<CodeStyleSettings> setup, Consumer<CodeStyleSettings> test) {
    doTest("tslint.json", configText, setup, test);
  }

  private void doTestYaml(@Language("YAML") String configText, Consumer<CodeStyleSettings> test) {
    doTest("tslint.yaml", configText, EmptyConsumer.getInstance(), test);
  }

  private void doTest(String configFileName,
                      String configText,
                      Consumer<CodeStyleSettings> setup, Consumer<CodeStyleSettings> test) {
    JSTestUtils.testWithTempCodeStyleSettings(getProject(), settings -> {

      CodeStyleSettings codeStyleSettings = CodeStyle.getSettings(myFixture.getProject());
      setup.consume(codeStyleSettings);

      PsiFile configFile = myFixture.configureByText(configFileName, configText);
      TsLintConfigWrapper config = TsLintConfigWrapper.Companion.getConfigForFile(configFile);
      Collection<TsLintRule> rulesToApply = config.getRulesToApply(getProject());
      config.applyRules(getProject(), rulesToApply);
      
      test.consume(codeStyleSettings);
    });
  }
}
