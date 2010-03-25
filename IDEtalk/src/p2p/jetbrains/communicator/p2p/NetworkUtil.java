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
package jetbrains.communicator.p2p;

import jetbrains.communicator.core.Pico;
import jetbrains.communicator.util.TimeoutCachedValue;
import jetbrains.communicator.util.XmlRpcTarget;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * @author Kir
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class NetworkUtil {
  private static final Logger LOG = Logger.getLogger(NetworkUtil.class);

  private static final TimeoutCachedValue<List<InetAddress>> ourInterfaces = new TimeoutCachedValue<List<InetAddress>>(30 * 1000) {
    @Override
    protected List<InetAddress> calculate() {
      final List<InetAddress> result = new ArrayList<InetAddress>();
      try {
        final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
          NetworkInterface ni = networkInterfaces.nextElement();
          final Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
          while (inetAddresses.hasMoreElements()) {
            InetAddress inetAddress = inetAddresses.nextElement();
            if (inetAddress instanceof Inet4Address) {       // Inet6Address is not supported - unable to reference via URL
              if (!inetAddress.isLoopbackAddress() || Pico.isUnitTest()) {
                result.add(inetAddress);
              }
            }
          }
        }
      } catch (SocketException e) {
        LOG.error("Cannot get list of local interfaces", e);
      }
      return result;
    }
  };

  private NetworkUtil() {
  }

  public static InetAddress[] getSelfAddresses() throws SocketException {
    final List<InetAddress> res = ourInterfaces.getValue();
    return res.toArray(new InetAddress[res.size()]);
  }

  public static Object sendMessage(XmlRpcTarget target, String xmlRpcId, String method, Object... parameters) {
    return sendMessage(target, xmlRpcId, method, Arrays.asList(parameters));
  }

  private static Object sendMessage(XmlRpcTarget target, String xmlRpcId, String method, List<Object> parameters) {
    checkParameters(parameters, method);

    String url = "http://" + target.getAddress().getHostAddress() + ':' + target.getPort() + '/';
    String fullMethodName = xmlRpcId + '.' + method;

    String logLine = "Call [" + url + "] " + fullMethodName;
    if (LOG.isDebugEnabled()) {
      LOG.info(buildFullLogLine(logLine, parameters));
    }
    for (int i = 0; i < parameters.size(); i++) {
      Object o = parameters.get(i);
      assert o != null : "Null parameter in position " + i;
    }

    try {
      XmlRpcClient client = new XmlRpcClient(url);
      return client.execute(fullMethodName, new Vector<Object>(parameters));
    } catch (MalformedURLException e) {
      LOG.info(buildFullLogLine(logLine, parameters)+ ' ' +e.getLocalizedMessage());
    } catch (IOException e) {
      LOG.info(buildFullLogLine(logLine, parameters)+ ' ' + e.getLocalizedMessage());
    } catch (XmlRpcException e) {
      LOG.info(buildFullLogLine(logLine, parameters)+ ' ' + e.getLocalizedMessage());
    }
    return null;
  }

  private static String buildFullLogLine(String logLine, List<? extends Object> parameters) {
    return logLine + '(' + parameters + ')';
  }

  private static void checkParameters(List<? extends Object> parameters, String method) {
    for (int i = 0; i < parameters.size(); i++) {
      assert parameters.get(i) != null : " null parameter " + i + " to " + method;
    }
  }

  public static boolean isOwnAddress(InetAddress address) {
    if (address == null) return false;
    try {
      return address.isLoopbackAddress() || Arrays.asList(getSelfAddresses()).contains(address);
    } catch (SocketException e) {
      LOG.info(e.getLocalizedMessage());
    }
    return false;
  }

  public static boolean isPortBusy(int port) {
    ServerSocket socket = null;
    try {
      socket = new ServerSocket(port);
    } catch (IOException e) {
      return true;
    }
    finally {
      close(socket);
    }
    return false;
  }

  private static void close(ServerSocket socket) {
    if (socket != null) {
      try {
        socket.close();
      } catch (IOException e) {
        LOG.info(e.getLocalizedMessage());
      }
    }
  }
}
