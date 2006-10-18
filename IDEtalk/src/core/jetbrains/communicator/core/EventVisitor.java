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
