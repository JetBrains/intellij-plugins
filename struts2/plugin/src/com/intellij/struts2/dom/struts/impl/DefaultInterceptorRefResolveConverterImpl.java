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

import com.intellij.struts2.dom.ConverterUtil;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.DefaultInterceptorRefResolveConverter;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorStack;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Yann CŽbron
 */
public class DefaultInterceptorRefResolveConverterImpl extends DefaultInterceptorRefResolveConverter {

  @NotNull
  public Collection<? extends InterceptorStack> getVariants(final ConvertContext context) {
    final StrutsModel strutsModel = ConverterUtil.getStrutsModel(context);
    if (strutsModel == null) {
      return Collections.emptySet();
    }

    return getAllInterceptorStacks(strutsModel);
  }

  public InterceptorStack fromString(@Nullable @NonNls final String name, final ConvertContext context) {
    if (name == null) {
      return null;
    }

    final StrutsModel strutsModel = ConverterUtil.getStrutsModel(context);
    if (strutsModel == null) {
      return null;
    }

    final Set<InterceptorStack> interceptorStacks = getAllInterceptorStacks(strutsModel);
    for (final InterceptorStack interceptorStack : interceptorStacks) {
      if (name.equals(interceptorStack.getName().getStringValue())) {
        return interceptorStack;
      }
    }

    return null;
  }

  private Set<InterceptorStack> getAllInterceptorStacks(@NotNull final StrutsModel strutsModel) {
    final Set<InterceptorStack> variants = new HashSet<InterceptorStack>();
    for (final StrutsPackage strutsPackage : strutsModel.getStrutsPackages()) {
      final List<InterceptorStack> interceptorStackList = strutsPackage.getInterceptorStacks();
      variants.addAll(interceptorStackList);
    }

    return variants;
  }

}