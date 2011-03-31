package com.intellij.flex.uiDesigner;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SocketInputHandlerImpl implements SocketInputHandler {
  protected Reader reader;

  protected void createReader(InputStream inputStream) {
    reader = new Reader(new BufferedInputStream(inputStream));
  }

  @Override
  public void read(InputStream inputStream) throws IOException {
    createReader(inputStream);
    final FlexUIDesignerApplicationManager designerAppManager = FlexUIDesignerApplicationManager.getInstance();

    readProcess:
    while (true) {
      final int command = reader.read();
      switch (command) {
        case -1:
          break readProcess;

        case ServerMethod.goToClass:
          final Module module = designerAppManager.getClient().getModule(reader.readInt());
          final String className = reader.readUTF();
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
              JSClass classElement =
                ((JSClass)JSResolveUtil.findClassByQName(className, module.getModuleWithDependenciesAndLibrariesScope(false)));
              classElement.navigate(true);
              ProjectUtil.focusProjectWindow(classElement.getProject(), true);
            }
          });
          break;

        case ServerMethod.openFile:
          final Project project = designerAppManager.getClient().getModule(reader.readInt()).getProject();
          final OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, reader.readFile(), reader.readInt());
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
              openFileDescriptor.navigate(true);
              ProjectUtil.focusProjectWindow(project, true);
            }
          });
          break;

        case ServerMethod.resolveExternalInlineStyleDeclarationSource:
          ApplicationManager.getApplication()
            .invokeLater(new ResolveExternalInlineStyleSourceAction(reader, designerAppManager.getClient().getModule(reader.readInt())));
          break;

        case ServerMethod.showError:
          FlexUIDesignerApplicationManager.LOG.error(reader.readUTF());
          break;

        default:
          throw new IllegalArgumentException("unknown client command: " + command);
      }
    }
  }

  @Override
  public void close() throws IOException {
    if (reader != null) {
      reader.close();
    }
  }

  protected static class Reader extends DataInputStream {
    private Reader(InputStream in) {
      super(in);
    }

    public VirtualFile readFile() throws IOException {
      String url = readUTF();
      VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(url);
      if (file == null) {
        FlexUIDesignerApplicationManager.LOG.error("can't find file " + url);
      }

      return file;
    }
  }
}