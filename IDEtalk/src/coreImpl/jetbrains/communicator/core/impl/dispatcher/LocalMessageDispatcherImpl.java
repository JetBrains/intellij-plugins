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
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.core.IDEtalkListener;
import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.dispatcher.LocalMessageDispatcher;
import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.transport.TransportEvent;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.OwnMessageEvent;
import org.jetbrains.annotations.NonNls;
import org.picocontainer.Disposable;

import javax.swing.*;
import java.util.Date;

/**
 * @author Kir
 */
public class LocalMessageDispatcherImpl extends AbstractMessageDispatcher implements LocalMessageDispatcher, Disposable {
  @NonNls
  private static final String FILE_NAME = "pendingLocalMessages.xml";

  private final MyEventsListener myListener;
  private final IDEFacade myFacade;
  private final UserModel myUserModel;
  private final MessageHistory myHistory;

  public LocalMessageDispatcherImpl(EventBroadcaster eventBroadcaster, IDEFacade facade, UserModel userModel) {
    super(eventBroadcaster, facade.getCacheDir());

    myFacade = facade;
    myUserModel = userModel;
    myListener = new MyEventsListener(eventBroadcaster);
    myHistory = new MessageHistory(facade, userModel);
    load();
  }

  public void dispose() {
    myHistory.dispose();
    myListener.dispose();
    super.dispose();
  }

  protected String getEventsFileName() {
    return FILE_NAME;
  }


  public void addPendingMessage(User user, LocalMessage message) {
    super.addPendingMessage(user, message);
  }

  public Icon getBlinkingIcon() {
    LocalMessage localMessage = getMessageWhichRequireIconBlinking();
    if (localMessage != null) {
      return localMessage.getMessageIcon(0);
    }
    return null;
  }

  public int countPendingMessages() {
    int result = 0;
    for (User usersWithMessage : getUsersWithMessages()) {
      result += getPendingMessages(usersWithMessage).length;
    }
    return result;
  }

  private LocalMessage getMessageWhichRequireIconBlinking() {
    User[] usersWithMessages = getUsersWithMessages();
    if (usersWithMessages.length > 0) {
      return (LocalMessage) getPendingMessages(usersWithMessages[0])[0];
    }
    return null;
  }


  protected boolean performDispatch(User user, Message message) {
    boolean result = super.performDispatch(user, message);
    if (result) {
      myHistory.addMessage(user, (LocalMessage) message);
    }
    return result;
  }

  public LocalMessage[] getHistory(User user, Date since) {
    return myHistory.getHistory(user, since);
  }

  public void clearHistory() {
    myHistory.clear();
  }

  public boolean isHistoryEmpty() {
    return myHistory.isEmpty();
  }

  private class MyEventsListener implements IDEtalkListener {
    private EventBroadcaster myEventBroadcaster;

    MyEventsListener(EventBroadcaster eventBroadcaster) {
      myEventBroadcaster = eventBroadcaster;
      myEventBroadcaster.addListener(this);
    }
    public void dispose() {
      myEventBroadcaster.removeListener(this);
    }

    public void beforeChange(IDEtalkEvent event) { }

    public void afterChange(IDEtalkEvent event) {
      //noinspection RefusedBequest
      event.accept(new EventVisitor(){

        public void visitTransportEvent(TransportEvent event) {
          User user = event.createUser(myUserModel);
          addPendingMessage(user, myFacade.createLocalMessageForIncomingEvent(event));
        }

        public void visitOwnMessageEvent(OwnMessageEvent event) {
          sendNow(event.getTargetUser(), myFacade.createLocalMessageForOutgoingEvent(event));
        }
      });
    }

  }
}
