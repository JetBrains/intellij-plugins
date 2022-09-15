// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.structure;

import com.intellij.icons.AllIcons;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.IconManager;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.PlatformIcons;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import org.dartlang.analysis.server.protocol.Element;
import org.dartlang.analysis.server.protocol.ElementKind;
import org.dartlang.analysis.server.protocol.Outline;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;

import static com.intellij.icons.AllIcons.Nodes.*;

final class DartStructureViewElement extends PsiTreeElementBase<PsiElement> {
  private static final LayeredIcon STATIC_FINAL_FIELD_ICON = new LayeredIcon(IconManager.getInstance().getPlatformIcon(PlatformIcons.Field), IconManager.getInstance().getPlatformIcon(PlatformIcons.StaticMark), IconManager.getInstance().getPlatformIcon(
    PlatformIcons.FinalMark));
  private static final LayeredIcon FINAL_FIELD_ICON = new LayeredIcon(Field, IconManager.getInstance().getPlatformIcon(PlatformIcons.FinalMark));
  private static final LayeredIcon STATIC_FIELD_ICON = new LayeredIcon(Field, IconManager.getInstance().getPlatformIcon(PlatformIcons.StaticMark));
  private static final LayeredIcon STATIC_METHOD_ICON = new LayeredIcon(Method, IconManager.getInstance().getPlatformIcon(PlatformIcons.StaticMark));
  private static final LayeredIcon TOP_LEVEL_FUNCTION_ICON = new LayeredIcon(Lambda, IconManager.getInstance().getPlatformIcon(PlatformIcons.StaticMark));
  private static final LayeredIcon TOP_LEVEL_VAR_ICON = new LayeredIcon(Variable, IconManager.getInstance().getPlatformIcon(PlatformIcons.StaticMark));
  private static final LayeredIcon CONSTRUCTOR_INVOCATION_ICON = new LayeredIcon(IconManager.getInstance().getPlatformIcon(PlatformIcons.Class), TabPin);
  private static final LayeredIcon FUNCTION_INVOCATION_ICON = new LayeredIcon(Method, TabPin);
  private static final LayeredIcon TOP_LEVEL_CONST_ICON = new LayeredIcon(Variable, IconManager.getInstance().getPlatformIcon(PlatformIcons.StaticMark), IconManager.getInstance().getPlatformIcon(
    PlatformIcons.FinalMark));

  @NotNull private final PsiFile myPsiFile;
  @NotNull private final Outline myOutline;
  @NotNull private final String myPresentableText;

  DartStructureViewElement(@NotNull final PsiFile psiFile, @NotNull final Outline outline) {
    super(findBestPsiElementForOutline(psiFile, outline));
    myPsiFile = psiFile;
    myOutline = outline;
    myPresentableText = getPresentableText(outline);
  }

  @NotNull
  @Override
  public Collection<StructureViewTreeElement> getChildrenBase() {
    if (myOutline.getChildren().isEmpty()) return Collections.emptyList();
    return ContainerUtil.map2List(myOutline.getChildren(), outline -> new DartStructureViewElement(myPsiFile, outline));
  }

  @Override
  public void navigate(boolean requestFocus) {
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(myPsiFile.getProject());
    final int offset = service.getConvertedOffset(myPsiFile.getVirtualFile(), myOutline.getElement().getLocation().getOffset());
    PsiNavigationSupport.getInstance().createNavigatable(myPsiFile.getProject(), myPsiFile.getVirtualFile(), offset).navigate(requestFocus);
  }

  @NotNull
  @Override
  public String getPresentableText() {
    return myPresentableText;
  }

  @NotNull
  private static String getPresentableText(@NotNull final Outline outline) {
    final Element element = outline.getElement();
    final StringBuilder b = new StringBuilder();
    if (ElementKind.EXTENSION.equals(element.getKind())) {
      b.append("extension ");
    }
    b.append(element.getName());
    if (StringUtil.isNotEmpty(element.getTypeParameters())) {
      b.append(element.getTypeParameters());
    }
    if (StringUtil.isNotEmpty(element.getParameters())) {
      b.append(element.getParameters());
    }
    if (StringUtil.isNotEmpty(element.getReturnType())) {
      b.append(" ").append(DartPresentableUtil.RIGHT_ARROW).append(" ").append(element.getReturnType());
    }
    return b.toString();
  }

