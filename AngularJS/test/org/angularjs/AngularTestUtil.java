// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;
import com.intellij.util.ObjectUtils;
import com.intellij.webSymbols.testFramework.WebTestUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.intellij.testFramework.UsefulTestCase.assertInstanceOf;
import static junit.framework.TestCase.assertEquals;

public final class AngularTestUtil {

    public static String getBaseTestDataPath(Class<?> clazz) {
        String contribPath = getContribPath();
        return contribPath + "/AngularJS/testResources/" + clazz.getPackage().getName().replace('.', '/') + "/data/";
    }

  private static String getContribPath() {
        final String homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy();
        if (new File(homePath, "contrib/.gitignore").isFile()) {
            return homePath + File.separatorChar + "contrib";
        }
        return homePath;
    }

    public static String getDirectiveDefinitionText(PsiElement resolve) {
        return ObjectUtils.notNull(PsiTreeUtil.getParentOfType(resolve, ES6Decorator.class), resolve.getParent()).getText();
    }

    @SuppressWarnings("unchecked")
    public static <T extends JSElement> T checkVariableResolve(final String signature,
                                                               final String varName,
                                                               final Class<T> varClass,
                                                               @NotNull CodeInsightTestFixture fixture) {
        PsiElement resolve = resolveReference(signature, fixture);
        assertInstanceOf(resolve, varClass);
        assertEquals(varName, varClass.cast(resolve).getName());
        return (T) resolve;
    }

    public static void moveToOffsetBySignature(@NotNull String signature, @NotNull CodeInsightTestFixture fixture) {
        WebTestUtil.moveToOffsetBySignature(fixture, signature);
    }

    public static int findOffsetBySignature(String signature, final PsiFile psiFile) {
        return WebTestUtil.findOffsetBySignature(psiFile, signature);
    }

    @NotNull
    public static PsiElement resolveReference(@NotNull String signature, @NotNull CodeInsightTestFixture fixture) {
        return WebTestUtil.resolveReference(fixture, signature);
    }

}
