// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.tslint.config;

import com.intellij.internal.statistic.beans.MetricEvent;
import com.intellij.internal.statistic.eventLog.EventLogGroup;
import com.intellij.internal.statistic.eventLog.events.EventId;
import com.intellij.internal.statistic.service.fus.collectors.ProjectUsagesCollector;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.linter.AutodetectLinterPackage;
import com.intellij.lang.javascript.linter.ExtendedLinterState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TslintOptionsUsageCollector extends ProjectUsagesCollector {
  private static final EventLogGroup GROUP = new EventLogGroup("js.tslint.options", 3);
  private static final EventId ENABLED = GROUP.registerEvent("enabled");
  private static final EventId NODE_PACKAGE_AUTODETECT = GROUP.registerEvent("node.package.autodetect");
  private static final EventId NODE_PACKAGE_CUSTOM_PACKAGE = GROUP.registerEvent("node.package.custom.package");
  private static final EventId ADDITIONAL_RULES_SPECIFIED = GROUP.registerEvent("additional.rules.specified");
  private static final EventId CUSTOM_CONFIG_SPECIFIED = GROUP.registerEvent("custom.config.specified");

  @Override
  public @NotNull Set<MetricEvent> getMetrics(@NotNull Project project) {
    ExtendedLinterState<TsLintState> extendedState = TsLintConfiguration.getInstance(project).getExtendedState();
    if (!extendedState.isEnabled()) {
      return Collections.emptySet();
    }
    Set<MetricEvent> set = new HashSet<>();
    set.add(ENABLED.metric());

    TsLintState state = extendedState.getState();

    NodePackageRef nodePackageRef = state.getNodePackageRef();
    if (nodePackageRef == AutodetectLinterPackage.INSTANCE) {
      set.add(NODE_PACKAGE_AUTODETECT.metric());
    }
    NodePackage constantPackage = nodePackageRef.getConstantPackage();
    if (constantPackage != null && !constantPackage.isEmptyPath()) {
      set.add(NODE_PACKAGE_CUSTOM_PACKAGE.metric());
    }

    if (StringUtil.isNotEmpty(state.getRulesDirectory())) {
      set.add(ADDITIONAL_RULES_SPECIFIED.metric());
    }
    if (state.isCustomConfigFileUsed() && StringUtil.isNotEmpty(state.getCustomConfigFilePath())) {
      set.add(CUSTOM_CONFIG_SPECIFIED.metric());
    }
    return set;
  }

  @Override
  public EventLogGroup getGroup() {
    return GROUP;
  }
}
