/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.util;

import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.net.ServerSocket;

public final class SocketUtils {
  private static final Logger LOG = Logger.getInstance(SocketUtils.class);

  public static int findFreePortForApi() {
    ServerSocket socket = null;
    try {
      socket = new ServerSocket(0);
      return socket.getLocalPort();
    }
    catch (IOException e) {
      LOG.debug(e);
    }
    finally {
      if (socket != null) {
        try {
          socket.close();
        }
        catch (IOException e) {
          LOG.debug(e);
        }
      }
    }
    return -1;
  }
}