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
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.Consumer;
import com.intellij.util.EmptyConsumer;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public class TsLintCodeStyleImportBasicTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = sharedCodeInsightFixture(projectFixture);

  @Test
  public void testSimpleStringValue() {
    doTestJson("{\"rules\": {\"semicolon\": [true,\"never\"]}}", (settings) -> assertFalse(settings.getCustomSettings(TypeScriptCodeStyleSettings.class).USE_SEMICOLON_AFTER_STATEMENT));
  }

  @Test
  public void testSimpleNumberValue() {
    doTestJson("{\"rules\": {\"max-line-length\": [true, 12]}}", (settings) -> {
      CommonCodeStyleSettings commonSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
      assertEquals(12, commonSettings.RIGHT_MARGIN);
    });
  }

  @Test
  public void testDisabledRule() {
    doTestJson("{\"rules\": {\"semicolon\": false}}", (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      assertTrue(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      assertFalse(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  @Test
  public void testDisabledRuleWithOptions() {
    doTestJson("{\"rules\": {\"semicolon\": [false,\"never\"]}}", (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      assertTrue(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      assertFalse(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  @Test
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
      assertTrue(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      assertFalse(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  @Test
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
      assertTrue(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      assertFalse(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  @Test
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
      assertFalse(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      assertTrue(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  @Test
  public void testWithOptionsObject() {
    doTestJson("""
                 {
                   "rules": {
                     "space-before-function-paren": [true, {"anonymous": "never"}]
                   }
                 }
                 """, (settings) -> {
      TypeScriptCodeStyleSettings tsSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings.class);
      assertFalse(tsSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH);
    });
  }

  @Test
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
      assertFalse(tsSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH);
    });
  }

  @Test
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
      assertFalse(tsSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH);
    });
  }
  
  @Test
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
      assertFalse(tsSettings.USE_SEMICOLON_AFTER_STATEMENT);
      assertTrue(tsSettings.FORCE_SEMICOLON_STYLE);
    });
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testSimpleStringValueYaml() {
    doTestYaml("rules:\n" +
               "    semicolon: [true, \"never\"]", settings -> assertFalse(settings.getCustomSettings(TypeScriptCodeStyleSettings.class).USE_SEMICOLON_AFTER_STATEMENT));
  }

  @Test
  public void testSimpleNumberValueYaml() {
    doTestYaml("""
                 rules:
                     semicolon: [true, "never"]
                     max-line-length: [true, 12]""", settings -> {
      CommonCodeStyleSettings tsSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
      assertEquals(12, tsSettings.RIGHT_MARGIN);
    });
  }

  @Test
  public void testStringValueFromOptionsYaml() {
    doTestYaml("""
                 rules:
                     semicolon:
                         options:
                             - never""", settings -> assertFalse(settings.getCustomSettings(TypeScriptCodeStyleSettings.class).USE_SEMICOLON_AFTER_STATEMENT));
  }

  @Test
  public void testNumberValueFromOptionsYaml() {
    doTestYaml("""
                 rules:
                     max-line-length:
                         options: 12""", settings -> {
      CommonCodeStyleSettings tsSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT);
      assertEquals(12, tsSettings.RIGHT_MARGIN);
    }); 
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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
    CodeInsightTestFixture myFixture = codeInsightFixture.get();
    Project project = myFixture.getProject();
    JSTestUtils.testWithTempCodeStyleSettings(project, settings -> {

      CodeStyleSettings codeStyleSettings = CodeStyle.getSettings(project);
      setup.consume(codeStyleSettings);

      PsiFile configFile = myFixture.configureByText(configFileName, configText);
      TsLintConfigWrapper config = ReadAction.computeBlocking(() -> TsLintConfigWrapper.Companion.getConfigForFile(configFile));
      Collection<TsLintRule> rulesToApply = ReadAction.computeBlocking(() -> config.getRulesToApply(project));
      config.applyRules(project, rulesToApply);
      
      test.consume(codeStyleSettings);
    });
  }
}
