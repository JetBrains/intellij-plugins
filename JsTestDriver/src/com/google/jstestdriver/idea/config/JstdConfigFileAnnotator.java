/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea.config;

import com.google.common.collect.Sets;
import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.TreeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.List;
import java.util.Set;

public class JstdConfigFileAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    YAMLFile yamlFile = CastUtils.tryCast(element, YAMLFile.class);
    if (yamlFile != null) {
      annotateFile(yamlFile, holder);
    }
  }

  public void annotateFile(@NotNull YAMLFile yamlFile, @NotNull AnnotationHolder holder) {
    List<YAMLDocument> documents = yamlFile.getDocuments();
    boolean annotated = false;
    for (YAMLDocument document : documents) {
      if (annotated) {
        holder.createErrorAnnotation(document, "JsTestDriver Configuration File must have one document");
      } else {
        annotateDocument(document, holder);
      }
      annotated = true;
    }
  }

  private void annotateDocument(YAMLDocument document, final AnnotationHolder holder) {
    final Set<String> visitedKeys = Sets.newHashSet();
    document.acceptChildren(new PsiElementVisitor() {
      @Override
      public void visitElement(PsiElement childElement) {
        YAMLKeyValue keyValue = CastUtils.tryCast(childElement, YAMLKeyValue.class);
        if (keyValue != null) {
          PsiElement keyElement = keyValue.getKey();
          String keyStr = keyElement.getText();
          if (keyStr.endsWith(":")) {
            keyStr = keyStr.substring(0, keyStr.length() - 1);
          }
          if (!JstdConfigFileUtils.VALID_TOP_LEVEL_KEYS.contains(keyStr)) {
            holder.createErrorAnnotation(keyElement, "Unexpected key '" + keyStr + "'");
          }
          if (!visitedKeys.add(keyStr)) {
            holder.createErrorAnnotation(keyElement, "Duplicated '" + keyStr + "' key");
          }
        } else {
          if (childElement instanceof TreeElement) {
            TreeElement treeElement = (TreeElement) childElement;
            if (treeElement.getElementType() != YAMLTokenTypes.EOL) {
              holder.createErrorAnnotation(childElement, "Unexpected element");
            }
          } else {
            holder.createErrorAnnotation(childElement, "Unexpected element");
          }
        }
      }
    });
  }

}
