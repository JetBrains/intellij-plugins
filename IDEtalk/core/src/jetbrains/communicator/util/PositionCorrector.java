// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

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
    for (Change change : myChanges) {
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
