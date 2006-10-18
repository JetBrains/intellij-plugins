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
package jetbrains.communicator.core.impl.dispatcher;

import com.thoughtworks.xstream.converters.SingleValueConverter;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.core.users.User;

/**
 * @author Kir
*/
class UserConverter implements SingleValueConverter {
  private final UserModel myUserModel;

  UserConverter(UserModel userModel) {
    myUserModel = userModel;
  }

  public String toString(Object obj) {
    User user = ((User) obj);
    return user.getTransportCode() + ':' + user.getName();
  }

  public Object fromString(String str) {
    int idx = str.indexOf(':');
    return myUserModel.createUser(str.substring(idx + 1), str.substring(0, idx));
  }

  public boolean canConvert(Class type) {
    return User.class.isAssignableFrom(type);
  }
}
