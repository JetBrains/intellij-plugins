// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.impl.users;

import com.thoughtworks.xstream.XStream;
import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.IDEtalkAdapter;
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.core.users.GroupEvent;
import jetbrains.communicator.core.users.UserEvent;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.XStreamUtil;

import java.io.File;

/**
 * @author Kir
 */
public class PersistentUserModelImpl extends UserModelImpl {

  public static final String FILE_NAME = "userModel.xml";

  private final transient File myDataDir;
  private final transient XStream myXStream;
  private final transient IDEtalkAdapter mySaver;
  private transient boolean mySaved;

  public PersistentUserModelImpl(EventBroadcaster broadcaster, IDEFacade ideFacade) {
    super(broadcaster);
    myDataDir = ideFacade.getConfigDir();
    myXStream = XStreamUtil.createXStream();
    myXStream.alias("user", UserImpl.class);
    myXStream.alias("users", getClass());

    readAll();

    mySaver = new IDEtalkAdapter() {
      @Override
      public void afterChange(IDEtalkEvent event) {
        event.accept(new EventVisitor(){
          @Override public void visitUserAdded(UserEvent.Added event) {
            super.visitUserAdded(event);
            saveAll();
          }

          @Override public void visitUserRemoved(UserEvent.Removed event) {
            super.visitUserRemoved(event);
            saveAll();
          }

          @Override public void visitUserUpdated(UserEvent.Updated event) {
            super.visitUserUpdated(event);
            if (
                BaseUserImpl.CAN_ACCESS_MY_FILES.equals(event.getPropertyName()) ||
                BaseUserImpl.DISPLAY_NAME.equals(event.getPropertyName()) ||
                BaseUserImpl.GROUP.equals(event.getPropertyName())
                ) {
              saveAll();
            }
          }

          @Override public void visitGroupEvent(GroupEvent event) {
            super.visitGroupEvent(event);
            saveAll();
          }
        });
      }
    };
    myBroadcaster.addListener(mySaver);
  }

  @Override
  public void dispose() {
    synchronized(myUsersGroupsLock) {
      myUsers.clear();
      myGroups.clear();
    }
    new File(getUsersFileName()).delete();
    myBroadcaster.removeListener(mySaver);
    super.dispose();
  }

  void saveAll() {
    synchronized (myUsersGroupsLock) {
      XStreamUtil.toXml(myXStream, getUsersFileName(), this);
    }
    mySaved = true;
  }

  private void readAll() {
    Object persistentModel = XStreamUtil.fromXml(myXStream, getUsersFileName(), false);
    if (persistentModel instanceof PersistentUserModelImpl) {
      PersistentUserModelImpl model = (PersistentUserModelImpl) persistentModel;
      myGroups.addAll(model.myGroups);
      myUsers.addAll(model.myUsers);
    }
  }

  private String getUsersFileName() {
    return new File(myDataDir, FILE_NAME).getAbsolutePath();
  }

  public boolean testSaved() {
    boolean result = mySaved;
    mySaved = false;
    return result;
  }
}
