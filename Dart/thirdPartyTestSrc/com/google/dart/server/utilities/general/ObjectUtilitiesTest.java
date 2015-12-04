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
package com.google.dart.server.utilities.general;

import junit.framework.TestCase;

import static org.fest.assertions.Assertions.assertThat;

public class ObjectUtilitiesTest extends TestCase {
  public void test_combinesHashCodes() {
    assertThat(ObjectUtilities.combineHashCodes(1, 2)).isNotEqualTo(0);
  }

  public void test_equals_notNull_notNull_false() {
    assertFalse(ObjectUtilities.equals("foo", "bar"));
  }

  public void test_equals_notNull_notNull_true() {
    assertTrue(ObjectUtilities.equals("foo", "foo"));
  }

  public void test_equals_notNull_null() {
    assertFalse(ObjectUtilities.equals("test", null));
  }

  public void test_equals_null_notNull() {
    assertFalse(ObjectUtilities.equals(null, "test"));
  }

  public void test_equals_null_null() {
    assertTrue(ObjectUtilities.equals(null, null));
  }
}
