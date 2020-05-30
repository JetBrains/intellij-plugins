// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.impl.dispatcher;

import com.thoughtworks.xstream.XStream;
import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.IDEtalkAdapter;
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.dispatcher.MessageDispatcher;
import jetbrains.communicator.core.impl.users.UserImpl;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserEvent;
import jetbrains.communicator.util.XStreamUtil;
import org.apache.log4j.Logger;
import org.picocontainer.Disposable;

import java.io.File;
import java.util.*;

/**
 * @author Kir
 */
public abstract class AbstractMessageDispatcher implements MessageDispatcher, Disposable {
  private static final Logger LOG = Logger.getLogger(AbstractMessageDispatcher.class);
  private XStream myXStream;

  private final File myDataDir;
  private final MyEventListener myEventListener;

  private final Object myUser2MessagesLock = new Object();
  private final Map<User, List<Message>> myUser2Messages = new HashMap<>();
  private final EventBroadcaster myEventBroadcaster;

  protected AbstractMessageDispatcher(EventBroadcaster eventBroadcaster, File dataDir) {
    myDataDir = dataDir;
    myEventListener = new MyEventListener(eventBroadcaster);
    myEventBroadcaster = eventBroadcaster;
    load();
  }

  @Override
  public void dispose() {
    myEventListener.dispose();
    LOG.debug("Disposed.");
  }

  protected abstract String getEventsFileName();

  protected boolean performDispatch(User user, Message message) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Start dispatching " + message + " to " + user);
    }
    boolean result = message.send(user);

    synchronized (myUser2MessagesLock) {
      List<Message> userMessages = getMessages(user);
      if (result) {
        userMessages.remove(message);
        if (userMessages.size() == 0) {
          myUser2Messages.remove(user);
        }
      }
      else if (!userMessages.contains(message)) {
        userMessages.add(message);
      }
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("End   dispatching " + message + " to " + user);
    }
    return result;
  }

  @Override
  public boolean hasUsersWithMessages() {
    synchronized (myUser2MessagesLock) {
      return !myUser2Messages.isEmpty();
    }
  }

  @Override
  public User[] getUsersWithMessages() {
    synchronized (myUser2MessagesLock) {
      Set<User> users = myUser2Messages.keySet();
      return users.toArray(new User[0]);
    }
  }

  @Override
  public Message[] getPendingMessages(User user) {
    synchronized (myUser2MessagesLock) {
      List<Message> messages = myUser2Messages.get(user);
      if (messages == null) {
        return new Message[0];
      }

      return messages.toArray(new Message[0]);
    }
  }

  @Override
  public EventBroadcaster getBroadcaster() {
    return myEventBroadcaster;
  }

  @Override
  public boolean sendNow(User user, Message message) {
    boolean result = performDispatch(user, message);
    if (result) {
      save();
    }
    return result;
  }

  protected void addPendingMessage(User user, Message message) {
    if (message == null) return;

    synchronized (myUser2MessagesLock) {
      List<Message> userMessages = getMessages(user);
      if (!userMessages.contains(message)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Added pending message " + message + "\nfor user " + user);
        }
        userMessages.add(message);
        save();
      }
    }
  }

  private List<Message> getMessages(User user) {
    synchronized (myUser2MessagesLock) {
      List<Message> result = myUser2Messages.get(user);
      if (result == null) {
        result = new ArrayList<>(5);
        myUser2Messages.put(user, result);
      }
      return result;
    }
  }

  protected final void load() {
    synchronized (myUser2MessagesLock) {
      Object pendingEventsStorable = XStreamUtil.fromXml(getXStream(), getFileName(), false);
      if (pendingEventsStorable instanceof MessagesStorable) {
        loadFromStorableMessages((MessagesStorable)pendingEventsStorable);
      }
    }
  }

  protected void loadFromStorableMessages(MessagesStorable messagesStorable) {
    myUser2Messages.clear();
    myUser2Messages.putAll(messagesStorable.getUser2Messages());
  }

  private String getFileName() {
    return new File(myDataDir, getEventsFileName()).getAbsolutePath();
  }

  private void removeUser(User user) {
    synchronized (myUser2MessagesLock) {
      myUser2Messages.remove(user);
      save();
    }
  }

  protected void save() {
    if (myDataDir == null || !myDataDir.exists()) return;

    LOG.debug("Save start");
    synchronized (myUser2MessagesLock) {
      XStreamUtil.toXml(getXStream(), getFileName(), createStorableMessages());
    }
    LOG.debug("Save finish");
  }

  protected MessagesStorable createStorableMessages() {
    return new MessagesStorable(myUser2Messages);
  }

  private XStream getXStream() {
    if (myXStream == null) {
      myXStream = XStreamUtil.createXStream();
      myXStream.alias("user", UserImpl.class);
      myXStream.alias("pendingEvents", MessagesStorable.class);
    }

    return myXStream;
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
      event.accept(new EventVisitor() {
        @Override
        public void visitUserRemoved(UserEvent.Removed event) {
          removeUser(event.getUser());
        }
      });
    }
  }

  protected interface MessageProcessor {

    /**
     * returns true if processing should be stopped
     */
    boolean process(User user, Message message);
  }
}
