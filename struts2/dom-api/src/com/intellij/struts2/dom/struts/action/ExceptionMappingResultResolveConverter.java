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

package com.intellij.struts2.dom.struts.action;

import com.intellij.openapi.util.Condition;
import com.intellij.struts2.dom.struts.HasResultType;
import com.intellij.struts2.dom.struts.strutspackage.GlobalResults;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ResolvingConverter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 */
public class ExceptionMappingResultResolveConverter extends ResolvingConverter<HasResultType> {

  @Override
  @NotNull
  public Collection<? extends HasResultType> getVariants(final ConvertContext context) {
    final DomElement invocationElement = context.getInvocationElement();
    final Action action = invocationElement.getParentOfType(Action.class, true);
    if (action == null) {
      return Collections.emptySet();
    }

    final List<HasResultType> variants = new ArrayList<>();
    variants.addAll(action.getResults()); // Action-local first

    final StrutsPackage strutsPackage = action.getStrutsPackage();
    final GlobalResults globalResults = strutsPackage.getGlobalResults();
    variants.addAll(globalResults.getResults());

    return variants;
  }

  @Override
  public HasResultType fromString(@Nullable @NonNls final String value, final ConvertContext context) {
    if (value == null) {
      return null;
    }

    return ContainerUtil.find(getVariants(context),
                              (Condition<HasResultType>)result -> Objects.equals(result.getName().getStringValue(), value));
  }

  @Override
  public String toString(@Nullable final HasResultType result, final ConvertContext context) {
    if (result == null) {
      return null;
    }

    return result.getName().getStringValue();
  }

  @Override
  public String getErrorMessage(@Nullable final String value, final ConvertContext context) {
    return "Cannot resolve action-result '" + value + "'";
  }

}