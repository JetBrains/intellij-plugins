package org.jetbrains.plugins.cucumber.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
 */
public class GherkinElementType extends IElementType {
  public GherkinElementType(@NotNull @NonNls String debugName) {
    super(debugName, GherkinLanguage.INSTANCE);
  }
}
