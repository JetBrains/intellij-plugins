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

import junit.framework.TestCase;
import jetbrains.communicator.core.vfs.CodePointer;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.mock.MockTransport;
import org.jdom.Element;

/**
 * @author Kir
 */
public class CodePointerXmlMessageTest extends TestCase {

  public void testSimplest() throws Throwable {
    _test("", VFile.create("some/path"), new CodePointer(1,2,3,4));
  }

  public void testComplexVFile() throws Throwable {
    VFile expected = VFile.create("foo/bar", "bar", false);
    expected.setContents("#$@#@#$\u0447\u0442\u043e-\u0442\u043e \u0442\u0430\u043a\u043e\u0435");
    expected.setProjectName("project");
    expected.setFQName("some FQName");
    expected.setSourcePath("some source path");

    _test("", expected, new CodePointer(1,2,3,4));
  }

  private void _test(String text, VFile vFile, CodePointer codePointer) {
    Element root = new Element("element");
    CodePointerXmlMessage xmlMessage = new CodePointerXmlMessage(text, codePointer, vFile);
    xmlMessage.fillRequest(root);

    CodePointerEvent event = CodePointerXmlMessage.createEvent(new MockTransport(), "someUser", root);

    assertEquals("someUser", event.getRemoteUser());
    assertEquals("Wrong code pointer restored", codePointer, event.getCodePointer());
    vFile.assertEquals(event.getFile());
  }

}
