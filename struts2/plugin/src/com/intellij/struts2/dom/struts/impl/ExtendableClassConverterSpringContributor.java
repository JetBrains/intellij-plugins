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

package com.intellij.struts2.dom.struts.impl;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.spring.SpringManager;
import com.intellij.spring.contexts.model.SpringModel;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.dom.ExtendableClassConverter;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomJavaUtil;
import com.intellij.util.xml.ExtendClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Extends "class" resolving to Spring beans.
 *
 * @author Yann C&eacute;bron
 */
public class ExtendableClassConverterSpringContributor
  extends ExtendableClassConverter.ExtendableClassConverterContributor {

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
  public boolean isSuitable(@NotNull final ConvertContext convertContext) {
    final Module module = convertContext.getModule();
    if (module == null) {
      return false;
    }

    if (SpringFacet.getInstance(module) == null) {
      return false;
    }

    return DomJavaUtil.findClass(StrutsConstants.SPRING_OBJECT_FACTORY_CLASS,
                                 convertContext.getInvocationElement()) != null;
  }

  @NotNull
  public PsiReference[] getReferences(@NotNull final ConvertContext convertContext,
                                      @NotNull final PsiElement psiElement,
                                      @NotNull final ExtendClass extendClass) {
    return new PsiReference[]{new SpringBeanReference((XmlAttributeValue)psiElement,
                                                      convertContext.getModule(),
                                                      extendClass)};
  }


  // TODO provide QuickFix to create Spring bean?
  private static class SpringBeanReference extends PsiReferenceBase<XmlAttributeValue> {

    private final Module module;
    private final ExtendClass extendClass;

    private SpringBeanReference(final XmlAttributeValue element,
                                final Module module,
                                final ExtendClass extendClass) {
      super(element, true);
      this.module = module;
      this.extendClass = extendClass;
    }

    @Nullable
    private SpringModel getSpringModel() {
      return SpringManager.getInstance(module.getProject()).getCombinedModelWithDeps(module);
    }

    public PsiElement resolve() {
      final String beanName = myElement.getValue();
      if (StringUtil.isEmpty(beanName)) {
        return null;
      }

      final SpringModel springModel = getSpringModel();
      if (springModel == null) {
        return null;
      }

      final SpringBeanPointer springBean = springModel.findBeanByName(beanName);
      if (springBean == null) {
        return null;
      }

      if (springBean.isAbstract()) {
        return null;
      }

      return springBean.getBeanClass();
    }

    @NotNull
    @SuppressWarnings({"unchecked"})
    public Object[] getVariants() {
      final SpringModel springModel = getSpringModel();
      if (springModel == null) {
        return EMPTY_ARRAY;
      }

      final PsiClass subClass = getPossibleSubClass();
      final Collection<? extends SpringBeanPointer> list;
      if (subClass != null) {
        list = springModel.findBeansByPsiClassWithInheritance(subClass.getQualifiedName());
      }
      else {
        list = springModel.getAllCommonBeans();
      }

      final List variants = new ArrayList(list.size());
      for (final SpringBeanPointer bean : list) {
        if (bean.isAbstract()) {
          continue;
        }

        final String beanName = bean.getName();
        final PsiFile psiFile = bean.getContainingFile();

        if (psiFile != null && StringUtil.isNotEmpty(beanName)) {
          //noinspection ConstantConditions
          variants.add(LookupElementBuilder.create(beanName)
                         .withIcon(bean.getBeanIcon())
                         .withTailText(" (" + psiFile.getName() + ")", true));
        }
      }

      return ArrayUtil.toObjectArray(variants, LookupElementBuilder.class);
    }

    /**
     * Determines a possible subclass for the current reference element.
     *
     * @return Subclass the Spring bean reference must implement or {@code null} if no subclass defined.
     */
    @Nullable
    private PsiClass getPossibleSubClass() {
      final String subClassName = extendClass.value();
      if (subClassName == null) {
        return null;
      }

      final GlobalSearchScope searchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false);
      return JavaPsiFacade.getInstance(module.getProject()).findClass(subClassName, searchScope);
    }
  }
}