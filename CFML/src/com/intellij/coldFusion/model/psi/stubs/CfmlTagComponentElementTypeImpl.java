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
package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.coldFusion.model.psi.impl.CfmlTagComponentImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CfmlTagComponentElementTypeImpl extends CfmlComponentElementType {
  public CfmlTagComponentElementTypeImpl(@NotNull @NonNls final String debugName) {
    super(debugName, CfmlLanguage.INSTANCE);
  }

  @Override
  public CfmlComponent createPsi(@NotNull CfmlComponentStub stub) {
    return new CfmlTagComponentImpl(stub);
  }
}
