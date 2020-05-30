// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.impl.users;

import com.intellij.openapi.util.TimeoutCachedValue;
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

import javax.swing.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

  @Override
  public String getTransportCode() {
    return myTransportCode;
  }

  @Override
  public UserPresence getPresence() {
    updateVars();
    return myUserPresenceCache.get();
  }

  @Override
  public boolean isOnline() {
    if (isSelf()) return getTransport().isOnline();

    return getTransport().getUserPresence(this).isOnline();
  }

  @Override
  public boolean isSelf() {
    return getTransport().isSelf(this);
  }

  @Override
  public Icon getIcon() {
    updateVars();
    return myIconCache.get();
  }

  @Override
  public String[] getProjects() {
    return getTransport().getProjects(this);
  }

  @Override
  public ProjectsData getProjectsData(IDEFacade ideFacade) {
    return Helper.doGetProjectsData(getTransport(), this, ideFacade);
  }

  @Override
  public String getVFile(VFile vFile, IDEFacade ideFacade) {
    Helper.fillVFileContent(getTransport(), this, vFile, ideFacade);
    return vFile.getContents();
  }

  @Override
  public void sendMessage(final String message, EventBroadcaster eventBroadcaster) {
    eventBroadcaster.doChange(new SendMessageEvent(message, this), () -> sendXmlMessage(new TextXmlMessage(message)));
  }

  @Override
  public void sendCodeIntervalPointer(final VFile file, final CodePointer pointer,
                                      final String comment, EventBroadcaster eventBroadcaster) {
    eventBroadcaster.doChange(new SendCodePointerEvent(comment, file, pointer, this),
                              () -> sendXmlMessage(new CodePointerXmlMessage(comment, pointer, file)));
  }

  @Override
  public void sendXmlMessage(XmlMessage message) {
    getTransport().sendXmlMessage(this, message);
  }

  @Override
  public boolean hasIDEtalkClient() {
    return getTransport().hasIdeTalkClient(this);
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
      myUserPresenceCache =
        new TimeoutCachedValue<>(CACHE_TIMEOUT, TimeUnit.MILLISECONDS, () -> getTransport().getUserPresence(UserImpl.this));
      myIconCache = new TimeoutCachedValue<>(CACHE_TIMEOUT, TimeUnit.MILLISECONDS, () -> getTransport().getIcon(getPresence()));
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
