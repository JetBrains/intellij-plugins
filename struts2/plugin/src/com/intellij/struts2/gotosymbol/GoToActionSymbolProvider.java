/*
 * Copyright 2007 The authors
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

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Go to {@link Action} by name (CTRL+ALT+SHIFT+N).
 *
 * @author Yann CŽbron
 */
public class GoToActionSymbolProvider extends GoToSymbolProvider {

  protected void getNames(@NotNull final Module module, final Set<String> result) {
    final StrutsModel strutsModel = StrutsManager.getInstance(module.getProject()).getCombinedModel(module);
    if (strutsModel != null) {
      final List<StrutsPackage> strutsPackageList = strutsModel.getStrutsPackages();
      for (final StrutsPackage strutsPackage : strutsPackageList) {
        for (final Action action : strutsPackage.getActions()) {
          result.add(action.getName().getStringValue());
        }
      }
    }
  }

  protected void getItems(@NotNull final Module module, final String name, final List<NavigationItem> result) {
    final StrutsModel strutsModel = StrutsManager.getInstance(module.getProject()).getCombinedModel(module);
    if (strutsModel != null) {
      final List<Action> actions = strutsModel.findActionsByName(name, null);
      for (final Action action : actions) {
        final NavigationItem item = createNavigationItem(action, "[" + action.getNamespace() + "]");
        ContainerUtil.addIfNotNull(item, result);
      }
    }
  }

  public NavigationItem[] getItemsByName(final String name, final String pattern, final Project project,
                                         final boolean includeNonProjectItems) {
    return new NavigationItem[0];
  }
}