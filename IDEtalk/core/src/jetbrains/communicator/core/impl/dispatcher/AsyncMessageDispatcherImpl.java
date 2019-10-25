// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
    Thread t = new Thread(this, "Network Message Dispatcher");
    t.setDaemon(true);
    t.start();
  }

  public boolean isRunning() {
    return myThread != null && myThread.isAlive();
  }

  @Override
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

  @Override
  public void dispose() {
    myEventListener.dispose();

    synchronized(myWorkingThreadLock) {
      myWorkingThread = null;
      myWorkingThreadLock.notifyAll();
    }
    new WaitFor(10000){
      @Override
      protected boolean condition() {
        return !isRunning();
      }
    };

    super.dispose();
  }

  @Override
  protected String getEventsFileName() {
    return FILE_NAME;
  }

  @Override
  public void sendLater(User user, Message message) {
    synchronized(myWorkingThreadLock) {
      addPendingMessage(user, message);
      triggerDelivery();
    }
  }

  @Override
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

    @Override
    public void afterChange(IDEtalkEvent event) {
      event.accept(new EventVisitor(){
        @Override public void visitUserOnline(UserEvent.Online online) {
          triggerDelivery();
        }
      });
    }
  }
}
