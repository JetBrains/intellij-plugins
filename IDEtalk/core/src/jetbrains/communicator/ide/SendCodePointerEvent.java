// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.ide;

import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.CodePointer;
import jetbrains.communicator.core.vfs.VFile;

/**
 * @author Kir
 */
public class SendCodePointerEvent extends OwnMessageEvent {
  private final VFile myFile;
  private final CodePointer myCodePointer;

  public SendCodePointerEvent(String message, VFile file, CodePointer codePointer, User user) {
    super(message, user);
    myFile = file;
    myCodePointer = codePointer;
  }

  @Override
  public void accept(EventVisitor visitor) {
    visitor.visitSendCodePointerEvent(this);
  }

  public VFile getFile() {
    return myFile;
  }

  public CodePointer getCodePointer() {
    return myCodePointer;
  }
}
