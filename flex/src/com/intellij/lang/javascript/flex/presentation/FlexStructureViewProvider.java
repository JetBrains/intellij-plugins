package com.intellij.lang.javascript.flex.presentation;

import com.intellij.ide.structureView.*;
import com.intellij.ide.structureView.xml.XmlStructureViewBuilderProvider;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.index.JSIndexEntryBase;
import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.lang.javascript.index.JSNamespace;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.lang.javascript.structureView.JSStructureViewElement;
import com.intellij.lang.javascript.structureView.JSStructureViewModel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveState;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.TIntHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Maxim.Mossienko
 *         Date: Jul 17, 2008
 *         Time: 10:31:43 PM
 */
public class FlexStructureViewProvider implements XmlStructureViewBuilderProvider {

  public StructureViewBuilder createStructureViewBuilder(@NotNull final XmlFile file) {
    if (!JavaScriptSupportLoader.isFlexMxmFile(file)) return null;

    return new TreeBasedStructureViewBuilder() {
      @NotNull
      public StructureViewModel createStructureViewModel() {
        final JSClass clazz = XmlBackedJSClassImpl.getXmlBackedClass(file);

        return new JSStructureViewModel(clazz) {
          @Override
          protected JSStructureViewElement createRoot(PsiElement root) {
            return new FlexStructureViewElement(clazz, file);
          }
        };
      }
    };
  }

  static class FlexStructureViewElement extends JSStructureViewElement {
    private final XmlFile myFile;

    public FlexStructureViewElement(final JSClass clazz, final XmlFile file) {
      super(clazz);
      myFile = file;
    }

    @Override
    protected List<StructureViewTreeElement> collectMyElements(final TIntHashSet referencedNamedIds, final JSIndexEntryBase entry,
                                                           final JSNamespace ns, final JavaScriptIndex index, PsiFile contextFile) {
      final List<StructureViewTreeElement> result = new ArrayList<StructureViewTreeElement>();
      final ResolveProcessor processor = new ResolveProcessor(null) {
        @Override
        public boolean execute(final PsiElement element, final ResolveState state) {
          result.add(new JSStructureViewElement(element));
          return true;
        }
      };
      processor.setLocalResolve(true);
      myElement.processDeclarations(processor, ResolveState.initial(), myElement, myElement);
      myFile.acceptChildren(new XmlRecursiveElementVisitor() {
        @Override
        public void visitXmlTag(final XmlTag tag) {
          if ("style".equalsIgnoreCase(tag.getLocalName())) {
            for(StructureViewExtension ext: StructureViewFactoryEx.getInstanceEx(myFile.getProject()).getAllExtensions(XmlTag.class)) {
              final StructureViewTreeElement[] structureViewTreeElements = ext.getChildren(tag);

              if (structureViewTreeElements != null && structureViewTreeElements.length > 0) {
                ContainerUtil.addAll(result, structureViewTreeElements);
              }
            }
          } else {
            super.visitXmlTag(tag);
          }
        }
      });
      return result;
    }

    @Override
    protected JSStructureViewElement createStructureViewElement(final PsiElement element, final JSNamedElementProxy proxy) {
      if (element instanceof XmlBackedJSClassImpl) {
        PsiFile file = element.getContainingFile();
        return new FlexStructureViewElement((JSClass)element, (XmlFile)file);
      } else {
        return super.createStructureViewElement(element, proxy);
      }
    }
  }
}
