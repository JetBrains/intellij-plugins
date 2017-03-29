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
