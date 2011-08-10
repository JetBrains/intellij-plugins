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

package com.intellij.struts2.dom.struts.model;

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
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
import com.intellij.util.containers.ConcurrentFactoryMap;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.model.impl.DomModelImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

  private final StrutsModelImpl.ActionsForNamespaceCachedValuesProvider actionsCachedValueProvider =
      new ActionsForNamespaceCachedValuesProvider();

  private static final Key<CachedValue<Map<String, List<Action>>>> ACTIONS_FOR_NAMESPACE =
      Key.create("STRUTS2_ACTIONS_FOR_NAMESPACE");

  /**
   * Dummy identifier for "all namespaces".
   */
  @NonNls
  private static final String EMPTY_NAMESPACE = "STRUTS2_PLUGIN_EMPTY_NAMESPACE";


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
  public Set<InterceptorOrStackBase> getAllInterceptorsAndStacks() {
    final Set<InterceptorOrStackBase> interceptorOrStackBases = new HashSet<InterceptorOrStackBase>();
    for (final StrutsPackage strutsPackage : getStrutsPackages()) {
      final List<InterceptorStack> interceptorList = strutsPackage.getInterceptorStacks();
      interceptorOrStackBases.addAll(interceptorList);
      final List<Interceptor> interceptors = strutsPackage.getInterceptors();
      interceptorOrStackBases.addAll(interceptors);
    }
    return interceptorOrStackBases;
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

  @NotNull
  public List<Action> findActionsByClass(@NotNull final PsiClass clazz) {
    return findActionsByClassInner(clazz, false);
  }

  private List<Action> findActionsByClassInner(final PsiClass clazz,
                                               final boolean skipOnFirst) {
    final List<Action> actionResultList = new SmartList<Action>();

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

  public List<Action> getActionsForNamespace(@Nullable @NonNls final String namespace) {
    CachedValue<Map<String, List<Action>>> packageToActionMap = myMergedModel.getUserData(ACTIONS_FOR_NAMESPACE);
    if (packageToActionMap == null) {
      packageToActionMap = CachedValuesManager.getManager(myMergedModel.getManager().getProject())
                                              .createCachedValue(actionsCachedValueProvider, false);

      myMergedModel.putUserData(ACTIONS_FOR_NAMESPACE, packageToActionMap);
    }

    return packageToActionMap.getValue().get(StringUtil.notNullize(namespace, EMPTY_NAMESPACE));
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

  private class ActionsForNamespaceCachedValuesProvider implements CachedValueProvider<Map<String, List<Action>>> {
    public Result<Map<String, List<Action>>> compute() {
      final Map<String, List<Action>> map = new ConcurrentFactoryMap<String, List<Action>>() {
        @Override
        protected List<Action> create(final String namespace) {
          final List<Action> actionResultList = new SmartList<Action>();

          for (final StrutsPackage strutsPackage : getStrutsPackages()) {
            if (Comparing.equal(namespace, EMPTY_NAMESPACE) ||
                Comparing.equal(namespace, strutsPackage.searchNamespace())) {
              actionResultList.addAll(strutsPackage.getActions());
            }
          }
          return actionResultList;
        }
      };

      return Result.create(map, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
    }
  }

}