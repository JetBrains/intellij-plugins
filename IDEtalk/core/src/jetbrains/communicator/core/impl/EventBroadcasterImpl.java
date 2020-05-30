// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.impl;

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.core.IDEtalkListener;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Kir Maximov
 */
public class EventBroadcasterImpl implements EventBroadcaster {
  private static final Logger LOG = Logger.getLogger(EventBroadcasterImpl.class);
  public static final Runnable NO_ACTION = () -> { };

  private final List<IDEtalkListener> myListeners = new CopyOnWriteArrayList<>();

  @Override
  public void addListener(IDEtalkListener listener) {
    assert !myListeners.contains(listener);
    myListeners.add(listener);
  }

  @Override
  public void removeListener(IDEtalkListener listener) {
    myListeners.remove(listener);
  }

  @Override
  public void doChange(@NotNull IDEtalkEvent event, Runnable action) {
    try {
      fireBeforeChange(event);
      action.run();
    }
    finally {
      fireAfterChange(event);
    }
  }

  @Override
  public void fireEvent(@NotNull IDEtalkEvent event) {
    doChange(event, NO_ACTION);
  }

  private void fireBeforeChange(IDEtalkEvent event) {
    for (IDEtalkListener listener : myListeners) {
      listener.beforeChange(event);
    }
  }

  private void fireAfterChange(IDEtalkEvent event) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("afterChange: " + event);
    }
    for (IDEtalkListener listener : myListeners) {
      listener.afterChange(event);
    }
  }

  @TestOnly
  IDEtalkListener[] getListeners() {
    return myListeners.toArray(new IDEtalkListener[0]);
  }

  @TestOnly
  void clearListeners() {
    myListeners.clear();
  }

}
