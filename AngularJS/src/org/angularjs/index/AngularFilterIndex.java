package org.angularjs.index;

import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularFilterIndex extends AngularIndexBase {
  public static final ID<String, Void> INDEX_ID = ID.create("angularjs.filter.index");

  @NotNull
  @Override
  public ID<String, Void> getName() {
    return INDEX_ID;
  }
}
