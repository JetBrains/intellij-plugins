/*
 * Copyright 2012 Dart project authors.
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

package com.jetbrains.lang.dart.ide.runner.server.google;


import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Contract;

/**
 * This class contains static utility methods for use by the debugger.
 */
public class DebuggerUtils {

  private DebuggerUtils() {
  }

  public static boolean isInternalMethodName(String methodName) {
    return methodName.startsWith("Object._noSuchMethod@");
  }

  /**
   * The names of private fields are mangled by the VM.
   * <p/>
   * _foo@652376 ==> _foo
   * <p/>
   * Also, remove "set:" and "get:", as these are artifacts of the VM implementation.
   * <p/>
   * e.x., Cat.get:color() ==> Cat.color()
   *
   * @param name
   * @return
   */
  @Contract("null->null")
  public static String demangleVmName(String name) {
    if (name == null) {
      return null;
    }

    int atIndex = name.indexOf('@');

    while (atIndex != -1) {
      // check for _foo@76876.bar (or _Process@14117cc4._reportError@14117cc4)
      int endIndex = name.indexOf('.', atIndex);

      if (endIndex == -1) {
        name = name.substring(0, atIndex);
      }
      else {
        name = name.substring(0, atIndex) + name.substring(endIndex);
      }

      atIndex = name.indexOf('@');
    }

    // Also remove the trailing '.' for default constructors.
    name = StringUtil.trimEnd(name, ".");

    // remove "set:" and "get:"
    // Cat.get:color() ==> Cat.color()
    if (name.contains(".set:")) {
      int index = name.indexOf(".set:");
      name = name.substring(0, index + 1) + name.substring(index + 5);
    }

    if (name.contains(".get:")) {
      int index = name.indexOf(".get:");
      name = name.substring(0, index + 1) + name.substring(index + 5);
    }

    return name;
  }
}
