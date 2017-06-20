/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.jetbrains.plugins.ruby.motion.symbols;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.jetbrains.cidr.CocoaDocumentationManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Constant;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.RTypedSyntheticSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;

import javax.swing.*;

/**
 * @author Dennis.Ushakov
 */
public class ConstantSymbol extends RTypedSyntheticSymbol implements MotionSymbol {
  @NotNull private final Module myModule;
  @NotNull private final Constant myConstant;

  public ConstantSymbol(@NotNull Module module,
                        @NotNull Constant constant,
                        @Nullable String name,
                        @NotNull RType returnType) {
    super(module.getProject(), name, Type.CONSTANT, null, returnType, 0);
    myModule = module;
    myConstant = constant;
  }

  @NotNull
  @Override
  public Module getModule() {
    return myModule;
  }

  @Nullable
  @Override
  public Icon getExplicitIcon() {
    return myConstant instanceof org.jetbrains.plugins.ruby.motion.bridgesupport.Enum ? AllIcons.Nodes.Enum : null;
  }

  @Override
  public CocoaDocumentationManagerImpl.DocTokenType getInfoType() {
    return myConstant instanceof org.jetbrains.plugins.ruby.motion.bridgesupport.Enum ?
           CocoaDocumentationManagerImpl.DocTokenType.ENUM_CONSTANT :
           CocoaDocumentationManagerImpl.DocTokenType.CLASS_CONSTANT;
  }

  @Override
  public String getInfoName() {
    final String name = getName();
    assert name != null;
    return name;
  }

  @NotNull
  public Constant getConstant() {
    return myConstant;
  }
}
