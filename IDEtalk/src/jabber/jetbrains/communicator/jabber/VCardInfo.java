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
