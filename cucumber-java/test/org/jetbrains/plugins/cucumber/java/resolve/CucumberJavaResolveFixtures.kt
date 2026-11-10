// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:JvmName("CucumberJavaResolveFixtures")

package org.jetbrains.plugins.cucumber.java.resolve

import com.intellij.openapi.application.PathManager
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.impl.PomTargetPsiElementImpl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.LightJavaProject
import com.intellij.testFramework.fixtures.lightJavaCodeInsightFixture
import com.intellij.testFramework.junit5.fixture.TestFixture
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil
import org.jetbrains.plugins.cucumber.resolve.CucumberResolveFixture
import org.jetbrains.plugins.cucumber.resolve.CucumberResolveFixture.Companion.cucumberResolveFixture
import java.util.function.Function

/** The test data directory of the Java step definition resolve tests, relative to the home path. */
const val RESOLVE_RELATED_TEST_DATA_PATH: String = CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "resolve"

/**
 * Creates a [CucumberResolveFixture] for the Java step definitions in the module of [projectFixture].
 * Declare it as an instance field, and [projectFixture] as a static field with
 * [com.intellij.testFramework.fixtures.lightJavaProjectFixture] and the Cucumber descriptor of the class.
 *
 * [relatedTestDataPath] is the directory of the test data, relative to the home path.
 * [stepDefinitionName] gives the name of a step definition, by default the name of the step definition method.
 */
@JvmOverloads
fun cucumberJavaResolveFixture(
  projectFixture: TestFixture<LightJavaProject>,
  relatedTestDataPath: String = RESOLVE_RELATED_TEST_DATA_PATH,
  stepDefinitionName: Function<PsiElement, String?> = Function(::getJavaStepDefinitionName),
): TestFixture<CucumberResolveFixture> =
  cucumberResolveFixture(lightJavaCodeInsightFixture(projectFixture), PathManager.getHomePath() + relatedTestDataPath, stepDefinitionName)

private fun getJavaStepDefinitionName(element: PsiElement): String? {
  return when (element) {
    is PsiAnnotation -> PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)!!.name
    is PsiMethodCallExpression -> element.methodExpression.qualifiedName
    is PomTargetPsiElementImpl -> element.name
    else -> null
  }
}
