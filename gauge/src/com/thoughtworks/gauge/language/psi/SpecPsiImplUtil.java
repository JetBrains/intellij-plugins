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

package com.thoughtworks.gauge.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.thoughtworks.gauge.GaugeBootstrapService;
import com.thoughtworks.gauge.StepValue;
import com.thoughtworks.gauge.connection.GaugeConnection;
import com.thoughtworks.gauge.core.GaugeCli;
import com.thoughtworks.gauge.language.psi.impl.SpecStepImpl;
import com.thoughtworks.gauge.util.GaugeUtil;
import com.thoughtworks.gauge.util.StepUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;

public final class SpecPsiImplUtil {

  private SpecPsiImplUtil() {
  }

  public static @NotNull StepValue getStepValue(SpecStep element) {
    ASTNode step = element.getNode();
    String stepText = step.getText().trim();
    int newLineIndex = stepText.indexOf("\n");
    int endIndex = newLineIndex == -1 ? stepText.length() : newLineIndex;
    SpecTable inlineTable = element.getInlineTable();
    stepText = stepText.substring(1, endIndex).trim();
    return getStepValueFor(element, stepText, inlineTable != null);
  }

  public static @NotNull StepValue getStepValueFor(PsiElement element, String stepText, Boolean hasInlineTable) {
    Module module = GaugeUtil.moduleForPsiElement(element);
    if (module == null) return getDefaultStepValue(element);

    return getStepValueFor(module, element, stepText, hasInlineTable);
  }

  public static StepValue getStepValueFor(@NotNull Module module, PsiElement element, String stepText, Boolean hasInlineTable) {
    GaugeBootstrapService bootstrapService = GaugeBootstrapService.getInstance(module.getProject());

    GaugeCli gaugeCli = bootstrapService.getGaugeCli(module, false);
    if (gaugeCli == null) {
      return getDefaultStepValue(element);
    }
    GaugeConnection apiConnection = gaugeCli.getGaugeConnection();
    if (apiConnection == null) {
      return getDefaultStepValue(element);
    }
    StepValue value = StepUtil.getStepValue(apiConnection, stepText, hasInlineTable);
    return value == null ? getDefaultStepValue(element) : value;
  }

  private static StepValue getDefaultStepValue(PsiElement element) {
    return new StepValue(element.getText(), element.getText(), new ArrayList<>());
  }

  public static ItemPresentation getPresentation(final SpecStepImpl element) {
    return new ItemPresentation() {
      @Override
      public @Nullable String getPresentableText() {
        return element.getText();
      }

      @Override
      public String getLocationString() {
        return element.getContainingFile().getName();
      }

      @Override
      public @Nullable Icon getIcon(boolean unused) {
        return null;
      }
    };
  }
}
