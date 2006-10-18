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

  public String getTagName() {
    return GetProjectsDataXmlMessage.TAG;
  }

  protected void doProcess(Element request, Element response) {
    if (OptionFlag.HIDE_ALL_KEY.isSet()) {
      return;
    }

    Element serialized = myIdeFacade.getProjectsData().serialize();
    serialized.detach();
    response.addContent(serialized);
  }
}
