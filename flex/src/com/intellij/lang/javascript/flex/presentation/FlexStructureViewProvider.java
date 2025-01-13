// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.presentation;

import com.intellij.ide.structureView.*;
import com.intellij.ide.structureView.impl.xml.XmlStructureViewTreeModel;
import com.intellij.ide.structureView.xml.XmlStructureViewBuilderProvider;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSQualifiedName;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.lang.javascript.structureView.JSStructureViewElement;
import com.intellij.lang.javascript.structureView.JSStructureViewElementBase;
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

import java.util.*;

final class FlexStructureViewProvider implements XmlStructureViewBuilderProvider {
  @Override
  public StructureViewBuilder createStructureViewBuilder(final @NotNull XmlFile file) {
    if (!FlexSupportLoader.isFlexMxmFile(file)) return null;

    return new TreeBasedStructureViewBuilder() {
      @Override
      public @NotNull StructureViewModel createStructureViewModel(@Nullable Editor editor) {
        return new XmlStructureViewTreeModel(file, editor) {
          @Override
          public Sorter @NotNull [] getSorters() {
            return Sorter.EMPTY_ARRAY;
          }
        };
      }
    };
  }

  static final class FlexStructureViewClassElement extends JSStructureViewElement {
    private final XmlFile myFile;

    FlexStructureViewClassElement(@NotNull JSClass clazz) {
      this(clazz, false);
    }

    FlexStructureViewClassElement(@NotNull JSClass clazz, boolean inherited) {
      super(Collections.singletonList(clazz), null, null, true, inherited);
      myFile = (XmlFile)clazz.getContainingFile();
    }

    @Override
    protected @NotNull JSStructureViewElementBase copyWithInheritedImpl() {
      return new FlexStructureViewClassElement(((JSClass)Objects.requireNonNull(getElement())), true);
    }

    @Override
    protected List<StructureViewTreeElement> collectMyElements(@NotNull Set<String> outChildrenNames,
                                                               JSQualifiedName ns,
                                                               PsiFile contextFile) {
      List<StructureViewTreeElement> result = new ArrayList<>();
      ResolveProcessor processor = new ResolveProcessor(null) {
        @Override
        public boolean execute(final @NotNull PsiElement element, final @NotNull ResolveState state) {
          result.add(new JSStructureViewElement(element, true));
          return true;
        }
      };
      processor.setLocalResolve(true);
      getElement().processDeclarations(processor, ResolveState.initial(), getElement(), getElement());
      myFile.acceptChildren(new XmlRecursiveElementVisitor() {
        @Override
        public void visitXmlTag(final @NotNull XmlTag tag) {
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
    protected JSStructureViewElementBase createStructureViewElement(PsiElement element,
                                                                @Nullable OwnAndAncestorSiblings ownAndAncestorSiblings) {
      if (element instanceof XmlBackedJSClassImpl) {
        return new FlexStructureViewClassElement((JSClass)element);
      }
      else {
        return super.createStructureViewElement(element, ownAndAncestorSiblings);
      }
    }
  }
}
