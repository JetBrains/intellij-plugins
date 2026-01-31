// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.groovy.steps.search;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProvider;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral;

import javax.swing.Icon;

/**
 * @author Max Medvedev
 */
@NotNullByDefault
public final class GrStepDefinitionPresentationProvider implements ItemPresentationProvider<GrMethodCall> {
  private static final Logger LOG = Logger.getInstance(GrStepDefinitionPresentationProvider.class);

  @Override
  public @Nullable ItemPresentation getPresentation(GrMethodCall item) {
    if (!GrCucumberUtil.isStepDefinition(item)) return null;
    return new ItemPresentation() {
      @Override
      public @Nullable String getPresentableText() {
        final GrReferenceExpression stepRef = GrCucumberUtil.getCucumberStepRef(item);
        if (stepRef == null) {
          return null;
        }

        final GrLiteral stepRefPattern = GrCucumberUtil.getStepDefinitionPattern(item);
        if (stepRefPattern == null) {
          return null;
        }

        return CucumberBundle.message("step.definition.0.1", stepRef.getText(), stepRefPattern.getText());
      }

      @Override
      public @Nullable String getLocationString() {
        final PsiFile file = item.getContainingFile();
        final ItemPresentation presentation = ItemPresentationProviders.getItemPresentation(file);
        LOG.assertTrue(presentation != null, file.getClass().getName());
        return presentation.getPresentableText();
      }

      @Override
      public @Nullable Icon getIcon(boolean unused) {
        return null;
      }
    };
  }
}
