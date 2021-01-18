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
package com.intellij.struts2.dom.struts.impl;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Ref;
import com.intellij.struts2.dom.struts.strutspackage.DefaultInterceptorRefResolveConverter;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorStack;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackageHierarchyWalker;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
final class DefaultInterceptorRefResolveConverterImpl extends DefaultInterceptorRefResolveConverter {
  @Override
  @NotNull
  public Collection<? extends InterceptorStack> getVariants(final ConvertContext context) {
    final List<InterceptorStack> results = new SmartList<>();
    final Processor<StrutsPackage> processor = strutsPackage -> {
      results.addAll(strutsPackage.getInterceptorStacks());
      return true;
    };

    final StrutsPackageHierarchyWalker walker = new StrutsPackageHierarchyWalker(getCurrentStrutsPackage(context),
                                                                                 processor);
    walker.walkUp();

    return results;
  }

  @Override
  public InterceptorStack fromString(@Nullable @NonNls final String name, final ConvertContext context) {
    if (name == null) {
      return null;
    }

    final Condition<InterceptorStack> nameCondition = interceptorStack -> name.equals(interceptorStack.getName().getStringValue());

    final Ref<InterceptorStack> resolveResult = new Ref<>();
    final Processor<StrutsPackage> processor = strutsPackage -> {
      final InterceptorStack result = ContainerUtil.find(strutsPackage.getInterceptorStacks(), nameCondition);
      if (result != null) {
        resolveResult.set(result);
        return false;
      }
      return true;
    };
    final StrutsPackageHierarchyWalker walker = new StrutsPackageHierarchyWalker(getCurrentStrutsPackage(context),
                                                                                 processor);
    walker.walkUp();

    return resolveResult.get();
  }

  private static StrutsPackage getCurrentStrutsPackage(final ConvertContext context) {
    final StrutsPackage strutsPackage = DomUtil.getParentOfType(context.getInvocationElement(),
                                                                StrutsPackage.class,
                                                                true);
    assert strutsPackage != null : context.getInvocationElement();
    return strutsPackage;
  }

}