/*
 * Copyright 2011 The authors
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
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Provides "Go to related Action/Action-method" for (web)-results.
 *
 * @author Yann C&eacute;bron
 */
public class GotoRelatedActionProvider extends GotoRelatedProvider {

  // TODO restrict to "realistic" results
  private static final String[] SUPPORTED_EXTENSIONS = new String[]{
    "ftl", "htm", "html", "jsp", "jspx", "txt", "vm"
  };

  @NotNull
  @Override
  public List<? extends GotoRelatedItem> getItems(@NotNull final PsiElement psiElement) {
    final PsiFile containingFile = psiElement.getContainingFile().getOriginalFile();
    final String filename = containingFile.getName();

    final String extension = FileUtil.getExtension(filename);
    if (Arrays.binarySearch(SUPPORTED_EXTENSIONS, extension) < 0) {
      return Collections.emptyList();
    }

    final StrutsManager strutsManager = StrutsManager.getInstance(psiElement.getProject());
    final StrutsModel strutsModel = strutsManager.getCombinedModel(ModuleUtil.findModuleForPsiElement(psiElement));
    if (strutsModel == null) {
      return Collections.emptyList();
    }

    final Set<Action> actions = new HashSet<Action>();
    final List<GotoRelatedItem> items = new ArrayList<GotoRelatedItem>();
    strutsModel.processActions(new Processor<Action>() {
      @Override
      public boolean process(final Action action) {
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
          final List<PsiFile> allFiles = containingFile.getViewProvider().getAllFiles();
          if (ContainerUtil.find(allFiles, resolve) == null) {
            continue;
          }

          if (!actions.contains(action)) {
            items.add(new DomGotoRelatedItem(action));
            actions.add(action);
          }

          if (action.getActionClass() != null) {
            final PsiClass actionClass = action.getActionClass().getValue();
            if (actionClass != null) {
              final PsiMethod actionMethod = action.searchActionMethod();
              items.add(new GotoRelatedItem(actionMethod != null ? actionMethod : actionClass));
            }
          }
        }

        return true;
      }
    });

    return items;
  }

}