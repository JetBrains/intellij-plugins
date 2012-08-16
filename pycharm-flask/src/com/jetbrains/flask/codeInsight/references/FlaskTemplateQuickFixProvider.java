/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package com.jetbrains.flask.codeInsight.references;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.Consumer;
import com.jetbrains.flask.codeInsight.FlaskTemplateManager;
import com.jetbrains.python.inspections.PyUnresolvedReferenceQuickFixProvider;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.templateLanguages.ConfigureTemplateDirectoriesAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
 */
public class FlaskTemplateQuickFixProvider implements PyUnresolvedReferenceQuickFixProvider {
  @Override
  public void registerQuickFixes(PsiReference reference, Consumer<LocalQuickFix> fixConsumer) {
    if (reference instanceof FileReference && FlaskTemplateManager.getTemplatesDirectory(reference.getElement()) != null) {
      FileReference fileReference = (FileReference)reference;
      FileReferenceSet refSet = fileReference.getFileReferenceSet();

      PsiElement element = reference.getElement();
      if (fileReference.getIndex() == refSet.getAllReferences().length-1 &&
          element instanceof PyStringLiteralExpression &&
          FlaskTemplateManager.isTemplateReference((PyStringLiteralExpression)element)) {
        PyStringLiteralExpression literal = (PyStringLiteralExpression)element;
        fixConsumer.consume(new CreateTemplateIntentionAction(literal.getStringValue()));
        fixConsumer.consume(new ConfigureTemplateDirectoriesAction());
      }
    }
  }

  private static class CreateTemplateIntentionAction implements LocalQuickFix {
    private final String myTemplateName;

    public CreateTemplateIntentionAction(String templateName) {
      myTemplateName = templateName;
    }

    @NotNull
    @Override
    public String getName() {
      return "Create template '" + myTemplateName + "'";
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return "Create Template";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      PsiDirectory templatesDir = FlaskTemplateManager.getTemplatesDirectory(descriptor.getPsiElement());
      if (templatesDir == null) return;
      PsiFile file = templatesDir.createFile(myTemplateName);
      file.navigate(true);
    }
  }
}
