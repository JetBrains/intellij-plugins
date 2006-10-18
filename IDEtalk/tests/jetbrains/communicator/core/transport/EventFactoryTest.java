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

import com.intellij.openapi.util.io.FileUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import jetbrains.communicator.mock.MockTransport;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * @author Kir
 * @noinspection JUnitTestCaseWithNonTrivialConstructors
 */
public class EventFactoryTest extends TestCase {
  public static Test suite() throws IOException {
    TestSuite result = new TestSuite();

    URL self = EventFactoryTest.class.getResource("EventFactoryTest.class");
    File parent = new File(new File(self.getFile()).getParentFile(), "efData");

    File[] files = parent.listFiles();
    for (int i = 0; i < files.length; i++) {
      char[] contents = FileUtil.loadFileText(files[i]);
      String []data = new String(contents).split("==[^=]*==\r?\n");
      result.addTest(new MessageEventTst(files[i].getName(), data));
    }


    //result.addTestSuite(EventFactoryTest.class);
    return result;
  }

  private static class MessageEventTst extends TestCase {
    private String[] myData;

    MessageEventTst(String name, String[] data) {
      super(name);
      myData = data;
    }

    protected void runTest() throws Throwable {
      MessageEvent event = EventFactory.createMessageEvent(new MockTransport(), "user", myData[0]);
      assertEquals("Wrong message extracted", myData[1], event.getMessage());

      if (myData.length == 2) {
        assertFalse(event.toString(), event instanceof StacktraceEvent);
      }
      else if (myData.length == 3) {
        assertTrue(event.toString(), event instanceof StacktraceEvent);
        assertEquals("Wrong stacktrace extracted", myData[2],
            ((StacktraceEvent) event).getStacktrace());
      }
      else {
        fail(Arrays.asList(myData).toString());
      }
    }

  }
}
