package org.jetbrains.plugins.cucumber.groovy.steps.search;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProvider;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

import javax.swing.*;

/**
 * @author Max Medvedev
 */
public class GrStepDefinitionPresentationProvider implements ItemPresentationProvider<GrMethodCall> {
  private static final Logger LOG = Logger.getInstance(GrStepDefinitionPresentationProvider.class);

  @Nullable
  @Override
  public ItemPresentation getPresentation(final GrMethodCall item) {
    if (!GrCucumberUtil.isStepDefinition(item)) return null;
    return new ItemPresentation() {
      @Nullable
      @Override
      public String getPresentableText() {
        final String stepRef = GrCucumberUtil.getCucumberStepRef(item).getText();
        final String pattern = GrCucumberUtil.getStepDefinitionPattern(item).getText();
        return CucumberBundle.message("step.definition.0.1", stepRef, pattern);
      }

      @Nullable
      @Override
      public String getLocationString() {
        final PsiFile file = item.getContainingFile();
        final ItemPresentation presentation = ItemPresentationProviders.getItemPresentation(file);
        LOG.assertTrue(presentation != null, file.getClass().getName());
        return presentation.getPresentableText();
      }

      @Nullable
      @Override
      public Icon getIcon(boolean unused) {
        return null;
      }
    };
  }
}
