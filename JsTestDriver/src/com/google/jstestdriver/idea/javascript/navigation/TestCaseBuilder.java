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

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.intellij.execution.Location;
import com.intellij.lang.javascript.psi.JSCallExpression;

public class TestCaseBuilder {
  private final String myName;
  private final Location<JSCallExpression> myLocation;
  private final List<Test> myTests = Lists.newArrayList();

  TestCaseBuilder(String name, @Nullable Location<JSCallExpression> location) {
    myName = name;
    myLocation = location;
  }

  public Location<JSCallExpression> getLocation() {
    return myLocation;
  }

  public TestCaseBuilder addTest(Test test) {
    myTests.add(test);
    return this;
  }

  public TestCase build() {
    return new TestCase(myName, myLocation, myTests);
  }
}
