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

package com.thoughtworks.gauge.inspection;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class GaugeInspectionProvider extends LocalInspectionTool {
  @Override
  public ProblemDescriptor @Nullable [] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
    if (isOnTheFly) return ProblemDescriptor.EMPTY_ARRAY;
    File dir = GaugeUtil.moduleDir(GaugeUtil.moduleForPsiElement(file));
    if (dir == null) return ProblemDescriptor.EMPTY_ARRAY;
    return getDescriptors(GaugeErrors.get(dir.getAbsolutePath()), manager, file);
  }

  private ProblemDescriptor[] getDescriptors(List<GaugeError> errors, InspectionManager manager, PsiFile file) {
    List<ProblemDescriptor> descriptors = new ArrayList<>();
    for (GaugeError e : errors) {
      if (!e.isFrom(file.getVirtualFile().getPath())) continue;
      PsiElement element = getElement(file.findElementAt(e.getOffset(file.getText())));
      if (element == null) continue;
      descriptors.add(manager.createProblemDescriptor(element, e.getMessage(), null, ProblemHighlightType.ERROR, false, false));
    }
    return descriptors.toArray(ProblemDescriptor.EMPTY_ARRAY);
  }

  abstract PsiElement getElement(PsiElement element);
}

