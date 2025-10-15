package org.jetbrains.plugins.cucumber.psi;

import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.plugins.cucumber.psi.impl.*;

public abstract class GherkinElementVisitor extends PsiElementVisitor {
  public void visitFeature(GherkinFeature feature) {
    visitElement(feature);
  }

  public void visitRule(GherkinRule rule) {
    visitElement(rule);
  }

  public void visitFeatureHeader(GherkinFeatureHeaderImpl header) {
    visitElement(header);
  }

  public void visitScenario(GherkinScenario scenario) {
    visitElement(scenario);
  }

  public void visitScenarioOutline(GherkinScenarioOutline outline) {
    visitElement(outline);
  }

  public void visitExamplesBlock(GherkinExamplesBlockImpl block) {
    visitElement(block);
  }

  public void visitStep(GherkinStep step) {
    visitElement(step);
  }

  public void visitTable(GherkinTableImpl table) {
    visitElement(table);
  }

  public void visitTableRow(GherkinTableRowImpl row) {
    visitElement(row);
  }

  public void visitTableHeaderRow(GherkinTableHeaderRowImpl row) {
    visitElement(row);
  }

  public void visitTag(GherkinTagImpl gherkinTag) {
    visitElement(gherkinTag);
  }

  public void visitStepParameter(GherkinStepParameterImpl gherkinStepParameter) {
    visitElement(gherkinStepParameter);
  }

  public void visitGherkinTableCell(GherkinTableCell cell) {
    visitElement(cell);
  }

  public void visitPystring(GherkinPystring phstring) {
    visitElement(phstring);
  }
}
