package com.jetbrains.lang.dart.ide.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.ide.util.treeView.NodeDescriptorProvidingKey;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import icons.DartIcons;
import org.dartlang.analysis.server.protocol.Element;
import org.dartlang.analysis.server.protocol.ElementKind;
import org.dartlang.analysis.server.protocol.Outline;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.intellij.icons.AllIcons.Nodes.*;
import static com.intellij.icons.AllIcons.Nodes.Class;
import static com.intellij.icons.AllIcons.Nodes.Enum;
import static com.intellij.icons.AllIcons.RunConfigurations.Junit;

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

  @NotNull private final String myValue;
  @NotNull private final String myPresentableText;

  public DartStructureViewElement(@NotNull final PsiFile psiFile, @NotNull final Outline outline) {
    myPsiFile = psiFile;
    myOutline = outline;
    myValue = getValue(outline);
    myPresentableText = getPresentableText(outline);
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
        return Junit;
      case ElementKind.UNIT_TEST_TEST:
        return DartIcons.TestNode;

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
  public String getValue() {
    return myValue;
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
    return obj instanceof DartStructureViewElement && ((DartStructureViewElement)obj).getValue().equals(getValue());
  }

  @Override
  public int hashCode() {
    return getValue().hashCode();
  }
}
