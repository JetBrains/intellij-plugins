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

package com.intellij.struts2.dom.struts.impl;

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.struts2.dom.ConverterUtil;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorOrStackBase;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorRefResolveConverter;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Yann C&eacute;bron
 */
public class InterceptorRefResolveConverterImpl extends InterceptorRefResolveConverter {

  @NotNull
  public Collection<? extends InterceptorOrStackBase> getVariants(final ConvertContext context) {
    final StrutsModel strutsModel = ConverterUtil.getStrutsModel(context);

    if (strutsModel == null) {
      return Collections.emptySet();
    }

    return strutsModel.getAllInterceptorsAndStacks();
  }

  public InterceptorOrStackBase fromString(@Nullable @NonNls final String name, final ConvertContext context) {
    if (name == null) {
      return null;
    }

    final StrutsModel strutsModel = ConverterUtil.getStrutsModel(context);

    if (strutsModel == null) {
      return null;
    }

    return ContainerUtil.find(strutsModel.getAllInterceptorsAndStacks(), new Condition<InterceptorOrStackBase>() {
      public boolean value(final InterceptorOrStackBase interceptorOrStackBase) {
        return Comparing.strEqual(interceptorOrStackBase.getName().getStringValue(), name);
      }
    });
  }

}