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
package com.intellij.coldFusion.mxunit;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.coldFusion.model.psi.impl.CfmlFunctionImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlTagFunctionImpl;
import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testIntegration.TestLocationProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CfmlUnitQualifiedNameLocationProvider implements TestLocationProvider {
  @NonNls private static final String PROTOCOL_ID = "php_qn";

  @NotNull
  public List<Location> getLocation(@NotNull String protocolId, @NotNull String locationData, Project project) {
    if (PROTOCOL_ID.equals(protocolId)) {
      PsiElement element = findElement(locationData, project);
      if (element != null) {
        return Collections.<Location>singletonList(new PsiLocation<PsiElement>(project, element));
      }
    }

    return Collections.emptyList();
  }

  @Nullable
  private PsiElement findElement(String link, Project project) {
    String[] location = link.split("::");

    int tokensNumber = location.length;
    if (tokensNumber <= 0 || tokensNumber > 3) {
      return null;
    }
    PsiElement result;
    String filePath = location[0];
    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
    if (virtualFile != null) {
      result = PsiManager.getInstance(project).findFile(virtualFile);
      if (result == null || !(result instanceof CfmlFile)) {
        return null;
      }
      // TODO: to move to index
      final CfmlTag[] tags = PsiTreeUtil.getChildrenOfType(result, CfmlTag.class);
      if (tags == null || tags.length == 0) {
        return result;
      }
      for (CfmlTag tag : tags) {
        if ("cfcomponent".equals(tag.getTagName().toLowerCase())) {
          result = tag;
          break;
        }
      }
    }
    else {
      return null;
    }
    if (tokensNumber > 1) {
      String functionName = location[1];
      final CfmlTagFunctionImpl[] functions = PsiTreeUtil.getChildrenOfType(result, CfmlTagFunctionImpl.class);
      if (functions != null) {
        for (CfmlTagFunctionImpl function : functions) {
          if (functionName.equals(function.getName())) {
            result = function.getNavigationElement();
            break;
          }
        }
      }
      final CfmlFunctionImpl[] scriptFunctions = PsiTreeUtil.getChildrenOfType(result, CfmlFunctionImpl.class);
      if (scriptFunctions != null) {
        for (CfmlFunctionImpl function : scriptFunctions) {
          if (functionName.equals(function.getName())) {
            result = function.getNavigationElement();
            break;
          }
        }
      }
    }
    return result;
  }
}
