/*
 * Copyright (c) 2015, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.dart.server.internal.remote;

/**
 * This {@link Exception} is thrown by {@link RemoteAnalysisServerImpl#start()} if the underlying
 * analysis server doesn't match to a server version that this Java API can communicate with.
 */
public class ServerVersionMismatchException extends Exception {

  /**
   * The version of the analysis server that the API was pointed to.
   */
  private final String foundVersion;

  /**
   * The version of the analysis server that the Java API can support up to.
   */
  private final String upToVersion;

  public ServerVersionMismatchException(String foundVersion, String upToVersion, String message) {
    super(message);
    this.foundVersion = foundVersion;
    this.upToVersion = upToVersion;
  }

  public String getFoundVersion() {
    return foundVersion;
  }

  public String getUpToVersion() {
    return upToVersion;
  }
}
