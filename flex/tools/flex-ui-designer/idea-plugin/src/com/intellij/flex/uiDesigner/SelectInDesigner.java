package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.actions.RunDesignViewAction;
import com.intellij.ide.SelectInContext;
import com.intellij.ide.SelectInTarget;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;

import java.util.List;

public class SelectInDesigner implements SelectInTarget {
  @Override
  public boolean canSelect(SelectInContext context) {
    if (DesignerApplicationManager.getApplication() == null) {
      return false;
    }

    if (DocumentFactoryManager.getInstance().getNullableInfo(context.getVirtualFile()) == null) {
      return false;
    }

    final Object selectorInFile = context.getSelectorInFile();
    if (selectorInFile instanceof PsiElement) {
      final PsiElement psiElement = (PsiElement)selectorInFile;
      return RunDesignViewAction.canDo(psiElement.getProject(), psiElement.getContainingFile());
    }

    return false;
  }

  @Override
  public void selectIn(SelectInContext context, boolean requestFocus) {
    final DocumentFactoryManager.DocumentInfo documentInfo = DocumentFactoryManager.getInstance().getNullableInfo(context.getVirtualFile());
    if (documentInfo == null) {
      return;
    }

    final List<RangeMarker> rangeMarkers = documentInfo.getRangeMarkers();
    final int rangeMarkersSize = rangeMarkers.size();
    
    final Object selectorInFile = context.getSelectorInFile();
    if (!(selectorInFile instanceof PsiElement)) {
      return;
    }

    PsiElement element = (PsiElement)selectorInFile;
    final Module module = ModuleUtil.findModuleForPsiElement(element);
    if (module == null) {
      return;
    }
    
    if (element instanceof XmlFile) {
      DesignerApplicationManager.getInstance().openDocument(module, (XmlFile)element, false);
    }
    else {
      while (element != null) {
        if (element instanceof XmlTag && ((XmlTag)element).getDescriptor() instanceof ClassBackedElementDescriptor) {
          for (int i = 0; i < rangeMarkersSize; i++) {
            RangeMarker rangeMarker = rangeMarkers.get(i);
            if (rangeMarker.getStartOffset() == element.getTextOffset()) {
              Client.getInstance().selectComponent(module, documentInfo.getId(), i);
              return;
            }
          }
        }
        
        element = element.getContext();
      }
    }

    LogMessageUtil.LOG.warn("Can't find target component");
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
