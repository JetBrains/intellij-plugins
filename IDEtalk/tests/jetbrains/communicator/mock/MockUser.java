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

import jetbrains.communicator.commands.Helper;
import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.impl.users.BaseUserImpl;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.transport.XmlMessage;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.core.vfs.CodePointer;
import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.CommunicatorStrings;

import javax.swing.*;

/**
 * @author Kir Maximov
 */
public class MockUser extends BaseUserImpl {
  private boolean myOnline;
  private String[] myProjects = new String[0];
  private Transport myTransport;
  private boolean mySelf = true;
  private boolean myIDEtalkUser;


  public MockUser() {
    this(CommunicatorStrings.getMyUsername(), "", true);
    mySelf = true;
  }

  public MockUser(String name, String group, boolean isOnline) {
    super(name, group);
    myOnline = isOnline;
    mySelf = false;
  }

  public MockUser(String name, String group) {
    this(name, group, false);
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getTransportCode() {
    return MockTransport.NAME;
  }

  @Override
  public UserPresence getPresence() {
    return new UserPresence(myOnline);
  }

  @Override
  public boolean isOnline() {
    return myOnline;
  }

  @Override
  public boolean isSelf() {
    return mySelf;
  }

  public void setOnline(boolean online) {
    myOnline = online;
  }

  @Override
  public String[] getProjects() {
    return myProjects;
  }

  @Override
  public ProjectsData getProjectsData(IDEFacade ideFacade) {
    return Helper.doGetProjectsData(getTransport(), this, ideFacade);
  }

  public Transport getTransport() {
    if (myTransport == null) {
      return new MockTransport();
    }
    return myTransport;
  }

  @Override
  public void sendMessage(String message, EventBroadcaster eventBroadcaster) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void sendXmlMessage(XmlMessage message) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void sendCodeIntervalPointer(VFile file, CodePointer pointer, String comment, EventBroadcaster eventBroadcaster) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public String getVFile(VFile vFile, IDEFacade ideFacade) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public boolean hasIDEtalkClient() {
    return myIDEtalkUser;
  }

  public void setTransport(Transport mockTransport) {
    myTransport = mockTransport;
  }

  public String toString() {
    return super.toString() + " online:" + myOnline ;
  }

  public void setProjects(String[] projects) {
    myProjects = projects;
  }

  public void setIDEtalkUser(boolean b) {
    myIDEtalkUser = b;
  }
}
