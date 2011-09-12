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

import com.google.jstestdriver.idea.util.JsPsiUtils;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequence;

import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.TreeElement;

public class JstdConfigFileGotoFileHandler implements GotoDeclarationHandler {

  @Override
  public PsiElement[] getGotoDeclarationTargets(PsiElement sourceElement) {
    if (sourceElement != null && JsPsiUtils.isElementOfType(sourceElement, YAMLTokenTypes.TEXT)) {
      YAMLDocument document = JstdConfigFileUtils.getVerifiedHierarchyHead(sourceElement.getParent(),
          new Class[] {
              YAMLSequence.class,
              YAMLCompoundValue.class,
              YAMLKeyValue.class
          },
          YAMLDocument.class
      );
      if (document != null) {
        VirtualFile basePath = JstdConfigFileUtils.extractBasePath(document);
        if (basePath != null) {
          String relativePath = sourceElement.getText();
          VirtualFile gotoVFile = basePath.findFileByRelativePath(relativePath);
          if (gotoVFile != null) {
            PsiElement psiElement = PsiManager.getInstance(sourceElement.getProject()).findFile(gotoVFile);
            if (psiElement != null) {
              return new PsiElement[] { psiElement };
            }
          }
        }
      }
    }
    return PsiElement.EMPTY_ARRAY;
  }

}
