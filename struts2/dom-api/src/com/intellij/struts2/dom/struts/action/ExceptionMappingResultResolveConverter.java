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

package com.intellij.struts2.dom.struts.action;

import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Yann C&eacute;bron
 */
public class ExceptionMappingResultResolveConverter extends ResolvingConverter<Result> {

  @NotNull
  public Collection<? extends Result> getVariants(final ConvertContext context) {
    final DomElement invocationElement = context.getInvocationElement();
    final Action action = invocationElement.getParentOfType(Action.class, true);
    if (action != null) {
      return action.getResults();
    }

    return Collections.emptySet();
  }

  public Result fromString(@Nullable @NonNls final String value, final ConvertContext context) {
    if (value == null) {
      return null;
    }

    for (final Result result : getVariants(context)) {
      if (value.equals(result.getName().getStringValue())) {
        return result;
      }
    }

    return null;
  }

  public String toString(@Nullable final Result result, final ConvertContext context) {
    if (result == null) {
      return null;
    }

    return result.getName().getStringValue();
  }

  public String getErrorMessage(@Nullable final String value, final ConvertContext context) {
    return "Cannot resolve action-result ''" + value + "''";
  }

}