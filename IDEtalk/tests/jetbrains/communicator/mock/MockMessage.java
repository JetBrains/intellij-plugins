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

import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.util.icons.EmptyIcon;

import javax.swing.*;
import java.util.Date;

/**
 * @author Kir
 */
public class MockMessage implements LocalMessage {
  private boolean mySendSuccessful;
  protected String myLog = "";
  private Date myDate = new Date();
  private String myMessage;

  public MockMessage() {
    this(true);
  }

  public MockMessage(Date date) {
    this(date, "");
  }

  public MockMessage(Date date, String msg) {
    this();
    myDate = date;
    myMessage = msg;
  }

  @Override
  public boolean containsString(String searchString) {
    return myMessage.contains(searchString);
  }

  @Override
  public Icon getMessageIcon(int refreshCounter) {
    return new EmptyIcon(3,3);
  }

  public MockMessage(boolean sendSuccessful) {
    mySendSuccessful = sendSuccessful;
  }


  public String getMessage() {
    return myMessage;
  }

  @Override
  public Date getWhen() {
    return myDate;
  }

  @Override
  public boolean send(User user) {
    myLog = "sent to " + user + ":" + (mySendSuccessful ? "success" : "fail");
    return mySendSuccessful;
  }

  public void setSendSuccessful(boolean sendSuccessful) {
    mySendSuccessful = sendSuccessful;
  }

  public void clearLog() {
    myLog = "";
  }

  public String getLog() {
    return myLog;
  }
}
