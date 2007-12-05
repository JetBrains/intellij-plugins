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
package jetbrains.communicator.core.impl.dispatcher;

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.IDEtalkAdapter;
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.core.dispatcher.AsyncMessageDispatcher;
import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserEvent;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.WaitFor;
import org.apache.log4j.Logger;

/**
 * @author Kir
 */
public class AsyncMessageDispatcherImpl extends AbstractMessageDispatcher implements AsyncMessageDispatcher, Runnable {
  private static final Logger LOG = Logger.getLogger(AsyncMessageDispatcherImpl.class);

  private static final String FILE_NAME = "pendingNetworkMessages.xml";
  private final MyEventListener myEventListener;
  private final IDEFacade myIdeFacade;

  private final Object myWorkingThreadLock = new Object();
  private Thread myWorkingThread;
  private Thread myThread;

  private boolean myShouldDeliverNow;

  public AsyncMessageDispatcherImpl(EventBroadcaster broadcaster, IDEFacade ideFacade) {
    super(broadcaster, ideFacade.getCacheDir());

    myIdeFacade = ideFacade;
    myEventListener = new MyEventListener(broadcaster);

    start();
  }

  private void start() {
    assert !isRunning(): "Already started";
    new Thread(this, "Network Message Dispatcher").start();
  }

  public boolean isRunning() {
    return myThread != null && myThread.isAlive();
  }

  public void run() {
    LOG.debug("Starting " + Thread.currentThread().getName());

    synchronized (myWorkingThreadLock) {
      myWorkingThread = Thread.currentThread();
      myThread = Thread.currentThread();
    }
    try {
      LOG.debug("Started Network Message Dispatcher thread");
      while (notDisposed()) {
        synchronized(myWorkingThreadLock) {
          while (!myShouldDeliverNow && notDisposed()) {
            myWorkingThreadLock.wait();
          }
          myShouldDeliverNow = false;
        }
        LOG.debug("Process pending network messages");

        if (notDisposed()) {
          dispatchAllMessages();
        }
      }
    } catch (InterruptedException e) {
      LOG.warn(e.getMessage(), e);
    }
    finally {
      synchronized(myWorkingThreadLock) {
        myWorkingThread = null;
      }
    }
  }

  private boolean notDisposed() {
    return myWorkingThread != null;
  }

  private void dispatchAllMessages() {
    User[] usersWithMessages = getUsersWithMessages();
    for (int i = 0; i < usersWithMessages.length && isRunning(); i++) {
      User user = usersWithMessages[i];
      Message[] pendingMessages = getPendingMessages(user);
      for (int j = 0; j < pendingMessages.length && isRunning(); j++) {
        performDispatch(user, pendingMessages[j]);
      }
    }
    save();
  }

  public void dispose() {
    myEventListener.dispose();

    synchronized(myWorkingThreadLock) {
      myWorkingThread = null;
      myWorkingThreadLock.notifyAll();
    }
    new WaitFor(1000){
      protected boolean condition() {
        return !isRunning();
      }
    };

    super.dispose();
  }

  protected String getEventsFileName() {
    return FILE_NAME;
  }

  public void sendLater(User user, Message message) {
    synchronized(myWorkingThreadLock) {
      addPendingMessage(user, message);
      triggerDelivery();
    }
  }

  public IDEFacade getIdeFacade() {
    return myIdeFacade;
  }

  void triggerDelivery() {
    synchronized(myWorkingThreadLock) {
      myShouldDeliverNow = true;
      myWorkingThreadLock.notifyAll();
    }
  }

  private class MyEventListener extends IDEtalkAdapter {
    private final EventBroadcaster myBroadcaster;

    MyEventListener(EventBroadcaster broadcaster) {
      myBroadcaster = broadcaster;
      broadcaster.addListener(this);
    }

    public void dispose() {
      myBroadcaster.removeListener(this);
    }

    public void afterChange(IDEtalkEvent event) {
      event.accept(new EventVisitor(){
        @Override public void visitUserOnline(UserEvent.Online online) {
          triggerDelivery();
        }
      });
    }
  }
}
