package org.angularjs.index;

import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularSymbolIndex extends AngularIndexBase {
  public static final ID<String, Void> INDEX_ID = ID.create("angularjs.symbol.index");

  @NotNull
  @Override
  public ID<String, Void> getName() {
    return INDEX_ID;
  }
}
