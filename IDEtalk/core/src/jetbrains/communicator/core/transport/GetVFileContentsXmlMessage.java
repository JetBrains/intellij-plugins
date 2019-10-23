// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.transport;

import jetbrains.communicator.core.vfs.VFile;
import org.jdom.Element;

/**
 * @author Kir
 */
public class GetVFileContentsXmlMessage implements XmlMessage {
  public static final String TAG = "fillContents";
  private final VFile myFile;

  public GetVFileContentsXmlMessage(VFile file) {
    myFile = file;
  }

  @Override
  public String getTagName() {
    return TAG;
  }

  @Override
  public String getTagNamespace() {
    return Transport.NAMESPACE;
  }

  @Override
  public boolean needsResponse() {
    return true;
  }

  @Override
  public void fillRequest(Element element) {
    myFile.saveTo(element);
  }

  @Override
  public void processResponse(Element responseElement) {
    VFile from = VFile.createFrom(responseElement);
    if (from != null) {
      myFile.setContents(from.getContents());
    }
  }
}
