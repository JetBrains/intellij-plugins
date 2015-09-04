package org.jetbrains.plugins.ruby.motion.symbols;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.v2.ClassModuleSymbolWithMixins;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;

import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class MotionEnabledClassModuleSymbol extends ClassModuleSymbolWithMixins {

  public MotionEnabledClassModuleSymbol(@NotNull RContainer container,
                                        @Nullable List<Symbol> implicitIncludes,
                                        final List<Symbol> implicitExtends,
                                        final List<Symbol> implicitSuperclasses) {
    super(container, null, implicitIncludes, implicitExtends, implicitSuperclasses);
  }
}
