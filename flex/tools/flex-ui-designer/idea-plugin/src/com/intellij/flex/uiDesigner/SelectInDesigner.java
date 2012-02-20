package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.actions.RunDesignViewAction;
import com.intellij.ide.SelectInContext;
import com.intellij.ide.SelectInTarget;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SelectInDesigner implements SelectInTarget {
  @Override
  public boolean canSelect(SelectInContext context) {
    PsiElement element = getPsiElement(context);
    return element != null && RunDesignViewAction.canDo(context.getProject(), element.getContainingFile());
  }

  @Nullable
  private static PsiElement getPsiElement(SelectInContext context) {
    Object selectorInFile = context.getSelectorInFile();
    if (selectorInFile instanceof PsiElement) {
      PsiElement element = (PsiElement)selectorInFile;
      return element.getContainingFile() instanceof XmlFile ? element : null;
    }

    return null;
  }

  @Override
  public void selectIn(SelectInContext context, boolean requestFocus) {
    final PsiElement element = getPsiElement(context);
    final Module module = element == null ? null : ModuleUtil.findModuleForPsiElement(element);
    if (module == null) {
      return;
    }

    final VirtualFile virtualFile = context.getVirtualFile();

    final Runnable doAction = new Runnable() {
      @Override
      public void run() {
        DocumentFactoryManager.DocumentInfo info = DocumentFactoryManager.getInstance().getNullableInfo(virtualFile);
        if (info == null) {
          return;
        }

        final List<RangeMarker> rangeMarkers = info.getRangeMarkers();

        if (element instanceof XmlFile) {
          DesignerApplicationManager.getInstance().openDocument(module, (XmlFile)element, false);
        }
        else {
          PsiElement effectiveElement = element;
          do {
            if (effectiveElement instanceof XmlTag && ((XmlTag)effectiveElement).getDescriptor() instanceof ClassBackedElementDescriptor) {
              for (int i = 0; i < rangeMarkers.size(); i++) {
                RangeMarker rangeMarker = rangeMarkers.get(i);
                if (rangeMarker.getStartOffset() == effectiveElement.getTextOffset()) {
                  Client.getInstance().selectComponent(module, info.getId(), i);
                  return;
                }
              }
            }

            effectiveElement = effectiveElement.getContext();
          }
          while (effectiveElement != null);

          LogMessageUtil.LOG.warn("Can't find target component");
        }
      }
    };

    if (DesignerApplicationManager.getInstance().isApplicationClosed() || !DocumentFactoryManager.getInstance().isRegistered(virtualFile)) {
      DesignerApplicationManager.getInstance().renderDocument((XmlFile)element.getContainingFile()).doWhenDone(doAction);
    }
    else {
      doAction.run();
    }
  }

  @Override
  public String getToolWindowId() {
    return null;
  }

  @Override
  public String getMinorViewId() {
    return null;
  }

  @Override
  public float getWeight() {
    return 0;
  }

  @Override
  public String toString() {
    return FlashUIDesignerBundle.message("select.in.designer");
  }
}
