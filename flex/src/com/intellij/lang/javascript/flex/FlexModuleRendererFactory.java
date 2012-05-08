package com.intellij.lang.javascript.flex;

import com.intellij.ide.util.ModuleRendererFactory;
import com.intellij.ide.util.PsiElementModuleRenderer;
import com.intellij.lang.javascript.flex.projectStructure.FlexCompositeSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleJdkOrderEntry;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;

/**
 * User: ksafonov
 */
public class FlexModuleRendererFactory extends ModuleRendererFactory {

  @Override
  protected boolean handles(final Object element) {
    return true;
  }

  @Override
  public DefaultListCellRenderer getModuleRenderer() {
    return new PsiElementModuleRenderer() {
      @Override
      protected String getPresentableName(final OrderEntry order, final VirtualFile vFile) {
        if (order instanceof ModuleJdkOrderEntry) {
          Sdk sdk = ((ModuleJdkOrderEntry)order).getJdk();
          if (sdk instanceof FlexCompositeSdk) {
            return "< " + ((FlexCompositeSdk)sdk).getName(vFile) + " >";
          }
        }
        return super.getPresentableName(order, vFile);
      }
    };
  }
}
