// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.impl.dispatcher;

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.IDEtalkAdapter;
import jetbrains.communicator.core.IDEtalkEvent;
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

  @Override
  public void dispose() {
    myHistory.dispose();
    myListener.dispose();
    super.dispose();
  }

  @Override
  protected String getEventsFileName() {
    return FILE_NAME;
  }


  @Override
  public void addPendingMessage(User user, LocalMessage message) {
    super.addPendingMessage(user, message);
  }

  @Override
  public Icon getBlinkingIcon() {
    LocalMessage localMessage = getMessageWhichRequireIconBlinking();
    if (localMessage != null) {
      return localMessage.getMessageIcon(0);
    }
    return null;
  }

  @Override
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
      return (LocalMessage)getPendingMessages(usersWithMessages[0])[0];
    }
    return null;
  }


  @Override
  protected boolean performDispatch(User user, Message message) {
    boolean result = super.performDispatch(user, message);
    if (result) {
      myHistory.addMessage(user, (LocalMessage)message);
    }
    return result;
  }

  @Override
  public LocalMessage[] getHistory(User user, Date since) {
    return myHistory.getHistory(user, since);
  }

  @Override
  public void clearHistory() {
    myHistory.clear();
  }

  @Override
  public boolean isHistoryEmpty() {
    return myHistory.isEmpty();
  }

  private class MyEventsListener extends IDEtalkAdapter {
    private final EventBroadcaster myEventBroadcaster;

    MyEventsListener(EventBroadcaster eventBroadcaster) {
      myEventBroadcaster = eventBroadcaster;
      myEventBroadcaster.addListener(this);
    }

    public void dispose() {
      myEventBroadcaster.removeListener(this);
    }

    @Override
    public void afterChange(IDEtalkEvent event) {
      event.accept(new EventVisitor() {
        @Override
        public void visitTransportEvent(TransportEvent event) {
          User user = event.createUser(myUserModel);
          addPendingMessage(user, myFacade.createLocalMessageForIncomingEvent(event));
        }

        @Override
        public void visitOwnMessageEvent(OwnMessageEvent event) {
          sendNow(event.getTargetUser(), myFacade.createLocalMessageForOutgoingEvent(event));
        }
      });
    }
  }
}
