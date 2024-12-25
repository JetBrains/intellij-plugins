/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.execution;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Function;
import com.thoughtworks.gauge.helper.ModuleHelper;
import com.thoughtworks.gauge.language.token.SpecTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

final class TestRunLineMarkerProvider extends RunLineMarkerContributor {
  private static final Function<PsiElement, String> TOOLTIP_PROVIDER = psiElement -> "Run Element";
  private final ModuleHelper helper;

  TestRunLineMarkerProvider() {
    this.helper = new ModuleHelper();
  }

  @Override
  public @Nullable Info getInfo(@NotNull PsiElement psiElement) {
    if (!this.helper.isGaugeModule(psiElement)) return null;
    List<IElementType> types = Arrays.asList(SpecTokenTypes.SPEC_HEADING, SpecTokenTypes.SCENARIO_HEADING);
    if (psiElement instanceof LeafPsiElement && types.contains(((LeafPsiElement)psiElement).getElementType())) {
      return new Info(AllIcons.RunConfigurations.TestState.Run, TOOLTIP_PROVIDER, ExecutorAction.getActions(1));
    }
    else {
      return null;
    }
  }
}
