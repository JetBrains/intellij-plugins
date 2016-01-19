package org.angularjs.editor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static java.lang.Math.min;

/**
 * @author Irina.Chernushina on 12/2/2015.
 */
public class AngularJSInjectorMatchingEndFinder {
  public static int findMatchingEnd(@NotNull String startSymbol, @NotNull String endSymbol, @NotNull String text, int afterStartIdx) {
    if (afterStartIdx < 0) return -1;

    final Character shortStartSymbol = defineShortSymbol(startSymbol);
    final Character shortEndSymbol = defineShortSymbol(endSymbol);

    if (shortStartSymbol != null && shortEndSymbol != null)
      return findForShortSymbols(shortStartSymbol, shortEndSymbol, text, afterStartIdx, endSymbol);
    return findForLongSymbols(text, afterStartIdx, startSymbol, endSymbol);
  }

  private static Character defineShortSymbol(final String s) {
    if (s.length() == 2 && s.charAt(0) == s.charAt(1)) return s.charAt(0);
    return null;
  }

  private static int findForShortSymbols(char shortStartSymbol, char shortEndSymbol, @NotNull final String text,
                                                          int afterStartIdx, final @NotNull String endSymbol) {
    int totalNumStarts = 1;
    int lookFrom = afterStartIdx;
    while (totalNumStarts > 0) {
      --totalNumStarts;
      int nextEndIdx = text.indexOf(endSymbol, lookFrom);
      if (nextEndIdx == -1) return -1;
      final int numStarts = getOccurrenceCount(text, lookFrom, nextEndIdx, shortStartSymbol);
      final int numEnds = getOccurrenceCount(text, lookFrom, nextEndIdx, shortEndSymbol);
      totalNumStarts += numStarts - numEnds;
      lookFrom = nextEndIdx + 1;
      if (totalNumStarts <= 0) return nextEndIdx;
    }
    return -1;
  }

  private static int findForLongSymbols(@NotNull final String text, int afterStartIdx,
                                                         final @NotNull String startSymbol, final @NotNull String endSymbol) {
    int totalNumStarts = 1;
    int lookFrom = afterStartIdx;
    while (totalNumStarts > 0) {
      --totalNumStarts;
      int nextEndIdx = text.indexOf(endSymbol, lookFrom);
      if (nextEndIdx == -1) return -1;
      final int numStarts = getOccurrenceCount(text, lookFrom, nextEndIdx, startSymbol);
      if (numStarts > 0) {
        totalNumStarts += numStarts;
      }
      lookFrom = nextEndIdx + endSymbol.length();
      if (totalNumStarts == 0) return nextEndIdx;
    }
    return -1;
  }

  @Contract(pure = true)
  private static int getOccurrenceCount(@NotNull String text, final int from, final int toExcluding, final char c) {
    int res = 0;
    int i = from;
    final int limit = min(text.length(), toExcluding);
    while (i < limit) {
      i = text.indexOf(c, i);
      if (i >= 0 && i < limit) {
        res++;
        i++;
      }
      else {
        break;
      }
    }
    return res;
  }

  @Contract(pure = true)
  private static int getOccurrenceCount(@NotNull String text, final int from, final int toExcluding, final String s) {
    int res = 0;
    int i = from;
    final int limit = min(text.length(), toExcluding);
    while (i < limit) {
      i = text.indexOf(s, i);
      if (i >= 0 && i < limit) {
        res++;
        i+=s.length();
      }
      else {
        break;
      }
    }
    return res;
  }
}
