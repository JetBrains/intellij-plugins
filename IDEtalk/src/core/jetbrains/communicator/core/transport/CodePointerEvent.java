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
