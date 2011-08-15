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

import com.google.common.collect.Maps;
import com.intellij.execution.Location;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describe test structure for given JavaScript file.
 */
public class NavigationRegistry {

  private final Map<String, TestCase> myTestCaseMap;
  private final VirtualFile myVirtualFile;
  private final Map<PsiElement, Object> myMap;

  NavigationRegistry(VirtualFile virtualFile, List<TestCase> testCases) {
    myVirtualFile = virtualFile;
    myTestCaseMap = Maps.newHashMap();
    for (TestCase testCase : testCases) {
      myTestCaseMap.put(testCase.getName(), testCase);
    }
    Map<PsiElement, Object> map = new HashMap<PsiElement, Object>();
    for (TestCase testCase : testCases) {
      Location<JSCallExpression> location = testCase.getLocation();
      if (location != null) {
        map.put(location.getPsiElement(), testCase);
      }
      for (Test test : testCase.getTests()) {
        Location<? extends JSElement> location1 = test.getLocation();
        if (location1 != null) {
          map.put(location1.getPsiElement(), test);
        }
      }
    }
    myMap = map;
  }

  public VirtualFile getVirtualFile() {
    return myVirtualFile;
  }

  public TestCase getTestCaseByName(String name) {
    return myTestCaseMap.get(name);
  }

  @NotNull
  public Collection<TestCase> listTestCases() {
    return myTestCaseMap.values();
  }

  public Object getTarget(PsiElement psiElement) {
    return myMap.get(psiElement);
  }

}
