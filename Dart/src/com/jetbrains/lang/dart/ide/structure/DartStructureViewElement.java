package com.jetbrains.lang.dart.ide.structure;

import com.intellij.icons.AllIcons;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.NodeDescriptorProvidingKey;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
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

public class DartStructureViewElement implements StructureViewTreeElement, ItemPresentation, NodeDescriptorProvidingKey {

  @NotNull private final PsiFile myPsiFile;
  @NotNull private final Outline myOutline;

  private final String myValue;
  private final String myPresentableText;

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
    new OpenFileDescriptor(myPsiFile.getProject(), myPsiFile.getVirtualFile(), offset).navigate(requestFocus);
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
    final Icon baseIcon;
    final Element element = myOutline.getElement();
    switch (element.getKind()) {
      case ElementKind.CLASS:
        baseIcon = element.isAbstract() ? AllIcons.Nodes.AbstractClass : AllIcons.Nodes.Class;
        break;
      case ElementKind.CLASS_TYPE_ALIAS:
        baseIcon = DartComponentType.TYPEDEF.getIcon();
        break;
      case ElementKind.CONSTRUCTOR:
        baseIcon = DartComponentType.CONSTRUCTOR.getIcon();
        break;
      case ElementKind.ENUM:
        baseIcon = AllIcons.Nodes.Enum;
        break;
      case ElementKind.ENUM_CONSTANT:
        baseIcon = AllIcons.Nodes.Field;
        break;
      case ElementKind.FIELD:
        baseIcon = DartComponentType.FIELD.getIcon();
        break;
      case ElementKind.FUNCTION:
        baseIcon = DartComponentType.FUNCTION.getIcon();
        break;
      case ElementKind.FUNCTION_TYPE_ALIAS:
        baseIcon = DartComponentType.TYPEDEF.getIcon();
        break;
      case ElementKind.GETTER:
        baseIcon = AllIcons.Nodes.PropertyRead;
        break;
      case ElementKind.LOCAL_VARIABLE:
        baseIcon = DartComponentType.VARIABLE.getIcon();
        break;
      case ElementKind.METHOD:
        baseIcon = DartComponentType.METHOD.getIcon();
        break;
      case ElementKind.SETTER:
        baseIcon = AllIcons.Nodes.PropertyWrite;
        break;
      case ElementKind.TOP_LEVEL_VARIABLE:
        baseIcon = DartComponentType.VARIABLE.getIcon();
        break;
      default:
        final String name = myOutline.getElement().getName();
        if (name.startsWith("test ") || name.startsWith("group ")) {
          baseIcon = DartIcons.TestNode;
        }
        else {
          baseIcon = null;
        }
    }

    return baseIcon;
  }

  @Override
  public String getValue() {
    return myValue;
  }

  @NotNull
  public static String getValue(@NotNull final Outline outline) {
    final Outline parent = outline.getParent();
    return parent != null ? getPresentableText(parent) + getPresentableText(outline) : getPresentableText(outline);
  }

  @NotNull
  @Override
  public Object getKey() {
    return getPresentableText();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof DartStructureViewElement && ((DartStructureViewElement)obj).getPresentableText().equals(getPresentableText());
  }

  @Override
  public int hashCode() {
    return getPresentableText().hashCode();
  }
}
