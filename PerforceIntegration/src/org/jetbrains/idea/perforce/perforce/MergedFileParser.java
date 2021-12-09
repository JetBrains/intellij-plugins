/*
 * Copyright 2000-2005 JetBrains s.r.o.
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

import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

public class MergedFileParser {
  private final StringBuffer myOriginal = new StringBuffer();
  private final StringBuffer myLocal = new StringBuffer();
  private final StringBuffer myLast = new StringBuffer();
  private final String mySource;
  @NonNls private static final String ORIGINAL_PREFIX = ">>>> ORIGINAL";
  @NonNls private static final String YOURS_PREFIX = "==== YOURS";
  @NonNls private static final String THEIRS_PREFIX = "==== THEIRS";

  public MergedFileParser(String dataWithMarkers) throws IOException {
    mySource = dataWithMarkers;
    process();
  }

  public String getOriginal() {
    return myOriginal.toString();
  }

  public String getLocal() {
    return myLocal.toString();
  }

  public String getLast() {
    return myLast.toString();
  }

  private void process() throws IOException {

    final StringReader stringReader = new StringReader(mySource);
    final LineNumberReader lineNumberReader = new LineNumberReader(stringReader);

    try {
      String line;

      StringBuffer current = null;

      while ((line = lineNumberReader.readLine()) != null) {
        if (line.startsWith(ORIGINAL_PREFIX)) {
          current = myOriginal;
        } else if (line.startsWith(YOURS_PREFIX)){
          current = myLocal;
        } else if (line.startsWith(THEIRS_PREFIX)) {
          current = myLast;
        } else if (line.startsWith("<<<<") && current != null) {
          current = null;
        } else {
          if (current != null) {
            current.append(line + "\n");
          } else {
            myOriginal.append(line + "\n");
            myLocal.append(line + "\n");
            myLast.append(line + "\n");
          }
        }
      }
    }
    finally {
      lineNumberReader.close();
    }
  }
}

