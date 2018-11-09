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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.RubyMotionSymbolProvider;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtilImpl;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Class;
import org.jetbrains.plugins.ruby.motion.bridgesupport.*;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.fqn.FQN;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.RTypedSyntheticSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RTypeFactory;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.impl.REmptyType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.impl.RSymbolTypeImpl;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.ArgumentInfo;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @author Dennis.Ushakov
 */
public class MotionSymbolUtil {
  private static final Set<String> FLOAT_TYPES;
  private static final Set<String> INT_TYPES;
  static {
    final HashSet<String> floatTypes = new HashSet<>();
    Collections.addAll(floatTypes, "float", "double", "Float32", "Float64", "CGFloat");
    FLOAT_TYPES = Collections.unmodifiableSet(floatTypes);

    final HashSet<String> intTypes = new HashSet<>();
    Collections.addAll(intTypes, "int", "short", "char", "long", "long long", "Byte", "SignedByte", "NSInteger", "NSUInteger", "size_t");
    INT_TYPES = Collections.unmodifiableSet(intTypes);
  }

  public static RTypedSyntheticSymbol createFunctionSymbol(@NotNull final Module module,
                                                           @Nullable final MotionClassSymbol parent,
                                                           final Function function) {
    return createFunctionSymbol(module, parent, function, getFunctionName(function));
  }

  public static List<RTypedSyntheticSymbol> createSelectorSymbols(@NotNull final Module module,
                                                                  @Nullable final MotionClassSymbol parent,
                                                                  final Function function) {
    final List<RTypedSyntheticSymbol> result = new ArrayList<>();
    for (String name : getSelectorNames(function)) {
      result.add(createFunctionSymbol(module, parent, function, name));
    }
    return result;
  }

  static RTypedSyntheticSymbol createFunctionSymbol(final Module module,
                                                    @Nullable final MotionClassSymbol parent,
                                                    final Function function,
                                                    final String name) {
    final RType rType = getTypeByName(module, function.getReturnValue());
    return new FunctionSymbol(module, name, parent, rType, function);
  }

  public static RTypedSyntheticSymbol createConstantSymbol(final Module module,
                                                           final Constant constant) {
    final String name = getConstantName(constant);
    return new ConstantSymbol(module, constant, name, REmptyType.INSTANCE);
  }

  public static Symbol[] createStructFieldSymbols(final Module module,
                                                  Symbol parent,
                                                  Struct struct,
                                                  String name) {
    final String typeName = struct.getFieldType(name);
    final RType type = getTypeByName(module, typeName);
    final RTypedSyntheticSymbol reader = new RTypedSyntheticSymbol(module.getProject(), name, Type.FIELD_READER, parent, type, Collections.emptyList());
    final RTypedSyntheticSymbol writer = new RTypedSyntheticSymbol(module.getProject(), name + "=", Type.FIELD_WRITER, parent, type,
                                                                   Collections.singletonList(new ArgumentInfo("value", ArgumentInfo.Type.SIMPLE)));
    return new Symbol[] {reader, writer};
  }

  private static String getConstantName(final Constant constant) {
    return StringUtil.capitalize(constant.getName());
  }

  public static RType getTypeByName(Module module, final String typeName) {
    return MotionTypeCache.getInstance(module).getType(typeName);
  }

  @NotNull
  private static RType doGetTypeByName(@Nullable Module module, String typeName) {
    if (module == null) {
      return REmptyType.INSTANCE;
    }
    final Project project = module.getProject();
    if (typeName.endsWith("*")) {
      final RType type = doGetTypeByName(module, dereferencePointerType(typeName));
      if (type != REmptyType.INSTANCE || "void".equals(dereferencePointerType(typeName))) {
        return RTypeFactory.createArrayType(project, type);
      }
    }

    final RType primitiveType = getPrimitiveType(project, typeName);
    if (primitiveType != null) {
      return primitiveType;
    }
    final Collection<Framework> frameworks = ((RubyMotionUtilImpl)RubyMotionUtil.getInstance()).getFrameworks(module);
    if (!typeName.endsWith("*")) {
      final Symbol symbol = RubyMotionSymbolProvider.findClassOrStruct(module, frameworks, FQN.Builder.fromString(typeName).asList());
      return symbol instanceof StructSymbol || (symbol != null && RubyMotionUtil.getInstance().isAndroid(module)) ?
             new RSymbolTypeImpl(symbol, Context.INSTANCE) : REmptyType.INSTANCE;
    }
    typeName = dereferencePointerType(typeName);
    final Symbol symbol = RubyMotionSymbolProvider.findClassOrStruct(module, frameworks, Collections.singletonList(typeName));
    return symbol != null ? new RSymbolTypeImpl(symbol, Context.INSTANCE) : REmptyType.INSTANCE;
  }

  @NotNull
  private static String dereferencePointerType(String typeName) {
    return typeName.substring(0, typeName.length() - 1).trim();
  }

