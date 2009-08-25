/*
 * Copyright 2008 The authors
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
 *
 */

package com.intellij.struts2.dom.struts.impl;

import com.intellij.codeInsight.lookup.LookupValueFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.dom.ExtendableClassConverter;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
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
public class ExtendableClassConverterSpringContributor extends ExtendableClassConverter.ExtendableClassConverterContributor {

  @NonNls
  private static final String STRUTS_SPRING_OBJECT_FACTORY = "org.apache.struts2.spring.StrutsSpringObjectFactory";

  /**
   * Checks if struts2-spring-plugin is present in current module.
   *
   * @param convertContext Current context.
   * @return true if yes.
   */
  public boolean isSuitable(@NotNull final ConvertContext convertContext) {
    return DomJavaUtil.findClass(STRUTS_SPRING_OBJECT_FACTORY, convertContext.getInvocationElement()) != null;
  }

  public String getContributorType() {
    return StrutsBundle.message("dom.extendable.class.converter.type.spring");
  }

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull final PsiElement element,
                                               @NotNull final ProcessingContext context) {
    final Module module = ModuleUtil.findModuleForPsiElement(element);
    if (module == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    final SpringModel springModel = SpringManager.getInstance(module.getProject()).getCombinedModel(module);
    if (springModel == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    return new PsiReference[]{new SpringBeanReference((XmlAttributeValue) element,
                                                      springModel,
                                                      getPossibleSubClass(element, module))};
  }

  /**
   * Determines a possible subclass for the current reference element.
   *
   * @param psiElement Current completion element.
   * @param module     Current module.
   * @return Subclass the Spring bean reference must implement or {@code null} if no subclass defined.
   */
  @Nullable
  private static PsiClass getPossibleSubClass(final PsiElement psiElement,
                                              final Module module) {
    final DomElement domElement = DomUtil.getDomElement(psiElement);
    assert domElement != null;
    final ExtendClass extendClass = domElement.getAnnotation(ExtendClass.class);
    assert extendClass != null : "must be annotated with @ExtendClass: " + psiElement.getText();

    final String subClassName = extendClass.value();
    if (StringUtil.isNotEmpty(subClassName)) {
      return JavaPsiFacade.getInstance(psiElement.getProject())
          .findClass(subClassName, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false));
    }
    return null;
  }

  // TODO provide QuickFix to create Spring bean?
  private static class SpringBeanReference extends PsiReferenceBase<XmlAttributeValue> {

    private final SpringModel springModel;

    @Nullable
    private final PsiClass subClass;

    private SpringBeanReference(final XmlAttributeValue element,
                                final SpringModel springModel,
                                final PsiClass subClass) {
      super(element);
      this.springModel = springModel;
      this.subClass = subClass;
    }

    public boolean isSoft() {
      return true;
    }

    public PsiElement resolve() {
      final String beanName = myElement.getValue();
      final SpringBeanPointer springBean = springModel.findBean(beanName);
      if (springBean == null) {
        return null;
      }

      return springBean.getBeanClass();
    }

    @NotNull
    @SuppressWarnings({"unchecked"})
    public Object[] getVariants() {
      final Collection<? extends SpringBeanPointer> list;
      if (subClass != null) {
        list = springModel.findBeansByPsiClassWithInheritance(subClass);
      } else {
        list = springModel.getAllCommonBeans(true);
      }

      final List variants = new ArrayList(list.size());
      for (final SpringBeanPointer bean : list) {
        final String beanName = bean.getName();
        final PsiFile psiFile = bean.getContainingFile();

        if (psiFile != null && StringUtil.isNotEmpty(beanName)) {
          //noinspection ConstantConditions
          variants.add(LookupValueFactory.createLookupValueWithHint(beanName, bean.getBeanIcon(), psiFile.getName()));
        }
      }

      return ArrayUtil.toObjectArray(variants);
    }

  }

}