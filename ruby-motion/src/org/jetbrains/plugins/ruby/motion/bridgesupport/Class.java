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

import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.RNameUtilCore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public class Class extends FunctionHolder {
  private String myName;
  private Map<String, Class> mySubClasses;

  public Class(final String name) {
    myName = name;
    mySubClasses = new HashMap<>();
  }

  public String getName() {
    return myName;
  }

  public Collection<Class> getSubClasses() {
    return mySubClasses.values();
  }

  public Class getSubClass(String name) {
    return mySubClasses.get(name);
  }

  public static Map<String, Class> mergeClasses(Collection<? extends Class> classes) {
    final Map<String, Class> result = new HashMap<>();
    for (Class clazz : classes) {
      final String name = clazz.getName();
      final int i = name.indexOf(RNameUtilCore.SYMBOL_DELIMITER);
      if (i < 0) {
        result.put(name, clazz);
        continue;
      }

      final String containerName = name.substring(0, i);
      Class container = result.get(containerName);
      if (container == null) {
        container = new Class(containerName);
        result.put(containerName, container);
      }
      clazz.myName = name.substring(i + 2);
      container.mySubClasses.put(clazz.getName(), clazz);
    }
    for (Class container : result.values()) {
      container.mySubClasses = mergeClasses(container.mySubClasses.values());
    }
    return result;
  }
}
