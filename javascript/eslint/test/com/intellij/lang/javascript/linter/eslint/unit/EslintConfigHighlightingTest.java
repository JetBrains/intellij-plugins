package com.intellij.lang.javascript.linter.eslint.unit;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction;
import com.intellij.json.codeinsight.JsonStandardComplianceInspection;
import com.intellij.lang.javascript.linter.JSLinterConfigLangSubstitutor;
import com.intellij.lang.javascript.linter.eslint.EslintUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.jsonSchema.impl.inspections.JsonSchemaComplianceInspection;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.testNameFixture;
import static org.assertj.core.api.Assertions.assertThat;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/javascript/eslint/testData/linter/eslint/config/highlighting")
public class EslintConfigHighlightingTest {
  public static final String ESLINTRC = ".eslintrc-schema.json";

  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);
  private final TestFixture<String> testName = testNameFixture();

  @BeforeEach
  void setUp() {
    codeInsight.get().enableInspections(JsonStandardComplianceInspection.class);
    codeInsight.get().enableInspections(JsonSchemaComplianceInspection.class);
  }

  @Test
  public void testCommentAcceptance() {
    CodeInsightTestFixture fixture = codeInsight.get();
    fixture.testHighlighting(true, false, true, getFilePath(testName.get()));
  }

  @Test
  public void testYamlAcceptance() {
    CodeInsightTestFixture fixture = codeInsight.get();
    PsiFile psiFile = fixture.configureByFile(getFilePath(testName.get()));
    Assertions.assertEquals(JSLinterConfigLangSubstitutor.YamlLanguageHolder.INSTANCE, psiFile.getLanguage());
  }

  @Test
  public void testSampleConfig() {
    CodeInsightTestFixture fixture = codeInsight.get();
    fixture.testHighlighting(true, false, true, getFilePath(testName.get()));
  }

  @Test
  public void testConfigWithOverrides() {
    CodeInsightTestFixture fixture = codeInsight.get();
    fixture.testHighlighting(true, false, true, getFilePath(testName.get()));
  }

  @Test
  public void testNavigationToJsonSchema() {
    CodeInsightTestFixture fixture = codeInsight.get();
    fixture.configureByFiles("schemaResolve/.eslintrc");
    ReadAction.run(() -> {
      int offset = fixture.getCaretOffset();
      final PsiElement resolve = GotoDeclarationAction.findTargetElement(fixture.getProject(), fixture.getEditor(), offset);
      Assertions.assertNotNull(resolve);
      Assertions.assertEquals("\"comma-dangle\"", resolve.getText());
      Assertions.assertEquals(ESLINTRC, resolve.getContainingFile().getName());
      Assertions.assertNotEquals(fixture.getFile(), resolve.getContainingFile());
    });
  }

  @Test
  public void testSchemaResolveCorrectFile() {
    CodeInsightTestFixture fixture = codeInsight.get();
    fixture.configureByFiles(testName.get() + "/.eslintrc");
    ReadAction.run(() -> {
      int offset = fixture.getCaretOffset();
      final PsiElement resolve = GotoDeclarationAction.findTargetElement(fixture.getProject(), fixture.getEditor(), offset);
      Assertions.assertNotNull(resolve);
      Assertions.assertEquals("\"env\"", resolve.getText());
      Assertions.assertEquals(ESLINTRC, resolve.getContainingFile().getName());
      Assertions.assertNotEquals(fixture.getFile(), resolve.getContainingFile());
    });
  }

  @Test
  public void testNavigationToJsonSchemaBrowserProperty() {
    CodeInsightTestFixture fixture = codeInsight.get();
    fixture.configureByFiles("schemaBrowserResolve/.eslintrc.json");
    ReadAction.run(() -> {
      int offset = fixture.getCaretOffset();
      final PsiElement resolve = GotoDeclarationAction.findTargetElement(fixture.getProject(), fixture.getEditor(), offset);
      Assertions.assertNotNull(resolve);
      Assertions.assertEquals("\"browser\"", resolve.getText());
      Assertions.assertEquals(ESLINTRC, resolve.getContainingFile().getName());
      Assertions.assertNotEquals(fixture.getFile(), resolve.getContainingFile());
    });
  }

  @Test
  public void testEslintConfigInPackageJsonCompletion() {
    CodeInsightTestFixture fixture = codeInsight.get();
    fixture.configureByFile(testName.get() + "/package.json");
    final LookupElement[] elements = fixture.completeBasic();
    final Set<String> strings = new HashSet<>(ContainerUtil.map(Arrays.asList(elements), LookupElement::getLookupString));
    Assertions.assertTrue(strings.contains("\"rules\""));
    Assertions.assertTrue(strings.contains("\"globals\""));
  }

  @Test
  public void testEslintConfigInPackageJsonNavigation1() {
    CodeInsightTestFixture fixture = codeInsight.get();
    fixture.configureByFile(testName.get() + "/package.json");
    ReadAction.run(() -> {
      int offset = fixture.getCaretOffset();
      final PsiElement resolve = GotoDeclarationAction.findTargetElement(fixture.getProject(), fixture.getEditor(), offset);
      Assertions.assertNotNull(resolve);
      Assertions.assertEquals("\"env\"", resolve.getText());
      Assertions.assertEquals(ESLINTRC, resolve.getContainingFile().getName());
      Assertions.assertNotEquals(fixture.getFile(), resolve.getContainingFile());
    });
  }

  @Test
  public void testEslintConfigInPackageJsonNavigation2() {
    CodeInsightTestFixture fixture = codeInsight.get();
    fixture.configureByFile(testName.get() + "/package.json");
    ReadAction.run(() -> {
      int offset = fixture.getCaretOffset();
      final PsiElement resolve = GotoDeclarationAction.findTargetElement(fixture.getProject(), fixture.getEditor(), offset);
      Assertions.assertNotNull(resolve);
      Assertions.assertEquals("\"arrow-spacing\"", resolve.getText());
      Assertions.assertEquals(ESLINTRC, resolve.getContainingFile().getName());
      Assertions.assertNotEquals(fixture.getFile(), resolve.getContainingFile());
    });
  }

  @Test
  public void testTopLevelPropertiesWithHints() {
    CodeInsightTestFixture fixture = codeInsight.get();
    fixture.configureByFile(getFilePath(testName.get()));
    fixture.complete(CompletionType.BASIC);
    assertThat(ContainerUtil.map(fixture.getLookupElementStrings(), StringUtil::unquoteString)).containsExactlyInAnyOrder(
                       "extends", "ecmaFeatures", "overrides", "settings",
                       "parser", "rules", "env", "globals", "parserOptions", "plugins", "root", "noInlineConfig", "ignorePatterns");
    final LookupElement[] elements = fixture.getLookupElements();
    assertElementHasHint(elements, "env", "An environment defines global variables that are predefined.");
    assertElementHasHint(elements, "overrides", "Allows to override configuration for files and folders, specified by glob patterns");
  }

  private static void assertElementHasHint(final LookupElement @NotNull [] elements,
                                           final @NotNull String name,
                                           final @NotNull String hint) {
    final LookupElement env =
      Arrays.stream(elements).filter(el -> name.equals(StringUtil.unquoteString(el.getLookupString()))).findFirst().orElse(null);
    Assertions.assertNotNull(env);
    final LookupElementPresentation presentation = new LookupElementPresentation();
    env.renderElement(presentation);
    Assertions.assertEquals(hint, presentation.getTypeText());
  }

  private static String getFilePath(@NotNull String subDirectoryName) {
    return subDirectoryName + "/" + EslintUtil.DEFAULT_CONFIG_PREFIX;
  }
}

