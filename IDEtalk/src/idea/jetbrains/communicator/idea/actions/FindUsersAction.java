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
package jetbrains.communicator.idea.actions;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import jetbrains.communicator.commands.FindUsersCommand;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.commands.UserCommand;
import jetbrains.communicator.core.transport.Transport;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kir Maximov
 */
public class FindUsersAction extends ActionGroup {
  private final List<AnAction> myActions;

  public FindUsersAction() {
    myActions = new ArrayList<AnAction>();
    BaseAction<FindUsersCommand> genericFindAction = new BaseAction<FindUsersCommand>(FindUsersCommand.class);

    myActions.add(new CreateGroupAction());
    myActions.add(genericFindAction);

    List instancesOfType = Pico.getInstance().getComponentInstancesOfType(Transport.class);
    for (Object aInstancesOfType : instancesOfType) {
      Transport transport = (Transport) aInstancesOfType;
      Class<? extends UserCommand> specificFinderClass = transport.getSpecificFinderClass();
      if (specificFinderClass != null) {
        myActions.add(new BaseAction(specificFinderClass));
      }
    }
  }

  public AnAction[] getChildren(AnActionEvent e) {
    return myActions.toArray(new AnAction[myActions.size()]);
  }

}
