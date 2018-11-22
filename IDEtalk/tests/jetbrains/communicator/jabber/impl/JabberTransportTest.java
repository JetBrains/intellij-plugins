/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package jetbrains.communicator.jabber.impl;

import icons.IdeTalkCoreIcons;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.dispatcher.AsyncMessageDispatcher;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.users.UserImpl;
import jetbrains.communicator.core.impl.users.UserModelImpl;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.jabber.JabberUI;
import jetbrains.communicator.jabber.register.MockJabberFacade;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.mock.MockUser;
import org.jmock.Mock;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Kir
 */
public class JabberTransportTest extends BaseTestCase {
  private MockJabberFacade myFacade;
  private Mock myUIMock;
  private JabberTransport myTransport;
  private UserModelImpl myUserModel;
  private MutablePicoContainer myProjectContainer;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myFacade = new MockJabberFacade();
    myUIMock = mock(JabberUI.class);
    myUserModel = new UserModelImpl(getBroadcaster());
    disposeOnTearDown(myUserModel);

    Mock dispatcherMock = mock(AsyncMessageDispatcher.class);
    MockIDEFacade IDEFacade = new MockIDEFacade();
    dispatcherMock.stubs().method("getIdeFacade").will(returnValue(IDEFacade));
    myTransport = new JabberTransport((JabberUI) myUIMock.proxy(), myFacade, myUserModel,
        (AsyncMessageDispatcher) dispatcherMock.proxy(), null);
    disposeOnTearDown(myTransport);

    myProjectContainer = Pico.getInstance().makeChildContainer();
  }

  public void testIsOnlineNotConnected() {
    assertFalse("Not online - return false", UserImpl.create("fake", myTransport.getName()).isOnline());
    assertEquals("Not online - offline icon expected", IdeTalkCoreIcons.IdeTalk.Offline, myTransport.getIcon(
      myTransport.getUserPresence(new MockUser())));
  }

  public void testInitialize_Connected() throws Exception {
    myUIMock.expects(once()).method("initPerProject");
    myFacade.setConnected(true);
    initJabberTransport();
    assertEquals("Expect 'connect' call", "connect", myFacade.getLog());
  }

  public void testInitialize_NotConnected() throws Exception {
    myFacade.setConnected(false);
    myUIMock.expects(once()).method("initPerProject");
    initJabberTransport();
  }

  private void initJabberTransport() throws InterruptedException {
    myTransport.initializeProject(null, myProjectContainer);
    Thread.sleep(200);
  }

  public void testInitialize_NotConnected_SkipConnection() {
    myUIMock.expects(once()).method("initPerProject");
    myFacade.setConnected(false);
    myTransport.initializeProject(null, myProjectContainer);
  }

  public void testThreadIdPreserving() {
    final User kir = createUser("kir");
    final String threadId = myTransport.getThreadId(kir);
    assertEquals(threadId, myTransport.getThreadId(kir));

    final String t2 = myTransport.getThreadId(createUser("some@other.user"));
    assertTrue("Another user - expect another threadId", !t2.equals(threadId));
  }

  private User createUser(String s) {
    return myUserModel.createUser(s, myTransport.getName());
  }


}
