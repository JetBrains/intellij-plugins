package com.jetbrains.actionscript.profiler.model;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim
 * Date: 28.08.2010
 */
public class ProfileSettings implements JDOMExternalizable {
  private static final int DEFAULT_PORT = 1310;
  private static final String DEFAULT_HOST = "127.0.0.1";

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;

  private static final String PORT_ATTR_NAME = "port";
  private static final String HOST_ATTR_NAME = "host";

  public void readExternal(Element element) throws InvalidDataException {
    setPortFromString(element.getAttributeValue(PORT_ATTR_NAME));
    setHostFromString(element.getAttributeValue(HOST_ATTR_NAME));
  }

  public void writeExternal(Element element) throws WriteExternalException {
    if (port != DEFAULT_PORT) element.setAttribute(PORT_ATTR_NAME, String.valueOf(port));
    if (!DEFAULT_HOST.equals(host)) element.setAttribute(HOST_ATTR_NAME, host);
  }

  public void setHostFromString(String s) {
    if (!StringUtil.isEmpty(s)) {
      host = s;
    }
    else {
      host = DEFAULT_HOST;
    }
  }

  public void setPortFromString(String s) {
    if (s != null) {
      try {
        port = Integer.parseInt(s);
      }
      catch (NumberFormatException ex) {
        port = DEFAULT_PORT;
      }
    }
    else {
      port = DEFAULT_PORT;
    }
  }

  public int getPort() {
    return port;
  }

  public String getHost() {
    return host;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ProfileSettings that = (ProfileSettings)o;

    if (port != that.port) return false;
    if (!host.equals(that.host)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = host.hashCode();
    result = 31 * result + port;
    return result;
  }
}
