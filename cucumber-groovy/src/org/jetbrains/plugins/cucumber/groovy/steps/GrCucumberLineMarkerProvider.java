package org.jetbrains.plugins.cucumber.groovy.steps;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import icons.CucumberIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

import java.util.Collection;
import java.util.List;

/**
 * @author Max Medvedev
 */
public class GrCucumberLineMarkerProvider implements LineMarkerProvider {

  private static final Function<GrMethodCall, String> TOOLTIP_PROVIDER = new Function<GrMethodCall, String>() {
    @Override
    public String fun(GrMethodCall stepDef) {
      return stepDef.getPresentation().getPresentableText();
    }
  };

  @Nullable
  @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
    if (GrCucumberUtil.isStepDefinition(element)) {
      return new LineMarkerInfo<GrMethodCall>(
        ((GrMethodCall)element),
        element.getTextRange().getStartOffset(),
        CucumberIcons.Cucumber,
        Pass.UPDATE_ALL,
        TOOLTIP_PROVIDER,
        null
      );
    }
    else {
      return null;
    }
  }

  @Override
  public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
  }
}
