/*
 * Copyright 2016 The authors
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
package com.intellij.struts2.spring;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.spring.SpringManager;
import com.intellij.spring.contexts.model.SpringModel;
import com.intellij.spring.model.SpringBeanPointer;
import com.intellij.spring.model.SpringModelSearchParameters;
import com.intellij.spring.model.converters.SpringConverterUtil;
import com.intellij.spring.model.utils.SpringCommonUtils;
import com.intellij.spring.model.utils.SpringModelSearchers;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.dom.ExtendableClassConverter;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomJavaUtil;
import com.intellij.util.xml.ExtendClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Extends "class" resolving to Spring beans.
 *
 * @author Yann C&eacute;bron
 */
final class ExtendableClassConverterSpringContributor
  extends ExtendableClassConverter.ExtendableClassConverterContributor {

  @Override
  @NotNull
  public String getTypeName() {
    return StrutsBundle.message("dom.extendable.class.converter.type.spring");
  }

  /**
   * Checks if struts2-spring-plugin is present in current module.
   *
   * @param convertContext Current context.
   * @return true if yes.
   */
  @Override
  public boolean isSuitable(@NotNull final ConvertContext convertContext) {
    if (!SpringCommonUtils.isSpringConfigured(convertContext.getModule())) return false;

    return DomJavaUtil.findClass(StrutsConstants.SPRING_OBJECT_FACTORY_CLASS, convertContext.getInvocationElement()) != null;
  }

  @Override
  public PsiReference @NotNull [] getReferences(@NotNull final ConvertContext convertContext,
                                                @NotNull final PsiElement psiElement,
                                                @NotNull final ExtendClass extendClass) {
    return new PsiReference[]{new SpringBeanReference((XmlAttributeValue)psiElement,
                                                      convertContext.getModule(),
                                                      extendClass)};
  }


  // TODO provide QuickFix to create Spring bean?
  private static final class SpringBeanReference extends PsiReferenceBase<XmlAttributeValue> {

    private final Module module;
    private final ExtendClass extendClass;

    private SpringBeanReference(final XmlAttributeValue element,
                                final Module module,
                                final ExtendClass extendClass) {
      super(element, true);
      this.module = module;
      this.extendClass = extendClass;
    }

    @NotNull
    private SpringModel getSpringModel() {
      return SpringManager.getInstance(module.getProject()).getCombinedModel(module);
    }

    @Override
    public PsiElement resolve() {
      final String beanName = myElement.getValue();
      if (StringUtil.isEmpty(beanName)) {
        return null;
      }

      final SpringModel springModel = getSpringModel();
      final SpringBeanPointer<?>  springBean = SpringModelSearchers.findBean(springModel, beanName);
      if (springBean == null) {
        return null;
      }

      if (springBean.isAbstract()) {
        return null;
      }

      return springBean.getBeanClass();
    }

    @Override
    public Object @NotNull [] getVariants() {
      final SpringModel springModel = getSpringModel();

      final @Nullable Set<PsiClass> subClasses = getPossibleSubClasses();

      final Collection<SpringBeanPointer<?>> list = new ArrayList<>();
      if (subClasses.size() > 0) {
        for (PsiClass subClass : subClasses) {
          list.addAll(SpringModelSearchers.findBeans(springModel, SpringModelSearchParameters.byClass(subClass).withInheritors()));
        }
      }
      else {
        list.addAll(springModel.getAllCommonBeans());
      }

      final List<LookupElement> variants = new ArrayList<>(list.size());
      for (final SpringBeanPointer<?>  bean : list) {
        if (bean.isAbstract()) {
          continue;
        }

        ContainerUtil.addIfNotNull(variants, SpringConverterUtil.createCompletionVariant(bean));
      }

      return variants.toArray(LookupElement.EMPTY_ARRAY);
    }

    /**
     * Determines a possible subclass for the current reference element.
     *
     * @return Subclass the Spring bean reference must implement or {@code null} if no subclass defined.
     */
    @Nullable
    private Set<PsiClass> getPossibleSubClasses() {
      final String[] subClassName = extendClass.value();
      if (subClassName.length == 0) {
        return null;
      }
      return Arrays.stream(subClassName)
        .map(s -> JavaPsiFacade.getInstance(module.getProject())
        .findClass(s, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false)))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    }
  }
}