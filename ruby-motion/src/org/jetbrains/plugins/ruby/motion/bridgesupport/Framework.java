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

import org.jetbrains.annotations.TestOnly;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.fqn.FQN;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public class Framework extends FunctionHolder {
  private final Map<String, Struct> myStructs = new HashMap<>();
  private Map<String, Class> myClasses = new HashMap<>();
  private final Map<String, Class> myProtocols = new HashMap<>();
  private final Map<String, Constant> myConstants = new HashMap<>();
  private final Map<String, String> myFunctionAliases = new HashMap<>();
  private final String myName;
  private final String myVersion;
  private final boolean myOSX;

  public Framework(String name, String version, boolean osx) {
    myName = name;
    myVersion = version;
    myOSX = osx;
  }

  public Collection<Class> getClasses() {
    return myClasses.values();
  }

  void addClass(Class clazz) {
    checkSeal();
    myClasses.put(clazz.getName(), clazz);
  }

  @TestOnly
  public Class getClass(final String name) {
    return getClass(FQN.Builder.fromString(name).asList());
  }

  public Class getClass(final List<String> name) {
    if (name.size() == 0) return null;
    Class current = myClasses.get(name.get(0));
    int i = 1;
    while (i < name.size() && current != null) {
      current = current.getSubClass(name.get(i));
      i++;
    }
    return current;
  }

  public Collection<Struct> getStructs() {
    return myStructs.values();
  }

  void addStruct(Struct clazz) {
    checkSeal();
    myStructs.put(clazz.getName(), clazz);
  }

  public Struct getStruct(final String name) {
    return myStructs.get(name);
  }

  public Collection<Class> getProtocols() {
    return myProtocols.values();
  }

  void addProtocol(Class clazz) {
    checkSeal();
    myProtocols.put(clazz.getName(), clazz);
  }

  public Class getProtocol(final String name) {
    return myProtocols.get(name);
  }

  public Collection<Constant> getConstants() {
    return myConstants.values();
  }

  void addConstant(Constant constant) {
    checkSeal();
    myConstants.put(constant.getName(), constant);
  }

  public Constant getConstant(final String name) {
    return myConstants.get(name);
  }

  public String getName() {
    return myName;
  }

  public String getVersion() {
    return myVersion;
  }

  public boolean isOSX() {
    return myOSX;
  }

  @Override
  public String toString() {
    return "Framework: " + myName + ", v." + myVersion;
  }

  public void addFunctionAlias(String name, String original) {
    checkSeal();
    myFunctionAliases.put(name, original);
  }

  public Map<String, String> getFunctionAliases() {
    return myFunctionAliases;
  }

  public String getOriginalFunctionName(String name) {
    return myFunctionAliases.get(name);
  }

  void mergeClasses() {
    if (!"android".equals(myName)) return;

    checkSeal();
    myClasses = Class.mergeClasses(myClasses.values());
  }
}
