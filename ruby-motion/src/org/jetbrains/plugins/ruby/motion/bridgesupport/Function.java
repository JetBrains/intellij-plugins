/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.ruby.motion.bridgesupport;

import com.intellij.openapi.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class Function extends Sealable {
  private final String myName;
  private final boolean myVariadic;
  private final boolean myClassMethod;
  private String myReturnValue;
  private final List<Pair<String, String>> myArguments = new ArrayList<>();

  public Function(final String name, final boolean isVariadic, final boolean isClassMethod) {
    myName = name;
    myVariadic = isVariadic;
    myClassMethod = isClassMethod;
  }

  public String getName() {
    return myName;
  }

  public String getReturnValue() {
    return myReturnValue;
  }

  public List<Pair<String, String>> getArguments() {
    return myArguments;
  }

  void addArgument(final String name, final String declaredType) {
    checkSeal();
    myArguments.add(Pair.create(name, declaredType));
  }

  void setReturnValue(String returnValue) {
    checkSeal();
    myReturnValue = returnValue;
  }

  public boolean isVariadic() {
    return myVariadic;
  }

  public boolean isClassMethod() {
    return myClassMethod;
  }

  public boolean isId() {
    return "id".equals(getReturnValue()) || "instancetype".equals(getReturnValue());
  }
}
