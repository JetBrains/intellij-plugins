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
package jetbrains.communicator.jabber.impl;

import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserEvent;
import static jetbrains.communicator.core.users.UserEvent.Updated.DISPLAY_NAME;
import static jetbrains.communicator.core.users.UserEvent.Updated.GROUP;
import jetbrains.communicator.jabber.JabberFacade;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPException;

/** Propagate local changes to Jabber Roster*/
class JabberSyncUserMessage implements Message {
  private static final Logger LOG = Logger.getLogger(JabberSyncUserMessage.class);
  private final UserEvent myEvent;

  JabberSyncUserMessage(UserEvent event) {
    myEvent = event;
  }

  public boolean send(User user) {
    final JabberTransport jabberTransport = JabberTransport.getInstance();
    if (jabberTransport != null && jabberTransport.isOnline()) {
      final Roster roster = jabberTransport.getFacade().getConnection().getRoster();
      final RosterEntry userEntry = roster.getEntry(myEvent.getUser().getName());
      if (userEntry != null) {
        jabberTransport.runIngnoringUserEvents(new Runnable() {
          public void run() {
            processEvent(jabberTransport.getFacade(), userEntry);
          }
        });
      }
      return true;
    }
    return false;
  }

  private void processEvent(final JabberFacade jabberFacade, final RosterEntry userEntry) {
    myEvent.accept(new EventVisitor(){
      public void visitUserUpdated(UserEvent.Updated event) {
        try {
          if (GROUP.equals(event.getPropertyName())) {
            changeUsersGroup(event, jabberFacade.getConnection().getRoster(), userEntry);
          } else if (DISPLAY_NAME.equals(event.getPropertyName())) {
            userEntry.setName((String) event.getNewValue());
          }
        } catch (XMPPException e) {
          processXMPPException(e, event);
        }
      }

      public void visitUserRemoved(UserEvent.Removed event) {
        try {
          jabberFacade.changeSubscription(userEntry.getUser(), false);
          jabberFacade.getConnection().getRoster().removeEntry(userEntry);
        } catch (XMPPException e) {
          processXMPPException(e, event);
        }
      }
    });
  }

  private void processXMPPException(XMPPException e, UserEvent event) {
    if (e.getXMPPError() == null || e.getXMPPError().getCode() != 406) { // Not acceptable
      final String s = e.getMessage() + "\nLocal event: " + event;
      LOG.error(s);
      LOG.info(s, e);
    }
  }

  private void changeUsersGroup(UserEvent.Updated event, final Roster roster, final RosterEntry userEntry) throws XMPPException {
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
