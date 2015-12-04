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

package com.google.dart.server.internal;

import com.google.common.base.Joiner;
import com.google.dart.server.generated.AnalysisServer;

import junit.framework.TestCase;

/**
 * Abstract base for any {@link AnalysisServer} implementation tests.
 */
public abstract class AbstractServerTest extends TestCase {
  protected static String makeSource(String... lines) {
    return Joiner.on("\n").join(lines);
  }

  protected AnalysisServer server;

  /**
   * Creates a concrete {@link AnalysisServer} instance.
   */
  protected abstract AnalysisServer createServer() throws Exception;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    server = createServer();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
}
