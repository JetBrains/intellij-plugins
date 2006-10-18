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
import jetbrains.communicator.util.XMLUtil;
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
  private final Map<User,List<Message>> myUser2Messages = new HashMap<User, List<Message>>();
  private EventBroadcaster myEventBroadcaster;
  private boolean myDispatching;

  protected AbstractMessageDispatcher(EventBroadcaster eventBroadcaster, File dataDir) {
    myDataDir = dataDir;
    myEventListener = new MyEventListener(eventBroadcaster);
    myEventBroadcaster = eventBroadcaster;
    load();
  }

  public void dispose() {
    myEventListener.dispose();
    //noinspection HardCodedStringLiteral
    LOG.debug("Disposed.");
  }

  protected abstract String getEventsFileName();

  protected boolean performDispatch(User user, Message message) {
    try {
      myDispatching = true;
      if (LOG.isDebugEnabled()) {
        //noinspection HardCodedStringLiteral
        LOG.debug("Start dispatching " + message + " to " + user);
      }
      boolean result = message.send(user);

      synchronized(myUser2MessagesLock) {
        List<Message> userMessages = getMessages(user);
        if (result) {
          userMessages.remove(message);
          if (userMessages.size() == 0) {
            myUser2Messages.remove(user);
          }
        }
        else if (!userMessages.contains(message) ){
          userMessages.add(message);
        }
      }

      if (LOG.isDebugEnabled()) {
        //noinspection HardCodedStringLiteral
        LOG.debug("End   dispatching " + message + " to " + user);
      }
      return result;
    } finally {
      myDispatching = false;
    }
  }

  public boolean hasUsersWithMessages() {
    synchronized(myUser2MessagesLock) {
      return !myUser2Messages.isEmpty();
    }
  }

  public User[] getUsersWithMessages() {
    synchronized(myUser2MessagesLock) {
      Set<User> users = myUser2Messages.keySet();
      return users.toArray(new User[users.size()]);
    }
  }

  public Message[] getPendingMessages(User user) {
    synchronized (myUser2MessagesLock) {
      List<Message> messages = myUser2Messages.get(user);
      if (messages == null) {
        return new Message[0];
      }

      return messages.toArray(new Message[messages.size()]);
    }
  }

  public EventBroadcaster getBroadcaster() {
    return myEventBroadcaster;
  }

  public boolean sendNow(User user, Message message) {
    boolean result = performDispatch(user, message);
    if (result) {
      save();
    }
    return result;
  }

  boolean isMessageDispatchInProgress() {
    return myDispatching;
  }

  protected void addPendingMessage(User user, Message message) {
    if (message == null) return;

    synchronized(myUser2MessagesLock) {
      List<Message> userMessages = getMessages(user);
      if (!userMessages.contains(message)) {
        if (LOG.isDebugEnabled()) {
          //noinspection HardCodedStringLiteral
          LOG.debug("Added pending message " + message + "\nfor user " + user);
        }
        userMessages.add(message);
        save();
      }
    }
  }

  protected void removePendingMessage(User user, int messageIndex) {
    synchronized(myUser2MessagesLock) {
      List<Message> userMessages = getMessages(user);
      userMessages.remove(messageIndex);
      save();
    }
  }

  protected void clearAll() {
    synchronized (myUser2MessagesLock) {
      myUser2Messages.clear();
      save();
    }
  }

  private List<Message> getMessages(User user) {
    synchronized (myUser2MessagesLock) {
      List<Message> result = myUser2Messages.get(user);
      if (result == null) {
        result = new ArrayList<Message>(5);
        myUser2Messages.put(user, result);
      }
      return result;
    }
  }

  protected final void load() {
    synchronized (myUser2MessagesLock) {
      Object pendingEventsStorable = XMLUtil.fromXml(getXStream(), getFileName(), false);
      if (pendingEventsStorable instanceof MessagesStorable) {
        loadFromStorableMessages((MessagesStorable) pendingEventsStorable);
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

  @SuppressWarnings({"HardCodedStringLiteral"})
  protected void save() {
    if (myDataDir == null || !myDataDir.exists()) return;

    LOG.debug("Save start");
    synchronized(myUser2MessagesLock) {
      XMLUtil.toXml(getXStream(), getFileName(), createStorableMessages());
    }
    LOG.debug("Save finish");
  }

  protected MessagesStorable createStorableMessages() {
    return new MessagesStorable(myUser2Messages);
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  private XStream getXStream() {
    if (myXStream == null) {
      myXStream = XMLUtil.createXStream();
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

    public void afterChange(IDEtalkEvent event) {
      event.accept(new EventVisitor(){
        @SuppressWarnings({"RefusedBequest"})
        public void visitUserRemoved(UserEvent.Removed event) {
          removeUser(event.getUser());
        }
      });
    }
  }

  protected interface MessageProcessor {

    /** returns true if processing should be stopped */
    boolean process(User user, Message message);
  }

}
