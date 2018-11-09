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
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.bridgesupport.BridgeSupportLoader;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Constant;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Framework;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Function;
import org.jetbrains.plugins.ruby.motion.symbols.ConstantSymbol;
import org.jetbrains.plugins.ruby.motion.symbols.FunctionSymbol;
import org.jetbrains.plugins.ruby.motion.symbols.MotionClassSymbol;
import org.jetbrains.plugins.ruby.motion.symbols.MotionSymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.AbstractRubyTypeProvider;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.ResolveUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.scope.ScopeVariable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.v2.ClassModuleSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RTypeUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPossibleCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RArgument;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RNamedArgument;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.references.RReference;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RConstant;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RIdentifier;

import java.util.Collection;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionTypeProvider extends AbstractRubyTypeProvider {
  @Nullable
  @Override
  public RType createTypeBySymbol(Symbol symbol, Context context) {
    if (symbol instanceof ConstantSymbol) {
      final Constant constant = ((ConstantSymbol)symbol).getConstant();
      return MotionSymbolUtil.getTypeByName(symbol.getModule(), constant.getDeclaredType());
    }
    return null;
  }

  @Nullable
  @Override
  public RType createTypeByRExpression(@NotNull RExpression expression) {
    if (!RubyMotionUtil.getInstance().hasMacRubySupport(expression)) return null;
    Module module = ModuleUtilCore.findModuleForPsiElement(expression.getContainingFile());
    if (module == null) return null;

    if (expression instanceof RReference) {
      return createTypeForRef((RReference)expression, module);
    }
    else if (expression instanceof RCall) {
      final PsiElement psiCommand = ((RCall)expression).getPsiCommand();
      if (psiCommand instanceof RReference) {
        return createTypeForRef((RReference)psiCommand, module);
      }
    }
    else if (expression instanceof RConstant) {
      return createTypeForConstant(expression, module);
    }
    else if (expression instanceof RIdentifier) {
      final ScopeVariable variable = ((RIdentifier)expression).getScopeVariable();
      if (variable != null) {
        for (RPsiElement element : variable.getDeclarations()) {
          if (element instanceof RIdentifier && ((RIdentifier)element).isParameterDeclaration()) {
            return createTypeForMethodParameter((RIdentifier)element, module);
          }
        }
      }
    }
    return null;
  }

  private static RType createTypeForMethodParameter(RExpression expression, @Nullable Module module) {
    final RMethod method = PsiTreeUtil.getParentOfType(expression, RMethod.class);
    final Symbol symbol = SymbolUtil.getSymbolByContainer(method);
    if (symbol == null) return null;

    List<RArgument> arguments = method.getArguments();
    final String rubyName = method.getName();
    final String objCName = calculateObjCName(rubyName, arguments);

    final String sdkVersion = RubyMotionUtil.getInstance().getSdkVersion(module);
    final String[] frameworks = RubyMotionUtil.getInstance().getRequiredFrameworks(module);
    boolean isSelector = false;
    for (String framework : frameworks) {
      if (BridgeSupportLoader.getInstance().isSelector(objCName, sdkVersion, framework)) {
        isSelector = true;
        break;
      }
    }
    if (!isSelector) return null;

    Symbol classSymbol = symbol.getParentSymbol();
    Ref<Symbol> superClassRef = Ref.create(classSymbol);
    if (classSymbol instanceof ClassModuleSymbol) {
      ((ClassModuleSymbol)classSymbol).processSuperClassSymbols(superclass -> {
        superClassRef.set(superclass);
        return true;
      }, expression, true);
    }
    if (superClassRef.get() instanceof ClassModuleSymbol) {
      classSymbol = ((ClassModuleSymbol)superClassRef.get()).getSuperClassSymbol(expression);
    }
    if (classSymbol instanceof MotionClassSymbol) {
      final List<Symbol> candidates = classSymbol.getChildren().getSymbolsByNameAndTypes(rubyName, symbol.getType().asSet(), expression);
      for (Symbol candidate : candidates) {
        final Function function = ((FunctionSymbol)candidate).getFunction();
        if (objCName.equals(function.getName())) {
          int argIndex = 0;
          while (argIndex < arguments.size() && !arguments.get(argIndex).getName().equals(expression.getName())) argIndex++;
          if (argIndex < arguments.size()) {
            return MotionSymbolUtil.getTypeByName(module, function.getArguments().get(argIndex).second);
          }
        }
      }
    }
    return null;
  }

  private static String calculateObjCName(final String methodName, final List<RArgument> arguments) {
    final StringBuilder objCNameBuilder = new StringBuilder(methodName).append(":");
    for (RArgument argument : arguments) {
      if (argument instanceof RNamedArgument) {
        final RPsiElement nameIdentifier = ((RNamedArgument)argument).getNameIdentifier();
        assert nameIdentifier != null;
        objCNameBuilder.append(nameIdentifier.getName()).append(":");
      }
    }
    return objCNameBuilder.toString();
  }

  @Nullable
  private static RType createTypeForConstant(@NotNull RExpression expression, @NotNull Module module) {
    final String name = expression.getName();
    if (name == null) return null;

    final Collection<Framework> frameworks = ((RubyMotionUtilImpl)RubyMotionUtil.getInstance()).getFrameworks(module);
    for (Framework framework : frameworks) {
      final Constant constant = RubyMotionSymbolProvider.findConstant(name, framework);
      if (constant != null) {
        return MotionSymbolUtil.getTypeByName(module, constant.getDeclaredType());
      }
    }
    return null;
  }

  @Nullable
  private static RType createTypeForRef(@NotNull RReference ref, @NotNull Module module) {
    final RPsiElement value = ref.getValue();
    // method name may be identifier on constant
    if (value instanceof RPossibleCall) {
      final String shortName = ((RPossibleCall)value).getCommand();
      final String sdkVersion = RubyMotionUtil.getInstance().getSdkVersion(module);
      final String[] frameworks = RubyMotionUtil.getInstance().getRequiredFrameworks(module);
      boolean isIdSelector = false;
      for (String framework : frameworks) {
        if (BridgeSupportLoader.getInstance().isIdSelector(shortName, sdkVersion, framework)) {
          isIdSelector = true;
          break;
        }
      }

      if (isIdSelector) {
        final Symbol callSymbol = ResolveUtil.resolveToSymbolWithCaching(ref.getReference());
        if (callSymbol instanceof FunctionSymbol) {
          final Function function = ((FunctionSymbol)callSymbol).getFunction();
          if (function.isId()) {
            return RTypeUtil.createTypeSameAsReceiverInstance(ref);
          }
        }
      }
    }
    return null;
  }
}
