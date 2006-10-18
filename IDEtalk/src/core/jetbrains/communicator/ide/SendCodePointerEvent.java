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
