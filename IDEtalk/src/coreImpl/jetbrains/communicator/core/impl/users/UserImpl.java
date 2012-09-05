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

import jetbrains.communicator.commands.Helper;
import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.impl.NullTransport;
import jetbrains.communicator.core.transport.CodePointerXmlMessage;
import jetbrains.communicator.core.transport.TextXmlMessage;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.transport.XmlMessage;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.core.vfs.CodePointer;
import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.SendCodePointerEvent;
import jetbrains.communicator.ide.SendMessageEvent;
import jetbrains.communicator.util.TimeoutCachedValue;

import javax.swing.*;
import java.util.List;

/**
 * @author Kir
 */
public final class UserImpl extends BaseUserImpl {
  private final String myTransportCode;
  private transient Transport myTransport;

  private static final int CACHE_TIMEOUT = 800;
  private transient TimeoutCachedValue<UserPresence> myUserPresenceCache;
  private transient TimeoutCachedValue<Icon> myIconCache;

  private UserImpl(String name, String transportCode) {
    super(name, "");
    myTransportCode = transportCode;
  }

  public static User create(String name, String transportCode) {
    assert transportCode != null : "Null transport code. Use Fake for tests";
    return new UserImpl(name, transportCode);
  }

  public String getTransportCode() {
    return myTransportCode;
  }

  public UserPresence getPresence() {
    updateVars();
    return myUserPresenceCache.getValue();
  }

  public boolean isOnline() {
    if (isSelf()) return getTransport().isOnline();
    
    return getTransport().getUserPresence(this).isOnline();
  }

  public boolean isSelf() {
    return getTransport().isSelf(this);
  }

  public Icon getIcon() {
    updateVars();
    return myIconCache.getValue();
  }

  public String[] getProjects() {
    return getTransport().getProjects(this);
  }

  public ProjectsData getProjectsData(IDEFacade ideFacade) {
    return Helper.doGetProjectsData(getTransport(), this, ideFacade);
  }

  public String getVFile(VFile vFile, IDEFacade ideFacade) {
    Helper.fillVFileContent(getTransport(), this, vFile, ideFacade);
    return vFile.getContents(); 
  }

  public void sendMessage(final String message, EventBroadcaster eventBroadcaster) {
    eventBroadcaster.doChange(new SendMessageEvent(message, this), new Runnable() {
      public void run() {
        sendXmlMessage(new TextXmlMessage(message));
      }
    });
  }

  public void sendCodeIntervalPointer(final VFile file, final CodePointer pointer, 
                                      final String comment, EventBroadcaster eventBroadcaster) {
    eventBroadcaster.doChange(new SendCodePointerEvent(comment, file, pointer, this), new Runnable() {
      public void run() {
        sendXmlMessage(new CodePointerXmlMessage(comment, pointer, file));
      }
    });
  }

  public void sendXmlMessage(XmlMessage message) {
    getTransport().sendXmlMessage(this, message);
  }

  public boolean hasIDEtalkClient() {
    return getTransport().hasIDEtalkClient(this);
  }

  Transport getTransport() {
    if (myTransport == null) {
      myTransport = _getTransport();
    }
    return myTransport;
  }

  private Transport _getTransport() {
    List availableTransports = Pico.getInstance().getComponentInstancesOfType(Transport.class);
    for (Object availableTransport : availableTransports) {
      Transport transport = (Transport) availableTransport;
      if (transport.getName().equals(myTransportCode)) {
        return transport;
      }
    }
    return new NullTransport();
  }

  private void updateVars() {
    if (myUserPresenceCache == null) {
      myUserPresenceCache = new TimeoutCachedValue<UserPresence>(CACHE_TIMEOUT) {
        protected UserPresence calculate() {
          return getTransport().getUserPresence(UserImpl.this);
        }
      };
      myIconCache = new TimeoutCachedValue<Icon>(CACHE_TIMEOUT) {
        protected Icon calculate() {
          return getTransport().getIcon(getPresence());
        }
      };
    }
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserImpl)) return false;
    if (!super.equals(o)) return false;

    final UserImpl user = (UserImpl) o;

    return !(myTransportCode != null ? !myTransportCode.equals(user.myTransportCode) : user.myTransportCode != null);

  }

  public int hashCode() {
    int result = super.hashCode();
    result = 29 * result + (myTransportCode != null ? myTransportCode.hashCode() : 0);
    return result;
  }
}
