// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.groovy.steps;

import com.intellij.ide.util.EditSourceUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.pom.Navigatable;
import com.intellij.pom.PomNamedTarget;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.params.GrParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Max Medvedev
 */
public class GrStepDefinition extends AbstractStepDefinition implements PomNamedTarget {
  public GrStepDefinition(GrMethodCall stepDefinition) {
    super(stepDefinition);
  }

  public static GrStepDefinition getStepDefinition(final GrMethodCall statement) {
    return CachedValuesManager.getCachedValue(statement, () -> {
      final Document document = PsiDocumentManager.getInstance(statement.getProject()).getDocument(statement.getContainingFile());
      return CachedValueProvider.Result.create(new GrStepDefinition(statement), document);
    });
  }

  @Override
  public List<String> getVariableNames() {
    PsiElement element = getElement();
    if (element instanceof GrMethodCall) {
      GrClosableBlock[] closures = ((GrMethodCall)element).getClosureArguments();
      assert closures.length == 1;
      GrParameter[] parameters = closures[0].getParameterList().getParameters();
      ArrayList<String> result = new ArrayList<>();
      for (GrParameter parameter : parameters) {
        result.add(parameter.getName());
      }

      return result;
    }
    return Collections.emptyList();
  }

  @Override
  protected @Nullable String getCucumberRegexFromElement(PsiElement element) {
    if (!(element instanceof GrMethodCall)) {
      return null;
    }
    return GrCucumberUtil.getStepDefinitionPatternText((GrMethodCall)element);
  }

  @Override
  public String getName() {
    return getCucumberRegex();
  }

  @Override
  public boolean isValid() {
    final PsiElement element = getElement();
    return element != null && element.isValid();
  }

  @Override
  public void navigate(boolean requestFocus) {
    Navigatable descr = EditSourceUtil.getDescriptor(getElement());
    if (descr != null) descr.navigate(requestFocus);
  }

  @Override
  public boolean canNavigate() {
    return EditSourceUtil.canNavigate(getElement());
  }

  @Override
  public boolean canNavigateToSource() {
    return canNavigate();
  }
}
