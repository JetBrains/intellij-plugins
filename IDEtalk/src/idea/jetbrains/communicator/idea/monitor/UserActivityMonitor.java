// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.monitor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.ProjectUtil;
import jetbrains.communicator.core.IDEtalkOptions;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.users.PresenceMode;
import jetbrains.communicator.core.users.UserPresence;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.AWTEventListener;

/**
 * @author Kir
 *
 * Monitors user's activity in IDEA and updates own UserPresence.
 *
 * @see Transport#setOwnPresence(UserPresence)
 */
public final class UserActivityMonitor implements Disposable, Runnable {
  private static final Logger LOG = Logger.getInstance(UserActivityMonitor.class);

  private static final int REFRESH_INTERVAL = 60 * 1000;
  public static final int AWAY_MINS = 7;
  public static final int EXTENDED_AWAY_MINS = 30;

  private final Object myMonitor = new Object();
  private long myLastActionTimestamp;
  private volatile boolean myStop;
  private volatile boolean myThreadDisposed = true;

  UserActivityMonitor() {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return;
    }

    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
      activity();
      return false;
    });

    AWTEventListener listener = new AWTEventListener() {
      @Override
      public void eventDispatched(AWTEvent event) {
        activity();
      }
    };

    Toolkit.getDefaultToolkit().addAWTEventListener(listener,
        AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);

    activity();

    ProjectUtil.runWhenProjectOpened(project -> {
      Thread t = new Thread(this, getClass().getSimpleName() + " thread");
      t.setDaemon(true);
      t.start();
    });
  }

  @Override
  public void dispose() {
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
      if (System.currentTimeMillis() - myLastActionTimestamp > REFRESH_INTERVAL) {
        myLastActionTimestamp = System.currentTimeMillis();
        myMonitor.notifyAll();
      }
      myLastActionTimestamp = System.currentTimeMillis();
    }
  }

  @Override
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
          myMonitor.wait(REFRESH_INTERVAL);
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

  @NotNull
  private UserPresence calculatePresence() {
    synchronized(myMonitor) {
      double inactivitySecs = (System.currentTimeMillis() - myLastActionTimestamp)/1000.0;

      if (inactivitySecs < timeout(IDEtalkOptions.TIMEOUT_AWAY_MIN, AWAY_MINS)) {
        return new UserPresence(PresenceMode.AVAILABLE);
      }
      if (inactivitySecs < timeout(IDEtalkOptions.TIMEOUT_XA_MIN, EXTENDED_AWAY_MINS)){
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
