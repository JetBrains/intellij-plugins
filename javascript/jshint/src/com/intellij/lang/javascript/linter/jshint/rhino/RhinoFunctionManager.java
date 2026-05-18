package com.intellij.lang.javascript.linter.jshint.rhino;

import com.google.common.base.Supplier;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

/**
 * @author Sergey Simonchik
 */
public class RhinoFunctionManager {

  private static final Logger LOG = Logger.getInstance(RhinoFunctionManager.class);

  private final ThreadLocal<FunctionWithScope> myThreadLocalFunction = new ThreadLocal<>() {
    @Override
    protected FunctionWithScope initialValue() {
      if (myScript == null) {
        synchronized (myThreadLocalFunction) {
          if (myScript == null) {
            myScript = compileScript(9);
          }
        }
      }
      return extractFunctionWithScope(myScript);
    }
  };

  private volatile Script myScript;

  private final Supplier<String> myScriptSourceProvider;
  private final String myFunctionName;
  private final String myVersion;

  public RhinoFunctionManager(@NotNull Supplier<String> scriptSourceProvider,
                              @NotNull String functionName,
                              @Nullable String version) {
    myScriptSourceProvider = scriptSourceProvider;
    myFunctionName = functionName;
    myVersion = version;
  }

  public @NotNull String getFunctionName() {
    return myFunctionName;
  }

  private Script compileScript(int optimizationLevel) {
    long startNano = System.nanoTime();
    Context context = Context.enter();
    try {
      context.setOptimizationLevel(optimizationLevel);
      String scriptSource = myScriptSourceProvider.get();
      return context.compileString(scriptSource, "<" + myFunctionName + " script>", 1, null);
    } finally {
      Context.exit();
      LOG.info(formatMessage(startNano, prependVersion("script rhino compilation")));
    }
  }

  private @NotNull String prependVersion(@NotNull String message) {
    StringBuilder out = new StringBuilder(myFunctionName).append(" ");
    if (myVersion != null) {
      out.append(myVersion).append(" ");
    }
    out.append(message);
    return out.toString();
  }

  private @NotNull FunctionWithScope extractFunctionWithScope(@NotNull Script script) {
    long startNano = System.nanoTime();
    Context context = Context.enter();
    try {
      Scriptable scope = context.initStandardObjects();
      script.exec(context, scope);
      Object jsLintObj = scope.get(myFunctionName, scope);
      if (jsLintObj instanceof Function jsLint) {
        return new FunctionWithScope(jsLint, scope);
      } else {
        throw new RuntimeException(prependVersion("is undefined or not a function."));
      }
    } finally {
      Context.exit();
      LOG.info(formatMessage(startNano, prependVersion("function extraction")));
    }
  }

  private static String formatMessage(long startTimeNano, @NotNull String actionName) {
    long nanoDuration = System.nanoTime() - startTimeNano;
    return String.format("[%s] %s took %.2f ms",
                         Thread.currentThread().getName(),
                         actionName,
                         nanoDuration / 1000000.0);
  }

  public @NotNull FunctionWithScope getFunctionWithScope() {
    return myThreadLocalFunction.get();
  }

}
