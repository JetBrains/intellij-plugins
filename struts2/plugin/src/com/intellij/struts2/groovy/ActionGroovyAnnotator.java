/*
 * Copyright 2015 The authors
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
package com.intellij.struts2.groovy;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.struts2.Struts2Icons;
import com.intellij.struts2.annotators.ActionAnnotatorBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrClassDefinition;

import javax.swing.*;

/**
 * Annotator for Groovy-Action-classes.
 *
 * @author Yann C&eacute;bron
 */
final class ActionGroovyAnnotator extends ActionAnnotatorBase {
  @Override
  public String getId() {
    return "ActionGroovyAnnotator";
  }

  @Override
  public String getName() {
    return "Action (Groovy)";
  }

  @Override
  public @NotNull Icon getIcon() {
    return Struts2Icons.Action;
  }

  @Override
  protected PsiClass getActionPsiClass(@NotNull final PsiElement psiElement) {
    if (!(psiElement instanceof GrClassDefinition)) {
      return null;
    }

    return (GrClassDefinition)psiElement;
  }
}