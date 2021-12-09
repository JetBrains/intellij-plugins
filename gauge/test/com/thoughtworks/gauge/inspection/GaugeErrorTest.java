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

package com.thoughtworks.gauge.inspection;

import org.junit.Test;

import static org.junit.Assert.*;

public class GaugeErrorTest {
  @Test
  public void getInstance() {
    String message =
      "Duplicate scenario definition 'Vowel counts in single word' found in the same specification => 'Vowel counts in single word'";
    GaugeError error = GaugeError.parseCliError("[ParseError] specs/example.spec:37 " + message);

    assertNotNull(error);
    assertEquals("[ParseError] line number: 37, " + message, error.getMessage());
    assertTrue(error.isFrom("specs/example.spec"));
    assertFalse(error.isFrom("example.spec"));
  }

  @Test
  public void getInstanceWithErrorInWrongFormat() {
    String message =
      "Duplicate scenario definition 'Vowel counts in single word' found in the same specification => 'Vowel counts in single word'";
    GaugeError error = GaugeError.parseCliError("[ParseError] " + message);

    assertNull(error);
  }
}