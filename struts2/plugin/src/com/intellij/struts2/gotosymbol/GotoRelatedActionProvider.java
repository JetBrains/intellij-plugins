/*
 * Copyright 2013 The authors
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
package com.intellij.struts2.gotosymbol;

import com.intellij.codeInsight.navigation.DomGotoRelatedItem;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.navigation.GotoRelatedProvider;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.util.containers.CollectionFactory;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Provides "Go to related Action/Action-method" for (web)-results.
 *
 * @author Yann C&eacute;bron
 */
final class GotoRelatedActionProvider extends GotoRelatedProvider {
  // TODO restrict to "realistic" results
  private static final Set<String> SUPPORTED_EXTENSIONS = CollectionFactory.createFilePathSet(Arrays.asList("ftl", "htm", "html", "jsp", "jspx", "txt", "vm"));

  @NotNull
  @Override
  public List<? extends GotoRelatedItem> getItems(@NotNull final PsiElement psiElement) {
    PsiFile psiFile = psiElement.getContainingFile();
    if (psiFile == null) return Collections.emptyList();

    final PsiFile containingFile = psiFile.getOriginalFile();
    final String filename = containingFile.getName();

    final String extension = FileUtilRt.getExtension(filename);
    if (!SUPPORTED_EXTENSIONS.contains(extension)) {
      return Collections.emptyList();
    }

    final StrutsManager strutsManager = StrutsManager.getInstance(psiElement.getProject());
    final StrutsModel strutsModel = strutsManager.getCombinedModel(psiElement);
    if (strutsModel == null) {
      return Collections.emptyList();
    }

    final List<PsiFile> allFiles = containingFile.getViewProvider().getAllFiles();

    final Set<Action> actions = new HashSet<>();
    final List<GotoRelatedItem> items = new ArrayList<>();
    strutsModel.processActions(action -> {
      for (final Result result : action.getResults()) {

        final PathReference pathReference = result.getValue();
        if (pathReference == null) {
          continue;
        }

        final String path = pathReference.getPath();
        if (!path.endsWith(filename)) {
          continue;
        }

        final PsiElement resolve = pathReference.resolve();
        if (ContainerUtil.find(allFiles, resolve) == null) {
          continue;
        }

        if (!actions.contains(action)) {
          items.add(new DomGotoRelatedItem(action));
          actions.add(action);
        }

        final PsiClass actionClass = action.searchActionClass();
        if (actionClass == null) {
          continue;
        }

        final PsiMethod actionMethod = action.searchActionMethod();
        items.add(new GotoRelatedItem(actionMethod != null ? actionMethod : actionClass));
      }

      return true;
    });

    return items;
  }

}