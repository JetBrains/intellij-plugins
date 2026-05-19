package com.intellij.lang.javascript.linter.eslint;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction;
import com.intellij.json.codeinsight.JsonStandardComplianceInspection;
import com.intellij.lang.javascript.linter.JSLinterConfigLangSubstitutor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.jsonSchema.impl.inspections.JsonSchemaComplianceInspection;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EslintConfigHighlightingTest extends BasePlatformTestCase {
  public static final String ESLINTRC = ".eslintrc-schema.json";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(JsonStandardComplianceInspection.class);
    myFixture.enableInspections(JsonSchemaComplianceInspection.class);
  }

  @Override
  protected String getBasePath() {
    return EslintTestUtil.ESLINT_TEST_DATA_RELATIVE_PATH + "/linter/eslint/config/highlighting";
  }

  public void testCommentAcceptance() {
    myFixture.testHighlighting(true, false, true, getFilePath(getTestName(true)));
  }

  public void testYamlAcceptance() {
    PsiFile psiFile = myFixture.configureByFile(getFilePath(getTestName(true)));
    assertEquals(JSLinterConfigLangSubstitutor.YamlLanguageHolder.INSTANCE, psiFile.getLanguage());
  }

  public void testSampleConfig() {
    myFixture.testHighlighting(true, false, true, getFilePath(getTestName(true)));
  }

  public void testConfigWithOverrides() {
    myFixture.testHighlighting(true, false, true, getFilePath(getTestName(true)));
  }

  public void testNavigationToJsonSchema() {
    myFixture.configureByFiles("schemaResolve/.eslintrc");
    int offset = myFixture.getCaretOffset();
    final PsiElement resolve = GotoDeclarationAction.findTargetElement(getProject(), myFixture.getEditor(), offset);
    Assert.assertNotNull(resolve);
    Assert.assertEquals("\"comma-dangle\"", resolve.getText());
    Assert.assertEquals(ESLINTRC, resolve.getContainingFile().getName());
    Assert.assertNotEquals(myFixture.getFile(), resolve.getContainingFile());
  }

  public void testSchemaResolveCorrectFile() {
    myFixture.configureByFiles(getTestName(true) + "/.eslintrc");
    int offset = myFixture.getCaretOffset();
    final PsiElement resolve = GotoDeclarationAction.findTargetElement(getProject(), myFixture.getEditor(), offset);
    Assert.assertNotNull(resolve);
    Assert.assertEquals("\"env\"", resolve.getText());
    Assert.assertEquals(ESLINTRC, resolve.getContainingFile().getName());
    Assert.assertNotEquals(myFixture.getFile(), resolve.getContainingFile());
  }

  public void testNavigationToJsonSchemaBrowserProperty() {
    myFixture.configureByFiles("schemaBrowserResolve/.eslintrc.json");
    int offset = myFixture.getCaretOffset();
    final PsiElement resolve = GotoDeclarationAction.findTargetElement(getProject(), myFixture.getEditor(), offset);
    Assert.assertNotNull(resolve);
    Assert.assertEquals("\"browser\"", resolve.getText());
    Assert.assertEquals(ESLINTRC, resolve.getContainingFile().getName());
    Assert.assertNotEquals(myFixture.getFile(), resolve.getContainingFile());
  }

  public void testEslintConfigInPackageJsonCompletion() {
    myFixture.configureByFile(getTestName(true) + "/package.json");
    final LookupElement[] elements = myFixture.completeBasic();
    final Set<String> strings = new HashSet<>(ContainerUtil.map(Arrays.asList(elements), LookupElement::getLookupString));
    assertTrue(strings.contains("\"rules\""));
    assertTrue(strings.contains("\"globals\""));
  }

  public void testEslintConfigInPackageJsonNavigation1() {
    myFixture.configureByFile(getTestName(true) + "/package.json");
    int offset = myFixture.getCaretOffset();
    final PsiElement resolve = GotoDeclarationAction.findTargetElement(getProject(), myFixture.getEditor(), offset);
    Assert.assertNotNull(resolve);
    Assert.assertEquals("\"env\"", resolve.getText());
    Assert.assertEquals(ESLINTRC, resolve.getContainingFile().getName());
    Assert.assertNotEquals(myFixture.getFile(), resolve.getContainingFile());
  }

  public void testEslintConfigInPackageJsonNavigation2() {
    myFixture.configureByFile(getTestName(true) + "/package.json");
    int offset = myFixture.getCaretOffset();
    final PsiElement resolve = GotoDeclarationAction.findTargetElement(getProject(), myFixture.getEditor(), offset);
    Assert.assertNotNull(resolve);
    Assert.assertEquals("\"arrow-spacing\"", resolve.getText());
    Assert.assertEquals(ESLINTRC, resolve.getContainingFile().getName());
    Assert.assertNotEquals(myFixture.getFile(), resolve.getContainingFile());
  }

  public void testTopLevelPropertiesWithHints() {
    myFixture.configureByFile(getFilePath(getTestName(true)));
    myFixture.complete(CompletionType.BASIC);
    assertSameElements(ContainerUtil.map(myFixture.getLookupElementStrings(), StringUtil::unquoteString),
                       "extends", "ecmaFeatures", "overrides", "settings",
                       "parser", "rules", "env", "globals", "parserOptions", "plugins", "root", "noInlineConfig", "ignorePatterns");
    final LookupElement[] elements = myFixture.getLookupElements();
    assertElementHasHint(elements, "env", "An environment defines global variables that are predefined.");
    assertElementHasHint(elements, "overrides", "Allows to override configuration for files and folders, specified by glob patterns");
  }

  private static void assertElementHasHint(final LookupElement @NotNull [] elements,
                                           final @NotNull String name,
                                           final @NotNull String hint) {
    final LookupElement env =
      Arrays.stream(elements).filter(el -> name.equals(StringUtil.unquoteString(el.getLookupString()))).findFirst().orElse(null);
    Assert.assertNotNull(env);
    final LookupElementPresentation presentation = new LookupElementPresentation();
    env.renderElement(presentation);
    Assert.assertEquals(hint, presentation.getTypeText());
  }

  private static String getFilePath(@NotNull String subDirectoryName) {
    return subDirectoryName + "/" + EslintUtil.DEFAULT_CONFIG_PREFIX;
  }
}

