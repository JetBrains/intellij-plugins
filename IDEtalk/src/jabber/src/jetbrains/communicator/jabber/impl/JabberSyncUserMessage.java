// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.jabber.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserEvent;
import jetbrains.communicator.jabber.JabberFacade;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPException;

/** Propagate local changes to Jabber Roster*/
final class JabberSyncUserMessage implements Message {
  private static final Logger LOG = Logger.getInstance(JabberSyncUserMessage.class);
  private final UserEvent myEvent;

  JabberSyncUserMessage(UserEvent event) {
    myEvent = event;
  }

  @Override
  public boolean send(User user) {
    final JabberTransport jabberTransport = JabberTransport.getInstance();
    if (jabberTransport != null && jabberTransport.isOnline()) {
      final Roster roster = jabberTransport.getFacade().getConnection().getRoster();
      final RosterEntry userEntry = roster.getEntry(myEvent.getUser().getName());
      if (userEntry != null) {
        jabberTransport.runIngnoringUserEvents(() -> processEvent(jabberTransport.getFacade(), userEntry));
      }
      return true;
    }
    return false;
  }

  private void processEvent(final JabberFacade jabberFacade, final RosterEntry userEntry) {
    myEvent.accept(new EventVisitor(){
      @Override public void visitUserUpdated(UserEvent.Updated event) {
        try {
          if (UserEvent.Updated.GROUP.equals(event.getPropertyName())) {
            changeUsersGroup(event, jabberFacade.getConnection().getRoster(), userEntry);
          } else if (UserEvent.Updated.DISPLAY_NAME.equals(event.getPropertyName())) {
            userEntry.setName((String) event.getNewValue());
          }
        } catch (XMPPException e) {
          processXMPPException(e, event);
        }
      }

      @Override public void visitUserRemoved(UserEvent.Removed event) {
        try {
          jabberFacade.changeSubscription(userEntry.getUser(), false);
          jabberFacade.getConnection().getRoster().removeEntry(userEntry);
        } catch (XMPPException e) {
          processXMPPException(e, event);
        }
      }
    });
  }

  private static void processXMPPException(XMPPException e, UserEvent event) {
    if (e.getXMPPError() == null || e.getXMPPError().getCode() != 406) { // Not acceptable
      final String s = e.getMessage() + "\nLocal event: " + event;
      LOG.error(s);
      LOG.info(s, e);
    }
  }

  private static void changeUsersGroup(UserEvent.Updated event, final Roster roster, final RosterEntry userEntry) throws XMPPException {
    RosterGroup oldGroup = roster.getGroup((String) event.getOldValue());
    RosterGroup newGroup = roster.getGroup((String) event.getNewValue());
    if (oldGroup != null) {
      oldGroup.removeEntry(userEntry);
    }

    if (newGroup == null) {
      newGroup = roster.createGroup((String) event.getNewValue());
    }
    newGroup.addEntry(userEntry);
  }

  public String toString() {
    return "JabberSyncUserMessage: " + myEvent;
  }
}
