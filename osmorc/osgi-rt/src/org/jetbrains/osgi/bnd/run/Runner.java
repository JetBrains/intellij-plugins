/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package org.jetbrains.osgi.bnd.run;

import aQute.bnd.build.Workspace;

import java.io.File;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class Runner {
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("usage: " + Runner.class.getSimpleName() + " <file.bndrun>");
      System.exit(1);
    }

    File runFile = new File(args[0]);
    if (!runFile.isFile()) {
      System.err.println("invalid run file: " + runFile);
      System.exit(2);
    }

    Workspace.getRun(runFile).runLocal();
  }
}
