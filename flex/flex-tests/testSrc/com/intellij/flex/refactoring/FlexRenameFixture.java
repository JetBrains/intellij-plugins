// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.refactoring;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSRenameFixture;
import com.intellij.lang.javascript.JSTestFixtures;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static com.intellij.codeInsight.TargetElementUtil.findTargetElement;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The rename helpers of the JUnit 3 test {@code FlexRenameTest} for a JUnit 5 test. Create it with {@link #flexRenameFixture}.
 * It replaces the {@code findTarget()}, {@code doTest()} and {@code findRenamedRefsToReferencedElementAtCaret()} overrides of the test.
 * Call a member from a background thread.
 */
@TestOnly
public class FlexRenameFixture extends JSRenameFixture {
  public FlexRenameFixture(@NotNull CodeInsightTestFixture fixture,
                           @NotNull Class<?> testClass,
                           @NotNull Method testMethod,
                           @NotNull JSFixtureOptions options) {
    super(fixture, testClass, testMethod, options);
  }

  @TestOnly
  public static @NotNull TestFixture<FlexRenameFixture> flexRenameFixture(@NotNull TestFixture<Project> projectFixture,
                                                                         @NotNull JSFixtureOptions options) {
    return JSTestFixtures.jsFixture("flexRenameFixture", projectFixture, options, FlexRenameFixture::new);
  }

  @Override
  public PsiElement findTarget() {
    return EdtTestUtil.runInEdtAndGet(() -> {
      PsiElement target =
        findTargetElement(getFixture().getEditor(), TargetElementUtil.ELEMENT_NAME_ACCEPTED | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED);
      return ObjectUtils.coalesce(target, getFixture().getFile());
    });
  }

  @Override
  public void doTest(@NotNull String newName, @NotNull String ext) {
    final String name = getTestName(false);
    doTest(newName, name + "_after." + ext, true, true, false,
           name + "." + ext); // current test data expects that string refs are renamed
  }

  public void doTest(final String newName, String fileNameAfter, String... fileNames) {
    doTest(newName, fileNameAfter, false, true, false, fileNames);
  }

  public void doTest(final String newName, boolean substituteElement, String... fileNames) {
    doTest(helper().defaultParameters().withName(newName).substitute(substituteElement).withFiles(fileNames));
  }

  public void doTest(final String newName, String fileNameAfter, boolean substituteElement, String... fileNames) {
    doTest(newName, fileNameAfter, substituteElement, false, false, fileNames);
  }

  public void doTest(final String newName,
                     final String fileNameAfter,
                     final boolean substituteElement,
                     final boolean searchInCommentsAndStrings, //true by default
                     final boolean searchForTextOccurrences, //false by default
                     final String... fileNames) {
    getFixture().configureByFiles(fileNames);
    performDialogRename(helper().defaultParameters()
                          .withName(newName)
                          .substitute(substituteElement)
                          .searchForTextOccurrences(searchForTextOccurrences)
                          .searchInCommentsAndStrings(searchInCommentsAndStrings));
    getFixture().checkResultByFile(fileNameAfter);
  }

  @Override
  public PsiReference @NotNull [] findRenamedRefsToReferencedElementAtCaret() {
    PsiElement target = findTarget();
    assertNotNull(target);
    Project project = getProject();
    return ReadAction.computeBlocking(() -> {
      PsiElement object = target;
      PsiReference[] references =
        ReferencesSearch.search(object, GlobalSearchScope.allScope(project), true).toArray(PsiReference.EMPTY_ARRAY);

      if (object instanceof JSFileImpl) {
        JSNamedElement element = ActionScriptResolveUtil.findMainDeclaredElement((JSFileImpl)object);
        if (element != null) object = element;
      }

      PsiElement additionalTarget;
      if (object instanceof JSClass) {
        additionalTarget = ((JSClass)object).getConstructor();
      }
      else if (object instanceof JSFunction && ((JSFunction)object).isConstructor()) {
        additionalTarget = object.getParent();
      }
      else {
        additionalTarget = null;
      }

      Set<PsiReference> uniquesSet = ContainerUtil.newHashSet(references);
      if (additionalTarget != null) {
        uniquesSet.addAll(ReferencesSearch.search(additionalTarget, GlobalSearchScope.allScope(project), true).findAll());
      }

      if (object instanceof JSFunction) {
        CommonProcessors.CollectProcessor<JSFunction> allFunctions =
          new CommonProcessors.CollectProcessor<>(Collections.synchronizedList(new ArrayList<>()));
        JSInheritanceUtil.iterateMethodsDown((JSFunction)object, allFunctions);
        for (JSFunction function : allFunctions.getResults()) {
          uniquesSet.addAll(ReferencesSearch.search(function, GlobalSearchScope.allScope(project), true).findAll());
        }
      }
      return uniquesSet.toArray(PsiReference.EMPTY_ARRAY);
    });
  }
}
