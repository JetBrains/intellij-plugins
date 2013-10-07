package org.jetbrains.plugins.cucumber.actions;

import com.intellij.codeInsight.editorActions.wordSelection.AbstractWordSelectioner;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinPsiUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dennis.Ushakov
 */
public class GherkinStepParameterSelectioner extends AbstractWordSelectioner {
  private final static Set<Pair<String, String>> START_END = new LinkedHashSet<Pair<String, String>>();

  static {
    START_END.add(Pair.create("'", "'"));
    START_END.add(Pair.create("\"", "\""));
    START_END.add(Pair.create("<", ">"));
    START_END.add(Pair.create("(", ")"));
    START_END.add(Pair.create("[", "]"));
  }

  @Override
  public boolean canSelect(@NotNull PsiElement e) {
    return e.getParent() instanceof GherkinStep;
  }

  @NotNull
  @Override
  public List<TextRange> select(@NotNull final PsiElement e,
                                @NotNull final CharSequence editorText,
                                final int cursorOffset,
                                @NotNull final Editor editor) {
    final List<TextRange> result = new ArrayList<TextRange>();
    if (editor.getSettings().isCamelWords()) {
      result.addAll(super.select(e, editorText, cursorOffset, editor));
    }
    final GherkinStep step = (GherkinStep)e.getParent();
    final PsiReference[] references = step.getReferences();
    for (PsiReference reference : references) {
      if (reference instanceof CucumberStepReference) {
        final AbstractStepDefinition definition = ((CucumberStepReference)reference).resolveToDefinition();
        if (definition != null) {
          final List<TextRange> ranges =
            GherkinPsiUtil.buildParameterRanges(step, definition, step.getTextOffset() + reference.getRangeInElement().getStartOffset());
          if (ranges != null) {
            result.addAll(ranges);
            result.addAll(buildAdditionalRanges(ranges, editorText));
          }
        }
      }
    }
    buildAdditionalRanges(result, editorText);
    return result;
  }

  @NotNull
  private static List<TextRange> buildAdditionalRanges(@NotNull final List<TextRange> ranges, @NotNull final CharSequence editorText) {
    final List<TextRange> result = new ArrayList<TextRange>();
    for (TextRange textRange : ranges) {
      if (textRange.isEmpty()) continue;
      addRangesForText(result, textRange, editorText);
    }
    return result;
  }

  private static void addRangesForText(@NotNull final List<TextRange> result,
                                       @NotNull final TextRange textRange,
                                       @NotNull final CharSequence charSequence) {
    final int startOffset = textRange.getStartOffset();
    final int endOffset = textRange.getEndOffset();
    final String text = charSequence.subSequence(startOffset, endOffset).toString();
    for (Pair<String, String> startEnd : START_END) {
      final String start = startEnd.first;
      final String end = startEnd.second;

      if (text.startsWith(start) && text.endsWith(end)) {
        final TextRange newRange = TextRange.create(startOffset + start.length(), endOffset - end.length());
        if (!newRange.isEmpty()) {
          result.add(newRange);
          addRangesForText(result, newRange, charSequence);
        }
      }

      //with edges
      int startOffsetWithEdge = startOffset - start.length();
      int endOffsetWithEdge = endOffset + end.length();
      if (startOffsetWithEdge >= 0 && endOffsetWithEdge <= charSequence.length()) {
        final String textWithEdges = charSequence.subSequence(startOffsetWithEdge, endOffsetWithEdge).toString();
        if (textWithEdges.startsWith(start) && textWithEdges.endsWith(end)) {
          final TextRange newRange = TextRange.create(startOffsetWithEdge, endOffsetWithEdge);
          if (!newRange.isEmpty()) {
            result.add(newRange);
          }
        }
      }
    }
  }
}
