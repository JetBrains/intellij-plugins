// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.config;

import com.intellij.internal.statistic.beans.MetricEvent;
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
import java.util.Objects;
import java.util.Set;

public class TslintOptionsUsageCollector extends ProjectUsagesCollector {
  @NotNull
  @Override
  public Set<MetricEvent> getMetrics(@NotNull Project project) {
    ExtendedLinterState<TsLintState> extendedState = TsLintConfiguration.getInstance(project).getExtendedState();
    if (!extendedState.isEnabled()) {
      return Collections.emptySet();
    }
    Set<MetricEvent> set = new HashSet<>();
    set.add(new MetricEvent("enabled"));

    TsLintState state = extendedState.getState();
    if (!Objects.equals(state.getInterpreterRef(), TsLintState.DEFAULT.getInterpreterRef())) {
      set.add(new MetricEvent("node.interpreter.custom"));
    }

    NodePackageRef nodePackageRef = state.getNodePackageRef();
    if (nodePackageRef == AutodetectLinterPackage.INSTANCE) {
      set.add(new MetricEvent("node.package.autodetect"));
    }
    NodePackage constantPackage = nodePackageRef.getConstantPackage();
    if (constantPackage != null && !constantPackage.isEmptyPath()) {
      set.add(new MetricEvent("node.package.custom.package"));
    }

    if (StringUtil.isNotEmpty(state.getRulesDirectory())) {
      set.add(new MetricEvent("additional.rules.specified"));
    }
    if (state.isCustomConfigFileUsed() && StringUtil.isNotEmpty(state.getCustomConfigFilePath())) {
      set.add(new MetricEvent("custom.config.specified"));
    }
    return set;
  }

  @NotNull
  @Override
  public String getGroupId() {
    return "js.tslint.options";
  }
}
