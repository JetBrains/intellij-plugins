// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.impl.transport;

import jetbrains.communicator.core.transport.GetVFileContentsXmlMessage;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.IDEFacade;
import org.jdom.Element;

/**
 * @author Kir
 */
public class GetVFileContentsProvider extends FileAccessProvider {

  public GetVFileContentsProvider(IDEFacade ideFacade, UserModel userModel) {
    super(ideFacade, userModel);
  }

  @Override
  public String getTagName() {
    return GetVFileContentsXmlMessage.TAG;
  }

  @Override
  protected void doProcess(Element request, Element response) {
    VFile from = VFile.createFrom(request);
    myIdeFacade.fillFileContents(from);
    from.saveTo(response);
  }
}
