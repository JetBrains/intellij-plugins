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
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Struct;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Children;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.ChildrenImpl;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolImpl;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.v2.SymbolPsiProcessor;

/**
 * @author Dennis.Ushakov
 */
public class StructSymbol extends SymbolImpl implements MotionSymbol {
  @NotNull private final Module myModule;
  @NotNull private final Struct myStruct;

  public StructSymbol(@NotNull Module module,
                      @NotNull Struct struct) {
    super(module.getProject(), struct.getName(), Type.CLASS, null);
    myModule = module;
    myStruct = struct;
  }

  @NotNull
  @Override
  public Children getChildren() {
    return new ChildrenImpl() {
      @Override
      public boolean processChildren(SymbolPsiProcessor processor, final PsiElement invocationPoint) {
        for (String name : myStruct.getFields()) {
          final Symbol[] symbols = MotionSymbolUtil.createStructFieldSymbols(myModule, StructSymbol.this, myStruct, name);
          for (Symbol symbol : symbols) {
            if (!processor.process(symbol)) {
              return false;
            }
          }
        }
        return true;
      }
    };
  }

  @NotNull
  @Override
  public MotionDocType getInfoType() {
    return MotionSymbol.MotionDocType.TYPEDEF;
  }

  @Override
  public String getInfoName() {
    final String name = getName();
    assert name != null;
    return name;
  }

  @NotNull
  @Override
  public Module getModule() {
    return myModule;
  }
}
