package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class GherkinParser implements PsiParser {

  @NotNull
  public ASTNode parse(IElementType root, PsiBuilder builder) {
    final PsiBuilder.Marker marker = builder.mark();
    parseFileTopLevel(builder);
    marker.done(GherkinElementTypes.GHERKIN_FILE);
    return builder.getTreeBuilt();
  }

  private static void parseFileTopLevel(PsiBuilder builder) {
    while(!builder.eof()) {
      final IElementType tokenType = builder.getTokenType();
      if (tokenType == GherkinTokenTypes.FEATURE_KEYWORD) {
        parseFeature(builder);
      } else if (tokenType == GherkinTokenTypes.TAG) {
        parseTags(builder);
      } else {
        builder.advanceLexer();
      }
    }
  }

  private static void parseFeature(PsiBuilder builder) {
    final PsiBuilder.Marker marker = builder.mark();

    assert builder.getTokenType() == GherkinTokenTypes.FEATURE_KEYWORD;
    final int featureEnd = builder.getCurrentOffset() + getTokenLength(builder.getTokenText());

    PsiBuilder.Marker descMarker = null;
    while(true) {
      final IElementType tokenType = builder.getTokenType();
      if (tokenType == GherkinTokenTypes.TEXT && descMarker == null) {
        if (hadLineBreakBefore(builder, featureEnd)) {
          descMarker = builder.mark();
        }
      }

      if (tokenType == GherkinTokenTypes.SCENARIO_KEYWORD ||
          tokenType == GherkinTokenTypes.BACKGROUND_KEYWORD ||
          tokenType == GherkinTokenTypes.SCENARIO_OUTLINE_KEYWORD ||
          tokenType == GherkinTokenTypes.TAG) {
        if (descMarker != null) {
          descMarker.done(GherkinElementTypes.FEATURE_HEADER);
          descMarker = null;
        }
        parseFeatureElements(builder);

        if (builder.getTokenType() == GherkinTokenTypes.FEATURE_KEYWORD) {
          break;
        }
      }
      builder.advanceLexer();
      if (builder.eof()) break;
    }
    if (descMarker != null) {
      descMarker.done(GherkinElementTypes.FEATURE_HEADER);
    }
    marker.done(GherkinElementTypes.FEATURE);
  }

  private static boolean hadLineBreakBefore(PsiBuilder builder, int prevTokenEnd) {
    if (prevTokenEnd < 0) return false;
    final String precedingText = builder.getOriginalText().subSequence(prevTokenEnd, builder.getCurrentOffset()).toString();
    return precedingText.contains("\n");
  }

  private static void parseTags(PsiBuilder builder) {
    while (builder.getTokenType() == GherkinTokenTypes.TAG) {
      final PsiBuilder.Marker tagMarker = builder.mark();
      builder.advanceLexer();
      tagMarker.done(GherkinElementTypes.TAG);
    }
  }

  private static void parseFeatureElements(PsiBuilder builder) {
    while (builder.getTokenType() != GherkinTokenTypes.FEATURE_KEYWORD && !builder.eof()) {
      final PsiBuilder.Marker marker = builder.mark();
      // tags
      parseTags(builder);

      // scenarios
      IElementType startTokenType = builder.getTokenType();
      final boolean outline = startTokenType == GherkinTokenTypes.SCENARIO_OUTLINE_KEYWORD;
      builder.advanceLexer();
      parseScenario(builder, outline);
      marker.done(outline ? GherkinElementTypes.SCENARIO_OUTLINE : GherkinElementTypes.SCENARIO);
    }
  }

  private static void parseScenario(PsiBuilder builder, boolean outline) {
    while (!atScenarioEnd(builder)) {
      if (builder.getTokenType() == GherkinTokenTypes.TAG) {
        final PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        if (atScenarioEnd(builder)) {
          marker.rollbackTo();
          break;
        } else {
          marker.drop();
        }
      }

      if (parseStepParameter(builder)) {
        continue;
      }

      if (builder.getTokenType() == GherkinTokenTypes.STEP_KEYWORD) {
        parseStep(builder);
      }
      else if (builder.getTokenType() == GherkinTokenTypes.EXAMPLES_KEYWORD && outline) {
        parseExamplesBlock(builder);
      }
      else {
        builder.advanceLexer();
      }
    }
  }

  private static boolean atScenarioEnd(PsiBuilder builder) {
    int i = 0;
    while (builder.lookAhead(i) == GherkinTokenTypes.TAG) {
      i++;
    }
    final IElementType tokenType = builder.lookAhead(i);
    return tokenType == null ||
           tokenType == GherkinTokenTypes.BACKGROUND_KEYWORD ||
           tokenType == GherkinTokenTypes.SCENARIO_KEYWORD ||
           tokenType == GherkinTokenTypes.SCENARIO_OUTLINE_KEYWORD ||
           tokenType == GherkinTokenTypes.FEATURE_KEYWORD;
  }

  private static boolean parseStepParameter(PsiBuilder builder) {
    if (builder.getTokenType() == GherkinTokenTypes.STEP_PARAMETER_TEXT) {
      final PsiBuilder.Marker stepParameterMarker = builder.mark();
      builder.advanceLexer();
      stepParameterMarker.done(GherkinElementTypes.STEP_PARAMETER);
      return true;
    }
    return false;
  }

  private static void parseStep(PsiBuilder builder) {
    final PsiBuilder.Marker marker = builder.mark();
    builder.advanceLexer();
    int prevTokenEnd = -1;
    while (builder.getTokenType() == GherkinTokenTypes.TEXT || builder.getTokenType() == GherkinTokenTypes.STEP_PARAMETER_BRACE
           || builder.getTokenType() == GherkinTokenTypes.STEP_PARAMETER_TEXT) {
      String tokenText = builder.getTokenText();
      if (hadLineBreakBefore(builder, prevTokenEnd)) {
        break;
      }
      prevTokenEnd = builder.getCurrentOffset() + getTokenLength(tokenText);
      if (!parseStepParameter(builder)) {
        builder.advanceLexer();
      }
    }
    final IElementType tokenTypeAfterName = builder.getTokenType();
    if (tokenTypeAfterName == GherkinTokenTypes.PIPE) {
      parseTable(builder);
    } else if (tokenTypeAfterName == GherkinTokenTypes.PYSTRING) {
      parsePystring(builder);
    }
    marker.done(GherkinElementTypes.STEP);
  }

  private static void parsePystring(PsiBuilder builder) {
    if (!builder.eof()) {
      final PsiBuilder.Marker marker = builder.mark();
      builder.advanceLexer();
      while (!builder.eof() && builder.getTokenType() != GherkinTokenTypes.PYSTRING) {
        if (!parseStepParameter(builder)) {
          builder.advanceLexer();
        }
      }
      if (!builder.eof()) {
        builder.advanceLexer();
      }
      marker.done(GherkinElementTypes.PYSTRING);
    }
  }

  private static void parseExamplesBlock(PsiBuilder builder) {
    final PsiBuilder.Marker marker = builder.mark();
    builder.advanceLexer();
    if (builder.getTokenType() == GherkinTokenTypes.COLON) builder.advanceLexer();
    while(builder.getTokenType() == GherkinTokenTypes.TEXT) {
      builder.advanceLexer();
    }
    if (builder.getTokenType() == GherkinTokenTypes.PIPE) {
      parseTable(builder);
    }
    marker.done(GherkinElementTypes.EXAMPLES_BLOCK);
  }

  private static void parseTable(PsiBuilder builder) {
    final PsiBuilder.Marker marker = builder.mark();
    PsiBuilder.Marker rowMarker = builder.mark();
    int prevCellEnd = -1;
    boolean isHeaderRow = true;
    PsiBuilder.Marker cellMarker = null;

    IElementType prevToken = null;
    while (builder.getTokenType() == GherkinTokenTypes.PIPE || builder.getTokenType() == GherkinTokenTypes.TABLE_CELL) {
      final IElementType tokenType = builder.getTokenType();

      final boolean hasLineBreakBefore = hadLineBreakBefore(builder, prevCellEnd);

      // cell - is all between pipes
      if (prevToken == GherkinTokenTypes.PIPE) {
        // Don't start new cell if prev was last in the row
        // it's not a cell, we just need to close a row
        if (!hasLineBreakBefore) {
          cellMarker = builder.mark();
        }
      }
      if (tokenType == GherkinTokenTypes.PIPE) {
        if (cellMarker != null) {
          closeCell(cellMarker);
          cellMarker = null;
        }
      }

      if (hasLineBreakBefore) {
        closeRowMarker(rowMarker, isHeaderRow);
        isHeaderRow = false;
        rowMarker = builder.mark();
      }
      prevCellEnd = builder.getCurrentOffset() + getTokenLength(builder.getTokenText());
      prevToken = tokenType;
      builder.advanceLexer();
    }

    if (cellMarker != null) {
      closeCell(cellMarker);
    }
    closeRowMarker(rowMarker, isHeaderRow);
    marker.done(GherkinElementTypes.TABLE);
  }

  private static void closeCell(PsiBuilder.Marker cellMarker) {
    cellMarker.done(GherkinElementTypes.TABLE_CELL);
  }

  private static void closeRowMarker(PsiBuilder.Marker rowMarker, boolean headerRow) {
    rowMarker.done(headerRow ? GherkinElementTypes.TABLE_HEADER_ROW : GherkinElementTypes.TABLE_ROW);
  }

  private static int getTokenLength(@Nullable final String tokenText) {
    return tokenText != null ? tokenText.length() : 0;
  }
}
