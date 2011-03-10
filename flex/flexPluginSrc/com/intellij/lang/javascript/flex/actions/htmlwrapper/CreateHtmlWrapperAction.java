package com.intellij.lang.javascript.flex.actions.htmlwrapper;

import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleBuilder;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.io.IOException;
import java.util.Properties;

public class CreateHtmlWrapperAction extends AnAction {

  public static final String HTML_WRAPPER_TEMPLATE_FILE_NAME = "index.template.html";

  public static final String HTML_PAGE_TITLE = "title";
  public static final String FLEX_APPLICATION_NAME = "application";
  public static final String SWF_FILE_NAME = "swf";
  public static final String APPLICATION_WIDTH = "width";
  public static final String APPLICATION_HEIGHT = "height";
  public static final String BG_COLOR = "bgcolor";
  public static final String REQUIRED_FLASH_PLAYER_VERSION_MAJOR = "version_major";
  public static final String REQUIRED_FLASH_PLAYER_VERSION_MINOR = "version_minor";
  public static final String REQUIRED_FLASH_PLAYER_VERSION_REVISION = "version_revision";

  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    final CreateHtmlWrapperDialog dialog = new CreateHtmlWrapperDialog(project);
    dialog.show();
    if (dialog.isOK()) {
      try {
        final HTMLWrapperParameters htmlWrapperParameters = dialog.getHTMLWrapperParameters();
        final VirtualFile htmlFile = createHtmlWrapper(htmlWrapperParameters);

        if (htmlFile != null) {
          if (dialog.isCreateRunConfigurationSelected() && project != null) {
            FlexModuleBuilder.createFlexRunConfiguration(project, htmlFile);
          }

          final ToolWindowManager manager = ToolWindowManager.getInstance(project);
          manager.notifyByBalloon(ToolWindowId.PROJECT_VIEW, MessageType.INFO,
                                  FlexBundle.message("file.created", htmlFile.getName()), null,
                                  new HyperlinkListener() {
                                    public void hyperlinkUpdate(final HyperlinkEvent e) {
                                      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && htmlFile.isValid()) {
                                        FileEditorManager.getInstance(project)
                                          .openTextEditor(new OpenFileDescriptor(project, htmlFile), true);
                                      }
                                    }
                                  });
        }
        else {
          Messages.showErrorDialog(project, FlexBundle.message("html.wrapper.creation.failed", ""), FlexBundle.message("error.title"));
        }
      }
      catch (IOException ex) {
        Messages.showErrorDialog(project, FlexBundle.message("html.wrapper.creation.failed", ex.getMessage()), "Error");
      }
    }
  }

  @Override
  public void update(AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    e.getPresentation().setEnabled(project != null && ModuleManager.getInstance(project).getModules().length > 0);
  }


  public static VirtualFile createHtmlWrapper(final HTMLWrapperParameters parameters) throws IOException {
    final Ref<IOException> exceptionRef = new Ref<IOException>();

    final VirtualFile htmlFile = ApplicationManager.getApplication().runWriteAction(new NullableComputable<VirtualFile>() {
      public VirtualFile compute() {
        try {
          VirtualFile _htmlFile = null;

          for (final VirtualFile file : parameters.getHtmlWrapperRootDir().getChildren()) {
            if (file.getName().equals(HTML_WRAPPER_TEMPLATE_FILE_NAME)) {
              final Properties attributes = new Properties();
              attributes.setProperty(HTML_PAGE_TITLE, parameters.getHtmlPageTitle());
              attributes.setProperty(FLEX_APPLICATION_NAME, parameters.getFlexApplicationName());
              attributes.setProperty(SWF_FILE_NAME, parameters.getSwfFileName());
              attributes.setProperty(APPLICATION_WIDTH, parameters.getApplicationWidth());
              attributes.setProperty(APPLICATION_HEIGHT, parameters.getApplicationHeight());
              attributes.setProperty(BG_COLOR, parameters.getBgColor());
              attributes.setProperty(REQUIRED_FLASH_PLAYER_VERSION_MAJOR, String.valueOf(parameters.getFlashPlayerVersionMajor()));
              attributes.setProperty(REQUIRED_FLASH_PLAYER_VERSION_MINOR, String.valueOf(parameters.getFlashPlayerVersionMinor()));
              attributes.setProperty(REQUIRED_FLASH_PLAYER_VERSION_REVISION, String.valueOf(parameters.getFlashPlayerVersionRevision()));

              final String resultText = FileTemplateUtil.mergeTemplate(attributes, VfsUtil.loadText(file));
              _htmlFile = FlexUtils.addFileWithContent(parameters.getHtmlFileName(), resultText, parameters.getHtmlFileLocation());
            }
            else {
              file.copy(null, parameters.getHtmlFileLocation(), file.getName());
            }
          }
          return _htmlFile;
        }
        catch (IOException e) {
          exceptionRef.set(e);
          return null;
        }
      }
    });

    if (!exceptionRef.isNull()) {
      throw exceptionRef.get();
    }

    return htmlFile;
  }
}
