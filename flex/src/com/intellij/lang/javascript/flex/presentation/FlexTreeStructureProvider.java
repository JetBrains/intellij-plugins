package com.intellij.lang.javascript.flex.presentation;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSNamespaceDeclaration;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.lang.javascript.structureView.JSStructureViewElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.SimpleTextAttributes;
import icons.JavaScriptLanguageIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: Maxim
 * Date: 17.05.2010
 * Time: 11:13:40
 */
public class FlexTreeStructureProvider implements TreeStructureProvider, DumbAware {
  public Collection<AbstractTreeNode> modify(AbstractTreeNode parent, Collection<AbstractTreeNode> children, ViewSettings settings) {
    List<AbstractTreeNode> result = new ArrayList<AbstractTreeNode>();
    if (parent instanceof SwfQualifiedNamedElementNode || parent instanceof FlexFileNode) {
      if (((ProjectViewNode)parent).getSettings().isShowMembers()) {
        JSQualifiedNamedElement parentElement = getElement(parent);
        if (parentElement != null) {
          JSStructureViewElement structureViewElement =
            parentElement instanceof XmlBackedJSClassImpl ? new FlexStructureViewProvider.FlexStructureViewElement(
              ((XmlBackedJSClassImpl)parentElement), (XmlFile)parentElement.getContainingFile())
                                                          : new JSStructureViewElement(parentElement);
          StructureViewTreeElement[] structureViewChildren = structureViewElement.getChildren(false);
          for (final StructureViewTreeElement structureViewChild : structureViewChildren) {
            if (structureViewChild instanceof JSStructureViewElement) {
              PsiElement childElement = ((JSStructureViewElement)structureViewChild).getUpToDateElement();
              result.add(new FlexClassMemberNode((JSElement)childElement, ((ProjectViewNode)parent).getSettings()));
            }
            else {
              result.add(new UnknownNode(parentElement.getProject(), structureViewChild, ((ProjectViewNode)parent).getSettings()));
            }
          }
        }
      }
    }
    else {
      for (final AbstractTreeNode child : children) {
        Object o = child.getValue();
        if (o instanceof JSFileImpl ||
            o instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile((PsiFile)o)) {
          result.add(new FlexFileNode((PsiFile)o, ((ProjectViewNode)parent).getSettings()));
          continue;
        }
        result.add(child);
      }
    }
    return result;
  }

  @Nullable
  private static JSQualifiedNamedElement getElement(AbstractTreeNode parent) {
    if (parent instanceof SwfQualifiedNamedElementNode) {
      return (JSQualifiedNamedElement)parent.getValue();
    }
    else {
      PsiFile file = ((FlexFileNode)parent).getValue();
      if (file instanceof JSFileImpl) {
        JSNamedElement element = JSFileImpl.findMainDeclaredElement((JSFileImpl)file);
        if (element instanceof JSQualifiedNamedElement) {
          return (JSQualifiedNamedElement)element;
        }
      }
      else if (file instanceof XmlFile) {
        return XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)file);
      }
    }
    return null;
  }

  public Object getData(Collection<AbstractTreeNode> selected, String dataName) {
    return null;
  }

  private static final int INTERFACE_VALUE = 10;
  private static final int NAMESPACE_VALUE = 7;
  private static final int FUNCTION_VALUE = 5;
  private static final int VARIABLE_VALUE = 4;
  private static final int CLASS_VALUE = 20;

  private static class FlexFileNode extends PsiFileNode {

    public FlexFileNode(final PsiFile value, final ViewSettings viewSettings) {
      super(value.getProject(), value, viewSettings);
    }

    @Override
    protected void updateImpl(final PresentationData data) {
      PsiFile value = getValue();

      String className = null;

      Icon icon = null;
      if (value instanceof JSFileImpl) {
        VirtualFile file = value.getVirtualFile();
        if (file != null && ProjectRootManager.getInstance(myProject).getFileIndex().getSourceRootForFile(file) != null) {
          JSNamedElement element = JSFileImpl.findMainDeclaredElement((JSFileImpl)value);
          if (element != null) {
            className = element.getName();
            icon = element.getIcon(Iconable.ICON_FLAG_VISIBILITY | Iconable.ICON_FLAG_READ_STATUS);
          }
        }
      }
      else if (value instanceof XmlFile) {
        VirtualFile file = value.getVirtualFile();
        if (file != null && ProjectRootManager.getInstance(myProject).getFileIndex().getSourceRootForFile(file) != null) {
          className = file.getNameWithoutExtension();
          icon = JavaScriptLanguageIcons.Flex.XmlBackedClass;
        }
      }

      if (className != null) {
        data.setPresentableText(className);
        data.setIcon(icon);
        return;
      }

      super.updateImpl(data);
    }

    @Override
    public int getTypeSortWeight(boolean sortByType) {
      if(sortByType) {
        PsiFile value = getValue();
        if (value instanceof JSFileImpl) {
          JSNamedElement element = JSFileImpl.findMainDeclaredElement((JSFileImpl)value);
          int weight = getElementWeight(element);
          if (weight != -1) {
            return weight;
          }
        } else if (value instanceof XmlFile) {
          return CLASS_VALUE;
        }
      }
      return super.getTypeSortWeight(sortByType);
    }

  }

  public static int getElementWeight(JSElement element) {
    if (element instanceof JSClass) {
      return ((JSClass)element).isInterface() ? INTERFACE_VALUE : CLASS_VALUE;
    }
    else if (element instanceof JSVariable) {
      return VARIABLE_VALUE;
    }
    else if (element instanceof JSFunction) {
      return FUNCTION_VALUE;
    }
    else if (element instanceof JSNamespaceDeclaration) {
      return NAMESPACE_VALUE;
    }
    return -1;
  }

  private static class UnknownNode extends ProjectViewNode<Object> {
    private final StructureViewTreeElement myElement;

    public UnknownNode(Project project,
                       final StructureViewTreeElement element, final ViewSettings viewSettings) {
      super(project, element.getValue(), viewSettings);
      myElement = element;
    }

    public boolean contains(@NotNull final VirtualFile file) {
      return false;
    }

    @NotNull
    public Collection<? extends AbstractTreeNode> getChildren() {
      return Collections.emptyList();
    }

    protected void update(final PresentationData presentation) {
      final ItemPresentation p = myElement.getPresentation();

      presentation.setPresentableText(p.getPresentableText());
      presentation.setIcon(p.getIcon(false));
      presentation.addText(p.getPresentableText(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
      presentation.addText(" ", SimpleTextAttributes.REGULAR_ATTRIBUTES);

      final String location = presentation.getLocationString();
      if (location != null && location.length() > 0) {
        presentation.addText(" (" + location + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
      }
    }

    public VirtualFile getVirtualFile() {
      final Object value = getValue();
      return value instanceof PsiElement ? ((PsiElement)value).getContainingFile().getVirtualFile() : null;
    }

    public boolean canNavigate() {
      return myElement.canNavigate();
    }

    public boolean canNavigateToSource() {
      return myElement.canNavigateToSource();
    }

    public void navigate(final boolean requestFocus) {
      myElement.navigate(requestFocus);
    }
  }
}
