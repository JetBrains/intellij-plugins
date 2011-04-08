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
    readProcess:
    while (true) {
      final int command = reader.read();
      switch (command) {
        case -1:
          break readProcess;

        case ServerMethod.goToClass:
          final Module module = readModule();
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
          final Project project = readProject();
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
          ApplicationManager.getApplication().invokeLater(new ResolveExternalInlineStyleSourceAction(reader, readModule()));
          break;

        case ServerMethod.unregisterDocumentFactories:
          unregisterDocumentFactories();
          break;

        case ServerMethod.showError:
          FlexUIDesignerApplicationManager.LOG.error(reader.readUTF());
          break;

        case ServerMethod.saveProjectWindowBounds:
          ProjectWindowBounds.save(readProject(), reader);
          break;

        default:
          throw new IllegalArgumentException("unknown client command: " + command);
      }
    }
  }

  private Module readModule() throws IOException {
    return FlexUIDesignerApplicationManager.getInstance().getClient().getModule(reader.readUnsignedShort());
  }

  private Project readProject() throws IOException {
    return FlexUIDesignerApplicationManager.getInstance().getClient().getProject(reader.readUnsignedShort());
  }

  private void unregisterDocumentFactories() throws IOException {
    DocumentFactoryManager.getInstance(readProject()).unregister(reader.readIntArray());
  }

  @Override
  public void close() throws IOException {
    if (reader != null) {
      reader.close();
    }
  }

  static class Reader extends DataInputStream {
    Reader(InputStream in) {
      super(in);
    }

    public int[] readIntArray() throws IOException {
      skipBytes(1);
      int n = readUInt29() >> 1;
      int[] array = new int[n];
      skipBytes(1);
      for (int i = 0; i < n; i++) {
        array[i] = readInt();
      }

      return array;
    }

    private int readUInt29() throws IOException {
      int value;

      // Each byte must be treated as unsigned
      int b = readByte() & 0xFF;

      if (b < 128) {
        return b;
      }

      value = (b & 0x7F) << 7;
      b = readByte() & 0xFF;

      if (b < 128) {
        return (value | b);
      }

      value = (value | (b & 0x7F)) << 7;
      b = readByte() & 0xFF;

      if (b < 128) {
        return (value | b);
      }

      value = (value | (b & 0x7F)) << 8;
      b = readByte() & 0xFF;

      return (value | b);
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