package com.jetbrains.plugins.jade.projectView;

import com.intellij.ide.projectView.ProjectViewNestingRulesProvider;
import org.jetbrains.annotations.NotNull;

public final class PugNestingRulesProvider implements ProjectViewNestingRulesProvider {
  @Override
  public void addFileNestingRules(@NotNull Consumer consumer) {
    consumer.addNestingRule(".pug", ".html");
    consumer.addNestingRule(".jade", ".html");
  }
}
