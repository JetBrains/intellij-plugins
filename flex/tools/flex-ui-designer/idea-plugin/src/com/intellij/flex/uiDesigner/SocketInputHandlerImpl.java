package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.AmfOutputStream;
import com.intellij.flex.uiDesigner.io.ByteArrayOutputStreamEx;
import com.intellij.flex.uiDesigner.io.ImageUtil;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.FocusCommand;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.AppIcon;
import com.intellij.util.StringBuilderSpinAllocator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

public class SocketInputHandlerImpl implements SocketInputHandler {
  private static final Logger LOG = Logger.getInstance(SocketInputHandlerImpl.class.getName());

  protected Reader reader;

  private File resultReadyFile;
  private File resultFile;
  private File appDir;

  private DataOutputStream errorOut;

  protected void createReader(InputStream inputStream) {
    reader = new Reader(new BufferedInputStream(inputStream));
  }

  public void init(@NotNull InputStream inputStream, @NotNull File appDir) throws IOException {
    resultReadyFile = new File(appDir, "d");
    if (resultReadyFile.exists()) {
      //noinspection ResultOfMethodCallIgnored
      resultReadyFile.delete();
    }
    
    createReader(inputStream);
    this.appDir = appDir;
  }

  @Override
  public void read(@NotNull InputStream inputStream, @NotNull File appDir) throws IOException {
    init(inputStream, appDir);
    process();
  }

  @Override
  public DataOutputStream getErrorOut() {
    return errorOut;
  }

  @Override
  public void setErrorOut(OutputStream out) {
    errorOut = new DataOutputStream(new BufferedOutputStream(out));
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

  private boolean safeProcessCommand(int command) throws IOException {
    try {
      return processCommand(command);
    }
    catch (RuntimeException e) {
      LOG.error(e);
    }
    catch (AssertionError e) {
      LOG.error(e);
    }
    finally {
      if (isFileBased(command)) {
        //noinspection ResultOfMethodCallIgnored
        resultReadyFile.createNewFile();
      }
    }

    return true;
  }

  protected boolean processCommand(int command) throws IOException {
    switch (command) {
      case ServerMethod.goToClass:
        goToClass();
        break;

      case ServerMethod.getResourceBundle:
        getResourceBundle();
        break;

      case ServerMethod.getBitmapData:
        getBitmapData();
        break;

      case ServerMethod.openFile:
        openFile();
        break;

      case ServerMethod.openDocument:
        openDocument();
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

      case ServerMethod.SET_PROPERTY:
        setProperty();
        break;

      default:
        throw new IllegalArgumentException("unknown client command: " + command);
    }

    return true;
  }

  private void setProperty() throws IOException {
    Project project = readProject();
    final DocumentFactoryManager.DocumentInfo info = DocumentFactoryManager.getInstance(project).getInfo(reader.readUnsignedShort());
    Document document = FileDocumentManager.getInstance().getDocument(info.getElement());
    assert document != null;
    final XmlFile psiFile;
    AccessToken token = ReadAction.start();
    try {
      psiFile = (XmlFile)PsiDocumentManager.getInstance(project).getPsiFile(document);
    }
    finally {
      token.finish();
    }

    assert psiFile != null;
    final XmlTag rootTag = psiFile.getRootTag();
    assert rootTag != null;
    final int offset = reader.readInt() - rootTag.getStartOffsetInParent();

    final String name = reader.readUTF();
    final String value;
    switch (reader.read()) {
      case Amf3Types.TRUE:
        value = "true";
        break;

      case Amf3Types.FALSE:
        value = "false";
        break;

      default:
        throw new IllegalArgumentException("unknown value type");
    }

    final XmlTag tag = PsiTreeUtil.getParentOfType(rootTag.findElementAt(offset), XmlTag.class);
    assert tag != null;

    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        AccessToken token = WriteAction.start();
        try {
          tag.setAttribute(name, value);
          info.psiModificationStamp = psiFile.getModificationStamp();
        }
        finally {
          token.finish();
        }
      }
    });
  }

  private void openFile() throws IOException {
    navigateToFile(new OpenFileDescriptor(readProject(), reader.readFile(), reader.readInt()));
  }

  private void openDocument() throws IOException {
    Project project = readProject();
    navigateToFile(new OpenFileDescriptor(project, DocumentFactoryManager.getInstance(project).getFile(reader.readUnsignedShort()),
      reader.readInt()));
  }

  private void navigateToFile(final OpenFileDescriptor openFileDescriptor) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        openFileDescriptor.navigate(true);
        focusProjectWindow(openFileDescriptor.getProject(), true);
      }
    });
  }

  public static void focusProjectWindow(final Project p, boolean executeIfAppInactive) {
    FocusCommand cmd = new FocusCommand() {
      @Override
      public ActionCallback run() {
        JFrame f = WindowManager.getInstance().getFrame(p);
        if (f != null) {
          f.toFront();
          f.requestFocus();
        }
        return new ActionCallback.Done();
      }
    };

    if (executeIfAppInactive) {
      AppIcon.getInstance().requestFocus((IdeFrame)WindowManager.getInstance().getFrame(p));
      cmd.run();
    } else {
      IdeFocusManager.getInstance(p).requestFocus(cmd, false);
    }
  }


  private void initResultFile() {
    if (resultFile == null) {
      resultFile = new File(appDir, "r");
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

          FileOutputStream fileOut = null;
          // IDEA-71568
          if (SystemInfo.isWindows) {
            for (int i = 0; i < 2; i++) {
              try {
                fileOut = new FileOutputStream(resultFile);
                break;
              }
              catch (FileNotFoundException e) {
                try {
                  Thread.sleep(10);
                }
                catch (InterruptedException ignored) {
                }
              }
            }

            if (fileOut == null) {
              FlexUIDesignerApplicationManager.LOG.error("fileOut null due to FileNotFoundException");
              return;
            }
          }
          else {
            fileOut = new FileOutputStream(resultFile);
          }

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
    final String userMessage = reader.readUTF();
    final String message = reader.readUTF();
    final String logMessage;
    Project project = null;
    if (reader.readBoolean()) {
      StringBuilder builder = StringBuilderSpinAllocator.alloc();
      try {
        project = readProject();
        final VirtualFile file = DocumentFactoryManager.getInstance(project).getFile(reader.readUnsignedShort());
        builder.append(message);
        builder.append("\nFile: ").append(file.getPath()).append("\nFile content: \n").append(LoadTextUtil.loadText(file));
        logMessage = builder.toString();
      }
      finally {
        StringBuilderSpinAllocator.dispose(builder);
      }
    }
    else {
      logMessage = message;
    }

    FlexUIDesignerApplicationManager.LOG.error(logMessage, new Throwable() {
      @Override
      public void printStackTrace(PrintStream s) {
      }

      @Override
      public Throwable fillInStackTrace() {
        return this;
      }
    });

    if (!userMessage.isEmpty()) {
      DocumentProblemManager.getInstance().report(project, userMessage + "<br><br>" + message);
    }
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