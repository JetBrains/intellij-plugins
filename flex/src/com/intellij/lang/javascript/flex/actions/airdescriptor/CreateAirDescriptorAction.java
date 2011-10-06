package com.intellij.lang.javascript.flex.actions.airdescriptor;

import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class CreateAirDescriptorAction extends DumbAwareAction {
  public static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.toolWindowGroup("Flex messages", ToolWindowId.PROJECT_VIEW, false);

  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    final CreateAirDescriptorDialog dialog = new CreateAirDescriptorDialog(project);
    dialog.show();
    if (dialog.isOK()) {
      try {
        final VirtualFile descriptorFile = createAirDescriptor(dialog.getAirDescriptorParameters());

        NOTIFICATION_GROUP.createNotification("", FlexBundle.message("file.created", descriptorFile.getName()),
                                              NotificationType.INFORMATION, new NotificationListener() {
          @Override
          public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && descriptorFile.isValid()) {
                                      FileEditorManager.getInstance(project)
                                        .openTextEditor(new OpenFileDescriptor(project, descriptorFile), true);
                                    }
                                  }
                                }).notify(project);
      }
      catch (IOException ex) {
        Messages.showErrorDialog(project, FlexBundle.message("air.descriptor.creation.failed", ex.getMessage()),
                                 FlexBundle.message("error.title"));
      }
    }
  }


  public void update(final AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    e.getPresentation().setEnabled(project != null && ModuleManager.getInstance(project).getModules().length > 0);
  }


  public static VirtualFile createAirDescriptor(final AirDescriptorParameters parameters) throws IOException {
    final Ref<IOException> exceptionRef = new Ref<IOException>();

    final VirtualFile file = ApplicationManager.getApplication().runWriteAction(new NullableComputable<VirtualFile>() {
      public VirtualFile compute() {
        try {
          final String template = StringUtil.compareVersionNumbers(parameters.getAirVersion(), "2.6") >= 0
                                  ? "air-2.6.xml.ft"
                                  : StringUtil.compareVersionNumbers(parameters.getAirVersion(), "2.5") >= 0
                                    ? "air-2.5.xml.ft"
                                    : "air-1.0.xml.ft";
          final InputStream stream = CreateAirDescriptorAction.class.getResourceAsStream(template);
          assert stream != null;
          // noinspection IOResourceOpenedButNotSafelyClosed
          final String airDescriptorContentTemplate = FileUtil.loadTextAndClose(new InputStreamReader(stream));
          final Properties attributes = new Properties();
          attributes.setProperty("air_version", parameters.getAirVersion());
          attributes.setProperty("id", parameters.getApplicationId());
          attributes.setProperty("filename", parameters.getApplicationFileName());
          attributes.setProperty("name", parameters.getApplicationName());
          attributes.setProperty("version", parameters.getApplicationVersion());
          attributes.setProperty("content", parameters.getApplicationContent());
          attributes.setProperty("title", parameters.getApplicationTitle());
          attributes.setProperty("visible", "true");
          attributes.setProperty("width", String.valueOf(parameters.getApplicationWidth()));
          attributes.setProperty("height", String.valueOf(parameters.getApplicationHeight()));
          attributes.setProperty("android_permissions_start", parameters.isAndroidPermissionsEnabled() ? "" : "<!--");
          attributes.setProperty("android_permissions_end", parameters.isAndroidPermissionsEnabled() ? "" : "-->");

          final String airDescriptorContent = FileTemplateUtil.mergeTemplate(attributes, airDescriptorContentTemplate, true);
          final VirtualFile descriptorFolder = VfsUtil.createDirectories(parameters.getDescriptorFolderPath());
          return FlexUtils.addFileWithContent(parameters.getDescriptorFileName(), airDescriptorContent, descriptorFolder);
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

    return file;
  }
}
