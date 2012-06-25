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
 */
package com.intellij.struts2.model.constant;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.spring.SpringManager;
import com.intellij.spring.contexts.model.SpringModel;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.struts2.StrutsConstants;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomJavaUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Resolve to Spring bean for suitable &lt;constant> "value".
 *
 * @author Yann C&eacute;bron
 */
public class ConstantValueConverterSpringClassContributor implements ConstantValueConverterClassContributor {


  @Nullable
  public PsiClass fromString(@NotNull @NonNls final String s, final ConvertContext convertContext) {
    final Module module = convertContext.getModule();
    if (module == null) {
      return null;
    }

    if (DomJavaUtil.findClass(StrutsConstants.SPRING_OBJECT_FACTORY_CLASS,
                              convertContext.getInvocationElement()) == null) {
      return null;
    }

    final SpringModel springModel = SpringManager.getInstance(module.getProject()).getCombinedModel(module);
    final SpringBeanPointer springBeanPointer = springModel.findBeanByName(s);
    if (springBeanPointer == null) {
      return null;
    }

    return springBeanPointer.getSpringBean().getBeanClass();
  }

}