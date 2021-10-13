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
package com.google.dart.server;

/**
 * The interface {@code ServerStatusListener} defines the behavior of objects that listen for
 * results from an analysis server.
 */
public interface AnalysisServerStatusListener {

  /**
   * Indicates whether the server is still running or not
   * 
   * @param isAlive {@code true} if server still alive
   */
  void isAliveServer(boolean isAlive);

}
