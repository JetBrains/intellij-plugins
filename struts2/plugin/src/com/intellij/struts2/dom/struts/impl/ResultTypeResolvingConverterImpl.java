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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.struts2.dom.ConverterUtil;
import com.intellij.struts2.dom.struts.action.ResultTypeResolvingConverter;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackageHierarchyWalker;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 */
public class ResultTypeResolvingConverterImpl extends ResultTypeResolvingConverter {

  @Override
  @NotNull
  public Collection<? extends ResultType> getVariants(final ConvertContext context) {
    final List<ResultType> results = new SmartList<>();
    final Processor<StrutsPackage> processor = strutsPackage -> {
      results.addAll(strutsPackage.getResultTypes());
      return true;
    };

    final StrutsPackageHierarchyWalker walker =
        new StrutsPackageHierarchyWalker(ConverterUtil.getCurrentStrutsPackage(context), processor);
    walker.walkUp();

    return results;
  }

  @Override
  public ResultType fromString(@Nullable @NonNls final String name, final ConvertContext context) {
    if (StringUtil.isEmpty(name)) {
      return null;
    }

    final Condition<ResultType> nameCondition = resultType -> Objects.equals(name, resultType.getName().getStringValue());

    final Ref<ResultType> resolveResult = new Ref<>();
    final Processor<StrutsPackage> processor = strutsPackage -> {
      final ResultType result = ContainerUtil.find(strutsPackage.getResultTypes(), nameCondition);
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

}