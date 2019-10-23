// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.transport;

import jetbrains.communicator.core.vfs.CodePointer;
import jetbrains.communicator.core.vfs.VFile;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jetbrains.annotations.NonNls;

/**
 * @author Kir
 */
public class CodePointerXmlMessage extends TextXmlMessage {
  public static final String TAGNAME = "codePointer";
  private final CodePointer myPointer;
  private final VFile myVFile;

  public CodePointerXmlMessage(String comment, CodePointer pointer, VFile vFile) {
    super(comment);
    myPointer = pointer;
    myVFile = vFile;
  }

  @Override
  public String getTagName() {
    return TAGNAME;
  }

  @Override
  public void fillRequest(@NonNls Element root) {
    super.fillRequest(root);

    root.setAttribute("line1", String.valueOf(myPointer.getLine1()));
    root.setAttribute("line2", String.valueOf(myPointer.getLine2()));
    root.setAttribute("column1", String.valueOf(myPointer.getColumn1()));
    root.setAttribute("column2", String.valueOf(myPointer.getColumn2()));

    Element vFile = new Element(VFile.ELEMENT_NAME, getTagNamespace());
    root.addContent(vFile);

    myVFile.saveTo(vFile);
  }

  public static CodePointerEvent createEvent(Transport transport, String remoteUser, @NonNls Element rootElement) {

    int line1 = Integer.parseInt(rootElement.getAttributeValue("line1"));
    int line2 = Integer.parseInt(rootElement.getAttributeValue("line2"));
    int column1 = Integer.parseInt(rootElement.getAttributeValue("column1"));
    int column2 = Integer.parseInt(rootElement.getAttributeValue("column2"));
    Element element = rootElement.getChild(VFile.ELEMENT_NAME, Namespace.getNamespace(Transport.NAMESPACE));
    if (element != null) {
      return EventFactory.createCodePointerEvent(transport, remoteUser,
          VFile.createFrom(element),
          line1, column1, line2, column2, rootElement.getText());
    }
    return null;
  }

}
