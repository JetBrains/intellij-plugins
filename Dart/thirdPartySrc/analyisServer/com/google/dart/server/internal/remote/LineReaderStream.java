/*
 * Copyright (c) 2014, the Dart project authors.
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

import java.io.IOException;

/**
 * A source of remote server lines.
 * 
 * @coverage dart.server.remote
 */
public interface LineReaderStream {

  /**
   * Takes the the next line from the stream and return it. Blocks if no response available.
   */
  String readLine() throws Exception;

  /**
   * Returns {@code true} if there is content to be read.
   */
  boolean ready() throws IOException;
}
