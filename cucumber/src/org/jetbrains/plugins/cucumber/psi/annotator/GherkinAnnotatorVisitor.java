// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.annotator;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinExamplesBlock;
import org.jetbrains.plugins.cucumber.psi.GherkinHighlighter;
import org.jetbrains.plugins.cucumber.psi.GherkinPsiUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinPystring;
import org.jetbrains.plugins.cucumber.psi.GherkinScenarioOutline;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.GherkinStepParameter;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;
import org.jetbrains.plugins.cucumber.psi.GherkinTable;
import org.jetbrains.plugins.cucumber.psi.GherkinTableCell;
import org.jetbrains.plugins.cucumber.psi.GherkinTableRow;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinExamplesBlockImpl;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinExamplesNavigator;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinScenarioOutlineImpl;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinTableHeaderRowImpl;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinTableImpl;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinTableNavigator;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jetbrains.plugins.cucumber.CucumberUtil.getCucumberStepReference;

/**
 * @author Roman.Chernyatchik
 */
@NotNullByDefault
public class GherkinAnnotatorVisitor extends GherkinElementVisitor {
  private final AnnotationHolder myHolder;

  public GherkinAnnotatorVisitor(AnnotationHolder holder) {
    myHolder = holder;
  }

  private void highlight(PsiElement element, TextAttributesKey colorKey) {
    myHolder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element).textAttributes(colorKey).create();
  }

  private void highlight(PsiElement element, TextRange range, TextAttributesKey colorKey) {
    range = range.shiftRight(element.getTextOffset());
    myHolder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(colorKey).create();
  }

  @Override
  public void visitElement(PsiElement element) {
    ProgressManager.checkCanceled();
    super.visitElement(element);

    boolean textInsideScenario =
      PsiUtilCore.getElementType(element) == GherkinTokenTypes.TEXT && element.getParent() instanceof GherkinStepsHolder;
    if (textInsideScenario && hasStepsBefore(element)) {
      myHolder.newAnnotation(HighlightSeverity.ERROR, CucumberBundle.message("gherkin.lexer.unexpected.element")).create();
    }
  }

  private static boolean hasStepsBefore(PsiElement element) {
    element = element.getPrevSibling();
    while (element != null && (!(element instanceof GherkinStep))) {
      element = element.getPrevSibling();
    }
    return element != null;
  }

  @Override
  public void visitStep(GherkinStep step) {
    CucumberStepReference reference = getCucumberStepReference(step);
    if (reference == null) {
      return;
    }
    final AbstractStepDefinition definition = reference.resolveToDefinition();

    if (definition != null) {
      final List<TextRange> parameterRanges = GherkinPsiUtil.buildParameterRanges(step, definition,
                                                                                  reference.getRangeInElement().getStartOffset());
      if (parameterRanges == null) return;
      for (TextRange range : parameterRanges) {
        if (range.getLength() > 0) {
          highlight(step, range, GherkinHighlighter.REGEXP_PARAMETER);
        }
      }
      highlightOutlineParams(step, reference);
    }
  }

  @Override
  public void visitScenarioOutline(GherkinScenarioOutline outline) {
    super.visitScenarioOutline(outline);

    final GherkinStepParameter[] params = PsiTreeUtil.getChildrenOfType(outline, GherkinStepParameter.class);
    if (params != null) {
      for (GherkinStepParameter param : params) {
        highlight(param, GherkinHighlighter.OUTLINE_PARAMETER_SUBSTITUTION);
      }
    }

    final ASTNode[] braces = outline.getNode().getChildren(TokenSet.create(GherkinTokenTypes.STEP_PARAMETER_BRACE));
    for (ASTNode brace : braces) {
      highlight(brace.getPsi(), GherkinHighlighter.REGEXP_PARAMETER);
    }
  }

  @Override
  public void visitTableHeaderRow(GherkinTableHeaderRowImpl row) {
    super.visitTableRow(row);

    ProgressManager.checkCanceled();

    final GherkinTableImpl table = GherkinTableNavigator.getTableByRow(row);
    final GherkinExamplesBlockImpl examplesSection = table != null
                                                     ? GherkinExamplesNavigator.getExamplesByTable(table)
                                                     : null;
    if (examplesSection == null) {
      // do noting if table isn't in Examples section
      return;
    }
    final List<GherkinTableCell> cells = row.getPsiCells();
    for (PsiElement cell : cells) {
      highlight(cell, GherkinHighlighter.TABLE_HEADER_CELL);
    }
  }

  private void highlightOutlineParams(GherkinStep step, CucumberStepReference reference) {
    final List<String> realSubstitutions = getRealSubstitutions(step);
    if (realSubstitutions != null && !realSubstitutions.isEmpty()) {
      // regexp for searching outline parameters substitutions
      final StringBuilder regexp = new StringBuilder();
      regexp.append("<(");
      for (String substitution : realSubstitutions) {
        if (regexp.length() > 2) {
          regexp.append("|");
        }
        regexp.append(Pattern.quote(substitution));
      }
      regexp.append(")>");

      // for each substitution - add highlighting
      final Pattern pattern = Pattern.compile(regexp.toString());

      // highlight in step name
      final int textStartOffset = reference.getRangeInElement().getStartOffset();
      highlightOutlineParamsForText(step.getName(), textStartOffset, pattern, step);

      // highlight in pystring
      final GherkinPystring pystring = step.getPystring();
      if (pystring != null) {
        final int textOffset = pystring.getTextOffset() - step.getTextOffset();
        highlightOutlineParamsForText(pystring.getText(), textOffset, pattern, step);
      }

      // highlight in table
      final PsiElement table = step.getTable();
      if (table != null) {
        final int textOffset = table.getTextOffset() - step.getTextOffset();
        highlightOutlineParamsForText(table.getText(), textOffset, pattern, step);
      }
    }
  }

  private void highlightOutlineParamsForText(String text, int textStartInElementOffset, Pattern pattern,
                                             GherkinStep step) {
    if (StringUtil.isEmpty(text)) {
      return;
    }

    final Matcher matcher = pattern.matcher(text);
    boolean result = matcher.find();
    if (result) {
      do {
        final String substitution = matcher.group(1);
        if (!StringUtil.isEmpty(substitution)) {
          final int start = matcher.start(1);
          final int end = matcher.end(1);
          final TextRange range = new TextRange(start, end).shiftRight(textStartInElementOffset);
          highlight(step, range, GherkinHighlighter.OUTLINE_PARAMETER_SUBSTITUTION);
        }
        result = matcher.find();
      }
      while (result);
    }
  }

  private static @Nullable List<String> getRealSubstitutions(GherkinStep step) {
    final List<String> possibleSubstitutions = step.getParamsSubstitutions();
    if (!possibleSubstitutions.isEmpty()) {
      // get step definition
      final GherkinStepsHolder holder = step.getStepHolder();
      // if step is in Scenario Outline
      if (holder instanceof GherkinScenarioOutlineImpl outline) {
        // then get header cell
        final List<GherkinExamplesBlock> examplesBlocks = outline.getExamplesBlocks();
        if (examplesBlocks.isEmpty()) {
          return null;
        }
        final GherkinTable table = examplesBlocks.getFirst().getTable();
        if (table == null) {
          return null;
        }
        final GherkinTableRow header = table.getHeaderRow();
        assert header != null;

        final List<GherkinTableCell> headerCells = header.getPsiCells();
        // fetch headers
        final List<String> headers = new ArrayList<>(headerCells.size() + 1);
        for (PsiElement headerCell : headerCells) {
          headers.add(headerCell.getText().trim());
        }
        // filter used substitutions names
        final List<String> realSubstitutions = new ArrayList<>(possibleSubstitutions.size() + 1);
        for (String substitution : possibleSubstitutions) {
          if (headers.contains(substitution)) {
            realSubstitutions.add(substitution);
          }
        }
        return realSubstitutions.isEmpty() ? null : realSubstitutions;
      }
    }
    return null;
  }
}
