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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public class FunctionHolder extends Sealable {
  private final Map<String, Function> myFunctions = new HashMap<>();

  public Collection<Function> getFunctions() {
    return myFunctions.values();
  }

  public void addFunction(Function function) {
    checkSeal();
    myFunctions.put(function.getName(), function);
  }

  public Function getFunction(final String name) {
    return myFunctions.get(name);
  }
}
