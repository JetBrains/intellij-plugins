// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.lang.javascript.uml;

import com.intellij.diagram.AbstractDiagramElementManager;
import com.intellij.diagram.presentation.DiagramState;
import com.intellij.javascript.flex.resolve.FlexResolveHelper;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.presentable.JSFormatUtil;
import com.intellij.lang.javascript.presentable.JSNamedElementPresenter;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClass;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.lang.javascript.psi.ecmal4.impl.ActionScriptClassImpl;
import com.intellij.lang.javascript.psi.impl.JSFunctionImpl;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.ElementBase;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiFormatUtilBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.PlatformIcons;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FlashUmlElementManager extends AbstractDiagramElementManager<Object> {

  @Override
  public boolean isAcceptableAsNode(@Nullable Object element) {
    return isAcceptableAsNodeStatic(element);
  }

  public static boolean isAcceptableAsNodeStatic(Object element) {
    if (element instanceof PsiFile && FlexSupportLoader.isFlexMxmFile((PsiFile)element)) {
      return true;
    }
    if (element instanceof PsiDirectory directory) {
      String packageName = JSResolveUtil.getExpectedPackageNameFromFile(directory.getVirtualFile(), directory.getProject());
      return packageName != null && packageExists(directory.getProject(), packageName, GlobalSearchScope.allScope(directory.getProject()));
    }

    if (element instanceof PsiElement && !DialectDetector.isActionScript((PsiElement)element)) {
      return false;
    }

    if (element instanceof JSClass) {
      return true;
    }
    if (element instanceof JSPackageStatement) {
      return true;
    }
    if (element instanceof JSFile) {
      return JSPsiImplUtils.findQualifiedElement((JSFile)element) instanceof JSClass;
    }

    return false;
  }

  public static boolean packageExists(final Project project, final String packageName, final GlobalSearchScope scope) {
    return FlexUtils.packageExists(packageName, scope) || FlexResolveHelper.mxmlPackageExists(packageName, project, scope);
  }

  @Override
  public @Nullable PsiElement findInDataContext(@NotNull DataContext context) {
    PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(context);
    if (isAcceptableAsNode(element)) {
      return element;
    }

    // if caret stands on a member or whitespace, show diagram for the enclosing class
    Editor editor = CommonDataKeys.EDITOR.getData(context);
    if (editor != null) {
      final PsiFile file = CommonDataKeys.PSI_FILE.getData(context);
      if (file != null) {
        PsiElement elementAtOffset = file.findElementAt(editor.getCaretModel().getOffset());
        final PsiNamedElement enclosing = PsiTreeUtil.getParentOfType(elementAtOffset, JSClass.class, JSFile.class, XmlFile.class);
        if (enclosing instanceof JSClass) {
          element = enclosing;
        }
        else if (enclosing instanceof JSFile) {
          final XmlBackedJSClass clazz = JSResolveUtil.getXmlBackedClass((JSFile)enclosing);
          if (clazz != null) {
            return clazz;
          }

          JSElement qualified = JSPsiImplUtils.findQualifiedElement((JSFile)enclosing);
          if (qualified instanceof JSClass) {
            return qualified;
          }
        }
        if (enclosing instanceof XmlFile && FlexSupportLoader.isFlexMxmFile((PsiFile)enclosing)) {
          return XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)enclosing);
        }
      }
    }

    // handle PsiPackage (invoked when 'View as packages' mode is selected)
    Project project = CommonDataKeys.PROJECT.getData(context);
    if (project != null) {
      VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(context);
      if (file != null) {
        PsiDirectory directory = PsiManager.getInstance(project).findDirectory(file);
        if (directory != null && isAcceptableAsNode(directory)) {
          return directory;
        }
      }
    }
    return element;
  }

  @Override
  public PsiElement @NotNull [] getNodeItems(Object parent) {
    if (parent instanceof JSClass clazz) {
      if (!clazz.isValid()) return PsiElement.EMPTY_ARRAY;
      final List<PsiElement> elements = new ArrayList<>();
      ContainerUtil.addAll(elements, clazz.getFields());
      boolean isInterface = clazz.isInterface();
      for (JSFunction method : clazz.getFunctions()) {
        if (isInterface && method.getKind() == JSFunction.FunctionKind.CONSTRUCTOR) continue;
        if (method.getKind() != JSFunction.FunctionKind.SETTER ||
            clazz.findFunctionByNameAndKind(method.getName(), JSFunction.FunctionKind.GETTER) == null) {
          elements.add(method);
        }
      }

      // TODO: need to perform kind sorting
      if (elements.isEmpty()) {
        return PsiElement.EMPTY_ARRAY;
      }
      else if (!(clazz instanceof ActionScriptClassImpl) || ((ActionScriptClassImpl)clazz).getStub() == null) {
        // this sort causes parsing in order to get ast node offset but
        // when we have class on stub our fields / functions already in natural order
        // TODO once we have stubs for xmlbackedclass we should update the code
        elements.sort(Comparator.comparingInt(PsiElement::getTextOffset));
      }
      return PsiUtilCore.toPsiElementArray(elements);
    }
    return PsiElement.EMPTY_ARRAY;
  }

  @Override
  public boolean isContainerFor(Object parent, Object child) {
    //if (parent instanceof JSPackage && child instanceof JSQualifiedNamedElement) {
    //  JSQualifiedNamedElement psiQualifiedNamedElement = (JSQualifiedNamedElement)child;
    //  JSPackage psiPackage = (JSPackage)parent;
    //  return psiQualifiedNamedElement.getQualifiedName() != null &&
    //         psiQualifiedNamedElement.getQualifiedName().startsWith(psiPackage.getQualifiedName());
    //}
    return false;
  }

  @Override
  public @Nullable String getElementTitle(Object element) {
    if (element instanceof JSNamedElement) {
      return ((JSNamedElement)element).getName();
    }
    else if (element instanceof String) {
      return JSFormatUtil.formatPackage((String)element);
    }
    else if (element instanceof JSFile) {
      //noinspection ConstantConditions
      return JSPsiImplUtils.findQualifiedElement((JSFile)element).getName();
    }
    else if (element instanceof XmlFile && FlexSupportLoader.isFlexMxmFile((PsiFile)element)) {
      return ((XmlFile)element).getVirtualFile().getNameWithoutExtension();
    }
    else {
      return null;
    }
  }

  @Override
  public @Nullable SimpleColoredText getItemName(@Nullable Object element, @NotNull DiagramState presentation) {
    if (element instanceof JSFunction) {
      return getMethodPresentableName((JSFunction)element);
    }
    else if (element instanceof JSVariable) {
      return getFieldPresentableName((JSVariable)element);
    }
    else if (element instanceof JSClass) {
      return getClassPresentableName((JSClass)element);
    }
    else if (element instanceof String s) {
      return decorate(getPackageDisplayName(s));
    }
    else if (element instanceof JSPackageStatement) {
      return decorate(((JSPackageStatement)element).getQualifiedName());
    }
    else if (element instanceof JSFile) {
      return decorate(getElementTitle(element));
    }
    else if (element instanceof XmlFile && FlexSupportLoader.isFlexMxmFile((PsiFile)element)) {
      return decorate(getElementTitle(element));
    }
    else if (element instanceof PsiDirectory directory) {
      String qName = JSResolveUtil.getExpectedPackageNameFromFile(directory.getVirtualFile(), directory.getProject());
      return decorate(getElementTitle(qName));
    }
    return null;
  }

  private static String getPackageDisplayName(String s) {
    return !s.isEmpty() ? s : FlexBundle.message("top.level");
  }

  private static SimpleColoredText decorate(String name) {
    SimpleColoredText text = new SimpleColoredText();
    text.append(name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
    return text;
  }

  private static SimpleColoredText getClassPresentableName(JSClass clazz) {
    int style = SimpleTextAttributes.STYLE_BOLD;
    if (clazz.isDeprecated()) style |= SimpleTextAttributes.STYLE_STRIKEOUT;
    if (!clazz.isPhysical()) style |= SimpleTextAttributes.STYLE_ITALIC;

    final SimpleColoredText text = new SimpleColoredText();
    String name = StringUtil.notNullize(clazz.getName());
    text.append(FlashUmlVfsResolver.fixVectorTypeName(name), new SimpleTextAttributes(style, DEFAULT_TEXT_ATTR.getFgColor()));
    return text;
  }

  private static SimpleColoredText getMethodPresentableName(JSFunction method) {
    int style = SimpleTextAttributes.STYLE_PLAIN;
    if (method.isDeprecated()) style |= SimpleTextAttributes.STYLE_STRIKEOUT;
    if (!method.isPhysical()) style |= SimpleTextAttributes.STYLE_ITALIC;
    final SimpleColoredText text = new SimpleColoredText();
    text.append(getMethodText(method),
      new SimpleTextAttributes(style, DEFAULT_TEXT_ATTR.getFgColor()));
    return text;
  }

  private static SimpleColoredText getFieldPresentableName(@NotNull JSVariable field) {
    int style = SimpleTextAttributes.STYLE_PLAIN;
    if (field.isDeprecated()) style |= SimpleTextAttributes.STYLE_STRIKEOUT;
    if (!field.isPhysical()) style |= SimpleTextAttributes.STYLE_ITALIC;
    return new SimpleColoredText(getFieldText(field), new SimpleTextAttributes(style, DEFAULT_TEXT_ATTR.getFgColor()));
  }

  public static String getMethodText(JSFunction method) {
    return JSFormatUtil.formatMethod(method, PsiFormatUtilBase.SHOW_NAME | PsiFormatUtilBase.SHOW_PARAMETERS, PsiFormatUtilBase.SHOW_TYPE);
  }

  public static String getFieldText(JSVariable field) {
    return JSFormatUtil.formatField(field, PsiFormatUtilBase.SHOW_NAME);
  }

  @Override
  public @Nullable SimpleColoredText getItemType(@Nullable Object element) {
    String text = getPresentableTypeStatic(element);
    return text != null ? new SimpleColoredText(text, DEFAULT_TEXT_ATTR) : null;
  }

  public static @Nullable String getPresentableTypeStatic(Object element) {
    if (element instanceof JSFunction) {
      return JSFormatUtil.formatMethod(((JSFunction)element), PsiFormatUtilBase.SHOW_TYPE, 0);
    }
    else if (element instanceof JSVariable) {
      return JSFormatUtil.formatField(((JSVariable)element), PsiFormatUtilBase.SHOW_TYPE);
    }
    else {
      return null;
    }
  }

  @Override
  public @Nullable @Nls String getNodeTooltip(Object element) {
    if (element instanceof JSClass) {
      return "<html><b>" + new JSNamedElementPresenter((JSClass)element).describeWithQualifiedName() + "</b></html>";
    }
    return "<html><b>" + JSFormatUtil.formatPackage((String)element) + "</b></html>";
  }

  @Override
  public @Nullable Icon getItemIcon(@Nullable Object element, @NotNull DiagramState presentation) {
    return getNodeElementIconStatic(element);
  }

  public static Icon getNodeElementIconStatic(Object element) {
    if (element instanceof JSFunction method) {
      if (method.getKind() == JSFunction.FunctionKind.GETTER || method.getKind() == JSFunction.FunctionKind.SETTER) {
        final Icon propertyIcon = JSFormatUtil.getPropertyIcon(method);
        return ElementBase.buildRowIcon(propertyIcon, method.getAttributeList().getAccessType().getIcon());
      }
      else if (method.getKind() == JSFunction.FunctionKind.CONSTRUCTOR) {
        return ElementBase.buildRowIcon(JSFunctionImpl.CONSTRUCTOR_ICON, method.getAttributeList().getAccessType().getIcon());
      }
    }
    return element instanceof Iconable ? ((Iconable)element).getIcon(0) : PlatformIcons.ERROR_INTRODUCTION_ICON;
  }
}