  @Nullable
  private static RType getPrimitiveType(final Project project, final String typeName) {
    if ("void".equals(typeName)) {
      return REmptyType.INSTANCE;
    }
    if ("BOOL".equals(typeName) || "bool".equals(typeName)) {
      return RTypeFactory.createBoolType(project);
    }
    if (FLOAT_TYPES.contains(typeName)) {
      return RTypeFactory.createFloatType(project);
    }
    if (INT_TYPES.contains(typeName) || INT_TYPES.contains(typeName.replace("unsigned ", "")) ||
        typeName.matches("[SU]?Int\\d*") || typeName.matches("u?int\\d*_t")) {
      return RTypeFactory.createIntType(project);
    }
    return null;
  }

  private static String getFunctionName(Function function) {
    final String name = function.getName();
    final int index = name.indexOf(':');
    return index >= 0 ? name.substring(0, index) : name;
  }

  public static List<String> getSelectorNames(Function function) {
    final List<String> names = new ArrayList<>();
    names.add(getFunctionName(function));
    final String name = function.getName();
    final int argsSize = function.getArguments().size();
    getSelectorNames(names, name, argsSize);
    return names;
  }

  public static void getSelectorNames(List<? super String> names, String name, int argsSize) {
    if (argsSize == 2 && name.startsWith("set") && name.endsWith(":forKey:")) {
      names.add("[]=");
    } else if (argsSize == 1 && name.endsWith("ForKey:")) {
      names.add("[]");
    } else if (name.startsWith("is") && !name.endsWith(":")) {
      names.add(StringUtil.decapitalize(name.substring(2)) + "?");
    } else if (name.startsWith("set") && name.endsWith(":")) {
      names.add(StringUtil.decapitalize(name.substring(3, name.length() - 1)) + "=");
    }
  }

  public static class MotionTypeCache {
    private final Module myModule;
    private final LoadingCache<String, RType> myCache = CacheBuilder.newBuilder().initialCapacity(512).maximumSize(1024).
      build(CacheLoader.from(new com.google.common.base.Function<String, RType>() {
        @Override
        public RType apply(@Nullable String typeName) {
          return doGetTypeByName(myModule, typeName);
        }
      }));

    public static MotionTypeCache getInstance(final Module module) {
      return ModuleServiceManager.getService(module, MotionTypeCache.class);
    }

    public MotionTypeCache(final Module module) {
      myModule = module;
    }

    public RType getType(final String typeName) {
      try {
        return myCache.get(typeName);
      }
      catch (ExecutionException ignored) { }
      catch (UncheckedExecutionException e) {
        final Throwable cause = e.getCause();
        if (cause instanceof ProcessCanceledException) {
          throw (ProcessCanceledException)cause;
        }
      }
      return REmptyType.INSTANCE;
    }

    public void reset() {
      myCache.invalidateAll();
    }
  }

  public static class MotionSymbolsCache {
    private final Module myModule;
    private volatile Set<Symbol> mySymbols;

    public static MotionSymbolsCache getInstance(final Module module) {
      return ModuleServiceManager.getService(module, MotionSymbolsCache.class);
    }

    public MotionSymbolsCache(final Module module) {
      myModule = module;
    }

    private void processMotionSymbols(final Set<Symbol> symbols) {
      final Collection<Framework> frameworks = ((RubyMotionUtilImpl)RubyMotionUtil.getInstance()).getFrameworks(myModule);
      processClasses(frameworks, symbols);
      processStructs(frameworks, symbols);
      processConstants(frameworks, symbols);
      processFunctions(frameworks, symbols);
    }

    private void processStructs(Collection<Framework> frameworks, Set<Symbol> symbols) {
      for (Framework framework : frameworks) {
        for (Struct struct : framework.getStructs()) {
          symbols.add(new StructSymbol(myModule, struct));
        }
      }
    }

    private void processFunctions(Collection<Framework> frameworks, Set<Symbol> symbols) {
      for (Framework framework : frameworks) {
        for (Function function : framework.getFunctions()) {
          symbols.add(createFunctionSymbol(myModule, null, function));
        }
        for (Map.Entry<String, String> entry : framework.getFunctionAliases().entrySet()) {
          final Function function = framework.getFunction(entry.getValue());
          if (function != null) {
            symbols.add(createFunctionSymbol(myModule, null, function, entry.getKey()));
          }
        }
      }
    }

    private void processConstants(Collection<Framework> frameworks, Set<Symbol> symbols) {
      for (Framework framework : frameworks) {
        for (Constant constant : framework.getConstants()) {
          symbols.add(createConstantSymbol(myModule, constant));
        }
      }
    }

    private void processClasses(Collection<Framework> frameworks, Set<Symbol> symbols) {
      final MultiMap<String, Class> classesMap = new MultiMap<>();
      for (Framework framework : frameworks) {
        for (Class clazz : framework.getClasses()) {
          classesMap.putValue(clazz.getName(), clazz);
        }
      }

      for (Map.Entry<String, Collection<Class>> entry : classesMap.entrySet()) {
        symbols.add(new MotionClassSymbol(myModule, (List<Class>)entry.getValue()));
      }
    }

    public Set<Symbol> getSymbols() {
      if (mySymbols == null) {
        final Set<Symbol> symbols = new LinkedHashSet<>();
        processMotionSymbols(symbols);
        mySymbols = symbols;
      }
      return mySymbols;
    }

    public void reset() {
      mySymbols = null;
    }
  }
}
