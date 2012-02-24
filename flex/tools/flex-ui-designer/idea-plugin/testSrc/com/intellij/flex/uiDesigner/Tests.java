package com.intellij.flex.uiDesigner;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceDescriptor;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.MutablePicoContainer;

final class Tests {
  public static final int GET_STAGE_OFFSET = 120;
  public static final int INFORM_DOCUMENT_OPENED = 121;

  public static void changeDesignerServicesImplementation() {
    final ExtensionPoint<ServiceDescriptor> extensionPoint = DesignerApplicationManager.getExtensionPoint();
    for (ServiceDescriptor extension : extensionPoint.getExtensions()) {
      if (extension.serviceInterface.equals(SocketInputHandler.class.getName())) {
        extension.serviceImplementation = TestSocketInputHandler.class.getName();
      }
      else if (extension.serviceInterface.equals(Client.class.getName())) {
        extension.serviceImplementation = TestClient.class.getName();
      }
    }
  }

  public static void changeDesignerServiceImplementation(Class serviceInterface, Class serviceImplementation) {
    final ExtensionPoint<ServiceDescriptor> extensionPoint = DesignerApplicationManager.getExtensionPoint();
    final String interfaceName = serviceInterface.getName();
    for (ServiceDescriptor extension : extensionPoint.getExtensions()) {
      if (extension.serviceInterface.equals(interfaceName)) {
        extension.serviceImplementation = serviceImplementation.getName();
      }
    }
  }

  public static void changeServiceImplementation(Class key, Class implementation) {
    MutablePicoContainer picoContainer = (MutablePicoContainer)ApplicationManager.getApplication().getPicoContainer();
    picoContainer.unregisterComponent(key.getName());
    picoContainer.registerComponentImplementation(key.getName(), implementation);
  }

  @NotNull
  public static XmlFile virtualToPsi(Project project, VirtualFile file) {
    XmlFile psiFile = (XmlFile)PsiManager.getInstance(project).findFile(file);
    assert psiFile != null;
    return psiFile;
  }
}