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

package com.intellij.struts2.dom.struts.model;

import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.Function;
import com.intellij.util.NotNullFunction;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.model.impl.DomModelImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Yann C&eacute;bron
 */
class StrutsModelImpl extends DomModelImpl<StrutsRoot> implements StrutsModel {

  private static final NotNullFunction<DomFileElement<StrutsRoot>, StrutsRoot> ROOT_ELEMENT_MAPPER =
          new NotNullFunction<DomFileElement<StrutsRoot>, StrutsRoot>() {
            @NotNull
            public StrutsRoot fun(final DomFileElement<StrutsRoot> strutsRootDomFileElement) {
              return strutsRootDomFileElement.getRootElement();
            }
          };

  private static final Function<StrutsRoot, Collection<? extends StrutsPackage>> STRUTS_PACKAGE_COLLECTOR =
          new Function<StrutsRoot, Collection<? extends StrutsPackage>>() {
            public Collection<? extends StrutsPackage> fun(final StrutsRoot strutsRoot) {
              return strutsRoot.getPackages();
            }
          };

  StrutsModelImpl(@NotNull final DomFileElement<StrutsRoot> strutsRootDomFileElement,
                  @NotNull final Set<XmlFile> xmlFiles) {
    super(strutsRootDomFileElement, xmlFiles);
  }

  @NotNull
  public List<StrutsRoot> getMergedStrutsRoots() {
    return ContainerUtil.map(getRoots(), ROOT_ELEMENT_MAPPER);
  }

  @NotNull
  public List<StrutsPackage> getStrutsPackages() {
    return ContainerUtil.concat(getMergedStrutsRoots(), STRUTS_PACKAGE_COLLECTOR);
  }

  @NotNull
  public List<Action> findActionsByName(@NotNull @NonNls final String name,
                                        @Nullable @NonNls final String namespace) {
    return ContainerUtil.findAll(getActionsForNamespace(namespace), new Condition<Action>() {
      public boolean value(final Action action) {
        return action.matchesPath(name);
      }
    });
  }

  // TODO performance, use caching?!
  @NotNull
  public List<Action> findActionsByClass(@NotNull final PsiClass clazz) {
    final List<Action> actionResultList = new ArrayList<Action>();

    for (final StrutsPackage strutsPackage : getStrutsPackages()) {
      for (final Action action : strutsPackage.getActions()) {
        final PsiClass actionClassValue = action.getActionClass().getValue();
        if (actionClassValue != null &&
            clazz.equals(actionClassValue)) {
          actionResultList.add(action);
        }
      }
    }

    return actionResultList;
  }

  public List<Action> getActionsForNamespace(@Nullable @NonNls final String namespace) {
    final List<Action> actionResultList = new ArrayList<Action>();

    for (final StrutsPackage strutsPackage : getStrutsPackages()) {
      if (namespace == null ||
          namespace.equals(strutsPackage.searchNamespace())) {
        actionResultList.addAll(strutsPackage.getActions());
      }
    }

    return actionResultList;
  }

  public boolean processActions(final Processor<Action> processor) {
    for (final StrutsPackage strutsPackage : getStrutsPackages()) {
      for (final Action action : strutsPackage.getActions()) {
        if (!processor.process(action)) {
          return false;
        }
      }
    }
    return true;
  }

}