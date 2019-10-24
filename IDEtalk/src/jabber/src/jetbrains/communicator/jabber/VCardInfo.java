// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.jabber;

/**
 * @author Kir
 */
public class VCardInfo {
  private final String myFirstname;
  private final String myLastname;
  private final String myNickname;

  public VCardInfo(String firstname, String lastname, String nickName) {
    if (firstname == null) firstname = "";
    if (lastname == null) lastname = "";
    if (nickName == null) nickName = "";
    myFirstname = firstname;
    myLastname = lastname;
    myNickname = nickName;
  }

  public String getFirstname() {
    return myFirstname;
  }

  public String getLastname() {
    return myLastname;
  }

  public String getNickName() {
    return myNickname;
  }
}
