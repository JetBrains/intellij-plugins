/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

package org.jetbrains.idea.perforce.perforce;

import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandArguments {
  private final List<String> myArguments = new ArrayList<>();

  public CommandArguments(List<String> arguments) {
    myArguments.addAll(arguments);
  }

  public CommandArguments() {
  }

  public String[] getArguments() {
    return ArrayUtilRt.toStringArray(myArguments);
  }

  public CommandArguments append(@NonNls @NotNull String argument) {
    myArguments.add(argument);
    return this;
  }

  public CommandArguments append(long argument) {
    myArguments.add(String.valueOf(argument));
    return this;
  }

  public static CommandArguments createOn(@NonNls P4Command argument) {
    final CommandArguments result = new CommandArguments();
    return result.append(argument.getName());
  }

  public CommandArguments createCopy() {
    return new CommandArguments(myArguments);
  }
}
