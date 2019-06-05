// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.structure;

import com.intellij.icons.AllIcons;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.ide.util.treeView.NodeDescriptorProvidingKey;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import org.dartlang.analysis.server.protocol.Element;
import org.dartlang.analysis.server.protocol.ElementKind;
import org.dartlang.analysis.server.protocol.Outline;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import java.util.Objects;

import static com.intellij.icons.AllIcons.Nodes.*;
import static com.intellij.icons.AllIcons.Nodes.Class;
import static com.intellij.icons.AllIcons.Nodes.Enum;

public class DartStructureViewElement implements StructureViewTreeElement, ItemPresentation, NodeDescriptorProvidingKey {

  private static final LayeredIcon STATIC_FINAL_FIELD_ICON = new LayeredIcon(Field, StaticMark, FinalMark);
  private static final LayeredIcon FINAL_FIELD_ICON = new LayeredIcon(Field, FinalMark);
  private static final LayeredIcon STATIC_FIELD_ICON = new LayeredIcon(Field, StaticMark);
  private static final LayeredIcon STATIC_METHOD_ICON = new LayeredIcon(Method, StaticMark);
  private static final LayeredIcon TOP_LEVEL_FUNCTION_ICON = new LayeredIcon(Function, StaticMark);
  private static final LayeredIcon TOP_LEVEL_VAR_ICON = new LayeredIcon(Variable, StaticMark);
  private static final LayeredIcon CONSTRUCTOR_INVOCATION_ICON = new LayeredIcon(Class, TabPin);
  private static final LayeredIcon FUNCTION_INVOCATION_ICON = new LayeredIcon(Method, TabPin);

  private static final LayeredIcon TOP_LEVEL_CONST_ICON = new LayeredIcon(Variable, StaticMark, FinalMark);

  @NotNull private final PsiFile myPsiFile;
  @NotNull private final Outline myOutline;

  /**
   * It is possible for multiple {@link PsiElement}s to be present at {@link myOutline}'s offset.
   * To make sure that we pick the most valuable one for {@link myValue}, we want to find the most local parent of the element.
   *
   * For example, we may have a field like this:
   *
   * void main(List<String> args) {
   *
   * }
   *
   * At the offset for the beginning of 'void', we could have a {@link com.jetbrains.lang.dart.psi.DartReturnType},
   * or we could have a {@link com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBody}, which corresponds to the function
   * and all its children. This is a parent of the ReturnType, and it has the same starting offset.
   *
   * To have a useful PsiElement value for the structure view, we want to pick the
   * {@link com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBody} because it has children. This allows us to refer up to this parent
   * from all of the leaves of the element.
   */
  @NotNull private final PsiElement myValue;
  @NotNull private final String myPresentableText;

  public DartStructureViewElement(@NotNull final PsiFile psiFile, @NotNull final Outline outline) {
    myPsiFile = psiFile;
    myOutline = outline;
    myPresentableText = getPresentableText(outline);
    // Determine the most appropriate PsiElement that shares the same offset in myOutline.
    // As the documentation of myValue explains, we want an element that has children and is at the same offset.
    PsiElement value = Objects.requireNonNull(psiFile.getViewProvider().findElementAt(outline.getOffset())).getNavigationElement();
    while (value.getChildren().length == 0
           && value.getParent().getTextRange().getStartOffset() == value.getTextRange().getStartOffset()
           // Terminate the loop at the file level.
           && !(value.getParent() instanceof DartFile)) {
      value = value.getParent();
    }
    myValue = value;
  }

  @NotNull
  @Override
  public ItemPresentation getPresentation() {
    return this;
  }

  @NotNull
  @Override
  public StructureViewTreeElement[] getChildren() {
    if (myOutline.getChildren().isEmpty()) return EMPTY_ARRAY;
    return ContainerUtil.map2Array(myOutline.getChildren(), DartStructureViewElement.class,
                                   outline -> new DartStructureViewElement(myPsiFile, outline));
  }

  @Override
  public void navigate(boolean requestFocus) {
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(myPsiFile.getProject());
    final int offset = service.getConvertedOffset(myPsiFile.getVirtualFile(), myOutline.getElement().getLocation().getOffset());
    PsiNavigationSupport.getInstance().createNavigatable(myPsiFile.getProject(), myPsiFile.getVirtualFile(), offset)
      .navigate(requestFocus);
  }

  @Override
  public boolean canNavigate() {
    return true;
  }

