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

package com.thoughtworks.gauge.language;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElementVisitor;
import com.thoughtworks.gauge.language.psi.SpecVisitor;
import org.jetbrains.annotations.NotNull;

public final class SpecFile extends PsiFileBase {
  public SpecFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, Specification.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return SpecFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "Specification File";
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SpecVisitor) {
      ((SpecVisitor)visitor).visitSpecFile(this);
      return;
    }

    visitor.visitFile(this);
  }
}
