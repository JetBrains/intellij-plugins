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
import com.intellij.struts2.dom.ConverterUtil;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorOrStackBase;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorRefResolveConverter;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackageHierarchyWalker;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
public class InterceptorRefResolveConverterImpl extends InterceptorRefResolveConverter {

  @Override
  @NotNull
  public Collection<? extends InterceptorOrStackBase> getVariants(final ConvertContext context) {
    final List<InterceptorOrStackBase> results = new SmartList<>();
    final Processor<StrutsPackage> processor = strutsPackage -> {
      final List<InterceptorOrStackBase> allInterceptors = getAllInterceptors(strutsPackage);
      results.addAll(allInterceptors);
      return true;
    };
    final StrutsPackageHierarchyWalker walker =
        new StrutsPackageHierarchyWalker(ConverterUtil.getCurrentStrutsPackage(context), processor);
    walker.walkUp();

    return results;
  }

  @Override
  public InterceptorOrStackBase fromString(@Nullable @NonNls final String name, final ConvertContext context) {
    if (name == null) {
      return null;
    }

    final Condition<InterceptorOrStackBase> nameCondition =
      interceptorOrStackBase -> name.equals(interceptorOrStackBase.getName().getStringValue());

    final Ref<InterceptorOrStackBase> resolveResult = new Ref<>();
    final Processor<StrutsPackage> processor = strutsPackage -> {
      final InterceptorOrStackBase result = ContainerUtil.find(getAllInterceptors(strutsPackage), nameCondition);
      if (result != null) {
        resolveResult.set(result);
        return false;
      }

      return true;
    };
    final StrutsPackageHierarchyWalker walker =
        new StrutsPackageHierarchyWalker(ConverterUtil.getCurrentStrutsPackage(context), processor);
    walker.walkUp();

    return resolveResult.get();
  }

  private static List<InterceptorOrStackBase> getAllInterceptors(final StrutsPackage strutsPackage) {
    return ContainerUtil.concat(strutsPackage.getInterceptors(), strutsPackage.getInterceptorStacks());
  }

}