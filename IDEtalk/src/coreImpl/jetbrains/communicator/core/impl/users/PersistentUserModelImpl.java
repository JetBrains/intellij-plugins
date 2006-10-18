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
package jetbrains.communicator.core.impl.users;

import com.thoughtworks.xstream.XStream;
import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.IDEtalkAdapter;
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.core.users.GroupEvent;
import jetbrains.communicator.core.users.UserEvent;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.XMLUtil;

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
    myXStream = XMLUtil.createXStream();
    myXStream.alias("user", UserImpl.class);
    myXStream.alias("users", getClass());

    readAll();

    mySaver = new IDEtalkAdapter() {
      public void afterChange(IDEtalkEvent event) {
        event.accept(new EventVisitor(){
          public void visitUserAdded(UserEvent.Added event) {
            super.visitUserAdded(event);
            saveAll();
          }

          public void visitUserRemoved(UserEvent.Removed event) {
            super.visitUserRemoved(event);
            saveAll();
          }

          public void visitUserUpdated(UserEvent.Updated event) {
            super.visitUserUpdated(event);
            if (
                BaseUserImpl.CAN_ACCESS_MY_FILES.equals(event.getPropertyName()) ||
                BaseUserImpl.DISPLAY_NAME.equals(event.getPropertyName()) ||
                BaseUserImpl.GROUP.equals(event.getPropertyName())
                ) {
              saveAll();
            }
          }

          public void visitGroupEvent(GroupEvent event) {
            super.visitGroupEvent(event);
            saveAll();
          }
        });
      }
    };
    myBroadcaster.addListener(mySaver);
  }

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
    synchronized(myUsersGroupsLock) {
      XMLUtil.toXml(myXStream, getUsersFileName(), this);
    }
    mySaved = true;
  }

  private void readAll() {
    Object persistentModel = XMLUtil.fromXml(myXStream, getUsersFileName(), false);
    if (persistentModel instanceof PersistentUserModelImpl) {
      PersistentUserModelImpl model = (PersistentUserModelImpl) persistentModel;
      if (model.myGroups != null) myGroups.addAll(model.myGroups);
      if (model.myUsers  != null) myUsers.addAll(model.myUsers);
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
