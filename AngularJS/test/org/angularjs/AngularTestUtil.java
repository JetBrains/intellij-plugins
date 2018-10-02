// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.openapi.application.PathManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.intellij.testFramework.UsefulTestCase.assertInstanceOf;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dennis.Ushakov
 */
public class AngularTestUtil {

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

  public static void moveToOffsetBySignature(@NotNull String signature,  @NotNull CodeInsightTestFixture fixture) {
    int offset = AngularTestUtil.findOffsetBySignature(signature, fixture.getFile());
    fixture.getEditor().getCaretModel().moveToOffset(offset);
  }

  public static int findOffsetBySignature(String signature, final PsiFile psiFile) {
    final String caretSignature = "<caret>";
    int caretOffset = signature.indexOf(caretSignature);
    assert caretOffset >= 0;
    signature = signature.substring(0, caretOffset) + signature.substring(caretOffset + caretSignature.length());
    int pos = psiFile.getText().indexOf(signature);
    assertTrue(pos >= 0);
    return pos + caretOffset;
  }

  public static String getDirectiveDefinitionText(PsiElement resolve) {
    return resolve.getParent().getText();
  }

  @NotNull
  public static PsiElement resolveReference(@NotNull String signature, @NotNull CodeInsightTestFixture fixture) {
    int offsetBySignature = findOffsetBySignature(signature, fixture.getFile());
    PsiReference ref = fixture.getFile().findReferenceAt(offsetBySignature);
    TestCase.assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    TestCase.assertNotNull(resolve);
    return resolve;
  }

  public static void assertUnresolvedReference(@NotNull String signature, @NotNull CodeInsightTestFixture fixture) {
    int offsetBySignature = findOffsetBySignature(signature, fixture.getFile());
    PsiReference ref = fixture.getFile().findReferenceAt(offsetBySignature);
    TestCase.assertNotNull(ref);
    TestCase.assertNull(ref.resolve());
  }

  @SuppressWarnings("unchecked")
  public static <T extends JSElement> T checkVariableResolve(final String signature, final String varName, final Class<T> varClass, @NotNull CodeInsightTestFixture fixture) {
    PsiElement resolve = resolveReference(signature, fixture);
    assertInstanceOf(resolve, varClass);
    assertEquals(varName, varClass.cast(resolve).getName());
    return (T)resolve;
  }

}
