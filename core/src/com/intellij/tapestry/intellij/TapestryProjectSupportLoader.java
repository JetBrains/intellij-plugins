package com.intellij.tapestry.intellij;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.compiler.MalformedPatternException;
import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.patterns.XmlPatterns;
import com.intellij.peer.PeerFactory;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.log.Logger;
import com.intellij.tapestry.core.log.LoggerFactory;
import com.intellij.tapestry.intellij.lang.descriptor.TapestryNamespaceDescriptor;
import com.intellij.tapestry.intellij.lang.reference.XmlTagValueReferenceProvider;
import com.intellij.tapestry.intellij.toolwindow.TapestryToolWindow;
import com.intellij.tapestry.intellij.util.Icons;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class TapestryProjectSupportLoader implements ProjectComponent, Disposable {

  public static final String TAPESTRY_TOOLWINDOW_ID = "Tapestry";

  private static final Logger _logger = LoggerFactory.getInstance().getLogger(TapestryProjectSupportLoader.class);

  private Project _project;
  private TapestryToolWindow _tapestryToolwindow;

  public TapestryProjectSupportLoader(Project project) {
    _project = project;
  }

  public void enableToolWindow() {
    if (ToolWindowManager.getInstance(_project).getToolWindow(TAPESTRY_TOOLWINDOW_ID) == null) registerToolWindow();

    ToolWindowManager.getInstance(_project).getToolWindow(TAPESTRY_TOOLWINDOW_ID).setAvailable(true, null);
  }

  public void initComponent() {
    ExternalResourceManagerEx.getInstanceEx().addImplicitNamespace(TapestryConstants.TEMPLATE_NAMESPACE, new TapestryNamespaceDescriptor(), this);
  }

  public void dispose() {
  }

  public void disposeComponent() {
    Disposer.dispose(this);
  }

  @NotNull
  public String getComponentName() {
    return TapestryProjectSupportLoader.class.getName();
  }

  public void projectOpened() {
    // register Tapestry ToolWindow
    registerToolWindow();

    if (TapestryUtils.getAllTapestryModules(_project).length > 0) {
      ToolWindowManager.getInstance(_project).getToolWindow(TAPESTRY_TOOLWINDOW_ID).setAvailable(true, null);

      StartupManager.getInstance(_project).runWhenProjectIsInitialized(new Runnable() {
        public void run() {
          try {
            addCompilerResources();
          }
          catch (Exception ex) {
            _logger.warn(ex);
          }
        }
      });
    }

    // register attribute values reference provider
    ReferenceProvidersRegistry.getInstance(_project)
        .registerReferenceProvider(XmlPatterns.xmlAttributeValue(), new XmlTagValueReferenceProvider());
  }


  public TapestryToolWindow getTapestryToolWindow() {
    if (_tapestryToolwindow == null) registerToolWindow();

    return _tapestryToolwindow;
  }

  public void projectClosed() {
    if (ToolWindowManager.getInstance(_project).getToolWindow(TAPESTRY_TOOLWINDOW_ID) != null) {
      ToolWindowManager.getInstance(_project).unregisterToolWindow(TAPESTRY_TOOLWINDOW_ID);
    }
  }

  public void addCompilerResources() throws MalformedPatternException {
    final CompilerConfigurationImpl compilerConfiguration = (CompilerConfigurationImpl)_project.getComponent(CompilerConfiguration.class);
    String[] filePatterns = compilerConfiguration.getResourceFilePatterns();

    final String tapestryFilePattern = "?*." + TapestryConstants.TEMPLATE_FILE_EXTENSION;
    if (Arrays.binarySearch(filePatterns, tapestryFilePattern) < 0) {
      compilerConfiguration.addResourceFilePattern(tapestryFilePattern);
    }
  }

  private void registerToolWindow() {
    if (ToolWindowManager.getInstance(_project).getToolWindow(TAPESTRY_TOOLWINDOW_ID) == null) {
      _tapestryToolwindow = new TapestryToolWindow(_project);

      ToolWindow toolwindow =
          ToolWindowManager.getInstance(_project).registerToolWindow(TAPESTRY_TOOLWINDOW_ID, false, ToolWindowAnchor.BOTTOM);
      toolwindow.getContentManager()
          .addContent(PeerFactory.getInstance().getContentFactory().createContent(_tapestryToolwindow.getMainPanel(), "Tapestry", true));

      toolwindow.setIcon(Icons.TAPESTRY_LOGO_SMALL);
      toolwindow.setAvailable(false, null);
    }
    else {
      ToolWindow toolwindow = ToolWindowManager.getInstance(_project).getToolWindow(TAPESTRY_TOOLWINDOW_ID);
      toolwindow.show(null);
    }
  }
}
