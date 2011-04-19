package com.intellij.lang.javascript.flex.presentation;

import com.intellij.ide.projectView.*;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSNamespaceDeclaration;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: Maxim
 * Date: 17.05.2010
 * Time: 11:13:40
 */
public class FlexTreeStructureProvider implements TreeStructureProvider {
  public Collection<AbstractTreeNode> modify(AbstractTreeNode parent, Collection<AbstractTreeNode> children, ViewSettings settings) {
    ArrayList<AbstractTreeNode> result = new ArrayList<AbstractTreeNode>();
    for (final AbstractTreeNode child : children) {
      Object o = child.getValue();
      if (o instanceof JSFileImpl ||
          o instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile((PsiFile)o)) {
        result.add(new FlexFileNode((PsiFile)o, ((ProjectViewNode)parent).getSettings()));
        continue;
      }
      result.add(child);
    }
    return result;
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

      if (value instanceof JSFileImpl) {
        VirtualFile file = value.getVirtualFile();
        if (file != null && ProjectRootManager.getInstance(myProject).getFileIndex().getSourceRootForFile(file) != null) {
          JSNamedElement element = JSFileImpl.findMainDeclaredElement((JSFileImpl)value);
          if (element != null) {
            className = element.getName();
          }
        }
      }
      else if (value instanceof XmlFile) {
        VirtualFile file = value.getVirtualFile();
        if (file != null && ProjectRootManager.getInstance(myProject).getFileIndex().getSourceRootForFile(file) != null) {
          className = file.getNameWithoutExtension();
        }
      }

      if (className != null) {
        data.setPresentableText(className);
        data.setIcons(value.getIcon(Iconable.ICON_FLAG_VISIBILITY | Iconable.ICON_FLAG_READ_STATUS));
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

  public static int getElementWeight(JSNamedElement element) {
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
}
