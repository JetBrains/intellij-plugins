package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.*;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.StringBuilderSpinAllocator;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

public class SocketInputHandlerImpl implements SocketInputHandler {
  protected Reader reader;

  private File resultReadyFile;
  private File resultFile;
  private File appDir;

  protected void createReader(InputStream inputStream) {
    reader = new Reader(new BufferedInputStream(inputStream));
  }

  public void init(@NotNull InputStream inputStream, @NotNull File appDir) throws IOException {
    createReader(inputStream);
    this.appDir = appDir;
  }

  @Override
  public void read(@NotNull InputStream inputStream, @NotNull File appDir) throws IOException {
    init(inputStream, appDir);
    process();
  }

  public void process() throws IOException {
    while (true) {
      final int command = reader.read();
      if (command == -1 || !safeProcessCommand(command)) {
        break;
      }
    }
  }

  protected boolean isFileBased(int command) {
    return command == ServerMethod.getResourceBundle || command == ServerMethod.getBitmapData;
  }

  protected boolean safeProcessCommand(int command) throws IOException {
    try {
      return processCommand(command);
    }
    finally {
      if (isFileBased(command)) {
        //noinspection ResultOfMethodCallIgnored
        resultReadyFile.createNewFile();
      }
    }
  }

  protected boolean processCommand(int command) throws IOException {
    switch (command) {
      case ServerMethod.goToClass:
        goToClass();
        break;

      case ServerMethod.getResourceBundle:
        try {
          getResourceBundle();
        }
        finally {
          //noinspection ResultOfMethodCallIgnored
          resultReadyFile.createNewFile();
        }
        break;

      case ServerMethod.getBitmapData:
        try {
          getBitmapData();
        }
        finally {
          //noinspection ResultOfMethodCallIgnored
          resultReadyFile.createNewFile();
        }
        break;

      case ServerMethod.openFile:
        openFile();
        break;

      case ServerMethod.resolveExternalInlineStyleDeclarationSource:
        ApplicationManager.getApplication().invokeLater(new ResolveExternalInlineStyleSourceAction(reader, readModule()));
        break;

      case ServerMethod.unregisterDocumentFactories:
        unregisterDocumentFactories();
        break;

      case ServerMethod.showError:
        showError();
        break;

      case ServerMethod.closeProject:
        Client.getInstance().unregisterProject(readProject());
        break;

      case ServerMethod.saveProjectWindowBounds:
        ProjectWindowBounds.save(readProject(), reader);
        break;

      default:
        throw new IllegalArgumentException("unknown client command: " + command);
    }

    return true;
  }

  private void openFile() throws IOException {
    final Project project = readProject();
    final OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, reader.readFile(), reader.readInt());
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        openFileDescriptor.navigate(true);
        ProjectUtil.focusProjectWindow(project, true);
      }
    });
  }

  private void initResultFile() {
    if (resultFile == null) {
      resultReadyFile = new File(appDir, "d");
      resultFile = new File(appDir, "r");
      
      resultFile.deleteOnExit();
      resultReadyFile.deleteOnExit();
    }
  }

  private void getResourceBundle() throws IOException {
    initResultFile();

    final ProjectInfo projectInfo = Client.getInstance().getRegisteredProjects().getInfo(readEntityId());
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      @Override
      public void run() {
        try {
          PropertiesFile resourceBundleFile =
            LibraryManager.getInstance().getResourceBundleFile(reader.readUTF(), reader.readUTF(), projectInfo);
          FileOutputStream fileOut = new FileOutputStream(resultFile);
          try {
            if (resourceBundleFile == null) {
              fileOut.write(Amf3Types.NULL);
              return;
            }

            final AmfOutputStream out = new AmfOutputStream(new ByteArrayOutputStreamEx(4 * 1024));
            // todo Embed, ClassReference, but idea doesn't support it too
            List<Property> properties = resourceBundleFile.getProperties();
            out.write(Amf3Types.DICTIONARY);
            out.writeUInt29((properties.size() << 1) | 1);
            out.write(0);
            for (Property property : properties) {
              out.write(property.getUnescapedKey());
              out.write(property.getUnescapedValue());
            }

            out.getByteArrayOut().writeTo(fileOut);
          }
          finally {
            fileOut.close();
          }
        }
        catch (IOException e) {
          FlexUIDesignerApplicationManager.LOG.error(e);
        }
      }
    });
  }

  private void getBitmapData() throws IOException {
    initResultFile();

    AssetInfo assetInfo = BinaryFileManager.getInstance().getInfo(reader.readUnsignedShort());
    BufferedImage image = ImageUtil.getImage(assetInfo.getElement(), assetInfo.mimeType);
    FileOutputStream fileOut = new FileOutputStream(resultFile);
    try {
      ImageUtil.write(image, fileOut, assetInfo.mimeType, assetInfo.getElement());
    }
    finally {
      fileOut.close();
    }
  }

  private void goToClass() throws IOException {
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
  }

  private void showError() throws IOException {
    final String message;
    if (reader.readBoolean()) {
      StringBuilder builder = StringBuilderSpinAllocator.alloc();
      try {
        Project project = readProject();
        final VirtualFile file = DocumentFactoryManager.getInstance(project).getFile(reader.readUnsignedShort());
        builder.append(reader.readUTF());
        builder.append("\nFile: ").append(file.getPath()).append("\nFile content: \n").append(LoadTextUtil.loadText(file));
        message = builder.toString();
      }
      finally {
        StringBuilderSpinAllocator.dispose(builder);
      }
    }
    else {
      message = reader.readUTF();
    }

    FlexUIDesignerApplicationManager.LOG.error(message, new Throwable() {
      @Override
      public void printStackTrace(PrintStream s) {
      }

      @Override
      public Throwable fillInStackTrace() {
        return this;
      }
    });
  }

  private Module readModule() throws IOException {
    return Client.getInstance().getModule(readEntityId());
  }

  private Project readProject() throws IOException {
    return Client.getInstance().getProject(readEntityId());
  }

  private int readEntityId() throws IOException {
    return reader.readUnsignedShort();
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
      int b;
      if ((b = readByte() & 0xFF) < 128) {
        return b;
      }

      value = (b & 0x7F) << 7;
      if ((b = readByte() & 0xFF) < 128) {
        return (value | b);
      }

      value = (value | (b & 0x7F)) << 7;
      if ((b = readByte() & 0xFF) < 128) {
        return value | b;
      }

      return (value | (b & 0x7F)) << 8 | (readByte() & 0xFF);
    }

    public VirtualFile readFile() throws IOException {
      String url = readUTF();
      VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(url);
      if (file == null) {
        FlexUIDesignerApplicationManager.LOG.error("Can't find file " + url);
      }

      return file;
    }
  }
}