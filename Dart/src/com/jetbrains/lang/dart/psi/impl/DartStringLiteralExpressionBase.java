// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression;
import com.jetbrains.lang.dart.psi.DartUriElement;
import com.jetbrains.lang.dart.util.DartPsiImplUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class DartStringLiteralExpressionBase extends DartClassReferenceImpl implements DartStringLiteralExpression {
  private static final Logger LOG = Logger.getInstance(DartStringLiteralExpressionBase.class.getName());

  public DartStringLiteralExpressionBase(ASTNode node) {
    super(node);
  }

  @Override
  public PsiReference @NotNull [] getReferences() {
    if (getParent() instanceof DartUriElement) {
      return super.getReferences(); // DartFileReferences are handled in DartUriElement implementation
    }
    return filterOutReferencesInTemplatesOrInjected(ReferenceProvidersRegistry.getReferencesFromProviders(this));
  }

  private PsiReference[] filterOutReferencesInTemplatesOrInjected(final PsiReference @NotNull [] references) {
    if (references.length == 0) return references;
    // String literal expression is a complex object that may contain injected HTML and regular Dart code (as a string template).
    // References in HTML and in Dart code are handled somewhere else, so if they occasionally appeared here we need to filter them out.
    final List<TextRange> forbiddenRanges = new SmartList<>();

    InjectedLanguageManager.getInstance(getProject()).enumerate(this, (injectedPsi, places) -> {
      for (PsiLanguageInjectionHost.Shred place : places) {
        if (place.getHost() == this) {
          forbiddenRanges.add(place.getRangeInsideHost());
        }
      }
    });

    PsiElement child = getFirstChild();
    while (child != null) {
      final IElementType type = child.getNode().getElementType();
      if (type != DartTokenTypes.OPEN_QUOTE &&
          type != DartTokenTypes.REGULAR_STRING_PART &&
          type != DartTokenTypes.CLOSING_QUOTE &&
          type != DartTokenTypes.RAW_SINGLE_QUOTED_STRING &&
          type != DartTokenTypes.RAW_TRIPLE_QUOTED_STRING) {
        forbiddenRanges.add(child.getTextRange().shiftRight(-getTextRange().getStartOffset()));
      }
      child = child.getNextSibling();
    }

    final List<PsiReference> result = new ArrayList<>(references.length);
    outer:
    for (PsiReference reference : references) {
      for (TextRange forbiddenRange : forbiddenRanges) {
        if (reference.getRangeInElement().intersectsStrict(forbiddenRange)) continue outer;
      }
      result.add(reference);
    }

    return result.toArray(PsiReference.EMPTY_ARRAY);
  }

  @Override
  public boolean isValidHost() {
    return true;
  }

  @Override
  public PsiLanguageInjectionHost updateText(final @NotNull String text) {
    return ElementManipulators.handleContentChange(this, text);
  }

  @Override
  public @NotNull LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
    // TODO
    return LiteralTextEscaper.createSimple(this);
  }

  public static final class DartStringManipulator extends AbstractElementManipulator<DartStringLiteralExpression> {
    @Override
    public DartStringLiteralExpression handleContentChange(final @NotNull DartStringLiteralExpression oldElement,
                                                           final @NotNull TextRange range,
                                                           final @NotNull String newContent) {
      // this check helps to avoid loosing text in case of concatenated strings + typing escape sequences; need proper fix
      final int expectedNewLength = oldElement.getTextLength() - range.getLength() + newContent.length();

      final String newText = StringUtil.replaceSubstring(oldElement.getText(), range, newContent);
      final PsiFile fileFromText = PsiFileFactory.getInstance(oldElement.getProject())
        .createFileFromText(DartLanguage.INSTANCE, "var a = " + newText + ";");
      final PsiElement elementAt = fileFromText.findElementAt("var a = ".length());
      if (elementAt != null &&
          elementAt.getParent() instanceof DartStringLiteralExpression &&
          expectedNewLength == elementAt.getParent().getTextLength()) {
        return (DartStringLiteralExpression)oldElement.replace(elementAt.getParent());
      }

      return oldElement;
    }

    @Override
    public @NotNull TextRange getRangeInElement(final @NotNull DartStringLiteralExpression element) {
      // StringLiteralExpression may consist of several strings (that become concatenated). We want to return range of the first one. If none (e.g. "$a") - return zero-length range after quote
      PsiElement child = element.getFirstChild();
      while (child != null) {
        final IElementType type = child.getNode().getElementType();
        if (type == DartTokenTypes.OPEN_QUOTE) {
          final PsiElement next = child.getNextSibling();
          if (next == null || next.getNode().getElementType() != DartTokenTypes.REGULAR_STRING_PART) {
            return TextRange.from(child.getStartOffsetInParent() + child.getTextLength(), 0);
          }
        }

        if (type == DartTokenTypes.REGULAR_STRING_PART) {
          return child.getTextRange().shiftRight(-element.getTextRange().getStartOffset());
        }

        if (type == DartTokenTypes.RAW_SINGLE_QUOTED_STRING || type == DartTokenTypes.RAW_TRIPLE_QUOTED_STRING) {
          final TextRange textRange = DartPsiImplUtil.getUnquotedDartStringAndItsRange(child.getText()).second;
          return textRange.shiftRight(child.getStartOffsetInParent());
        }

        child = child.getNextSibling();
      }

      LOG.error(element.getText());
      return element.getTextRange();
    }
  }
}
