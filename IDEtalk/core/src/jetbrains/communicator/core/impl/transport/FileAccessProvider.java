// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
