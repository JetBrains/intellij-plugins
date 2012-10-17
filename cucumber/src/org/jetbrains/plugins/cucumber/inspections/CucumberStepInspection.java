package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.ex.ProblemDescriptorImpl;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.*;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinScenarioOutlineImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yole
 */
public class CucumberStepInspection extends GherkinInspection {
  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return CucumberBundle.message("cucumber.inspection.undefined.step.name");
  }

  @NotNull
  public String getShortName() {
    return "CucumberUndefinedStep";
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new GherkinElementVisitor() {
      @Override
      public void visitStep(GherkinStep step) {
        super.visitStep(step);

        final PsiElement parent = step.getParent();
        if (parent instanceof GherkinStepsHolder) {
          final PsiReference[] references = step.getReferences();
          if (references.length != 1 || !(references[0] instanceof CucumberStepReference)) return;

          CucumberStepReference reference = (CucumberStepReference)references[0];
          final AbstractStepDefinition definition = reference.resolveToDefinition();
          if (definition == null) {
            CucumberCreateStepFix fix = null;
            if (CucumberStepsIndex.getInstance(step.getProject()).getExtensionCount() > 0) {
              fix = new CucumberCreateStepFix();
            }
            holder.registerProblem(reference.getElement(), reference.getRangeInElement(), CucumberBundle.message(
              "cucumber.inspection.undefined.step.msg.name") + " #loc #ref", fix);
          }
          else if (isOnTheFly) {
            // highlighting for regexp params
            final List<TextRange> parameterRanges = GherkinPsiUtil.buildParameterRanges(step, definition,
                                                                                        reference.getRangeInElement().getStartOffset());
            if (parameterRanges == null) return;
            for (TextRange range : parameterRanges) {
              registerHiglighting(GherkinHighlighter.REGEXP_PARAMETER, step, range, holder);
            }
          }

          if (isOnTheFly) {
            // highlighting for scenario outline params
            highlightOutlineParams(step, reference, holder);
          }
        }
      }
    };
  }

  private static void highlightOutlineParams(@NotNull final GherkinStep step,
                                             @NotNull final CucumberStepReference reference,
                                             @NotNull final ProblemsHolder holder) {
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
      highlightOutlineParamsForText(step.getStepName(), textStartOffset, pattern, step, holder);

      // highlight in pystring
      final GherkinPystring pystring = step.getPystring();
      if (pystring != null) {
        final int textOffset = pystring.getTextOffset() - step.getTextOffset();
        highlightOutlineParamsForText(pystring.getText(), textOffset, pattern, step, holder);
      }

      // highlight in table
      final PsiElement table = step.getTable();
      if (table != null) {
        final int textOffset = table.getTextOffset() - step.getTextOffset();
        highlightOutlineParamsForText(table.getText(), textOffset, pattern, step, holder);
      }
    }
  }

  private static void highlightOutlineParamsForText(final String text, final int textStartInElementOffset,
                                                    final Pattern pattern,
                                                    final GherkinStep step,
                                                    final ProblemsHolder holder) {
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
          registerHiglighting(GherkinHighlighter.OUTLINE_PARAMETER_SUBSTITUTION, step, range, holder);
        }
        result = matcher.find();
      } while (result);
    }
  }

  private static void registerHiglighting(TextAttributesKey attributesKey, GherkinStep step, TextRange range, ProblemsHolder holder) {
    final ProblemDescriptor descriptor = new ProblemDescriptorImpl(step, step, "", LocalQuickFix.EMPTY_ARRAY,
                                                                   ProblemHighlightType.INFORMATION, false, range, false, null,
                                                                   holder.isOnTheFly());
    descriptor.setTextAttributes(attributesKey);
    holder.registerProblem(descriptor);
  }

  @Nullable
  private static List<String> getRealSubstitutions(@NotNull final GherkinStep step) {
    final List<String> possibleSubstitutions = step.getParamsSubstitutions();
    if (!possibleSubstitutions.isEmpty()) {
      // get step definition
      final GherkinStepsHolder holder = step.getStepHolder();
      // if step is in Scenario Outline
      if (holder instanceof GherkinScenarioOutlineImpl) {
        // then get header cell
        final GherkinScenarioOutlineImpl outline = (GherkinScenarioOutlineImpl)holder;
        final List<GherkinExamplesBlock> examplesBlocks = outline.getExamplesBlocks();
        if (examplesBlocks.isEmpty()) {
          return null;
        }
        final GherkinTable table = examplesBlocks.get(0).getTable();
        if (table == null) {
          return null;
        }
        final GherkinTableRow header = table.getHeaderRow();
        assert header != null;

        final List<GherkinTableCell> headerCells = header.getPsiCells();
        // fetch headers
        final List<String> headers = new ArrayList<String>(headerCells.size() + 1);
        for (PsiElement headerCell : headerCells) {
          headers.add(headerCell.getText().trim());
        }
        // filter used substitutions names
        final List<String> realSubstitutions = new ArrayList<String>(possibleSubstitutions.size() + 1);
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
