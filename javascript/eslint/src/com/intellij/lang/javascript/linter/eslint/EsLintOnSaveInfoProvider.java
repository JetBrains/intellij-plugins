package com.intellij.lang.javascript.linter.eslint;

import com.intellij.ide.actionsOnSave.ActionOnSaveContext;
import com.intellij.ide.actionsOnSave.ActionOnSaveInfo;
import com.intellij.ide.actionsOnSave.ActionOnSaveInfoProvider;
import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EsLintOnSaveInfoProvider extends ActionOnSaveInfoProvider {
  @Override
  protected @NotNull Collection<? extends ActionOnSaveInfo> getActionOnSaveInfos(@NotNull ActionOnSaveContext context) {
    return Collections.singletonList((new EslintConfigurable.EsLintOnSaveActionInfo(context)));
  }

  @Override
  public Collection<String> getSearchableOptions() {
    return List.of(EslintBundle.message("eslint.run.on.save.checkbox.on.actions.on.save.page"));
  }
}
