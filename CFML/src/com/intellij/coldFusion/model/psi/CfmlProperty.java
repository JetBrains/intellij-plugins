/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
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
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.info.CfmlPropertyDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author vnikolaenko
 * @date 09.02.11
 */
public interface CfmlProperty extends CfmlPsiElement, CfmlTypedVariable {
  CfmlProperty[] EMPTY_ARRAY = new CfmlProperty[0];

  boolean hasGetter();

  boolean hasSetter();

  String getDefault();

  @Nullable
  CfmlComponent getComponent();

  @Nullable
  String getDescription();

  @Override
  @NotNull
  String getName();

  @NotNull
  CfmlPropertyDescription getPropertyInfo();
}
