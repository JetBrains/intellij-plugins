// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.impl.transport;

import jetbrains.communicator.OptionFlag;
import jetbrains.communicator.core.transport.GetProjectsDataXmlMessage;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import org.jdom.Element;

/**
 * @author Kir
 */
public class GetProjectsDataProvider extends FileAccessProvider {

  public GetProjectsDataProvider(IDEFacade ideFacade, UserModel userModel) {
    super(ideFacade, userModel);
  }

  @Override
  public String getTagName() {
    return GetProjectsDataXmlMessage.TAG;
  }

  @Override
  protected void doProcess(Element request, Element response) {
    if (OptionFlag.HIDE_ALL_KEY.isSet()) {
      return;
    }

    Element serialized = myIdeFacade.getProjectsData().serialize();
    serialized.detach();
    response.addContent(serialized);
  }
}
