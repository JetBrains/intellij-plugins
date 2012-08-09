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
package com.jetbrains.flask.codeInsight;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.DefaultGutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.util.ConstantFunction;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author yole
 */
public class FlaskLineMarkerProvider extends RelatedItemLineMarkerProvider {
  @Override
  protected void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
    if (element instanceof PyFunction) {
      RelatedItemLineMarkerInfo<PyFunction> info = createTemplateLineMarker((PyFunction)element);
      if (info != null) {
        result.add(info);
      }
    }
  }

  @Nullable
  private static RelatedItemLineMarkerInfo<PyFunction> createTemplateLineMarker(PyFunction function) {
    if (function.getText().contains(FlaskNames.RENDER_TEMPLATE)) {
      List<PsiFile> referencedFiles = new ArrayList<PsiFile>();
      List<PyStringLiteralExpression> templateReferences = FlaskTemplateManager.collectTemplateReferences(function);
      for (PyStringLiteralExpression literal : templateReferences) {
        for (PsiReference reference : literal.getReferences()) {
          if (reference instanceof FileReference) {
            PsiElement result = reference.resolve();
            if (result instanceof PsiFile) {
              referencedFiles.add((PsiFile) result);
            }
          }
        }
      }
      if (!referencedFiles.isEmpty()) {
        return createTemplateNavigationLineMarker(function, referencedFiles);
      }
    }
    return null;
  }

  private static RelatedItemLineMarkerInfo<PyFunction> createTemplateNavigationLineMarker(PyFunction function, List<PsiFile> templates) {
    PsiFile template = templates.iterator().next();
    String templateName = template.getName();
    final String msg = templates.size() == 1 ? "Go to template '" + templateName + "'" : "Goto templates";
    return new RelatedItemLineMarkerInfo<PyFunction>(function, function.getTextRange(),
                                                     AllIcons.FileTypes.Html, Pass.UPDATE_OVERRIDEN_MARKERS,
                                                     new ConstantFunction<PyFunction, String>(msg),
                                                     new DefaultGutterIconNavigationHandler<PyFunction>(templates, msg),
                                                     GutterIconRenderer.Alignment.RIGHT,
                                                     GotoRelatedItem.createItems(templates, "Templates"));
  }
}
