package org.angularjs.index;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public class AngularUiRouterStatesIndex extends AngularIndexBase {
  public static final StubIndexKey<String, JSImplicitElementProvider> KEY = StubIndexKey.createIndexKey("angularjs.ui.router.states.index");

  @Override
  public @NotNull StubIndexKey<String, JSImplicitElementProvider> getKey() {
    return KEY;
  }
}
