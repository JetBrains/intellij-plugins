package com.intellij.flex.uiDesigner;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.impl.ComponentManagerImpl;
import com.intellij.openapi.extensions.AreaInstance;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

class DesignerApplication extends ComponentManagerImpl implements AreaInstance, Consumer<Integer> {
  private AdlUtil.AdlProcessHandler processHandler;

  DesignerApplication() {
    super(ApplicationManager.getApplication());
  }

  @Override
  public <T> T[] getExtensions(ExtensionPointName<T> extensionPointName) {
    return Extensions.getArea(this).getExtensionPoint(extensionPointName).getExtensions();
  }

  void setProcessHandler(@NotNull AdlUtil.AdlProcessHandler processHandler) {
    this.processHandler = processHandler;
    processHandler.adlExitHandler = this;
  }

  @Override
  public void consume(Integer exitCode) {
    processHandler = null;
    DesignerApplicationManager.getInstance().disposeApplication();
    if (exitCode != 0) {
      AdlUtil.describeAdlExit(exitCode);
    }
  }

  @Override
  public void dispose() {
    try {
      super.dispose();
    }
    finally {
      if (processHandler != null) {
        processHandler.destroyProcess();
        processHandler = null;
      }
    }
  }
}