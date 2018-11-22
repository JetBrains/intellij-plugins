/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package jetbrains.communicator.idea.toolWindow;

import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.dispatcher.LocalMessageDispatcherImpl;
import jetbrains.communicator.core.impl.users.UserModelImpl;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.mock.MockIdeaMessage;
import jetbrains.communicator.mock.MockUser;

import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;

/**
 * @author Kir
 */
@SuppressWarnings({"ALL"})
public class UserTreeTest extends BaseTestCase {
  private LocalMessageDispatcherImpl myLocalMessageDispatcher;
  private UserTree myUserTree;

  private boolean myTreeChanged;
  private boolean mySendMessageInvoked;
  private MockUser myUser;
  private TreePath myTreePath;

  protected void setUp() throws Exception {
    super.setUp();

    UserModelImpl userModel = new UserModelImpl(getBroadcaster());
    disposeOnTearDown(userModel);
    myLocalMessageDispatcher = new LocalMessageDispatcherImpl(getBroadcaster(), new MockIDEFacade(getClass()), userModel);
    disposeOnTearDown(myLocalMessageDispatcher);
    myUserTree = new UserTree(myLocalMessageDispatcher) {
      public void treeDidChange() {
        super.treeDidChange();
        myTreeChanged = true;
      }

      @Override
      protected void invokeSendMessageAction() {
        mySendMessageInvoked = true;
      }
    };

    myUser = new MockUser();
    myTreePath = new TreePath(myUser);

    myTreeChanged = false;
    mySendMessageInvoked = false;
  }

  public void testSingleClickNoMessage() throws Exception {
    myUserTree.onClick(myTreePath, myTreePath.getLastPathComponent(), createMouseEvent());

    assertFalse("No change expected", myTreeChanged);
    assertFalse("No send message dialog expected", mySendMessageInvoked);
  }

  public void testDeliverLocalMessage() throws Exception {
    MockIdeaMessage message = new MockIdeaMessage();
    myLocalMessageDispatcher.addPendingMessage(myUser, message);

    MouseEvent mouseEvent = createMouseEvent();
    myUserTree.onClick(myTreePath, myTreePath.getLastPathComponent(), mouseEvent);

    verifyMessageDelivery();
  }

  public void testDoubleClickNoEvents() throws Exception {
    myUserTree.onDblClick(myTreePath, myTreePath.getLastPathComponent(), createMouseEvent());
    assertTrue("Expect send message dialog", mySendMessageInvoked);
  }

  public void testDoubleClickEventsInQueue() throws Exception {
    MockIdeaMessage message = new MockIdeaMessage();
    myLocalMessageDispatcher.addPendingMessage(myUser, message);

    MouseEvent mouseEvent = createMouseEvent();
    myUserTree.onClick(myTreePath, myTreePath.getLastPathComponent(), mouseEvent);
    myUserTree.onDblClick(myTreePath, myTreePath.getLastPathComponent(), mouseEvent);

    verifyMessageDelivery();
  }

  public void testDoubleClick_AfterDelivery() throws Exception {
    MockIdeaMessage message = new MockIdeaMessage();
    myLocalMessageDispatcher.addPendingMessage(myUser, message);

    MouseEvent mouseEvent = createMouseEvent();
    myUserTree.onClick(myTreePath, myTreePath.getLastPathComponent(), mouseEvent);
    myUserTree.onDblClick(myTreePath, myTreePath.getLastPathComponent(), mouseEvent);

    verifyMessageDelivery();

    myUserTree.onClick(myTreePath, myTreePath.getLastPathComponent(), mouseEvent);
    myUserTree.onDblClick(myTreePath, myTreePath.getLastPathComponent(), mouseEvent);
    assertTrue("Should invoke send message dialog when nothing to deliver", mySendMessageInvoked);
  }

  private void verifyMessageDelivery() {
    assertTrue("Message delivery occured, change expected", myTreeChanged);
    assertEquals("Message delivery expected", 0, myLocalMessageDispatcher.getPendingMessages(myUser).length);

    assertFalse("No send message dialog expected", mySendMessageInvoked);
  }

  private MouseEvent createMouseEvent() {
    return new MouseEvent(myUserTree, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 0, 0, 1, false);
  }
}
