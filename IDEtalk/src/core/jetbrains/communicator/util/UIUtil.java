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

package jetbrains.communicator.util;

import com.intellij.concurrency.JobScheduler;
import com.intellij.openapi.application.ApplicationManager;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.PresenceMode;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.ide.CanceledException;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.ProgressIndicator;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Kir
 */
@NonNls
public class UIUtil {
  public static final int BLINK_DELAY = 800;
  private static final Map<String,Icon> ourIcons = new HashMap<String, Icon>();

  private UIUtil() {
  }

  public static void run(Component component) {
    final JFrame jFrame = new JFrame();
    jFrame.getContentPane().setLayout(new BorderLayout());
    jFrame.getContentPane().add(component);
    jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    invokeLater(new Runnable() {
      public void run() {
        jFrame.pack();
        jFrame.setVisible(true);
      }
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

  public static void setupUserList(JList userListComponent, List<User> users) {

    Collections.sort(users, new Comparator<User>() {
      public int compare(User o1, User o2) {
        return compareUsers(o1, o2);
      }
    });

    userListComponent.setUI(new MultipleSelectionListUI());
    userListComponent.setListData(users.toArray());
    userListComponent.setCellRenderer(new DefaultListCellRenderer(){
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        User user = (User) value;
        Component rendererComponent = super.getListCellRendererComponent(list, user.getDisplayName(), index, isSelected, cellHasFocus);
        if (rendererComponent instanceof JLabel) {
          JLabel jLabel = (JLabel) rendererComponent;
          jLabel.setIcon(getUserIcon(user));
        }
        return rendererComponent;
      }
    });
  }

  public static Icon getUserIcon(User user) {
    return getIcon(user.getIconPath());
  }

  public static Icon getIcon(@NonNls String iconPath) {
    if (iconPath == null) return null;

    Icon icon = ourIcons.get(iconPath);
    if (icon == null) {
      URL resource = UIUtil.class.getResource(iconPath);
      assert resource != null : "Null resource for " + iconPath;
      icon = new ImageIcon(resource);
      ourIcons.put(iconPath, icon);
    }
    return icon;
  }

  public static void run(IDEFacade ideFacade, final String title, final Runnable runnable)
      throws CanceledException {

    IDEFacade.Process process = new IDEFacade.Process() {
      public void run(ProgressIndicator indicator) {
        indicator.setIndefinite(true);
        indicator.setText(title);

        Future<?> workerThreadFuture = invokeOnPooledThread(runnable);

        while (!workerThreadFuture.isDone()) {
          try {
            indicator.checkCanceled();
            indicator.setFraction(.5f); // Update indicator
            workerThreadFuture.get(100, TimeUnit.MILLISECONDS);
          }
          catch (TimeoutException e) {
            // Ok, spin a while
          }
          catch (Exception e) {
            workerThreadFuture.cancel(true);
            break;
          }
        }
      }
    };

    ideFacade.runLongProcess(title, process);
  }

  public static ScheduledFuture<?> scheduleDelayed(Runnable r, long delay, TimeUnit unit) {
    return JobScheduler.getScheduler().schedule(r, delay, unit);
  }

  private static ExecutorService ourTestExecutors;

  public static Future<?> invokeOnPooledThread(final Runnable task) {
    Future<?> workerThreadFuture;

    if (ApplicationManager.getApplication() != null) {
      workerThreadFuture = ApplicationManager.getApplication().executeOnPooledThread(task);
    } else {
      if (ourTestExecutors == null) {
        ourTestExecutors =
          new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {
            public Thread newThread(final Runnable r) {
              return new Thread(r, "IDETalk pooled thread");
            }
          });
      }
      workerThreadFuture = ourTestExecutors.submit(task);
    }
    return workerThreadFuture;
  }

  public static int compareUsers(User u1, User u2) {
    if (u1.isOnline() && !u2.isOnline()) return -1;
    if (!u1.isOnline() && u2.isOnline()) return 1;

    return u1.getDisplayName().compareTo(u2.getDisplayName());
  }

  public static void requestFocus(final Component c) {
    Timer timer = new Timer(150, new ActionListener() {
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
      public void componentResized(ComponentEvent e) {
        System.out.println("size:" + jTextArea.getSize());
        jTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        FontMetrics fm = jTextArea.getFontMetrics(jTextArea.getFont());
        int colSize = fm.charWidth('m');
        int cols = jTextArea.getSize().width / colSize;
        System.out.println("cols:" + cols);


      }
    });
    run(new JScrollPane(jTextArea));
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

  public static void setDefaultSelection(JList recepients, User[] selectedUsers) {
    List<User> selected = Arrays.asList(selectedUsers);
    final ListModel listModel = recepients.getModel();
    for (int i = 0; i < listModel.getSize(); i++) {
      if (selected.contains(listModel.getElementAt(i))) {
        recepients.addSelectionInterval(i, i);
        recepients.ensureIndexIsVisible(i);
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

  public static String getIcon(UserPresence userPresence, String onlineIcon, String dndIcon) {
    if (!userPresence.isOnline()) return "/ideTalk/offline.png";
    if (userPresence.getPresenceMode() == PresenceMode.AVAILABLE) {
      return onlineIcon;
    }
    if (userPresence.getPresenceMode() == PresenceMode.AWAY) {
      return "/ideTalk/away.png";
    }
    if (userPresence.getPresenceMode() == PresenceMode.EXTENDED_AWAY) {
      return "/ideTalk/notavailable.png";
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
