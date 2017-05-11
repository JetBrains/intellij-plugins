package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.plugins.cucumber.psi.*;

public class GherkinStepParameterReference extends GherkinSimpleReference {

  public GherkinStepParameterReference(GherkinStepParameter stepParameter) {
    super(stepParameter);
  }

  @Override
  public GherkinStepParameter getElement() {
    return (GherkinStepParameter)super.getElement();
  }

  //@Override
  //public TextRange getRangeInElement() {
  //  TextRange superRange = super.getRangeInElement();
  //  return new TextRange(1, superRange.getEndOffset() - 1);
  //}

  @Override
  public PsiElement resolve() {
    final GherkinScenarioOutline scenario = PsiTreeUtil.getParentOfType(getElement(), GherkinScenarioOutline.class);
    if (scenario != null) {
      final GherkinExamplesBlock exampleBlock = PsiTreeUtil.getChildOfType(scenario, GherkinExamplesBlock.class);
      if (exampleBlock != null) {
        final GherkinTable table = PsiTreeUtil.getChildOfType(exampleBlock, GherkinTable.class);
        if (table != null) {
          final GherkinTableHeaderRowImpl header = PsiTreeUtil.getChildOfType(table, GherkinTableHeaderRowImpl.class);
          if (header != null) {
            for (PsiElement cell : header.getChildren()) {
              if (cell instanceof GherkinTableCell) {
                final String cellText = cell.getText();
                if (cellText.equals(getElement().getName())) {
                  return cell;
                }
              }
            }
          }
        }
      }
    }
    return null;
  }
}
