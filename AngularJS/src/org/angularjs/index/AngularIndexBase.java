package org.angularjs.index;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.psi.stubs.StringStubIndexExtension;

/**
 * @author Dennis.Ushakov
 */
public abstract class AngularIndexBase extends StringStubIndexExtension<JSImplicitElementProvider> {
  private static final int VERSION = 1;
  @Override
  public int getVersion() {
    return VERSION;
  }
}
