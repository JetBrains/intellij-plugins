// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.settings.JSRootConfiguration;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.TestLookupElementPresentation;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import static com.intellij.testFramework.UsefulTestCase.assertInstanceOf;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dennis.Ushakov
 */
public class AngularTestUtil {

  public static void configureWithMetadataFiles(@NotNull CodeInsightTestFixture fixture,
                                                @NotNull String... names) {
    fixture.configureByFiles("package.json");
    for (String name : names) {
      fixture.configureByFiles(name + ".d.ts");
      fixture.copyFileToProject(name + ".metadata.json");
    }
  }

  public static void enableAstLoadingFilter(@NotNull UsefulTestCase testCase) {
    Registry.get("ast.loading.filter").setValue(true, testCase.getTestRootDisposable());
  }

  public static String getBaseTestDataPath(Class clazz) {
    String contribPath = getContribPath();
    return contribPath + "/AngularJS/test/" + clazz.getPackage().getName().replace('.', '/') + "/data/";
  }

  private static String getContribPath() {
    final String homePath = PathManager.getHomePath();
    if (new File(homePath, "contrib/.gitignore").isFile()) {
      return homePath + File.separatorChar + "contrib";
    }
    return homePath;
  }

  public static void moveToOffsetBySignature(@NotNull String signature, @NotNull CodeInsightTestFixture fixture) {
    PsiDocumentManager.getInstance(fixture.getProject()).commitAllDocuments();
    int offset = AngularTestUtil.findOffsetBySignature(signature, fixture.getFile());
    fixture.getEditor().getCaretModel().moveToOffset(offset);
  }

  public static int findOffsetBySignature(String signature, final PsiFile psiFile) {
    final String caretSignature = "<caret>";
    int caretOffset = signature.indexOf(caretSignature);
    assert caretOffset >= 0;
    signature = signature.substring(0, caretOffset) + signature.substring(caretOffset + caretSignature.length());
    int pos = psiFile.getText().indexOf(signature);
    assertTrue("Failed to locate '" + signature + "'", pos >= 0);
    return pos + caretOffset;
  }

  public static String getDirectiveDefinitionText(PsiElement resolve) {
    return ObjectUtils.notNull(PsiTreeUtil.getParentOfType(resolve, ES6Decorator.class), resolve.getParent()).getText();
  }

  @NotNull
  public static PsiElement resolveReference(@NotNull String signature, @NotNull CodeInsightTestFixture fixture) {
    int offsetBySignature = findOffsetBySignature(signature, fixture.getFile());
    PsiReference ref = fixture.getFile().findReferenceAt(offsetBySignature);
    TestCase.assertNotNull("No reference at '" + signature + "'", ref);
    PsiElement resolve = ref.resolve();
    TestCase.assertNotNull("Reference resolves to null at '" + signature + "'", resolve);
    return resolve;
  }

  public static void assertUnresolvedReference(@NotNull String signature, @NotNull CodeInsightTestFixture fixture) {
    int offsetBySignature = findOffsetBySignature(signature, fixture.getFile());
    PsiReference ref = fixture.getFile().findReferenceAt(offsetBySignature);
    TestCase.assertNotNull(ref);
    TestCase.assertNull(ref.resolve());
  }

  @SuppressWarnings("unchecked")
  public static <T extends JSElement> T checkVariableResolve(final String signature,
                                                             final String varName,
                                                             final Class<T> varClass,
                                                             @NotNull CodeInsightTestFixture fixture) {
    PsiElement resolve = resolveReference(signature, fixture);
    assertInstanceOf(resolve, varClass);
    assertEquals(varName, varClass.cast(resolve).getName());
    return (T)resolve;
  }

  public static List<String> renderLookupItems(@NotNull CodeInsightTestFixture fixture, boolean renderPriority, boolean renderTypeText) {
    return ContainerUtil.mapNotNull(fixture.getLookupElements(), el -> {
      StringBuilder result = new StringBuilder();
      TestLookupElementPresentation presentation = TestLookupElementPresentation.renderReal(el);
      if (renderPriority && presentation.isItemTextBold()) {
        result.append('!');
      }
      result.append(el.getLookupString());
      if (renderTypeText) {
        result.append('#');
        result.append(presentation.getTypeText());
      }
      if (renderPriority) {
        result.append('#');
        double priority = 0;
        if (el instanceof PrioritizedLookupElement) {
          priority = ((PrioritizedLookupElement)el).getPriority();
        }
        result.append((int)priority);
      }
      return result.toString();
    });
  }

  public static void testES6(@NotNull CodeInsightTestFixture fixture) {
    JSRootConfiguration configuration = JSRootConfiguration.getInstance(fixture.getProject());
    JSLanguageLevel previousLevel = configuration.getLanguageLevel();
    configuration.storeLanguageLevelAndUpdateCaches(JSLanguageLevel.ES6);
    Disposer.register(fixture.getProjectDisposable(), () ->
      configuration.storeLanguageLevelAndUpdateCaches(previousLevel == JSLanguageLevel.DEFAULT ? null : previousLevel));
  }
}
