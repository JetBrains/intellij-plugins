/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea.javascript.navigation;

import com.intellij.execution.Location;
import com.intellij.lang.javascript.psi.JSElement;

public class Test {

  private final String myName;
  private final Location<? extends JSElement> myLocation;
  private TestCase myTestCase;

  public Test(String name, Location<? extends JSElement> location) {
    this.myName = name;
    this.myLocation = location;
  }

  public TestCase getTestCase() {
    return myTestCase;
  }

  public void setTestCase(TestCase testCase) {
    myTestCase = testCase;
  }

  public String getName() {
    return myName;
  }

  public Location<? extends JSElement> getLocation() {
    return myLocation;
  }
}
