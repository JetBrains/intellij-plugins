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

package com.intellij.struts2.dom.struts.strutspackage;

import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ResolvingConverter;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Global {@code exception-mapping} "result".
 *
 * @author Yann C&eacute;bron
 */
public class GlobalExceptionMappingResultResolveConverter extends ResolvingConverter<GlobalResult> {

  @Override
  @NotNull
  public Collection<? extends GlobalResult> getVariants(final ConvertContext context) {
    final DomElement invocationElement = context.getInvocationElement();
    final StrutsPackage strutsPackage = invocationElement.getParentOfType(StrutsPackage.class, true);
    if (strutsPackage != null) {
      return strutsPackage.getGlobalResults().getResults();
    }

    return Collections.emptySet();
  }

  @Override
  public GlobalResult fromString(@Nullable @NonNls final String value, final ConvertContext context) {
    if (value == null) {
      return null;
    }

    return ContainerUtil.find(getVariants(context),
                              (Condition<GlobalResult>)globalResult -> Objects.equals(value, globalResult.getName().getStringValue()));
  }

  @Override
  public String toString(@Nullable final GlobalResult result, final ConvertContext context) {
    if (result == null) {
      return null;
    }

    return result.getName().getStringValue();
  }

  @Override
  public String getErrorMessage(@Nullable final String value, final ConvertContext context) {
    return "Cannot resolve global result '" + value + "'";
  }

}