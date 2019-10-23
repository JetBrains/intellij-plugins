// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.ide;

import jetbrains.communicator.core.users.User;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * @author Kir
 */
public interface UserListComponent {

  void startEditing();
  boolean isSingleItemSelected();

  Container getComponent();

  /** Object is either User or String - group name. Can be mixed */
  Object[] getSelectedNodes();

  void rebuild();

  @Nullable
  User getSelectedUser();
}
