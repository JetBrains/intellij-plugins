/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.communicator.mock;

import jetbrains.communicator.core.users.User;
import jetbrains.communicator.ide.UserListComponent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * @author Kir
 */
public class MockUserListComponent implements UserListComponent {
  private Object[] mySelectedNodes = new Object[0];

  public void setSelectedNodes(Object[] selectedNodes) {
    mySelectedNodes = selectedNodes;
  }

  public void startEditing() {
  }

  public void rebuild() {
  }

  public boolean isSingleItemSelected() {
    return false;
  }

  public void dispose() {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  public Container getComponent() {
    return null;
  }

  public Object[] getSelectedNodes() {
    return mySelectedNodes;
  }

  @Nullable
  public User getSelectedUser() {
    Object[] nodes = getSelectedNodes();
    if (nodes.length == 1 && nodes[0]instanceof User)
      return (User) nodes[0];
    return null;
  }
}
