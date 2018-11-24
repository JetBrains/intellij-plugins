package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinFileImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman.Chernyatchik
 * @date May 21, 2009
 */
public class GherkinPsiUtil {
  private GherkinPsiUtil() {
  }

  @Nullable
  public static GherkinFileImpl getGherkinFile(@NotNull final PsiElement element) {
    if (!element.isValid()){
      return null;
    }
    final PsiFile containingFile = element.getContainingFile();
    return containingFile instanceof GherkinFileImpl ? (GherkinFileImpl)containingFile : null;
  }

  @Nullable
  public static List<TextRange> buildParameterRanges(@NotNull GherkinStep step,
                                                     @NotNull AbstractStepDefinition definition,
                                                     final int shiftOffset) {
    final List<TextRange> parameterRanges = new ArrayList<>();
    final Pattern pattern = definition.getPattern();
    if (pattern == null) return null;
    final Perl5Matcher matcher = new Perl5Matcher();
    if (matcher.contains(step.getStepName(), pattern)) {
      final MatchResult match = matcher.getMatch();
      final int groupCount = match.groups();
      for (int i = 1; i < groupCount; i++) {
        final int start = match.beginOffset(i);
        final int end = match.endOffset(i);
        if (start >= 0 && end >= 0) {
          parameterRanges.add(new TextRange(start, end).shiftRight(shiftOffset));
        }
      }
    }

    int k = step.getText().indexOf(step.getStepName());
    k += step.getStepName().length();
    if (k < step.getText().length() - 1) {
      String text = step.getText().substring(k + 1);
      boolean inParam = false;
      int paramStart = 0;
      int i = 0;
      while (i < text.length()) {
        if (text.charAt(i) == '<') {
          paramStart = i;
          inParam = true;
        }

        if (text.charAt(i) == '>' && inParam) {
          parameterRanges.add(new TextRange(paramStart, i + 1).shiftRight(shiftOffset + step.getStepName().length() + 1));
          inParam = false;
        }
        i++;
      }

    }


    return parameterRanges;
  }
}
