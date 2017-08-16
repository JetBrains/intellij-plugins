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
package jetbrains.communicator;

import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.dispatcher.AsyncMessageDispatcherImpl;
import jetbrains.communicator.core.impl.users.UserModelImpl;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.util.WaitFor;
import jetbrains.communicator.util.WatchDog;

import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * @author Kir
 */
public abstract class AbstractTransportTestCase extends BaseTestCase {
  protected Transport myTransport;
  protected MockIDEFacade myIdeFacade;
  protected UserModelImpl myUserModel;
  protected AsyncMessageDispatcherImpl myDispatcher;
  protected User mySelf;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myIdeFacade = new MockIDEFacade(getClass());
    myUserModel = new UserModelImpl(getBroadcaster());
    myDispatcher = new AsyncMessageDispatcherImpl(getBroadcaster(), myIdeFacade);

    disposeOnTearDown(myUserModel);
    disposeOnTearDown(myDispatcher);

    registerResponseProviders(myUserModel, myIdeFacade);

    myTransport = createTransport();
    mySelf = createSelf();
  }

  protected abstract Transport createTransport();
  protected abstract User createSelf() throws UnknownHostException;
  protected abstract User createAnotherOnlineUser() throws Exception;


  public void testSendMessage_Functional() {
    TestUtil.testSendMessage_Functional(this, mySelf);
  }

  public void testSendCodePointer_Functional() {
    TestUtil.testSendCodePointer_Functional(this, mySelf);
  }

  public void testSendXmlMessage_NoResponse() {
    TestUtil.testSendXmlMessage_Functional(this, mySelf, false);
  }

  public void testSendXmlMessage_WithResponse() {
    TestUtil.testSendXmlMessage_Functional(this, mySelf, true);
  }

  public void testGetUserStatus() throws Exception {
    User someone = createAnotherOnlineUser();

    myUserModel.addUser(mySelf);
    myUserModel.addUser(someone);
    mySelf.setCanAccessMyFiles(true, myUserModel);
    someone.setCanAccessMyFiles(true, myUserModel);

    ProjectsData projectsData = someone.getProjectsData(myIdeFacade);
    assertSame("No self status yet", ProjectsData.NULL, projectsData);

    myIdeFacade.setReturnedProjects(new String[]{"project1"});

    assert mySelf.isOnline();
    ProjectsData status = mySelf.getProjectsData(myIdeFacade);

    assertNotSame("Real status is expected", ProjectsData.NULL, status);
    assertEquals("A project is expected in the status", 1, status.getProjects().length);
    assertEquals("Wrong project returned", "project1", status.getProjects()[0]);
  }

  public void testGetUserStatus_Disabled() {
    myUserModel.addUser(mySelf);
    myIdeFacade.setReturnedProjects(new String[]{"project1"});

    myOptions.setOption(OptionFlag.HIDE_ALL_KEY.toString(), true);
    ProjectsData selfStatus = mySelf.getProjectsData(myIdeFacade);
    assertEquals("No data expected - returning user data was disabled",
        0, selfStatus.getProjects().length);
  }

  public void testGetUserStatus_NotAllowedByUser() {
    myUserModel.addUser(mySelf);
    myIdeFacade.setReturnedProjects(new String[]{"project1"});
    myIdeFacade.setReturnedAnswer(false);

    mySelf.setCanAccessMyFiles(false, myUserModel);
    myOptions.setOption(OptionFlag.HIDE_ALL_KEY.toString(), true);
    ProjectsData selfStatus = mySelf.getProjectsData(myIdeFacade);
    assertEquals("No data expected - returning user data was not allowed by user",
        0, selfStatus.getProjects().length);
  }


  public void testGetFileContent_Success() {
    VFile vFile = VFile.create("a path");
    myUserModel.addUser(mySelf);
    mySelf.setCanAccessMyFiles(true, myUserModel);

    myIdeFacade.setReturnedFileText(vFile, "some tex&&&<<>t" + '\u0000');

    String text = mySelf.getVFile(vFile, myIdeFacade);

    assertEquals("Should successfully return file text", "some tex&&&<<>t" + '\u0000', text );
    assertEquals("Should put result to vFile", "some tex&&&<<>t" + '\u0000', vFile.getContents());
  }

  public void testGetFileContent_BigFile() {
    VFile vFile = VFile.create("a path");
    myUserModel.addUser(mySelf);
    mySelf.setCanAccessMyFiles(true, myUserModel);

    char buf [] = new char[100000];
    Arrays.fill(buf, 'd');
    myIdeFacade.setReturnedFileText(vFile, new String(buf));

    WatchDog s = new WatchDog("get 100000 bytes file");
    mySelf.getVFile(vFile, myIdeFacade);
    s.stop();

    assertEquals("Should successfully return file text", new String(buf), vFile.getContents() );
  }

  public void testGetFileContent_NoRights() {
    VFile vFile = VFile.create("a path");
    myUserModel.addUser(mySelf);
    myIdeFacade.setReturnedFileText(vFile, "some text");
    myIdeFacade.setReturnedAnswer(false);

    mySelf.setCanAccessMyFiles(false, myUserModel);
    String text = mySelf.getVFile(vFile, myIdeFacade);

    assertNull("Should not return file text", text);
    assertNull("Should not put result to vFile", vFile.getContents());
  }

  public void testGetFileContent_NoRights_AllowedByUser() {
    VFile vFile = VFile.create("a path");
    myUserModel.addUser(mySelf);
    myIdeFacade.setReturnedFileText(vFile, "some text");
    myIdeFacade.setReturnedAnswer(true);

    mySelf.setCanAccessMyFiles(false, myUserModel);
    String text = mySelf.getVFile(vFile, myIdeFacade);

    assertEquals("Should successfully return file text", "some text", text );
  }

  public void testGetFileContent_NoSuchFile() {
    VFile vFile = VFile.create("a path");

    String text = mySelf.getVFile(vFile, myIdeFacade);

    assertNull("No file - no text", text);
  }

  public void testSetOwnPresence_OnlineOffline() {
    assertTrue("Should be online by default", myTransport.isOnline());

    myTransport.setOwnPresence(new UserPresence(false));
    assertFalse("Should become offline", myTransport.isOnline());

    new WaitFor(1000) {
      @Override
      protected boolean condition() {
        return !mySelf.isOnline();
      }
    };
    assertFalse("Self user should become online", mySelf.isOnline());

    myTransport.setOwnPresence(new UserPresence(true));
    assertTrue("Should become online", myTransport.isOnline());
  }
}
