// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.transport;

import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.vfs.CodePointer;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.util.CommunicatorStrings;

/**
 * @author kir
 */
public class CodePointerEvent extends TransportEvent {

  private final String myComment;
  private final VFile myFile;
  private final CodePointer myCodePointer;

  CodePointerEvent(Transport transport, String remoteUser, VFile file, int line1, int col1, int line2, int col2, String comment) {
    super(transport, remoteUser);
    myComment = comment;
    myFile = file;
    myCodePointer = new CodePointer(line1, col1, line2, col2);
  }

  public String getComment() {
    return myComment;
  }

  public VFile getFile() {
    return myFile;
  }

  public CodePointer getCodePointer() {
    return myCodePointer;
  }

  @Override
  public void accept(EventVisitor visitor) {
    visitor.visitCodePointerEvent(this);
  }

  public String toString() {
    return CommunicatorStrings.toString(getClass(), new Object[]{
      myComment, myCodePointer,
      getRemoteUser(),
      getTransport()
    });
  }

}
