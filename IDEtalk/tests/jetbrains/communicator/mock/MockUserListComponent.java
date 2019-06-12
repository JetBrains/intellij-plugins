// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.mock;

import com.intellij.util.ArrayUtilRt;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.ide.UserListComponent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * @author Kir
 */
public class MockUserListComponent implements UserListComponent {
  private Object[] mySelectedNodes = ArrayUtilRt.EMPTY_OBJECT_ARRAY;

  public void setSelectedNodes(Object[] selectedNodes) {
    mySelectedNodes = selectedNodes;
  }

  @Override
  public void startEditing() {
  }

  @Override
  public void rebuild() {
  }

  @Override
  public boolean isSingleItemSelected() {
    return false;
  }

  public void dispose() {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public Container getComponent() {
    return null;
  }

  @Override
  public Object[] getSelectedNodes() {
    return mySelectedNodes;
  }

  @Override
  @Nullable
  public User getSelectedUser() {
    Object[] nodes = getSelectedNodes();
    if (nodes.length == 1 && nodes[0]instanceof User)
      return (User) nodes[0];
    return null;
  }
}
