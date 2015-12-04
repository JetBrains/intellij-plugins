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
package com.google.dart.server.internal.remote.processor;

import junit.framework.TestCase;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.AnalysisErrorSeverity;
import org.dartlang.analysis.server.protocol.Location;

import static org.mockito.Mockito.mock;

public class AnalysisErrorImplTest extends TestCase {
  public void test_new() throws Exception {
    String errorSeverity = AnalysisErrorSeverity.ERROR;
    String errorType = "COMPILE_TIME_ERROR";
    Location location = mock(Location.class);
    AnalysisError error = new AnalysisError(errorSeverity, errorType, location, "my message", "my correction", false);
    assertEquals(errorSeverity, error.getSeverity());
    assertEquals(errorType, error.getType());
    assertEquals(location, error.getLocation());
    assertEquals("my message", error.getMessage());
    assertEquals("my correction", error.getCorrection());
  }
}
