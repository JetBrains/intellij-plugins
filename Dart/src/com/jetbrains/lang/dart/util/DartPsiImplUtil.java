package com.jetbrains.lang.dart.util;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.*;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.resolve.DartResolveProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class DartPsiImplUtil {

  private static final String TRIPLE_APOS = "'''";
  private static final String TRIPLE_QUOTE = "\"\"\"";
  private static final String APOS = "'";
  private static final String QUOTE = "\"";
  private static final String R_TRIPLE_APOS = "r'''";
  private static final String R_TRIPLE_QUOTE = "r\"\"\"";
  private static final String R_APOS = "r'";
  private static final String R_QUOTE = "r\"";

  private static final Key<CachedValue<PsiElement>> DART_TYPE_CACHED_RESOLVE_RESULT_KEY = Key.create("DART_TYPE_CACHED_RESOLVE_RESULT_KEY");

  @NotNull
  public static String getUriString(@NotNull final DartUriBasedDirective uriBasedDirective) {
    return getUnquotedDartStringAndItsRange(uriBasedDirective.getUriElement().getText()).first;
  }

  @NotNull
  public static Pair<String, TextRange> getUriStringAndItsRange(@NotNull final DartUriElement uriElement) {
    return getUnquotedDartStringAndItsRange(uriElement.getText());
  }

  @NotNull
  public static Pair<String, TextRange> getUnquotedDartStringAndItsRange(@NotNull final String quotedDartString) {
    // r'''dart:core'''
    // """package:angular/angular.dart"""
    // "../foo/bar.dart"
    // also can be not closed string when completing for example import '<caret>
    final int startOffset;
    final int endOffset;
    if (quotedDartString.startsWith(TRIPLE_APOS)) {
      startOffset = TRIPLE_APOS.length();
      endOffset = quotedDartString.endsWith(TRIPLE_APOS) && quotedDartString.length() >= TRIPLE_APOS.length() * 2
                  ? quotedDartString.length() - TRIPLE_APOS.length() : quotedDartString.length();
    }
    else if (quotedDartString.startsWith(TRIPLE_QUOTE)) {
      startOffset = TRIPLE_QUOTE.length();
      endOffset = quotedDartString.endsWith(TRIPLE_QUOTE) && quotedDartString.length() >= TRIPLE_QUOTE.length() * 2
                  ? quotedDartString.length() - TRIPLE_QUOTE.length() : quotedDartString.length();
    }
    else if (quotedDartString.startsWith(APOS)) {
      startOffset = APOS.length();
      endOffset = quotedDartString.endsWith(APOS) && quotedDartString.length() >= APOS.length() * 2
                  ? quotedDartString.length() - APOS.length() : quotedDartString.length();
    }
    else if (quotedDartString.startsWith(QUOTE)) {
      startOffset = QUOTE.length();
      endOffset = quotedDartString.endsWith(QUOTE) && quotedDartString.length() >= QUOTE.length() * 2
                  ? quotedDartString.length() - QUOTE.length() : quotedDartString.length();
    }
    else if (quotedDartString.startsWith(R_TRIPLE_APOS)) {
      startOffset = R_TRIPLE_APOS.length();
      endOffset = quotedDartString.endsWith(TRIPLE_APOS) && quotedDartString.length() >= R_TRIPLE_APOS.length() + TRIPLE_APOS.length()
                  ? quotedDartString.length() - TRIPLE_APOS.length() : quotedDartString.length();
    }
    else if (quotedDartString.startsWith(R_TRIPLE_QUOTE)) {
      startOffset = R_TRIPLE_QUOTE.length();
      endOffset = quotedDartString.endsWith(TRIPLE_QUOTE) && quotedDartString.length() >= R_TRIPLE_QUOTE.length() + TRIPLE_QUOTE.length()
                  ? quotedDartString.length() - TRIPLE_QUOTE.length() : quotedDartString.length();
    }
    else if (quotedDartString.startsWith(R_APOS)) {
      startOffset = R_APOS.length();
      endOffset = quotedDartString.endsWith(APOS) && quotedDartString.length() >= R_APOS.length() + APOS.length()
                  ? quotedDartString.length() - APOS.length() : quotedDartString.length();
    }
    else if (quotedDartString.startsWith(R_QUOTE)) {
      startOffset = R_QUOTE.length();
      endOffset = quotedDartString.endsWith(QUOTE) && quotedDartString.length() >= R_QUOTE.length() + QUOTE.length()
                  ? quotedDartString.length() - QUOTE.length() : quotedDartString.length();
    }
    else {
      startOffset = 0;
      endOffset = quotedDartString.length();
    }

    return Pair.create(quotedDartString.substring(startOffset, endOffset), TextRange.create(startOffset, endOffset));
  }

  @NotNull
  public static String getLibraryName(@NotNull DartPartOfStatement partOfStatement) {
    return partOfStatement.getLibraryId().getText();
  }

  @NotNull
  public static List<DartMetadata> getMetadataList(@NotNull DartLabel element) {
    return Collections.emptyList();
  }

  @NotNull
  public static List<DartMetadata> getMetadataList(@NotNull DartEnumConstantDeclaration element) {
    return Collections.emptyList();
  }

  @NotNull
  public static List<DartMetadata> getMetadataList(@NotNull DartVarDeclarationListPart element) {
    return Collections.emptyList();
  }

  @Nullable
  public static DartComponentName getComponentName(@NotNull DartNamedConstructorDeclaration element) {
    final List<DartComponentName> list = element.getComponentNameList();
    return list.size() == 2 ? list.get(1) : null; // todo somehow remove this bogus code, it is here just to mimic old behavior
  }

  @Nullable
  public static DartComponentName getComponentName(@NotNull DartFactoryConstructorDeclaration element) {
    final List<DartComponentName> list = element.getComponentNameList();
    return list.size() == 2 ? list.get(1) : null; // todo somehow remove this bogus code, it is here just to mimic old behavior
  }

  @Nullable
  public static PsiElement resolveReference(@NotNull final DartType dartType) {
    CachedValue<PsiElement> cachedValue = dartType.getUserData(DART_TYPE_CACHED_RESOLVE_RESULT_KEY);

    if (cachedValue == null) {
      cachedValue = CachedValuesManager.getManager(dartType.getProject()).createCachedValue(
        () -> new CachedValueProvider.Result<PsiElement>(doResolveTypeReference(dartType), PsiModificationTracker.MODIFICATION_COUNT), false);

      dartType.putUserData(DART_TYPE_CACHED_RESOLVE_RESULT_KEY, cachedValue);
    }

    return cachedValue.getValue();
  }

  private static PsiElement doResolveTypeReference(final DartType dartType) {
    final DartExpression expression = dartType.getReferenceExpression();
    final String typeName = expression.getText();
    if (typeName.indexOf('.') != -1) {
      return ((DartReference)expression).resolve();
    }
    List<DartComponentName> result = new ArrayList<DartComponentName>();
    final DartResolveProcessor dartResolveProcessor = new DartResolveProcessor(result, typeName);

    final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(dartType.getContainingFile());
    if (virtualFile != null) {
      DartResolveUtil.processTopLevelDeclarations(dartType, dartResolveProcessor, virtualFile, typeName);
    }

    // find type parameter
    if (result.isEmpty()) {
      PsiTreeUtil.treeWalkUp(dartResolveProcessor, dartType, null, ResolveState.initial());
      for (Iterator<DartComponentName> iterator = result.iterator(); iterator.hasNext(); ) {
        if (!(iterator.next().getParent() instanceof DartTypeParameter)) {
          iterator.remove();
        }
      }
    }

    // global
    if (result.isEmpty()) {
      final List<VirtualFile> libraryFiles = DartResolveUtil.findLibrary(dartType.getContainingFile());
      DartResolveUtil.processTopLevelDeclarations(dartType, dartResolveProcessor, libraryFiles, typeName);
    }

    return result.isEmpty() ? null : result.iterator().next();
  }

  @Nullable
  public static DartComponentName findComponentName(final @NotNull DartNormalFormalParameter normalFormalParameter) {
    final DartFunctionSignature functionDeclaration = normalFormalParameter.getFunctionSignature();
    final DartFieldFormalParameter fieldFormalParameter = normalFormalParameter.getFieldFormalParameter();
    final DartSimpleFormalParameter simpleFormalParameter = normalFormalParameter.getSimpleFormalParameter();

    if (functionDeclaration != null) return functionDeclaration.getComponentName();
    if (fieldFormalParameter != null) return null;
    if (simpleFormalParameter != null) return simpleFormalParameter.getComponentName();

    return null;
  }

  public static DartExpression getParameterReferenceExpression(DartNamedArgument argument) {
    return PsiTreeUtil.getChildOfType(argument, DartExpression.class);
  }

  public static DartExpression getExpression(DartNamedArgument argument) {
    final DartExpression[] expressions = PsiTreeUtil.getChildrenOfType(argument, DartExpression.class);
    return expressions != null && expressions.length > 1 ? expressions[expressions.length - 1] : null;
  }

  @Nullable
  public static DartBlock getBlock(DartFunctionBody functionBody) {
    return PsiTreeUtil.getChildOfType(functionBody, DartBlock.class);
  }

  public static boolean isConstantObjectExpression(@NotNull final DartNewExpression newExpression) {
    final PsiElement child = newExpression.getFirstChild();
    return child != null && child.getNode().getElementType() == DartTokenTypes.CONST;
  }

  @Nullable
  public static DartArguments getArguments(@NotNull final DartNewExpression newExpression) {
    return PsiTreeUtil.findChildOfType(newExpression, DartArguments.class);
  }

  @Nullable
  public static DartArguments getArguments(@NotNull final DartCallExpression callExpression) {
    return PsiTreeUtil.findChildOfType(callExpression, DartArguments.class);
  }
}
