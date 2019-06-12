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
package jetbrains.communicator.jabber;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.Anchor;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.ProjectManager;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.StatusToolbar;
import jetbrains.communicator.jabber.impl.FindByJabberIdDialog;
import jetbrains.communicator.jabber.register.RegistrationDialog;
import jetbrains.communicator.jabber.register.RegistrationForm;
import jetbrains.communicator.util.CommunicatorStrings;
import jetbrains.communicator.util.UIUtil;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.MutablePicoContainer;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Kir
 */
public class JabberIdeaUI implements JabberUI {
  private static boolean ourInitialized;

  private final JabberFacade myFacade;
  private final IDEFacade myIdeFacade;

  public JabberIdeaUI(JabberFacade facade, IDEFacade ideFacade) {
    myFacade = facade;
    myIdeFacade = ideFacade;
  }

  @Override
  public void initPerProject(MutablePicoContainer projectLevelContainer) {
    StatusToolbar statusToolbar = ((StatusToolbar) projectLevelContainer.getComponentInstanceOfType(StatusToolbar.class));
    if (statusToolbar != null) {
      statusToolbar.addToolbarCommand(JabberConnectionCommand.class);
    }
    if (!ourInitialized && ApplicationManager.getApplication() != null && !Pico.isUnitTest()) {
      DefaultActionGroup group = ((DefaultActionGroup) ActionManager.getInstance().getAction("IDEtalk.OptionsGroup"));
      if (group != null) {
        group.add(new EditJabberSettingsAction(), new Constraints(Anchor.FIRST, ""));
      }
      ourInitialized = true;
    }
  }

  @Override
  public boolean connectAndLogin(String message) {
    myFacade.connect();
    if (!myFacade.isConnectedAndAuthenticated()) {
      RegistrationForm.INITIAL_MESSAGE = message;
      login(null);
    }
    return myFacade.isConnectedAndAuthenticated();
  }

  @Override
  public void connectAndLoginAsync(final String message, final AtomicBoolean connected) {
    if (SwingUtilities.isEventDispatchThread()) {
      myIdeFacade.runOnPooledThread(() -> connectAndLoginAsync(message, connected));
      return;
    }

    myFacade.connect();

    if (!myFacade.isConnectedAndAuthenticated()) {
      Runnable runnable = () -> {
        RegistrationForm.INITIAL_MESSAGE = message;
        login(null);

        if (connected != null) {
          connected.set(myFacade.isConnectedAndAuthenticated());
        }
      };
      UIUtil.invokeLater(runnable);
    }
  }

  @Override
  public void login(Component parentComponent) {
    Component parent = parentComponent;
    if (parent == null) {
      parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    }
    if (hasOpenProject()) {
      if (parent != null && parent.isShowing()) {
        new RegistrationDialog(myFacade, myIdeFacade, parent).show();
      }
      else {
        new RegistrationDialog(myFacade, myIdeFacade).show();
      }
    }
  }

  private boolean hasOpenProject() {
    return ProjectManager.getInstance().getOpenProjects().length > 0;
  }

  @Override
  public String getFindByIdData(List<String> availableGroups) {
    FindByJabberIdDialog dialog = new FindByJabberIdDialog(availableGroups);
    if (dialog.showAndGet()) {
      String group = dialog.getGroup();
      if ("".equals(group.trim())) {
        group = UserModel.DEFAULT_GROUP;
      }
      return group + ':' + dialog.getJabberIDs();
    }
    return null;
  }

  @Override
  public boolean shouldAcceptSubscriptionRequest(Presence requestFrom) {
    VCardInfo fromInfo = myFacade.getVCard(requestFrom.getFrom());

    return myIdeFacade.askQuestion(CommunicatorStrings.getMsg("jabber.subscribe.title"),
                                   CommunicatorStrings.getMsg("jabber.subscribe.text", requestFrom.getFrom(), buildUserInfo(fromInfo)));
  }

  private String buildUserInfo(VCardInfo fromInfo) {
    StringBuilder sb = new StringBuilder();
    sb.append(CommunicatorStrings.getMsg("nickname.info")).append(fromInfo.getNickName()).append('\n');
    sb.append(CommunicatorStrings.getMsg("first.name.info")).append(fromInfo.getFirstname()).append('\n');
    sb.append(CommunicatorStrings.getMsg("last.name.info")).append(fromInfo.getLastname());
    return sb.toString();
  }

}
