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
