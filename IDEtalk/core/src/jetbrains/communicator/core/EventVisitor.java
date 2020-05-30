// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core;

import jetbrains.communicator.core.transport.CodePointerEvent;
import jetbrains.communicator.core.transport.MessageEvent;
import jetbrains.communicator.core.transport.StacktraceEvent;
import jetbrains.communicator.core.transport.TransportEvent;
import jetbrains.communicator.core.users.GroupEvent;
import jetbrains.communicator.core.users.SettingsChanged;
import jetbrains.communicator.core.users.UserEvent;
import jetbrains.communicator.ide.OwnMessageEvent;
import jetbrains.communicator.ide.SendCodePointerEvent;
import jetbrains.communicator.ide.SendMessageEvent;

/**
 * @author Kir
 */
public class EventVisitor {

  public void visitUserEvent(UserEvent event) {
  }

  public void visitUserAdded(UserEvent.Added event) {
    visitUserEvent(event);
  }

  public void visitUserRemoved(UserEvent.Removed event) {
    visitUserEvent(event);
  }

  public void visitUserUpdated(UserEvent.Updated event) {
    visitUserEvent(event);
  }

  public void visitGroupEvent(GroupEvent event) {
  }

  public void visitTransportEvent(TransportEvent event) {
  }

  public void visitUserOnline(UserEvent.Online online) {
    visitUserUpdated(online);
  }

  public void visitUserOffline(UserEvent.Offline offline) {
    visitUserUpdated(offline);
  }

  public void visitStacktraceEvent(StacktraceEvent event) {
    visitMessageEvent(event);
  }

  public void visitCodePointerEvent(CodePointerEvent event) {
    visitTransportEvent(event);
  }

  public void visitMessageEvent(MessageEvent event) {
    visitTransportEvent(event);
  }

  public void visitSendMessageEvent(SendMessageEvent event) {
    visitOwnMessageEvent(event);
  }

  public void visitSendCodePointerEvent(SendCodePointerEvent event) {
    visitOwnMessageEvent(event);
  }

  public void visitSettingsChanged(SettingsChanged settingsChanged) {
  }

  public void visitOwnMessageEvent(OwnMessageEvent event) {
  }
}
