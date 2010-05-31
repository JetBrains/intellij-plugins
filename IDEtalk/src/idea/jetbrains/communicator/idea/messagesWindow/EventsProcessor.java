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
package jetbrains.communicator.idea.messagesWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.core.IDEtalkListener;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.dispatcher.LocalMessageDispatcher;
import jetbrains.communicator.core.transport.TransportEvent;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.idea.IDEtalkMessagesWindow;
import jetbrains.communicator.idea.IdeaLocalMessage;
import jetbrains.communicator.idea.config.IdeaFlags;
import jetbrains.communicator.util.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.picocontainer.Disposable;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;

/**
 * @author Kir
 */
class EventsProcessor extends EventVisitor implements IDEtalkListener, Disposable {

  @NonNls
  private static final String INCOMING_MESSAGE_WAV = "/incomingMessage.wav";
  private final IDEtalkMessagesWindow myMessagesWindow;
  private final UserModel myUserModel;
  private final LocalMessageDispatcher myMessageDispatcher;
  private final Project myProject;

  EventsProcessor(IDEtalkMessagesWindow messagesWindow, UserModel userModel, LocalMessageDispatcher messageDispatcher, Project project) {
    myMessagesWindow = messagesWindow;
    myUserModel = userModel;
    myMessageDispatcher = messageDispatcher;
    myProject = project;

    myUserModel.getBroadcaster().addListener(this);
  }


  public void dispose() {
    myUserModel.getBroadcaster().removeListener(this);
  }

  public void afterChange(IDEtalkEvent event) {
    event.accept(this);
  }

  public void beforeChange(IDEtalkEvent event) {
  }

  @Override
  public void visitTransportEvent(TransportEvent event) {
    if (IdeaFlags.SOUND_ON_MESSAGE.isSet()) {
      Applet.newAudioClip(getClass().getResource(INCOMING_MESSAGE_WAV)).play();
    }

    User user = myUserModel.findUser(event.getRemoteUser(), event.getTransport().getName());
    if (user != null) {
      myMessagesWindow.newMessageAvailable(user, event);
    }

    if (IdeaFlags.EXPAND_ON_MESSAGE.isSet()) {
      myMessagesWindow.expandToolWindow();
    }

    if (IdeaFlags.ACTIVATE_WINDOW_ON_MESSAGE.isSet()) {
      makeWindowBlinking();
    }

    if (user != null && IdeaFlags.POPUP_ON_MESSAGE.isSet()) {
      showPopupNotification(user, event);
    }

  }

  private void makeWindowBlinking() {
    UIUtil.invokeLater(new Runnable() {
      public void run() {
        Window window = myMessagesWindow.getWindow();
        if (window != null) {
          window.toFront();
        }
      }
    });
  }

  private void showPopupNotification(final User from, TransportEvent event) {
    if (myMessageDispatcher.countPendingMessages() > 5) return;

    IDEFacade ideFacade = ((IDEFacade)Pico.getInstance().getComponentInstanceOfType(IDEFacade.class));
    final IdeaLocalMessage localMessage = (IdeaLocalMessage)ideFacade.createLocalMessageForIncomingEvent(event);
    if (localMessage == null) return;

    UIUtil.invokeLater(new Runnable() {
      public void run() {
        JComponent content = localMessage.getPopupComponent(from, myProject);
        Color backgroundColor = new Color(255, 255, 217);
        content.setOpaque(true);
        content.setBackground(backgroundColor);
        WindowManager.getInstance().getStatusBar(myProject).fireNotificationPopup(
          content, backgroundColor);
      }
    });
  }

}
