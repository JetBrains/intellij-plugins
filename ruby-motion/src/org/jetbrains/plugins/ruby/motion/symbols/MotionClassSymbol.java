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
import com.jetbrains.cidr.CocoaDocumentationManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Class;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Function;
import org.jetbrains.plugins.ruby.motion.bridgesupport.InheritanceInfoHolder;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.*;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.v2.SymbolPsiProcessor;

import java.util.Collections;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class MotionClassSymbol extends SymbolImpl implements MotionSymbol {
  @NotNull private final Module myModule;
  @NotNull private final List<Class> myClasses;

  public MotionClassSymbol(@NotNull Module module,
                           @NotNull List<Class> classes) {
    super(module.getProject(), classes.get(0).getName(), Type.CLASS, null);
    myModule = module;
    myClasses = classes;
  }

  @NotNull
  @Override
  public Children getChildren() {
    return new ChildrenImpl() {
      @Override
      public boolean processChildren(SymbolPsiProcessor processor, final PsiElement invocationPoint) {
        final MotionClassSymbol parent = MotionClassSymbol.this;
        for (Class clazz : myClasses) {
          for (Function function : clazz.getFunctions()) {
            for (RTypedSyntheticSymbol functionSymbol : MotionSymbolUtil.createSelectorSymbols(myModule, parent, function)) {
              if (!processor.process(functionSymbol)) {
                return false;
              }
            }
          }
          for (Class subClass : clazz.getSubClasses()) {
            final MotionClassSymbol symbol = new MotionClassSymbol(myModule, Collections.singletonList(subClass));
            if (!processor.process(symbol)) {
              return false;
            }
          }
        }

        final Symbol superClass = getSuperClassSymbol(invocationPoint);
        if (superClass != null) {
          final Children children = superClass.getChildren();
          if (!children.processChildren(processor, invocationPoint)) {
            return false;
          }
        }
        return true;
      }
    };
  }

  @Nullable
  public Symbol getSuperClassSymbol(@Nullable PsiElement invocationPoint) {
    String sdkVersion = RubyMotionUtil.getInstance().getSdkVersion(myModule);
    String ancestor = InheritanceInfoHolder.getInstance().getInheritance(myName, sdkVersion);
    if (ancestor == null) {
      if (RubyMotionUtil.getInstance().isOSX(myModule)) {
        ancestor = InheritanceInfoHolder.getInstance().getInheritance(myName, "10.10");
      } else if (!RubyMotionUtil.getInstance().isAndroid(myModule)) {
        ancestor = InheritanceInfoHolder.getInstance().getInheritance(myName, "9.3");
      }
    }

    return ancestor != null ? SymbolUtil.findSymbol(getProject(), Type.CLASS, ancestor, invocationPoint) : null;
  }

  @Override
  public CocoaDocumentationManagerImpl.DocTokenType getInfoType() {
    return CocoaDocumentationManagerImpl.DocTokenType.CLASS;
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
