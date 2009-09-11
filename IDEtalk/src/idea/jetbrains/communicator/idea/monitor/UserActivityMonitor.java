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
package jetbrains.communicator.idea.monitor;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerAdapter;
import com.intellij.openapi.application.ApplicationManager;
import jetbrains.communicator.core.IDEtalkOptions;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.users.PresenceMode;
import jetbrains.communicator.core.users.UserPresence;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;

/**
 * @author Kir
 *
 * Monitors user's activity in IDEA and updates own UserPresence.
 *
 * @see Transport#setOwnPresence(UserPresence)
 */
public class UserActivityMonitor implements ApplicationComponent, Runnable {
  private static final Logger LOG = Logger.getLogger(UserActivityMonitor.class);

  private static final int REFRESH_INTERVAL = 60 * 1000;
  public static final int AWAY_MINS = 7;
  public static final int EXTENDED_AWAY_MINS = 30;

  private final Object myMonitor = new Object();
  private long myLastActionTimestamp;
  private volatile boolean myStop;
  private int myRefreshInterval = REFRESH_INTERVAL;
  private final ProjectManager myProjectManager;
  private volatile boolean myThreadDisposed = true;

  public UserActivityMonitor(ProjectManager projectManager) {
    myProjectManager = projectManager;
  }

  @NotNull
  @NonNls
  public String getComponentName() {
    return "UserActivityMonitor";
  }

  public void initComponent() {
    if (ApplicationManager.getApplication().isUnitTestMode()) return;

    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
      public boolean dispatchKeyEvent(KeyEvent e) {
        activity();
        return false;
      }
    });

    AWTEventListener listener = new AWTEventListener() {
      public void eventDispatched(AWTEvent event) {
        activity();
      }
    };

    Toolkit.getDefaultToolkit().addAWTEventListener(listener,
        AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);

    activity();
    myProjectManager.addProjectManagerListener(new ProjectManagerAdapter() {
      public void projectOpened(Project project) {
        //noinspection HardCodedStringLiteral
        new Thread(UserActivityMonitor.this, getComponentName() + " thread").start();
        myProjectManager.removeProjectManagerListener(this);
      }
    });
  }

  public void disposeComponent() {
    myStop = true;
    try {
      while (!myThreadDisposed) {
        synchronized (myMonitor) {
          myMonitor.notifyAll();
        }
        Thread.sleep(100);
      }
    }
    catch (InterruptedException ignored) {
    }
  }

  void activity() {
    synchronized (myMonitor) {
      if (System.currentTimeMillis() - myLastActionTimestamp > myRefreshInterval) {
        myLastActionTimestamp = System.currentTimeMillis();
        myMonitor.notifyAll();
      }
      myLastActionTimestamp = System.currentTimeMillis();
    }
  }


  void setRefreshInterval(int refreshInterval) {
    synchronized(myMonitor) {
      myRefreshInterval = refreshInterval;
    }
  }

  public void run() {
    try {
      myThreadDisposed = false;
      while (!myStop) {
        UserPresence userPresence = calculatePresence();
        LOG.debug("Calculated own presence: " + userPresence);
        for (Object o : Pico.getInstance().getComponentInstancesOfType(Transport.class)) {
          Transport transport = (Transport) o;
          if (transport.isOnline()) {
            transport.setOwnPresence(userPresence);
          }
        }

        synchronized (myMonitor) {
          myMonitor.wait(myRefreshInterval);
        }
      }
    }
    catch (InterruptedException e) {
      LOG.info("Interrupted");
    }
    finally {
      myThreadDisposed = true;
    }
  }

  private UserPresence calculatePresence() {
    synchronized(myMonitor) {
      double inactivitySecs = (System.currentTimeMillis() - myLastActionTimestamp)/1000.0;

      if (inactivitySecs < timeout(IDEtalkOptions.TIMEOUT_AWAY_MIN, AWAY_MINS)) {
        return new UserPresence(PresenceMode.AVAILABLE);
      }
      else if (inactivitySecs < timeout(IDEtalkOptions.TIMEOUT_XA_MIN, EXTENDED_AWAY_MINS)){
        return new UserPresence(PresenceMode.AWAY);
      }
      return new UserPresence(PresenceMode.EXTENDED_AWAY);
    }
  }

  private static double timeout(String option, int defaultVal) {
    IDEtalkOptions options = Pico.getOptions();
    if (options == null) return -1;

    return options.getNumber(option, defaultVal) * 60;
  }
}
