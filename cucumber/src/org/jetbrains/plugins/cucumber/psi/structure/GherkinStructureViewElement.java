package org.jetbrains.plugins.cucumber.psi.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberIcons;
import org.jetbrains.plugins.cucumber.psi.GherkinFeature;
import org.jetbrains.plugins.cucumber.psi.GherkinPsiElement;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinFeatureHeaderImpl;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinTableImpl;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinTagImpl;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author yole
 */
public class GherkinStructureViewElement extends PsiTreeElementBase<PsiElement> {
  protected GherkinStructureViewElement(PsiElement psiElement) {
    super(psiElement);
  }

  @NotNull
  public Collection<StructureViewTreeElement> getChildrenBase() {
    List<StructureViewTreeElement> result = new ArrayList<StructureViewTreeElement>();
    for (PsiElement element : getElement().getChildren()) {
      if (element instanceof GherkinPsiElement &&
          !(element instanceof GherkinFeatureHeaderImpl) &&
          !(element instanceof GherkinTableImpl) &&
          !(element instanceof GherkinTagImpl)) {
        result.add(new GherkinStructureViewElement(element));
      }
    }
    return result;
  }

  @Override
  public Icon getIcon(boolean open) {
    final PsiElement element = getElement();
    if (element instanceof GherkinFeature
        || element instanceof GherkinStepsHolder) {
      return open ? CucumberIcons.STRUCTURE_STEPS_GROUP_OPEN_ICON : CucumberIcons.STRUCTURE_STEPS_GROUP_CLOSED_ICON;
    }
    if (element instanceof GherkinStep) {
      return CucumberIcons.STRUCTURE_STEP_ICON;
    }
    return null;
  }


  public String getPresentableText() {
    return ((NavigationItem) getElement()).getPresentation().getPresentableText();
  }
}
