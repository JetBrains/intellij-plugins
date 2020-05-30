// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.toolWindow;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.communicator.commands.SendMessageCommand;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.idea.actions.BaseAction;
import jetbrains.communicator.util.TreeUtils;
import org.jetbrains.annotations.Nullable;

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
  private static final Logger LOG = Logger.getInstance(UserTreeTransferHandler.class);
  public static final Pattern EXCEPTION_PATTERN = Pattern.compile("\\s+at ");
  private final UserModel myUserModel;

  public UserTreeTransferHandler(UserModel userModel) {
    myUserModel = userModel;
  }

  @Override
  protected Transferable createTransferable(JComponent c) {
    return new UsersTransferable(getTree(c));
  }

  @Override
  public int getSourceActions(JComponent c) {
    return MOVE;
  }

  @Override
  public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
    List<DataFlavor> flavors = Arrays.asList(transferFlavors);
    return flavors.contains(UsersTransferable.getMyDataFlavor()) || (flavors.contains(DataFlavor.stringFlavor));
  }

  @Override
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
    for (User movedUser : movedUsers) {
      movedUser.setGroup(targetGroup, myUserModel);
    }
  }

  private static String getTargetGroup(JComponent comp) {
    final Object userObject = getUserObject(comp);
    String group = userObject != null ? userObject.toString() : UserModel.DEFAULT_GROUP;
    if (userObject instanceof User) {
      User user = (User) userObject;
      group = user.getGroup();
    }
    return group;
  }

  @Nullable
  private static Object getUserObject(JComponent comp) {
    final TreePath path = getTree(comp).getSelectionPath();
    return path != null ? TreeUtils.getUserObject(path) : null;
  }

  private static JTree getTree(JComponent c) {
    return ((JTree) c);
  }

  public static class UsersTransferable implements Transferable {
    private final List<User> myUsers = new ArrayList<>();
    private static DataFlavor ourDataFlavor;

    public UsersTransferable(JTree tree) {
      final TreePath[] selectionPaths = tree.getSelectionPaths();
      if (selectionPaths != null) {
        for (TreePath path : selectionPaths) {
          final Object userObject = TreeUtils.getUserObject(path.getLastPathComponent());
          if (userObject instanceof User) {
            myUsers.add((User)userObject);
          }
        }
      }
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
      if (myUsers.size() > 0)
        return new DataFlavor[] {DataFlavor.stringFlavor, getMyDataFlavor()};
      else
        return new DataFlavor[0];
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return Arrays.asList(getTransferDataFlavors()).contains(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {

      if (flavor == getMyDataFlavor()) {
        return myUsers.toArray(new User[0]);
      }
      else if (flavor == DataFlavor.stringFlavor) {
        StringBuilder sb = new StringBuilder(10 * myUsers.size());
        if (myUsers.size() > 0) {
          sb.append((myUsers.get(0)).getDisplayName());
          for (int i = 1; i < myUsers.size(); i++) {
            sb.append('\n');
            sb.append((myUsers.get(i)).getDisplayName());
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
