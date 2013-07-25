/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;

public class CfmlRecursiveElementVisitor extends PsiElementVisitor {

  public static class Stop extends RuntimeException {
    public static final Stop DONE = new Stop();

    public Throwable fillInStackTrace() {
      return this;
    }
  }

  public void visitElement(final PsiElement element) {
    element.acceptChildren(this);
  }

  public void visitCfmlFunction(CfmlFunction function) {
    visitElement(function);
  }

  public void visitCfmlComponent(CfmlComponent component) {
    visitElement(component);
  }

  public void visitCfmlTag(CfmlTag tag) {
    visitElement(tag);
  }

  @Override
  public void visitFile(PsiFile file) {
    if (file instanceof CfmlFile) {
      file.acceptChildren(this);
    }
  }
}
