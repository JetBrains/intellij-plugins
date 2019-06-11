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
package jetbrains.communicator.core.impl.transport;

import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.transport.XmlResponseProvider;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.CommunicatorStrings;
import org.jdom.Element;

/**
 * @author Kir
 */
public abstract class FileAccessProvider extends XmlResponseProvider {
  protected final IDEFacade myIdeFacade;
  protected final UserModel myUserModel;

  public FileAccessProvider(IDEFacade ideFacade, UserModel userModel) {
    myIdeFacade = ideFacade;
    myUserModel = userModel;
  }

  @Override
  public String getTagNamespace() {
    return Transport.NAMESPACE;
  }

  @Override
  public final boolean processAndFillResponse(Element response, Element request, Transport transport, String remoteUser) {
    User requestingUser = myUserModel.findUser(remoteUser, transport.getName());
    if (requestingUser == null) return false;

    if (!requestingUser.canAccessMyFiles()) {
      boolean allow = myIdeFacade.askQuestion(CommunicatorStrings.getMsg("FileAccessProvider.title"),
                                              CommunicatorStrings.getMsg("FileAccessProvider.message", requestingUser.getDisplayName()));
      if (allow) {
        requestingUser.setCanAccessMyFiles(true, myUserModel);
      }
      else {
        return false;
      }
    }

    doProcess(request, response);
    return true;
  }

  protected abstract void doProcess(Element request, Element response);
}
