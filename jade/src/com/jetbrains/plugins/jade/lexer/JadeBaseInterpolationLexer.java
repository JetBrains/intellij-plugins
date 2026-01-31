// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.lexer;

import com.intellij.embedding.EmbeddedLazyParseableElementType;
import com.intellij.embedding.MasqueradingLexer;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.plugins.jade.JadeLanguage;
import com.jetbrains.plugins.jade.parser.JadeParser;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class JadeBaseInterpolationLexer extends MasqueradingLexer.SmartDelegate {
  private CharSequence myBuffer;
  private int myStartOffset;
  private Set<TextRange> myInterpolations;

  public JadeBaseInterpolationLexer(@NotNull Lexer delegate) {
    super(delegate);
  }

  protected abstract CharSequence getSubstitutionForInterpolation(CharSequence buffer, int start, int end, TextRange interpolationRange);

  @Override
  public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
    myBuffer = buffer;
    myStartOffset = startOffset;
    List<TextRange> interpolations = findAllInterpolations(buffer, startOffset, endOffset);
    if (interpolations.isEmpty()) {
      myInterpolations = Collections.emptySet();
      super.start(buffer.subSequence(startOffset, endOffset), 0, endOffset - startOffset, initialState);
      return;
    }

    myInterpolations = new HashSet<>(interpolations);
    CharSequence newBuffer = substituteInterpolations(buffer, startOffset, endOffset, interpolations);
    super.start(newBuffer, 0, newBuffer.length(), initialState);
  }

  @Override
  public @Nullable IElementType getTokenType() {
    TextRange tokenRange = getTokenRange();
    for (TextRange interpolation : myInterpolations) {
      if (tokenRange.contains(interpolation)) {
        return InterpolationType.getInstance(super.getTokenType());
      }
    }

    return super.getTokenType();
  }

  @Override
  public @NotNull String getTokenText() {
    TextRange tokenRange = getTokenRange();
    return myBuffer.subSequence(tokenRange.getStartOffset(), tokenRange.getEndOffset()).toString();
  }

  private TextRange getTokenRange() {
    return TextRange.create(getTokenStart(), getTokenEnd()).shiftRight(myStartOffset);
  }

  private CharSequence substituteInterpolations(@NotNull CharSequence buffer, int start, int end, @NotNull List<TextRange> interpolations) {
    assert(!interpolations.isEmpty());

    int startPos = start;
    StringBuilder sb = new StringBuilder();

    for (TextRange interpolation : interpolations) {
      sb.append(buffer.subSequence(startPos, interpolation.getStartOffset()));
      sb.append(getSubstitutionForInterpolation(buffer, start, end, interpolation));
      startPos = interpolation.getEndOffset();
    }
    sb.append(buffer.subSequence(startPos, end));

    return sb.toString();
  }

  private static List<TextRange> findAllInterpolations(@NotNull CharSequence buffer, int start, int end) {
    List<TextRange> result = new ArrayList<>();
    while (start < end) {
      start = findInterpolationStartPos(buffer, start, end);
      if (start >= end) {
        break;
      }

      int endOfInterpolation = findClosingBraceWithRespectToOpeningOnes(buffer, start + 2, end);
      if (endOfInterpolation < end) {
        result.add(new TextRange(start, endOfInterpolation + 1));
      }
      start = endOfInterpolation + 1;
    }

    return result;
  }

  private static int findInterpolationStartPos(@NotNull CharSequence buffer, int start, int end) {
    for (int i = start; i + 2 < end; ++i) {
      if ((buffer.charAt(i) == '#' || buffer.charAt(i) == '!')
          && buffer.charAt(i + 1) == '{'
          && (i == start || buffer.charAt(i - 1) != '\\')) {
        return i;
      }
    }
    return end;
  }

  private static int findClosingBraceWithRespectToOpeningOnes(@NotNull CharSequence buffer, int startPos, int end) {
    int balance = 1;
    int pos = startPos;
    while (balance > 0 && pos < end) {
      char c = buffer.charAt(pos++);

      if (c == '{') {
        balance++;
      }
      else if (c == '}') {
        balance--;
      }
    }

    if (balance == 0) {
      return pos - 1;
    }
    else {
      return end;
    }
  }

  private static final class InterpolationType extends EmbeddedLazyParseableElementType {

    private static final Map<IElementType, InterpolationType> INSTANCES = new HashMap<>();

    private final @Nullable IElementType myType;

    public static InterpolationType getInstance(@Nullable IElementType flankingType) {
      if (INSTANCES.containsKey(flankingType)) {
        return INSTANCES.get(flankingType);
      }

      synchronized (INSTANCES) {
        if (!INSTANCES.containsKey(flankingType)) {
          INSTANCES.put(flankingType, new InterpolationType(flankingType));
        }
      }

      return INSTANCES.get(flankingType);
    }

    private InterpolationType(@Nullable IElementType flankingType) {
      super("INTERPOLATION", JadeLanguage.INSTANCE);
      myType = flankingType;
    }

    @Override
    public Lexer createLexer(@NotNull ASTNode chameleon, @NotNull Project project) {
      return new MyLexer(myType);
    }

    @Override
    public ASTNode parseAndGetTree(@NotNull PsiBuilder builder) {
      final PsiBuilder.Marker marker = builder.mark();
      while (!builder.eof()) {
        final IElementType type = builder.getTokenType();
        if (type == JadeTokenTypes.TEXT) {
          JadeParser.parsePlainTextLine(builder);
        } else {
          builder.advanceLexer();
        }
      }
      marker.done(this);
      return builder.getTreeBuilt();
    }

    private static class MyLexer extends LexerBase {

      private final IElementType myType;
      private int myLeft;
      private int myRight;

      private CharSequence myBuffer;
      private int myStartOffset;
      private int myEndOffset;
      private int myState;

      MyLexer(IElementType type) {
        myType = type;
      }

      @Override
      public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {

        myBuffer = buffer;
        myStartOffset = startOffset;
        myEndOffset = endOffset;
        myState = initialState;

        final boolean foundFirstInterpolation = findInterpolationAndInitLeftAndRight(myStartOffset);
        assert foundFirstInterpolation : "could not be invoked w/o interpolation";

        adjustState();
      }

      private boolean findInterpolationAndInitLeftAndRight(int from) {
        final int start = findInterpolationStartPos(myBuffer, from, myEndOffset);
        if (start >= myEndOffset) {
          return false;
        }
        final int end = findClosingBraceWithRespectToOpeningOnes(myBuffer, start + 2, myEndOffset);
        if (end >= myEndOffset) {
          return false;
        }

        myLeft = start - myStartOffset;
        myRight = myEndOffset - end - 1;
        return true;
      }

      @Override
      public int getState() {
        return myState;
      }

      @Override
      public @Nullable IElementType getTokenType() {
        return switch (myState) {
          case 0 -> myType;
          case 1, 3 -> JadeTokenTypes.TEXT;
          case 2 -> JadeTokenTypes.JS_EXPR;
          case 4 -> myType;
          default -> null;
        };
      }

      @Override
      public int getTokenStart() {
        return switch (myState) {
          case 0 -> myStartOffset;
          case 1 -> myStartOffset + myLeft;
          case 2 -> myStartOffset + myLeft + 2;
          case 3 -> myEndOffset - myRight - 1;
          case 4 -> myEndOffset - myRight;
          default -> myEndOffset;
        };
      }

      @Override
      public int getTokenEnd() {
        return switch (myState) {
          case 0 -> myStartOffset + myLeft;
          case 1 -> myStartOffset + myLeft + 2;
          case 2 -> myEndOffset - myRight - 1;
          case 3 -> myEndOffset - myRight;
          case 4 -> myEndOffset;
          default -> myEndOffset;
        };
      }

      @Override
      public void advance() {
        myState++;

        if (myState == 4) {
          myStartOffset = myEndOffset - myRight;
          final boolean foundNextInterpolation = findInterpolationAndInitLeftAndRight(myStartOffset);
          if (foundNextInterpolation) {
            myState = 0;
          }
        }
        adjustState();
      }

      private void adjustState() {
        if ((myState == 0 && myLeft == 0)
          || (myState == 4 && myRight == 0)) {
          myState++;
        }
      }

      @Override
      public @NotNull CharSequence getBufferSequence() {
        return myBuffer;
      }

      @Override
      public int getBufferEnd() {
        return myEndOffset;
      }
    }
  }

}
