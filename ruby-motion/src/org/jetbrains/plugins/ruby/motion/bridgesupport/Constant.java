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

/**
 * @author Dennis.Ushakov
 */
public class Constant {
  private final String myName;
  private final String myDeclaredType;

  public Constant(String name, String declaredType) {
    myName = name;
    myDeclaredType = declaredType;
  }

  public String getName() {
    return myName;
  }

  public String getDeclaredType() {
    return myDeclaredType;
  }
}