  @Nullable
  @Override
  public Icon getIcon(boolean unused) {
    final Element element = myOutline.getElement();
    final boolean finalOrConst = element.isConst() || element.isFinal();

    return switch (element.getKind()) {
      case ElementKind.CLASS -> element.isAbstract() ? AbstractClass : AllIcons.Nodes.Class;
      case ElementKind.EXTENSION -> Include;
      case ElementKind.MIXIN -> AbstractClass;
      case ElementKind.CONSTRUCTOR -> Method;
      case ElementKind.CONSTRUCTOR_INVOCATION -> CONSTRUCTOR_INVOCATION_ICON;
      case ElementKind.ENUM -> AllIcons.Nodes.Enum;
      case ElementKind.ENUM_CONSTANT -> STATIC_FINAL_FIELD_ICON;
      case ElementKind.FIELD -> {
        if (finalOrConst && element.isTopLevelOrStatic()) yield STATIC_FINAL_FIELD_ICON;
        if (finalOrConst) yield FINAL_FIELD_ICON;
        if (element.isTopLevelOrStatic()) yield STATIC_FIELD_ICON;
        yield Field;
      }
      case ElementKind.FUNCTION -> element.isTopLevelOrStatic() ? TOP_LEVEL_FUNCTION_ICON : Lambda;
      case ElementKind.FUNCTION_INVOCATION -> FUNCTION_INVOCATION_ICON;
      case ElementKind.FUNCTION_TYPE_ALIAS -> DartComponentType.TYPEDEF.getIcon();
      case ElementKind.GETTER -> element.isTopLevelOrStatic() ? PropertyReadStatic : PropertyRead;
      case ElementKind.METHOD -> {
        if (element.isAbstract()) yield AbstractMethod;
        yield element.isTopLevelOrStatic() ? STATIC_METHOD_ICON : Method;
      }
      case ElementKind.SETTER -> element.isTopLevelOrStatic() ? PropertyWriteStatic : PropertyWrite;
      case ElementKind.TOP_LEVEL_VARIABLE -> finalOrConst ? TOP_LEVEL_CONST_ICON : TOP_LEVEL_VAR_ICON;
      case ElementKind.UNIT_TEST_GROUP -> TestSourceFolder;
      case ElementKind.UNIT_TEST_TEST -> AllIcons.RunConfigurations.Junit;
      case ElementKind.CLASS_TYPE_ALIAS, ElementKind.COMPILATION_UNIT, ElementKind.FILE, ElementKind.LABEL, ElementKind.LIBRARY,
        ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER, ElementKind.PREFIX, ElementKind.TYPE_PARAMETER, ElementKind.UNKNOWN ->
        // unexpected
        null;
      default -> null;
    };
  }

  @Nullable
  static PsiElement findBestPsiElementForOutline(@NotNull PsiFile psiFile, @NotNull Outline outline) {
    DartAnalysisServerService das = DartAnalysisServerService.getInstance(psiFile.getProject());
    int startOffset = das.getConvertedOffset(psiFile.getVirtualFile(), outline.getCodeOffset());
    int endOffset = das.getConvertedOffset(psiFile.getVirtualFile(), outline.getCodeOffset() + outline.getCodeLength());
    TextRange outlineRange = TextRange.create(startOffset, endOffset);

    PsiElement element = psiFile.findElementAt(startOffset);
    while ((element instanceof PsiComment || element instanceof PsiWhiteSpace) && element.getTextRange().getEndOffset() < endOffset) {
      PsiElement next = element.getNextSibling();
      if (next != null) {
        element = PsiTreeUtil.getDeepestFirst(next);
      }
      else {
        break;
      }
    }

    if (element != null) {
      while (!(element instanceof PsiFile) && element.getParent() != null && outlineRange.contains(element.getParent().getTextRange())) {
        element = element.getParent();
      }
    }

    return element;
  }
}
