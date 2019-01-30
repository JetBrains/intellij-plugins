// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.config;

import com.intellij.internal.statistic.beans.UsageDescriptor;
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
  @NotNull
  @Override
  public Set<UsageDescriptor> getUsages(@NotNull Project project) {
    ExtendedLinterState<TsLintState> extendedState = TsLintConfiguration.getInstance(project).getExtendedState();
    if (!extendedState.isEnabled()) {
      return Collections.emptySet();
    }
    Set<UsageDescriptor> set = new HashSet<>();
    set.add(new UsageDescriptor("enabled"));

    TsLintState state = extendedState.getState();
    if (state.getInterpreterRef() != TsLintState.DEFAULT.getInterpreterRef()) {
      set.add(new UsageDescriptor("node.interpreter.custom"));
    }

    NodePackageRef nodePackageRef = state.getNodePackageRef();
    if (nodePackageRef == AutodetectLinterPackage.INSTANCE) {
      set.add(new UsageDescriptor("node.package.autodetect"));
    }
    NodePackage constantPackage = nodePackageRef.getConstantPackage();
    if (constantPackage != null && !constantPackage.isEmptyPath()) {
      set.add(new UsageDescriptor("node.package.custom.package"));
    }

    if (StringUtil.isNotEmpty(state.getRulesDirectory())) {
      set.add(new UsageDescriptor("additional.rules.specified"));
    }
    if (state.isCustomConfigFileUsed() && StringUtil.isNotEmpty(state.getCustomConfigFilePath())) {
      set.add(new UsageDescriptor("custom.config.specified"));
    }
    return set;
  }

  @NotNull
  @Override
  public String getGroupId() {
    return "js.tslint.options";
  }
}
