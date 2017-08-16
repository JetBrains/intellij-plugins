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
package jetbrains.communicator.idea.codePointer;

import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.transport.CodePointerEvent;
import jetbrains.communicator.core.transport.EventFactory;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.mock.MockTransport;

/**
 * @author kir
 */
public class CodePointerLocalMessageTest extends BaseTestCase {
  public void testGetLinkText() {
    testLinkText("a file (2..4)", "a file", 1, 3);

    testLinkText("file (2..4)", "a path/file", 1, 3);
    testLinkText("file (2..4)", "/a path/file", 1, 3);
    testLinkText("file (2..4)", "a path\\file", 1, 3);
    testLinkText("file (2..4)", "\\a path\\file", 1, 3);
    testLinkText("file (2..4)", "/a path\\file", 1, 3);
    testLinkText("file (2..4)", "\\a path/file", 1, 3);

    testLinkText("file:4", "file", 3, 3);

  }

  private void testLinkText(String expected, String path, int line1, int line2) {
    CodePointerEvent event = EventFactory.createCodePointerEvent(new MockTransport(), "remoteUser",
           VFile.create(path), line1, 22, line2, 43, "comment");
    IncomingCodePointerMessage message = new IncomingCodePointerMessage(event, null);

    assertEquals("Incorrect link text", expected, message.getLinkText());
  }
}
