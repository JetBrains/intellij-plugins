// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.util;

import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.ui.TimerUtil;
import icons.IdeTalkCoreIcons;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.PresenceMode;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.ide.CanceledException;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.TalkProgressIndicator;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Kir
 */
@NonNls
public final class UIUtil {
  public static final int BLINK_DELAY = 800;

  private UIUtil() {
  }

  public static void run(Component component) {
    final JFrame jFrame = new JFrame();
    jFrame.getContentPane().setLayout(new BorderLayout());
    jFrame.getContentPane().add(component);
    jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    invokeLater(() -> {
      jFrame.pack();
      jFrame.setVisible(true);
    });
  }

  public static void invokeLater(Runnable r) {
    if (Pico.isUnitTest()) {
      r.run();
    }
    else {
      SwingUtilities.invokeLater(r);
    }
  }

  public static void setupUserList(JList userListComponent, List<? extends User> users) {

    users.sort((o1, o2) -> compareUsers(o1, o2));

    userListComponent.setUI(new MultipleSelectionListUI());
    userListComponent.setListData(users.toArray());
    userListComponent.setCellRenderer(new DefaultListCellRenderer(){
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        User user = (User) value;
        Component rendererComponent = super.getListCellRendererComponent(list, user.getDisplayName(), index, isSelected, cellHasFocus);
        if (rendererComponent instanceof JLabel) {
          JLabel jLabel = (JLabel) rendererComponent;
          jLabel.setIcon(user.getIcon());
        }
        return rendererComponent;
      }
    });
  }

  public static void run(final IDEFacade ideFacade, final String title, final Runnable runnable)
      throws CanceledException {

    IDEFacade.Process process = new IDEFacade.Process() {
      @Override
      public void run(TalkProgressIndicator indicator) {
        indicator.setIndefinite(true);
        indicator.setText(title);

        Future<?> workerThreadFuture = ideFacade.runOnPooledThread(runnable);

        while (!workerThreadFuture.isDone()) {
          try {
            indicator.checkCanceled();
            indicator.setFraction(0.5f); // Update indicator
            workerThreadFuture.get(100, TimeUnit.MILLISECONDS);
          }
          catch (TimeoutException ignored) {
            // Ok, spin a while
          }
          catch (ExecutionException e) {
            workerThreadFuture.cancel(true);
            throw new RuntimeException(e);
          } catch (InterruptedException ignored) {
            workerThreadFuture.cancel(true);
            break;
          }
        }
      }
    };

    ideFacade.runLongProcess(title, process);
  }

  public static int compareUsers(User u1, User u2) {
    if (u1.isOnline() && !u2.isOnline()) return -1;
    if (!u1.isOnline() && u2.isOnline()) return 1;

    return u1.getDisplayName().compareTo(u2.getDisplayName());
  }

  public static void requestFocus(final Component c) {
    Timer timer = TimerUtil.createNamedTimer("IDETalk request focus", 150, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        c.requestFocus();
      }
    });
    timer.setRepeats(false);
    timer.start();
  }

  public static void main(String[] args) {
    final JTextArea jTextArea = new JTextArea();
    jTextArea.setWrapStyleWord(true);
    jTextArea.setLineWrap(true);

    jTextArea.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        System.out.println("size:" + jTextArea.getSize());
        jTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        FontMetrics fm = jTextArea.getFontMetrics(jTextArea.getFont());
        int colSize = fm.charWidth('m');
        int cols = jTextArea.getSize().width / colSize;
        System.out.println("cols:" + cols);


      }
    });
    run(ScrollPaneFactory.createScrollPane(jTextArea));
  }

  public static void setMnemonic(JLabel label, JComponent message, char mnemonic) {
    label.setDisplayedMnemonic(mnemonic);
    label.setLabelFor(message);
  }

  public static void removeListenersToPreventMemoryLeak(Container container) {
    ContainerListener[] containerListeners = container.getContainerListeners();
    for (ContainerListener containerListener : containerListeners) {
      container.removeContainerListener(containerListener);
    }

    FocusListener[] focusListeners = container.getFocusListeners();
    for (FocusListener focusListener : focusListeners) {
      container.removeFocusListener(focusListener);
    }

    for (HierarchyListener listener : container.getHierarchyListeners()) {
      container.removeHierarchyListener(listener);
    }

    cleanupParent(container);
  }

  public static void setDefaultSelection(JList recipients, User[] selectedUsers) {
    List<User> selected = Arrays.asList(selectedUsers);
    final ListModel listModel = recipients.getModel();
    for (int i = 0; i < listModel.getSize(); i++) {
      if (selected.contains(listModel.getElementAt(i))) {
        recipients.addSelectionInterval(i, i);
        recipients.ensureIndexIsVisible(i);
      }
    }
  }

  public static boolean traverse(Component root, TraverseAction traverseAction) {
    if (traverseAction.executeAndContinue(root)) {
      if (root instanceof Container) {
        Component[] children = ((Container) root).getComponents();
        for (Component child : children) {
          if (!traverse(child, traverseAction))
            return false;
        }
      }
      return true;
    }
    return false;
  }

  public static Icon getIcon(UserPresence userPresence, Icon onlineIcon, Icon dndIcon) {
    if (!userPresence.isOnline()) return IdeTalkCoreIcons.IdeTalk.Offline;
    if (userPresence.getPresenceMode() == PresenceMode.AVAILABLE) {
      return onlineIcon;
    }
    if (userPresence.getPresenceMode() == PresenceMode.AWAY) {
      return IdeTalkCoreIcons.IdeTalk.Away;
    }
    if (userPresence.getPresenceMode() == PresenceMode.EXTENDED_AWAY) {
      return IdeTalkCoreIcons.IdeTalk.Notavailable;
    }
    if (userPresence.getPresenceMode() == PresenceMode.DND) {
      return dndIcon;
    }
    return null;
  }

  public interface TraverseAction {
    boolean executeAndContinue(Component c);
  }


  private static void cleanupParent(Component component) {
    Container parent = component.getParent();
    if (parent != null) {
      parent.remove(component);
    }
  }

  public static void cleanupActions(Component component) {
    if (component instanceof AbstractButton) {
      AbstractButton button = (AbstractButton) component;
      for (ActionListener actionListener : button.getActionListeners()) {
        button.removeActionListener(actionListener);
      }
      button.setAction(null);
    }
    if (component instanceof Container) {
      Container container = (Container) component;

      for (Component component1 : container.getComponents()) {
        cleanupActions(component1);
      }
    }

    cleanupParent(component);
  }

  public static void runWhenShown(final Component c, final Runnable r) {
    if (c.isShowing()) {
      invokeLater(r);
      return;
    }

    final HierarchyListener[] l = new HierarchyListener[1];
    final HierarchyListener hierarchyListener = new HierarchyListener() {
      @Override
      public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) > 0 && c.isShowing()) {
          invokeLater(r);
          c.removeHierarchyListener(l[0]);
        }
      }
    };
    l[0] = hierarchyListener;

    c.addHierarchyListener(hierarchyListener);
  }
}