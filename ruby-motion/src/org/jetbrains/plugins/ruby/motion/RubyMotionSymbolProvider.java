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
package org.jetbrains.plugins.ruby.motion;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Class;
import org.jetbrains.plugins.ruby.motion.bridgesupport.*;
import org.jetbrains.plugins.ruby.motion.symbols.*;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.scope.RElementWithFQN;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.RubySymbolProviderBase;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.TypeSet;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.fqn.FQN;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.CoreTypes;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.modules.RModule;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionSymbolProvider extends RubySymbolProviderBase {
  @Override
  public Symbol findSymbol(@NotNull Symbol anchor,
                           @NotNull FQN fqn,
                           @NotNull TypeSet types,
                           @Nullable PsiElement invocationPoint) {
    if (!RubyMotionUtil.getInstance().hasMacRubySupport(invocationPoint)) return null;

    final Module module = getModule(invocationPoint);
    if (module == null) return null;

    final Collection<Framework> frameworks = ((RubyMotionUtilImpl)RubyMotionUtil.getInstance()).getFrameworks(module);
    final List<String> nameAsList = fqn.asList();
    if (nameAsList.isEmpty()) {
      return null;
    }
    final String name = nameAsList.get(0);
    if (name.isEmpty()) return null;

    if (types.contains(Type.CLASS)) {
      Symbol result = findClassOrStruct(module, frameworks, nameAsList);
      if (result != null) {
        return result;
      }
    }
    if (types.contains(Type.CLASS_METHOD)) {
      for (Framework framework : frameworks) {
        Function function = framework.getFunction(name);
        final String original = framework.getOriginalFunctionName(name);
        if (original != null) {
          function = framework.getFunction(original);
        }
        if (function != null) {
          return MotionSymbolUtil.createFunctionSymbol(module, null, function);
        }
      }
    }

    if (types.contains(Type.CONSTANT)) {
      for (Framework framework : frameworks) {
        Constant constant = findConstant(name, framework);
        if (constant != null) {
          return MotionSymbolUtil.createConstantSymbol(module, constant);
        }
      }
    }
    return null;
  }

  public static Constant findConstant(String name, Framework framework) {
    Constant constant = framework.getConstant(name);
    if (constant == null) {
      constant = framework.getConstant(Character.toLowerCase(name.charAt(0)) + name.substring(1));
    }
    return constant;
  }

  @Nullable
  public static Symbol findClassOrStruct(Module module, Collection<? extends Framework> frameworks, List<String> name) {
    final List<Class> classes = new ArrayList<>();
    for (Framework framework : frameworks) {
      final Class clazz = framework.getClass(name);
      ContainerUtil.addIfNotNull(classes, clazz);
    }
    if (!classes.isEmpty()) {
      return new MotionClassSymbol(module, classes);
    }
    if (name.size() > 1) return null;

    for (Framework framework : frameworks) {
      final Struct struct = framework.getStruct(name.get(0));
      if (struct != null) {
        return new StructSymbol(module, struct);
      }
    }
    return null;
  }

  @Nullable
  private static Module getModule(@Nullable PsiElement invocationPoint) {
    return invocationPoint != null ? ModuleUtilCore.findModuleForPsiElement(invocationPoint.getContainingFile()) : null;
  }

  @Override
  public Symbol createSymbolByContainer(@NotNull RContainer container,
                                        @NotNull FQN fqn,
                                        @Nullable Symbol parent) {
    if (!RubyMotionUtil.getInstance().hasMacRubySupport(container)) return null;
    final Module module = getModule(container);
    if (module == null) return null;

    final Symbol motionSymbol = getCachedSpecificSymbol(container, container);
    if (motionSymbol != null) {
      return motionSymbol;
    }
    return null;
  }

  @Nullable
  @Override
  protected Symbol getSpecificSymbol(PsiElement element, RElementWithFQN context) {
    if (context instanceof RClass || context instanceof RModule) {
      final boolean isObject = CoreTypes.Object.equals(context.getFQN().getFullPath());
      final RubyMotionSymbol symbol = isObject ? new RubyMotionSymbol((RFile)context.getContainingFile()) : null;
      final Symbol nsObject = SymbolUtil.findConstantByFQN(element.getProject(), Type.CLASS, getParentName((RContainer)context), element);
      final List<Symbol> includes = isObject ? Collections.singletonList(symbol) : Collections.emptyList();
      final List<Symbol> superclasses = nsObject != null ? Collections.singletonList(nsObject) : Collections.emptyList();
      return new MotionEnabledClassModuleSymbol((RContainer)context, includes, Collections.emptyList(), superclasses);
    }
    return null;
  }

  private static String getParentName(RContainer context) {
    final String name = context.getName();
    if (CoreTypes.String.equals(name)) {
      return "NSMutableString";
    }
    if (CoreTypes.Array.equals(name)) {
      return "NSMutableArray";
    }
    if (CoreTypes.Hash.equals(name)) {
      return "NSMutableDictionary";
    }
    if (CoreTypes.Numeric.equals(name)) {
      return "NSNumber";
    }
    if (CoreTypes.Time.equals(name)) {
      return "NSDate";
    }
    return "NSObject";
  }
}
