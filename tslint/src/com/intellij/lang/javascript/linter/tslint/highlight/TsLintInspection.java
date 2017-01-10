package com.intellij.lang.javascript.linter.tslint.highlight;

import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.linter.JSLinterInspection;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Irina.Chernushina on 6/3/2015.
 */
public final class TsLintInspection extends JSLinterInspection {
  @NotNull
  @Override
  protected TsLintExternalAnnotator getExternalAnnotatorForBatchInspection() {
    return TsLintExternalAnnotator.getInstanceForBatchInspection();
  }

  public static HighlightDisplayKey getHighlightDisplayKey() {
    return JSLinterInspection.getHighlightDisplayKeyByClass(TsLintInspection.class);
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "TSLint";
  }

  @NotNull
  @Override
  public String getGroupDisplayName() {
    return JSBundle.message("typescript.inspection.group.name");
  }

  @NotNull
  @Override
  protected List<String> getSettingsPath() {
    return ContainerUtil.newArrayList(
      JSBundle.message("typescript.compiler.configurable.name"),
      getDisplayName()
    );
  }
}
