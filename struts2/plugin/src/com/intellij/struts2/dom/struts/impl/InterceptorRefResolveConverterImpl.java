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

import com.intellij.struts2.dom.ConverterUtil;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.*;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

    return getAllInterceptorsAndStacks(strutsModel);
  }

  public InterceptorOrStackBase fromString(@Nullable @NonNls final String name, final ConvertContext context) {
    if (name == null) {
      return null;
    }

    final StrutsModel strutsModel = ConverterUtil.getStrutsModel(context);

    if (strutsModel == null) {
      return null;
    }

    final Set<InterceptorOrStackBase> interceptorOrStackBases = getAllInterceptorsAndStacks(strutsModel);
    for (final InterceptorOrStackBase interceptorOrStackBase : interceptorOrStackBases) {
      if (name.equals(interceptorOrStackBase.getName().getStringValue())) {
        return interceptorOrStackBase;
      }
    }

    return null;
  }

  @NotNull
  private static Set<InterceptorOrStackBase> getAllInterceptorsAndStacks(final StrutsModel strutsModel) {
    final Set<InterceptorOrStackBase> variants = new HashSet<InterceptorOrStackBase>();
    for (final StrutsPackage strutsPackage : strutsModel.getStrutsPackages()) {
      final List<InterceptorStack> interceptorList = strutsPackage.getInterceptorStacks();
      variants.addAll(interceptorList);
      final List<Interceptor> interceptors = strutsPackage.getInterceptors();
      variants.addAll(interceptors);
    }
    return variants;
  }

}