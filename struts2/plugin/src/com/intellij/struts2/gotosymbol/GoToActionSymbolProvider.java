/*
 * Copyright 2010 The authors
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
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.util.Processor;
import com.intellij.util.xml.model.gotosymbol.GoToSymbolProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Go to {@link Action} by name (CTRL+ALT+SHIFT+N).
 *
 * @author Yann C&eacute;bron
 */
public class GoToActionSymbolProvider extends GoToSymbolProvider {

  protected boolean acceptModule(final Module module) {
    return StrutsFacet.getInstance(module) != null;
  }

  protected void addNames(@NotNull final Module module, final Set<String> result) {
    final StrutsModel strutsModel = StrutsManager.getInstance(module.getProject()).getCombinedModel(module);
    if (strutsModel == null) {
      return;
    }

    strutsModel.processActions(new Processor<Action>() {
      public boolean process(final Action action) {
        result.add(action.getName().getStringValue());
        return true;
      }
    });
  }

  protected void addItems(@NotNull final Module module, final String name, final List<NavigationItem> result) {
    final StrutsModel strutsModel = StrutsManager.getInstance(module.getProject()).getCombinedModel(module);
    if (strutsModel == null) {
      return;
    }

    final List<Action> actions = strutsModel.findActionsByName(name, null);
    for (final Action action : actions) {
      final NavigationItem item = createNavigationItem(action.getXmlTag(),
                                                       action.getName().getStringValue() +
                                                       " [" + action.getNamespace() + "]",
                                                       StrutsIcons.ACTION);
      result.add(item);
    }
  }

}