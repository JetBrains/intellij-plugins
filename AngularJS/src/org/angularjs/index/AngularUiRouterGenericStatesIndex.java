package org.angularjs.index;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public class AngularUiRouterGenericStatesIndex extends AngularIndexBase {
  public static final StubIndexKey<String, JSImplicitElementProvider> KEY =
    StubIndexKey.createIndexKey("angularjs.ui.router.generic.states.index");

  @Override
  public @NotNull StubIndexKey<String, JSImplicitElementProvider> getKey() {
    return KEY;
  }
}
