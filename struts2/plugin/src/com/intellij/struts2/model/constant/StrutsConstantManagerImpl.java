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

package com.intellij.struts2.model.constant;

import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Condition;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsConstantManagerImpl extends StrutsConstantManager {

  @NotNull
  @Override
  public List<StrutsConstant> getConstants(@NotNull final Module module) {
    return ContainerUtil.concat(
        Extensions.getExtensions(EP_NAME),
        new Function<StrutsConstantContributor, Collection<? extends StrutsConstant>>() {
          public Collection<? extends StrutsConstant> fun(final StrutsConstantContributor contributor) {
            if (!contributor.isAvailable(module)) {
              return Collections.emptyList();
            }

            return contributor.getStrutsConstantDefinitions(module);
          }
        });
  }

  @Override
  @Nullable
  public StrutsConstant findByName(@NotNull final Module module, @NotNull @NonNls final String name) {
    return ContainerUtil.find(getConstants(module), new Condition<StrutsConstant>() {
      public boolean value(final StrutsConstant strutsConstant) {
        return strutsConstant.getName().equals(name);
      }
    });
  }

}