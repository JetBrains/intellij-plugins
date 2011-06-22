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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.intellij.execution.Location;
import com.intellij.lang.javascript.psi.JSCallExpression;

public class TestCase {

  private final String myName;
  private final Location<JSCallExpression> myLocation;
  private final List<Test> myTests;
  private final Map<String, Test> myTestByNameMap;

  public TestCase(String name, @Nullable Location<JSCallExpression> location, List<Test> tests) {
    myName = name;
    myLocation = location;
    myTests = new ArrayList<Test>(tests);
    myTestByNameMap = new HashMap<String, Test>();
    for (Test test : myTests) {
      myTestByNameMap.put(test.getName(), test);
    }
    for (Test test : tests) {
      test.setTestCase(this);
    }
  }

  public String getName() {
    return myName;
  }

  @Nullable
  public Location<JSCallExpression> getLocation() {
    return myLocation;
  }

  public List<Test> getTests() {
    return myTests;
  }

  public Test getTestByName(String name) {
    return myTestByNameMap.get(name);
  }
}
