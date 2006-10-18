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
package jetbrains.communicator.idea.toolWindow;

import jetbrains.communicator.commands.SendMessageCommand;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.idea.actions.BaseAction;
import jetbrains.communicator.util.TreeUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Kir
 */
public class UserTreeTransferHandler extends TransferHandler {
  private static final Logger LOG = Logger.getLogger(UserTreeTransferHandler.class);
  public static final Pattern EXCEPTION_PATTERN = Pattern.compile("\\s+at ");
  private UserModel myUserModel;

  public UserTreeTransferHandler(UserModel userModel) {
    myUserModel = userModel;
  }

  protected Transferable createTransferable(JComponent c) {
    return new UsersTransferable(getTree(c));
  }

  public int getSourceActions(JComponent c) {
    return MOVE;
  }

  public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
    List<DataFlavor> flavors = Arrays.asList(transferFlavors);
    return flavors.contains(UsersTransferable.getMyDataFlavor()) || (flavors.contains(DataFlavor.stringFlavor));
  }

  public boolean importData(JComponent comp, Transferable t) {
    if (canImport(comp, t.getTransferDataFlavors())) {

      try {

        if (Arrays.asList(t.getTransferDataFlavors()).contains(UsersTransferable.getMyDataFlavor())) {
          processUsersImport(comp, t);
          return true;
        }
        else { // import text
          String text = t.getTransferData(DataFlavor.stringFlavor).toString();

          return processTextImport(text, comp);
        }
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
    }
    return false;
  }

  private static boolean processTextImport(String text, Component c) {
    SendMessageCommand command =
            Pico.getCommandManager().getCommand(SendMessageCommand.class, BaseAction.getContainer(c));
    command.setMessage(text);
    if (command.isEnabled()) {
      command.execute();
      return true;
    }

    return false;
  }

  private void processUsersImport(JComponent comp, Transferable t) throws UnsupportedFlavorException, IOException {
    final String targetGroup = getTargetGroup(comp);
    final User[] movedUsers = (User[]) t.getTransferData(UsersTransferable.getMyDataFlavor());
    for (int i = 0; i < movedUsers.length; i++) {
      User movedUser = movedUsers[i];
      movedUser.setGroup(targetGroup, myUserModel);
    }
  }

  private String getTargetGroup(JComponent comp) {
    final Object userObject = getUserObject(comp);
    String group = userObject.toString();
    if (userObject instanceof User) {
      User user = (User) userObject;
      group = user.getGroup();
    }
    return group;
  }

  private static Object getUserObject(JComponent comp) {
    final TreePath path = getTree(comp).getSelectionPath();
    return TreeUtils.getUserObject(path);
  }

  private static JTree getTree(JComponent c) {
    return ((JTree) c);
  }

  public static class UsersTransferable implements Transferable {
    private List myUsers = new ArrayList();
    private static DataFlavor ourDataFlavor;

    public UsersTransferable(JTree tree) {
      final TreePath[] selectionPaths = tree.getSelectionPaths();
      if (selectionPaths != null) {
        for (int i = 0; i < selectionPaths.length; i++) {
          TreePath path = selectionPaths[i];
          final Object userObject = TreeUtils.getUserObject(path.getLastPathComponent());
          if (userObject instanceof User) {
            myUsers.add(userObject);
          }
        }
      }
    }
    
    public DataFlavor[] getTransferDataFlavors() {
      if (myUsers.size() > 0)
        return new DataFlavor[] {DataFlavor.stringFlavor, getMyDataFlavor()};
      else
        return new DataFlavor[0];
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return Arrays.asList(getTransferDataFlavors()).contains(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {

      if (flavor == getMyDataFlavor()) {
        return myUsers.toArray(new User[myUsers.size()]);
      }
      else if (flavor == DataFlavor.stringFlavor) {
        StringBuffer sb = new StringBuffer(10 * myUsers.size());
        if (myUsers.size() > 0) {
          sb.append(((User) myUsers.get(0)).getDisplayName());
          for (int i = 1; i < myUsers.size(); i++) {
            sb.append('\n');
            sb.append(((User) myUsers.get(i)).getDisplayName());
          }
        }
        return sb.toString();
      }
      throw new UnsupportedFlavorException(flavor);
    }

    private static DataFlavor getMyDataFlavor() {
      if (ourDataFlavor != null) return ourDataFlavor;
      try {
        ourDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + Map.class.getName());
        return ourDataFlavor;
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
