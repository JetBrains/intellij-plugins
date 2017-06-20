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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.hash.HashMap;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.plugins.ruby.motion.bridgesupport.BridgeSupportLoader;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Class;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Framework;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Function;
import org.jetbrains.plugins.ruby.motion.symbols.MotionSymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.AnyParamDef;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamDefManager;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamDefProvider;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.matcher.ParamDefExpression;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.matcher.ParamDefHash;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.matcher.ParamDefLeaf;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.matcher.ParamDefSeq;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.fqn.FQN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionParamdefsProvider implements ParamDefProvider {
  private static boolean ourParamdefsLoaded = false;

  @Override
  public void registerParamDefs(final ParamDefManager manager) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      doRegisterParamdefs(manager);
    }
  }

  private static void doRegisterParamdefs(ParamDefManager manager) {
    final Map<String, Map<String, Collection<Function>>> mergedFunctions = new HashMap<>();
    BridgeSupportLoader.getInstance().processFrameworks(framework -> loadAvailableSelectors(framework, mergedFunctions));
    for (Map.Entry<String, Map<String, Collection<Function>>> entry : mergedFunctions.entrySet()) {
      registerParamDef(manager, entry.getKey(), entry.getValue());
    }
  }

  private static void loadAvailableSelectors(Framework framework, Map<String, Map<String, Collection<Function>>> mergedFunctions) {
    final String version = framework.getVersion();
    for (Class clazz : framework.getClasses()) {
      for (Function function : clazz.getFunctions()) {
        if (!canHaveParamdef(function)) continue;
        final String name = clazz.getName() + "." + MotionSymbolUtil.getSelectorNames(function).get(0);
        Map<String, Collection<Function>> allFunctions = mergedFunctions.get(name);
        if (allFunctions == null) {
          allFunctions = new HashMap<>();
          mergedFunctions.put(name, allFunctions);
        }
        Collection<Function> frameworkFunctions = allFunctions.get(version);
        if (frameworkFunctions == null) {
          frameworkFunctions = new ArrayList<>();
          allFunctions.put(version, frameworkFunctions);
        }
        frameworkFunctions.add(function);
      }
    }
  }

  private static boolean canHaveParamdef(Function function) {
    final List<Pair<String,String>> arguments = function.getArguments();
    return arguments.size() >= 2 || (arguments.size() == 1 && "SEL".equals(arguments.get(0).second));
  }

  private static void registerParamDef(final ParamDefManager manager,
                                       final String name,
                                       final Map<String, Collection<Function>> functions) {
    final FQN fqn = FQN.Builder.fromString(name);
    if (manager.getParamDefExpression(fqn) == null) {
      manager.registerParamDefExpression(fqn, buildExpression(functions));
    }
  }

  private static ParamDefExpression buildExpression(final Map<String, Collection<Function>> functions) {
    boolean hadOneArg = false;
    boolean hadMoreArg = false;
    for (Collection<Function> collection : functions.values()) {
      for (Function function : collection) {
        hadOneArg |= function.getArguments().size() == 1;
        hadMoreArg |= function.getArguments().size() > 1;
      }
    }
    if (hadOneArg && !hadMoreArg) {
      return new ParamDefLeaf(SelectorKeysProvider.METHOD_REF_PARAM);
    }
    final ParamDefHash hash = new ParamDefHash(false, true, null);
    final SelectorKeysProvider provider = new SelectorKeysProvider(functions);
    hash.addKey(provider, new ParamDefLeaf(AnyParamDef.getInstance()));
    return new ParamDefSeq(new ParamDefLeaf(hadOneArg ? SelectorKeysProvider.METHOD_REF_PARAM : AnyParamDef.getInstance()), hash);
  }

  public static void ensureParamdefsLoaded() {
    if (ourParamdefsLoaded) return;
    doRegisterParamdefs(ParamDefManager.getInstance());
    ourParamdefsLoaded = true;
  }

  @TestOnly
  public static void reset() {
    ourParamdefsLoaded = false;
  }
}
