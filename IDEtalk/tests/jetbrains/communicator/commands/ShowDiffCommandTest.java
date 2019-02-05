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
package jetbrains.communicator.commands;

import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.mock.MockUser;
import org.jmock.Mock;

/**
 * @author kir
 */
public class ShowDiffCommandTest extends BaseTestCase {
  private ShowDiffCommand myCommand;

  private static final String myLog = "";
  private Mock myFacadeMock;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myFacadeMock = mock(IDEFacade.class);
    myCommand = new ShowDiffCommand((IDEFacade) myFacadeMock.proxy());
  }

  public void testEnabled() {
    assertFalse(myCommand.isEnabled());

    myCommand.setUser(new MockUser());
    assertFalse(myCommand.isEnabled());

    VFile vFile = VFile.create("a path");
    myCommand.setVFile(vFile);

    myFacadeMock.expects(once()).method("hasFile").with(eq(vFile)).will(returnValue(false));
    assertFalse(myCommand.isEnabled());

    myFacadeMock.expects(once()).method("hasFile").with(eq(vFile)).will(returnValue(true));
    assertTrue(myCommand.isEnabled());
  }

  public void testExecute() {
    final VFile vFile = VFile.create("a file");
    MockUser user = new MockUser() {
      @Override
      public String getVFile(VFile file, IDEFacade ideFacade) {
        assertSame(vFile, file);
        return "something";
      }
    };

    myCommand.setUser(user);
    myCommand.setVFile(vFile);

    myFacadeMock.expects(once()).method("showDiffFor").with(eq(user), eq(vFile), eq("something"));

    myCommand.execute();
  }

  public void testExecute_NoContent() {
    final VFile vFile = VFile.create("a file");
    MockUser user = new MockUser() {
      @Override
      public String getVFile(VFile file, IDEFacade ideFacade) {
        assertSame(vFile, file);
        return null;
      }
    };

    myCommand.setUser(user);
    myCommand.setVFile(vFile);

    myCommand.execute();
  }

}
