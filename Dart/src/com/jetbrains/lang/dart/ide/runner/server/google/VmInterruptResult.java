/*
 * Copyright (c) 2013, the Dart project authors.
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

package com.jetbrains.lang.dart.ide.runner.server.google;

import java.io.IOException;

/**
 * A result class used to conditionally resume execution of an isolate.
 */
public class VmInterruptResult {
  protected static VmInterruptResult createNoopResult(VmConnection connection) {
    return new VmInterruptResult();
  }

  protected static VmInterruptResult createResumeResult(VmConnection connection, VmIsolate isolate) {
    return new VmInterruptResult(connection, isolate);
  }

  private VmConnection connection;
  private VmIsolate isolate;
  ;

  private VmInterruptResult() {

  }

  private VmInterruptResult(VmConnection connection, VmIsolate isolate) {
    this.connection = connection;
    this.isolate = isolate;
  }

  public void resume() throws IOException {
    if (isolate != null) {
      connection.resume(isolate);
    }
  }
}
