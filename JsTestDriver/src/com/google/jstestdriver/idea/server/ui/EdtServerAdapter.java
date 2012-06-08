package com.google.jstestdriver.idea.server.ui;

import com.intellij.openapi.application.ApplicationManager;

/**
 * @author Sergey Simonchik
 */
public abstract class EdtServerAdapter extends ServerAdapter {

  @Override
  public final void serverStarted() {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        serverStateChanged(true);
      }
    });
  }

  @Override
  public final void serverStopped() {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        serverStateChanged(false);
      }
    });
  }

  public abstract void serverStateChanged(final boolean started);

}
