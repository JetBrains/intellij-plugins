package org.jetbrains.plugins.ruby.motion.symbols;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.v2.SymbolPsiProcessor;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.v2.TopLevelSymbol;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionSymbol extends TopLevelSymbol {
  private final Module myModule;

  public RubyMotionSymbol(@NotNull RFile file) {
    super(file);
    myModule = ModuleUtilCore.findModuleForPsiElement(file);
  }

  @Override
  protected boolean processChildrenInner(final SymbolPsiProcessor processor, final PsiElement invocationPoint) {
    final Module module = myModule;
    if (module != null) {
      for (Symbol symbol : MotionSymbolUtil.MotionSymbolsCache.getInstance(module).getSymbols()) {
        if (!processor.process(symbol)) {
          return false;
        }
      }
    }
    return true;
  }

  @Nullable
  @Override
  public Module getModule() {
    return myModule;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RubyMotionSymbol that = (RubyMotionSymbol)o;

    if (myModule != null ? !myModule.equals(that.myModule) : that.myModule != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return 31 + (myModule != null ? myModule.hashCode() : 0);
  }
}
