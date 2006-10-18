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
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.StatusToolbar;
import jetbrains.communicator.jabber.impl.FindByJabberIdDialog;
import jetbrains.communicator.jabber.register.RegistrationDialog;
import jetbrains.communicator.jabber.register.RegistrationForm;
import jetbrains.communicator.util.StringUtil;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.MutablePicoContainer;

import java.awt.*;
import java.util.List;

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

  public void initPerProject(MutablePicoContainer projectLevelContainer) {
    StatusToolbar statusToolbar = ((StatusToolbar) projectLevelContainer.getComponentInstanceOfType(StatusToolbar.class));
    if (statusToolbar != null) {
      statusToolbar.addToolbarCommand(JabberConnectionCommand.class);
    }
    if (!ourInitialized && ApplicationManager.getApplication() != null) {
      DefaultActionGroup group = ((DefaultActionGroup) ActionManager.getInstance().getAction("IDEtalk.OptionsGroup"));
      if (group != null) {
        group.add(new EditJabberSettingsAction(), new Constraints(Anchor.FIRST, ""));
      }
      ourInitialized = true;
    }
  }

  public boolean connectAndLogin(String message) {
    myFacade.connect();
    if (!myFacade.isConnectedAndAuthenticated()) {
      RegistrationForm.INITIAL_MESSAGE = message;
      login(null);
    }
    return myFacade.isConnectedAndAuthenticated();
  }

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

  public String getFindByIdData(List<String> availableGroups) {
    FindByJabberIdDialog dialog = new FindByJabberIdDialog(availableGroups);
    dialog.show();
    if (dialog.isOK()) {
      String group = dialog.getGroup();
      if ("".equals(group.trim())) {
        group = UserModel.DEFAULT_GROUP;
      }
      return group + ':' + dialog.getJabberIDs();
    }
    return null;
  }

  public boolean shouldAcceptSubscriptionRequest(Presence requestFrom) {
    VCardInfo fromInfo = myFacade.getVCard(requestFrom.getFrom());

    return myIdeFacade.askQuestion(StringUtil.getMsg("jabber.subscribe.title"),
        StringUtil.getMsg("jabber.subscribe.text", requestFrom.getFrom(), buildUserInfo(fromInfo)));
  }

  private String buildUserInfo(VCardInfo fromInfo) {
    StringBuffer sb = new StringBuffer();
    sb.append("Nickname: ").append(fromInfo.getNickName()).append('\n');
    sb.append("Firstname: ").append(fromInfo.getFirstname()).append('\n');
    sb.append("Lastname: ").append(fromInfo.getLastname());
    return sb.toString();
  }

}
