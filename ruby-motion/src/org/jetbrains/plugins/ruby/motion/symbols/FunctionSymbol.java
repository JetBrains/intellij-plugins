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

import com.intellij.openapi.module.Module;
import com.jetbrains.cidr.CocoaDocumentationManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Function;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.RTypedSyntheticSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;

/**
 * @author Dennis.Ushakov
 */
public class FunctionSymbol extends RTypedSyntheticSymbol implements MotionSymbol {
  @NotNull private final Module myModule;
  @NotNull private final Function myFunction;

  public FunctionSymbol(@NotNull Module module,
                        @Nullable String name,
                        @Nullable Symbol parent,
                        @NotNull RType returnType, @NotNull Function function) {
    super(module.getProject(), name, function.isClassMethod() ? Type.CLASS_METHOD : Type.INSTANCE_METHOD,
          parent, returnType, getMinParameterCount(function), getMaxParameterCount(function));
    myModule = module;
    myFunction = function;
  }

  @NotNull
  @Override
  public Module getModule() {
    return myModule;
  }

  @NotNull
  public Function getFunction() {
    return myFunction;
  }

  public static int getMinParameterCount(final Function function) {
    return function.getArguments().size();
  }

  public static int getMaxParameterCount(final Function function) {
    return function.isVariadic() ? -1 : function.getArguments().size();
  }

  @Override
  public CocoaDocumentationManagerImpl.DocTokenType getInfoType() {
    return getParentSymbol() != null ?
           myFunction.isClassMethod() ? CocoaDocumentationManagerImpl.DocTokenType.CLASS_METHOD :
           CocoaDocumentationManagerImpl.DocTokenType.INSTANCE_METHOD :
           CocoaDocumentationManagerImpl.DocTokenType.FUNCTION;
  }

  @Override
  public String getInfoName() {
    return myFunction.getName();
  }
}
