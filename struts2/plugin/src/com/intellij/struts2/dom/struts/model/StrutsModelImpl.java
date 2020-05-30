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

package com.intellij.struts2.dom.struts.model;

import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.strutspackage.Interceptor;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorOrStackBase;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorStack;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.Function;
import com.intellij.util.NotNullFunction;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.model.impl.DomModelImpl;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 */
class StrutsModelImpl extends DomModelImpl<StrutsRoot> implements StrutsModel {

  private static final NotNullFunction<DomFileElement<StrutsRoot>, StrutsRoot> ROOT_ELEMENT_MAPPER =
    strutsRootDomFileElement -> strutsRootDomFileElement.getRootElement();

  private static final Function<StrutsRoot, Collection<? extends StrutsPackage>> STRUTS_PACKAGE_COLLECTOR =
    strutsRoot -> strutsRoot.getPackages();

  StrutsModelImpl(@NotNull final DomFileElement<StrutsRoot> strutsRootDomFileElement,
                  @NotNull final Set<XmlFile> xmlFiles) {
    super(strutsRootDomFileElement, xmlFiles);
  }

  @Override
  @NotNull
  public List<StrutsRoot> getMergedStrutsRoots() {
    return ContainerUtil.map(getRoots(), ROOT_ELEMENT_MAPPER);
  }

  @Override
  @NotNull
  public List<StrutsPackage> getStrutsPackages() {
    return ContainerUtil.concat(getMergedStrutsRoots(), STRUTS_PACKAGE_COLLECTOR);
  }

  @Override
  @NotNull
  public Set<InterceptorOrStackBase> getAllInterceptorsAndStacks() {
    final Set<InterceptorOrStackBase> interceptorOrStackBases = new HashSet<>();
    for (final StrutsPackage strutsPackage : getStrutsPackages()) {
      final List<InterceptorStack> interceptorList = strutsPackage.getInterceptorStacks();
      interceptorOrStackBases.addAll(interceptorList);
      final List<Interceptor> interceptors = strutsPackage.getInterceptors();
      interceptorOrStackBases.addAll(interceptors);
    }
    return interceptorOrStackBases;
  }

  @Override
  @NotNull
  public List<Action> findActionsByName(@NotNull @NonNls final String name,
                                        @Nullable @NonNls final String namespace) {
    return ContainerUtil.findAll(getActionsForNamespace(namespace), action -> action.matchesPath(name));
  }

  @Override
  @NotNull
  public List<Action> findActionsByClass(@NotNull final PsiClass clazz) {
    return findActionsByClassInner(clazz, false);
  }

  private List<Action> findActionsByClassInner(final PsiClass clazz,
                                               final boolean skipOnFirst) {
    final List<Action> actionResultList = new SmartList<>();

    for (final StrutsPackage strutsPackage : getStrutsPackages()) {
      for (final Action action : strutsPackage.getActions()) {
        final PsiClass actionClassValue = action.searchActionClass();
        if (Comparing.equal(clazz, actionClassValue)) {
          actionResultList.add(action);
          if (skipOnFirst) {
            return actionResultList;
          }
        }
      }
    }

    return actionResultList;
  }

  @Override
  public boolean isActionClass(@NotNull final PsiClass clazz) {
    return !findActionsByClassInner(clazz, true).isEmpty();
  }

  @Override
  public List<Action> getActionsForNamespace(@Nullable @NonNls final String namespace) {
    final List<Action> actionResultList = new SmartList<>();

    for (final StrutsPackage strutsPackage : getStrutsPackages()) {
      if (namespace == null ||
          Objects.equals(namespace, strutsPackage.searchNamespace())) {
        actionResultList.addAll(strutsPackage.getActions());
      }
    }
    return actionResultList;
  }

  @Override
  public boolean processActions(final Processor<Action> processor) {
    for (final StrutsPackage strutsPackage : getStrutsPackages()) {
      final List<Action> actions = strutsPackage.getActions();
      if (!ContainerUtil.process(actions, processor)) return false;
    }
    return true;
  }
}
