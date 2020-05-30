/*
 * Copyright 2009 The authors
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

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.struts2.dom.ConverterUtil;
import com.intellij.struts2.dom.struts.Bean;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.dom.struts.UnknownHandlerRefConverter;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 */
public class UnknownHandlerRefConverterImpl extends UnknownHandlerRefConverter {

  @Override
  @NotNull
  public Collection<? extends Bean> getVariants(final ConvertContext convertContext) {
    final StrutsModel model = ConverterUtil.getStrutsModel(convertContext);

    if (model == null) {
      return Collections.emptyList();
    }

    return getBeansOfTypeUnknownHandler(model);
  }

  @Override
  public Bean fromString(@Nullable final String name, final ConvertContext convertContext) {
    if (StringUtil.isEmpty(name)) {
      return null;
    }

    final StrutsModel strutsModel = ConverterUtil.getStrutsModel(convertContext);
    if (strutsModel == null) {
      return null;
    }

    return ContainerUtil.find(getBeansOfTypeUnknownHandler(strutsModel), bean -> Objects.equals(bean.getName().getStringValue(), name));
  }

  private static final Function<StrutsRoot, Collection<? extends Bean>> BEANS_OF_TYPE_UNKNOWN_HANDLER_COLLECTOR =
    strutsRoot -> {
      final Set<Bean> unknownHandlerBeans = new HashSet<>(1);
      for (final Bean bean : strutsRoot.getBeans()) {
        if (Objects.equals(bean.getBeanType().getStringValue(), UNKNOWN_HANDLER_CLASS)) {
          unknownHandlerBeans.add(bean);
        }
      }
      return unknownHandlerBeans;
    };

  private static List<Bean> getBeansOfTypeUnknownHandler(@NotNull final StrutsModel model) {
    return ContainerUtil.concat(model.getMergedStrutsRoots(), BEANS_OF_TYPE_UNKNOWN_HANDLER_COLLECTOR);
  }

}