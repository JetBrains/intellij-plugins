package org.angularjs.editor;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Irina.Chernushina on 12/2/2015.
 */
public class AngularJSInjectorMatchingEndFinder {
  private final String myStartSymbol;
  private final String myEndSymbol;
  private final Character myShortStartSymbol;
  private final Character myShortEndSymbol;
  private int myNumStarts;
  private String myText;
  private final int myAfterStartIdx;

  public AngularJSInjectorMatchingEndFinder(@NotNull String startSymbol, @NotNull String endSymbol, @NotNull String text) {
    this(startSymbol, endSymbol, text, 0);
  }

  public AngularJSInjectorMatchingEndFinder(@NotNull String startSymbol, @NotNull String endSymbol, @NotNull String text, int fromIndex) {
    myStartSymbol = startSymbol;
    myEndSymbol = endSymbol;
    myShortStartSymbol = defineShortSymbol(startSymbol);
    myShortEndSymbol = defineShortSymbol(endSymbol);
    myNumStarts = 1;
    myText = text;
    final int startIdx = text.indexOf(startSymbol, fromIndex);
    myAfterStartIdx = startIdx < 0 ? -1 : (startIdx + startSymbol.length());
  }

  private static Character defineShortSymbol(final String s) {
    if (s.length() == 2 && s.charAt(0) == s.charAt(1)) return s.charAt(0);
    return null;
  }

  public int find() {
    if (myAfterStartIdx < 0) return -1;
    if (myShortStartSymbol != null && myShortEndSymbol != null) return findForShortSymbols();
    return findForLongSymbols();
  }

  public int getAfterStartIdx() {
    return myAfterStartIdx;
  }

  private int findForShortSymbols() {
    int lookFrom = myAfterStartIdx;
    while (myNumStarts > 0) {
      --myNumStarts;
      int nextEndIdx = myText.indexOf(myEndSymbol, lookFrom);
      if (nextEndIdx == -1) return -1;
      final int numStarts = StringUtil.getOccurrenceCount(myText.substring(lookFrom, nextEndIdx), myShortStartSymbol);
      final int numEnds = StringUtil.getOccurrenceCount(myText.substring(lookFrom, nextEndIdx), myShortEndSymbol);
      myNumStarts += numStarts - numEnds;
      lookFrom = nextEndIdx + 1;
      if (myNumStarts <= 0) return nextEndIdx;
    }
    return -1;
  }

  private int findForLongSymbols() {
    int lookFrom = myAfterStartIdx;
    while (myNumStarts > 0) {
      --myNumStarts;
      int nextEndIdx = myText.indexOf(myEndSymbol, lookFrom);
      if (nextEndIdx == -1) return -1;
      final int numStarts = StringUtil.getOccurrenceCount(myText.substring(lookFrom, nextEndIdx), myStartSymbol);
      if (numStarts > 0) {
        myNumStarts += numStarts;
      }
      lookFrom = nextEndIdx + myEndSymbol.length();
      if (myNumStarts == 0) return nextEndIdx;
    }
    return -1;
  }
}
