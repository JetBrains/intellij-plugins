// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.impl.dispatcher;

import com.thoughtworks.xstream.converters.SingleValueConverter;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;

/**
 * @author Kir
*/
class UserConverter implements SingleValueConverter {
  private final UserModel myUserModel;

  UserConverter(UserModel userModel) {
    myUserModel = userModel;
  }

  @Override
  public String toString(Object obj) {
    User user = ((User) obj);
    return user.getTransportCode() + ':' + user.getName();
  }

  @Override
  public Object fromString(String str) {
    int idx = str.indexOf(':');
    return myUserModel.createUser(str.substring(idx + 1), str.substring(0, idx));
  }

  @Override
  public boolean canConvert(Class type) {
    return User.class.isAssignableFrom(type);
  }
}
