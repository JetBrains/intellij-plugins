package com.intellij.lang.javascript.flex.presentation;

import com.intellij.ide.structureView.*;
import com.intellij.ide.structureView.impl.xml.XmlStructureViewTreeModel;
import com.intellij.ide.structureView.xml.XmlStructureViewBuilderProvider;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSQualifiedName;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.lang.javascript.structureView.JSStructureViewElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveState;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.util.HtmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Maxim.Mossienko
 * Date: Jul 17, 2008
 * Time: 10:31:43 PM
 */
public class FlexStructureViewProvider implements XmlStructureViewBuilderProvider {

  @Override
  public StructureViewBuilder createStructureViewBuilder(@NotNull final XmlFile file) {
    if (!JavaScriptSupportLoader.isFlexMxmFile(file)) return null;

    return new TreeBasedStructureViewBuilder() {
      @Override
      @NotNull
      public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
        return new XmlStructureViewTreeModel(file, editor) {
          @Override
          @NotNull
          public Sorter[] getSorters() {
            return Sorter.EMPTY_ARRAY;
          }
        };
      }
    };
  }

  static class FlexStructureViewElement extends JSStructureViewElement {
    private final XmlFile myFile;

    public FlexStructureViewElement(@NotNull JSClass clazz) {
      super(clazz, true);
      myFile = (XmlFile)clazz.getContainingFile();
    }

    @Override
    protected List<StructureViewTreeElement> collectMyElements(Set<String> referencedNames, 
                                                               JSQualifiedName ns,
                                                               PsiFile contextFile) {
      List<StructureViewTreeElement> result = new ArrayList<>();
      ResolveProcessor processor = new ResolveProcessor(null) {
        @Override
        public boolean execute(@NotNull final PsiElement element, @NotNull final ResolveState state) {
          result.add(new JSStructureViewElement(element, true));
          return true;
        }
      };
      processor.setLocalResolve(true);
      myElement.processDeclarations(processor, ResolveState.initial(), myElement, myElement);
      myFile.acceptChildren(new XmlRecursiveElementVisitor() {
        @Override
        public void visitXmlTag(final XmlTag tag) {
          if (HtmlUtil.STYLE_TAG_NAME.equalsIgnoreCase(tag.getLocalName())) {
            for (StructureViewExtension ext : StructureViewFactoryEx.getInstanceEx(myFile.getProject()).getAllExtensions(XmlTag.class)) {
              final StructureViewTreeElement[] structureViewTreeElements = ext.getChildren(tag);

              if (structureViewTreeElements != null && structureViewTreeElements.length > 0) {
                ContainerUtil.addAll(result, structureViewTreeElements);
              }
            }
          }
          else {
            super.visitXmlTag(tag);
          }
        }
      });
      return result;
    }

    @Override
    protected JSStructureViewElement createStructureViewElement(PsiElement element, Set<String> parentReferencedNames) {
      if (element instanceof XmlBackedJSClassImpl) {
        return new FlexStructureViewElement((JSClass)element);
      }
      else {
        return super.createStructureViewElement(element, parentReferencedNames);
      }
    }
  }
}
