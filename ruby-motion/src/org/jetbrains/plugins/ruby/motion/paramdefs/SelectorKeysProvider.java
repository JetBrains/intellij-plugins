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
package org.jetbrains.plugins.ruby.motion.paramdefs;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Function;
import org.jetbrains.plugins.ruby.rails.codeInsight.paramDefs.MethodRefParam;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.DynamicHashKeyProviderEx;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamContext;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.matcher.ParamDefExpression;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.matcher.ParamDefLeaf;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.assoc.RAssoc;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.Visibility;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RListOfExpressions;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.expressions.RListOfExpressionsNavigator;

import java.util.*;

/**
 * @author Dennis.Ushakov
 */
public class SelectorKeysProvider implements DynamicHashKeyProviderEx {
  static final MethodRefParam METHOD_REF_PARAM = new MethodRefParam(null, Visibility.PUBLIC);
  private final Map<String, Collection<Function>> mySelectors;

  public SelectorKeysProvider(final Map<String, Collection<Function>> selectors) {
    mySelectors = selectors;
  }

  @NotNull
  @Override
  public List<String> getKeys(ParamContext context) {
    final Collection<Function> functions = getFunctions(context);
    if (functions == null) return Collections.emptyList();

    final Pair<Integer, List<String>> pair = determineArgNumberAndPath(context);
    final int argument = pair.first;
    if (argument < 1) return Collections.emptyList();

    final List<String> result = new ArrayList<>();
    for (Function function : functions) {
      final String[] namedArguments = function.getName().split(":");
      if (pathMatches(argument, namedArguments, pair.second)) {
        result.add(namedArguments[argument]);
      }
    }
    return result;
  }

  private static boolean pathMatches(int argument, String[] arguments, List<String> path) {
    if (arguments.length <= argument) {
      return false;
    }
    for (int i = 1; i < argument; i++) {
      String arg = arguments[i];
      if (!StringUtil.equals(arg, path.get(i - 1))) {
        return false;
      }
    }
    return true;
  }

  private static Pair<Integer, List<String>> determineArgNumberAndPath(ParamContext context) {
    final RPsiElement element = context.getValueElement();
    final PsiElement parent = element.getParent();
    final PsiElement argument = parent instanceof RAssoc ? parent : element;
    final RListOfExpressions expressions = RListOfExpressionsNavigator.getByPsiElement(argument);
    if (expressions == null) return Pair.create(-1, null);

    final List<String> path = new ArrayList<>();
    for (int i = 1; i < expressions.getElements().size(); i++) {
      PsiElement arg = expressions.getElements().get(i);
      if (arg == argument) return Pair.create(i, path);
      if (arg instanceof RAssoc) {
        path.add(((RAssoc)arg).getKeyText());
      }
    }
    return Pair.create(-1, null);
  }

  @Override
  public boolean isSymbolOnly() {
    return true;
  }

  @Override
  public boolean hasKey(ParamContext context, String text) {
    return getKeys(context).contains(text);
  }

  @Nullable
  @Override
  public PsiElement resolveKey(ParamContext context, String text) {
    return null;
  }

  @Nullable
  @Override
  public ParamDefExpression getValue(ParamContext context, String text) {
    final Collection<Function> functions = getFunctions(context);
    if (functions == null) return null;

    final Pair<Integer, List<String>> pair = determineArgNumberAndPath(context);
    final int argument = pair.first;
    if (argument < 1) return null;

    for (Function function : functions) {
      final List<Pair<String, String>> arguments = function.getArguments();
      final String[] namedArguments = function.getName().split(":");
      if (pathMatches(argument, namedArguments, pair.second) && "SEL".equals(arguments.get(argument).second)) {
        return new ParamDefLeaf(METHOD_REF_PARAM);
      }
    }
    return null;
  }

  @Nullable
  private Collection<Function> getFunctions(ParamContext context) {
    final Module module = context.getModule();
    if (module == null) return null;

    final String version = RubyMotionUtil.getInstance().getSdkVersion(module);
    final Collection<Function> functions = mySelectors.get(version);
    if (functions == null) return null;
    return functions;
  }
}
