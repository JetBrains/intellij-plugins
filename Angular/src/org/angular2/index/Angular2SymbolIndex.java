package org.angular2.index;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public class Angular2SymbolIndex extends Angular2IndexBase<JSImplicitElementProvider> {
  public static final StubIndexKey<String, JSImplicitElementProvider> KEY = StubIndexKey.createIndexKey("angular2.symbol.index");

  @Override
  public @NotNull StubIndexKey<String, JSImplicitElementProvider> getKey() {
    return KEY;
  }
}
