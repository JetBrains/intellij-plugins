/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.lang.javascript.uml;

import com.intellij.diagram.AbstractDiagramElementManager;
import com.intellij.diagram.presentation.DiagramState;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSClassImpl;
import com.intellij.lang.javascript.psi.impl.JSFunctionImpl;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.lang.javascript.ui.JSFormatUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.ElementBase;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.PlatformIcons;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JSElementManager extends AbstractDiagramElementManager<Object> {
  private final JSUmlProvider myUmlProvider;
  private static final Logger LOG = Logger.getInstance("#com.intellij.lang.javascript.uml.JSElementManager");

  public JSElementManager(JSUmlProvider umlProvider) {
    myUmlProvider = umlProvider;
  }

  public boolean isAcceptableAsNode(Object element) {
    return isAcceptableAsNodeStatic(element);
  }

  public static boolean isAcceptableAsNodeStatic(Object element) {
    if (element instanceof JSClass) {
      return true;
    }
    else if (element instanceof JSPackageStatement) {
      return true;
    }
    else if (element instanceof JSFile) {
      return JSPsiImplUtils.findQualifiedElement((JSFile)element) instanceof JSClass;
    }
    else if (element instanceof PsiFile && JavaScriptSupportLoader.isFlexMxmFile((PsiFile)element)) {
      return true;
    }
    else if (element instanceof PsiDirectory) {
      PsiDirectory directory = (PsiDirectory)element;
      String packageName = JSResolveUtil.getExpectedPackageNameFromFile(directory.getVirtualFile(), directory.getProject());
      return packageName != null && JSUtils.packageExists(packageName, GlobalSearchScope.allScope(directory.getProject()));
    }
    return false;
  }

  public PsiElement findInDataContext(DataContext context) {
    PsiElement element = LangDataKeys.PSI_ELEMENT.getData(context);
    if (isAcceptableAsNode(element)) {
      return element;
    }

    // if caret stands on a member or whitespace, show diagram for the enclosing class
    final Editor editor = LangDataKeys.EDITOR.getData(context);
    if (editor != null) {
      final PsiFile file = LangDataKeys.PSI_FILE.getData(context);
      if (file != null) {
        PsiElement elementAtOffset = file.findElementAt(editor.getCaretModel().getOffset());
        final PsiNamedElement enclosing = PsiTreeUtil.getParentOfType(elementAtOffset, JSClass.class, JSFile.class, XmlFile.class);
        if (enclosing instanceof JSClass) {
          element = enclosing;
        }
        else if (enclosing instanceof JSFile) {
          final XmlBackedJSClassImpl clazz = JSResolveUtil.getXmlBackedClass((JSFile)enclosing);
          if (clazz != null) {
            return clazz;
          }

          JSElement qualified = JSPsiImplUtils.findQualifiedElement((JSFile)enclosing);
          if (qualified instanceof JSClass) {
            return qualified;
          }
        }
        if (enclosing instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile((PsiFile)enclosing)) {
          return XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)enclosing);
        }
      }
    }

    // handle PsiPackage (invoked when 'View as packages' mode is selected)
    Project project = PlatformDataKeys.PROJECT.getData(context);
    if (project != null) {
      VirtualFile file = PlatformDataKeys.VIRTUAL_FILE.getData(context);
      if (file != null) {
        PsiDirectory directory = PsiManager.getInstance(project).findDirectory(file);
        if (directory != null && isAcceptableAsNode(directory)) {
          return directory;
        }
      }
    }
    return element;
  }

  public PsiElement[] getNodeElements(Object parent) {
    if (parent instanceof JSClass) {
      final JSClass clazz = (JSClass)parent;
      if (!clazz.isValid()) { return PsiElement.EMPTY_ARRAY; }
      final List<PsiElement> elements = new ArrayList<PsiElement>();
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
      else if (!(clazz instanceof JSClassImpl) || clazz.getStub() == null) {
        // this sort causes parsing in order to get ast node offset but
        // when we have class on stub our fields / functions already in natural order
        // TODO once we have stubs for xmlbackedclass we should update the code
        Collections.sort(elements, new Comparator<PsiElement>() {
          public int compare(PsiElement o1, PsiElement o2) {
            return o1.getTextOffset() - o2.getTextOffset();
          }
        });
      }
      return PsiUtilBase.toPsiElementArray(elements);
    }
    return PsiElement.EMPTY_ARRAY;
  }

  public boolean canCollapse(Object element) {
    return false;
  }

  public boolean isContainerFor(Object parent, Object child) {
    //if (parent instanceof JSPackage && child instanceof JSQualifiedNamedElement) {
    //  JSQualifiedNamedElement psiQualifiedNamedElement = (JSQualifiedNamedElement)child;
    //  JSPackage psiPackage = (JSPackage)parent;
    //  return psiQualifiedNamedElement.getQualifiedName() != null &&
    //         psiQualifiedNamedElement.getQualifiedName().startsWith(psiPackage.getQualifiedName());
    //}
    return false;
  }

  public String getElementTitle(Object element) {
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
    else if (element instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile((PsiFile)element)) {
      //noinspection ConstantConditions
      return ((XmlFile)element).getVirtualFile().getNameWithoutExtension();
    }
    else {
      return null;
    }
  }

  public SimpleColoredText getPresentableName(Object element, DiagramState presentation) {
    if (element instanceof JSFunction) {
      return getMethodPresentableName((JSFunction)element);
    }
    else if (element instanceof JSVariable) {
      return getFieldPresentableName((JSVariable)element);
    }
    else if (element instanceof JSClass) {
      return getClassPresentableName((JSClass)element);
    }
    else if (element instanceof String) {
      String s = (String)element;
      return decorate(getPackageDisplayName(s));
    }
    else if (element instanceof JSPackageStatement) {
      return decorate(((JSPackageStatement)element).getQualifiedName());
    }
    else if (element instanceof JSFile) {
      return decorate(getElementTitle(element));
    }
    else if (element instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile((PsiFile)element)) {
      return decorate(getElementTitle(element));
    }
    else if (element instanceof PsiDirectory) {
      PsiDirectory directory = (PsiDirectory)element;
      String qName = JSResolveUtil.getExpectedPackageNameFromFile(directory.getVirtualFile(), directory.getProject());
      return decorate(getElementTitle(qName));
    }
    return null;
  }

  private static String getPackageDisplayName(String s) {
    return s.length() > 0 ? s : JSBundle.message("top.level");
  }

  private SimpleColoredText decorate(String name) {
    int style = SimpleTextAttributes.STYLE_BOLD;
    final SimpleColoredText text = new SimpleColoredText();
    text.append(name, new SimpleTextAttributes(style, getFGColor()));
    return text;
  }

  private SimpleColoredText getClassPresentableName(JSClass clazz) {
    int style = SimpleTextAttributes.STYLE_BOLD;
    if (clazz.isDeprecated()) style |= SimpleTextAttributes.STYLE_STRIKEOUT;
    if (!clazz.isPhysical()) style |= SimpleTextAttributes.STYLE_ITALIC;

    final SimpleColoredText text = new SimpleColoredText();
    text.append(StringUtil.notNullize(clazz.getName()), new SimpleTextAttributes(style, getFGColor()));
    return text;
  }

  private SimpleColoredText getMethodPresentableName(JSFunction method) {
    int style = SimpleTextAttributes.STYLE_PLAIN;
    if (method.isDeprecated()) style |= SimpleTextAttributes.STYLE_STRIKEOUT;
    if (!method.isPhysical()) style |= SimpleTextAttributes.STYLE_ITALIC;
    final SimpleColoredText text = new SimpleColoredText();
    text.append(getMethodText(method),
                new SimpleTextAttributes(style, getFGColor()));
    return text;
  }

  private SimpleColoredText getFieldPresentableName(@NotNull JSVariable field) {
    int style = SimpleTextAttributes.STYLE_PLAIN;
    if (field.isDeprecated()) style |= SimpleTextAttributes.STYLE_STRIKEOUT;
    if (!field.isPhysical()) style |= SimpleTextAttributes.STYLE_ITALIC;
    return new SimpleColoredText(getFieldText(field), new SimpleTextAttributes(style, getFGColor()));
  }

  public static String getMethodText(JSFunction method) {
    return JSFormatUtil.formatMethod(method, JSFormatUtil.SHOW_NAME | JSFormatUtil.SHOW_PARAMETERS, JSFormatUtil.SHOW_TYPE);
  }

  public static String getFieldText(JSVariable field) {
    return JSFormatUtil.formatField(field, JSFormatUtil.SHOW_NAME);
  }

  private Color getFGColor() {
    return myUmlProvider.getColorManager().getNodeForegroundColor(false);
  }


  public SimpleColoredText getPresentableType(Object element) {
    String text = getPresentableTypeStatic(element);
    return text != null ? new SimpleColoredText(text, new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, getFGColor())) : null;
  }

  @Nullable
  public static String getPresentableTypeStatic(Object element) {
    if (element instanceof JSFunction) {
      return JSFormatUtil.formatMethod(((JSFunction)element), JSFormatUtil.SHOW_TYPE, 0);
    }
    else if (element instanceof JSVariable) {
      return JSFormatUtil.formatField(((JSVariable)element), JSFormatUtil.SHOW_TYPE);
    }
    else {
      return null;
    }
  }

  public String getElementDescription(Object element) {
    if (element instanceof JSClass) {
      return "<html><b>" + JSFormatUtil.formatClass((JSClass)element, JSFormatUtil.SHOW_FQ_NAME) + "</b></html>";
    }
    return "<html><b>" + getPackageDisplayName((String)element) + "</b></html>";
  }

  @Override
  public Icon getNodeElementIcon(Object element, DiagramState presentation) {
    return getNodeElementIconStatic(element);
  }

  public static Icon getNodeElementIconStatic(Object element) {
    if (element instanceof JSFunction) {
      JSFunction method = (JSFunction)element;
      if (method.getKind() == JSFunction.FunctionKind.GETTER || method.getKind() == JSFunction.FunctionKind.SETTER) {
        final Icon propertyIcon = JSFormatUtil.getPropertyIcon(method, true);
        return ElementBase.buildRowIcon(propertyIcon, method.getAttributeList().getAccessType().getIcon());
      }
      else if (method.getKind() == JSFunction.FunctionKind.CONSTRUCTOR) {
        return ElementBase.buildRowIcon(JSFunctionImpl.CONSTRUCTOR_ICON, method.getAttributeList().getAccessType().getIcon());
      }
    }
    return element instanceof Iconable ? ((Iconable)element).getIcon(0) : PlatformIcons.ERROR_INTRODUCTION_ICON;
  }
}
