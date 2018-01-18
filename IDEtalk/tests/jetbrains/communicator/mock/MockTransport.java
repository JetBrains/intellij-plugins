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
package jetbrains.communicator.mock;

import jetbrains.communicator.core.impl.NullTransport;
import jetbrains.communicator.core.impl.users.UserImpl;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.ide.ProgressIndicator;
import jetbrains.communicator.util.WaitFor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kir
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class MockTransport extends NullTransport {
  protected String mySelfUserName;
  public static final String NAME = "Fake";
  private boolean myOnline = true;
  private UserPresence myPresence;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isSelf(User user) {
    return user != null && user.getName().equals(mySelfUserName);
  }

  @Override
  public String[] getProjects(User user) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public User[] findUsers(ProgressIndicator progressIndicator) {
    for(int  i = 0; i < 20; i ++) {
      progressIndicator.setText("ddd" + i);
      progressIndicator.setFraction(1.0 * (i + 1) / 20);
      new WaitFor(200){
        @Override
        protected boolean condition() {
          return false;
        }
      };

    }
    List<User> result = new ArrayList<>();
    result.add(UserImpl.create("user", NAME));
    result.add(UserImpl.create("user1", NAME));
    result.add(UserImpl.create("user2", NAME));
    return result.toArray(new User[0]);
  }

  @Override
  public boolean isOnline() {
    return myOnline;
  }


  public void setOnline(boolean online) {
    myOnline = online;
  }

  @Override
  public UserPresence getUserPresence(User user) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  public void setSelf(String selfUserName) {
    mySelfUserName = selfUserName;
  }


  @Override
  public void setOwnPresence(UserPresence userPresence) {
    myPresence = userPresence;
  }

  public UserPresence getPresence() {
    return myPresence;
  }
}