  @Override
  public boolean canNavigateToSource() {
    return true;
  }

  @NotNull
  @Override
  public String getPresentableText() {
    return myPresentableText;
  }

  @NotNull
  public static String getPresentableText(@NotNull final Outline outline) {
    final Element element = outline.getElement();
    final StringBuilder b = new StringBuilder(element.getName());
    if (!StringUtil.isEmpty(element.getTypeParameters())) {
      b.append(element.getTypeParameters());
    }
    if (!StringUtil.isEmpty(element.getParameters())) {
      b.append(element.getParameters());
    }
    if (!StringUtil.isEmpty(element.getReturnType())) {
      b.append(" ").append(DartPresentableUtil.RIGHT_ARROW).append(" ").append(element.getReturnType());
    }
    return b.toString();
  }

  @Nullable
  @Override
  public String getLocationString() {
    return null;
  }

  @Nullable
  @Override
  public Icon getIcon(boolean unused) {
    final Element element = myOutline.getElement();
    final boolean finalOrConst = element.isConst() || element.isFinal();

    switch (element.getKind()) {
      case ElementKind.CLASS:
        return element.isAbstract() ? AbstractClass : Class;
      case ElementKind.MIXIN:
        return AbstractClass;
      case ElementKind.CONSTRUCTOR:
        return Method;
      case ElementKind.CONSTRUCTOR_INVOCATION:
        return CONSTRUCTOR_INVOCATION_ICON;
      case ElementKind.ENUM:
        return Enum;
      case ElementKind.ENUM_CONSTANT:
        return STATIC_FINAL_FIELD_ICON;
      case ElementKind.FIELD:
        if (finalOrConst && element.isTopLevelOrStatic()) return STATIC_FINAL_FIELD_ICON;
        if (finalOrConst) return FINAL_FIELD_ICON;
        if (element.isTopLevelOrStatic()) return STATIC_FIELD_ICON;
        return Field;
      case ElementKind.FUNCTION:
        return element.isTopLevelOrStatic() ? TOP_LEVEL_FUNCTION_ICON : Function;
      case ElementKind.FUNCTION_INVOCATION:
        return FUNCTION_INVOCATION_ICON;
      case ElementKind.FUNCTION_TYPE_ALIAS:
        return DartComponentType.TYPEDEF.getIcon();
      case ElementKind.GETTER:
        return element.isTopLevelOrStatic() ? PropertyReadStatic : PropertyRead;
      case ElementKind.METHOD:
        if (element.isAbstract()) return AbstractMethod;
        return element.isTopLevelOrStatic() ? STATIC_METHOD_ICON : Method;
      case ElementKind.SETTER:
        return element.isTopLevelOrStatic() ? PropertyWriteStatic : PropertyWrite;
      case ElementKind.TOP_LEVEL_VARIABLE:
        return finalOrConst ? TOP_LEVEL_CONST_ICON : TOP_LEVEL_VAR_ICON;
      case ElementKind.UNIT_TEST_GROUP:
        return TestSourceFolder;
      case ElementKind.UNIT_TEST_TEST:
        return AllIcons.RunConfigurations.Junit;

      case ElementKind.CLASS_TYPE_ALIAS:
      case ElementKind.COMPILATION_UNIT:
      case ElementKind.FILE:
      case ElementKind.LABEL:
      case ElementKind.LIBRARY:
      case ElementKind.LOCAL_VARIABLE:
      case ElementKind.PARAMETER:
      case ElementKind.PREFIX:
      case ElementKind.TYPE_PARAMETER:
      case ElementKind.UNKNOWN:
      default:
        return null; // unexpected
    }
  }

  @Override
  @NotNull
  public Object getValue() {
    return myValue;
  }

  /**
   * The string representation of both this element and the parent element from {@link myOutline}.
   */
  @NotNull
  public String getStringValue() {
    return getValue(myOutline);
  }

  @NotNull
  public static String getValue(@NotNull final Outline outline) {
    final Outline parent = outline.getParent();
    return (parent != null ? getValue(parent) + parent.getChildren().indexOf(outline) : "") + getPresentableText(outline);
  }

  @NotNull
  @Override
  public Object getKey() {
    return getPresentableText();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof DartStructureViewElement && ((DartStructureViewElement)obj).getStringValue().equals(getStringValue());
  }

  @Override
  public int hashCode() {
    return getStringValue().hashCode();
  }
}
