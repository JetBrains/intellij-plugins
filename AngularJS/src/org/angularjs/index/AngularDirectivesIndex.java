package org.angularjs.index;

import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularDirectivesIndex extends AngularIndexBase {
  public static final ID<String, byte[]> INDEX_ID = ID.create("angularjs.directives.index");

  @NotNull
  @Override
  public ID<String, byte[]> getName() {
    return INDEX_ID;
  }
}
