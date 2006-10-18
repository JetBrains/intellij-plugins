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

package jetbrains.communicator.util;

import jetbrains.communicator.ide.Change;
import jetbrains.communicator.ide.IDEFacade;

/**
 * @author Kir
 *
 * This class uses diff to correct input lines
 */
public class PositionCorrector {
  private final String mySrcContent;
  private final String myDestContent;
  private Change[] myChanges;
  private String[] myDestLines;
  private final IDEFacade myFacade;

  public PositionCorrector(IDEFacade facade, String srcContent, String destContent) {
    mySrcContent = srcContent;
    myDestContent = destContent;
    myFacade = facade;
    doDiff();
  }

  private void doDiff() {
    if (myChanges == null && mySrcContent != null && myDestContent != null) {

      myDestLines = myDestContent.split("(\r|\n)");
      myChanges = myFacade.getDiff(mySrcContent.split("(\r|\n)"), myDestLines);
    }
  }

  public int getCorrectedLine(int line) {
    doDiff();
    if (myChanges == null) {
      return line;
    }

    int addOn = 0;
    for (int i = 0; i < myChanges.length; i++) {
      Change change = myChanges[i];

      if (line < change.getSrcLine()) {
        break;
      }

      addOn += change.getInserted();
      addOn -= change.getDeleted();
    }

    int result = Math.max(line + addOn, 0);
    if (myDestLines != null) {
      return Math.min(myDestLines.length - 1, result);
    }
    return result;
  }
}
