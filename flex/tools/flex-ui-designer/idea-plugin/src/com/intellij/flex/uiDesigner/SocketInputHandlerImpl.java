package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.abc.MovieSymbolTranscoder;
import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.AmfOutputStream;
import com.intellij.flex.uiDesigner.io.ByteArrayOutputStreamEx;
import com.intellij.flex.uiDesigner.io.ImageUtil;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.application.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.FocusCommand;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.AppIcon;
import com.intellij.util.StringBuilderSpinAllocator;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

public class SocketInputHandlerImpl extends SocketInputHandler {
  private static final Logger LOG = Logger.getInstance(SocketInputHandlerImpl.class.getName());

  protected Reader reader;

  private File resultFile;
  private File appDir;

  private DataOutputStream errorOut;

  private final MessageBus messageBus;

  public SocketInputHandlerImpl() {
    messageBus = ApplicationManager.getApplication().getMessageBus();
  }

  protected void createReader(InputStream inputStream) {
    reader = new Reader(new BufferedInputStream(inputStream));
  }

  public void init(@NotNull InputStream inputStream, @NotNull File appDir) {    
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

  protected static boolean isFileBased(int command) {
    return command == ServerMethod.GET_RESOURCE_BUNDLE || command == ServerMethod.GET_BITMAP_DATA || command == ServerMethod.GET_SWF_DATA;
  }

  private boolean safeProcessCommand(int command) throws IOException {
    final String resultReadyFilename = isFileBased(command) ? reader.readUTF() : null;
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
      if (resultReadyFilename != null) {
        //noinspection ResultOfMethodCallIgnored
        new File(appDir, resultReadyFilename).createNewFile();
      }
    }

    return true;
  }

  protected boolean processCommand(int command) throws IOException {
    switch (command) {
      case ServerMethod.GO_TO_CLASS:
        goToClass();
        break;

      case ServerMethod.GET_RESOURCE_BUNDLE:
        getResourceBundle();
        break;

      case ServerMethod.GET_BITMAP_DATA:
        getBitmapData();
        break;

      case ServerMethod.GET_SWF_DATA:
        getSwfData();
        break;

      case ServerMethod.OPEN_FILE:
        openFile();
        break;

      case ServerMethod.OPEN_FILE_AND_FIND_XML_ATTRIBUTE_OR_TAG:
        openFileAndFindXmlAttributeOrTag();
        break;

      case ServerMethod.OPEN_DOCUMENT:
        openDocument();
        break;

      case ServerMethod.RESOLVE_EXTERNAL_INLINE_STYLE_DECLARATION_SOURCE:
        ApplicationManager.getApplication().invokeLater(new ResolveExternalInlineStyleSourceAction(reader, readModule()));
        break;

      case ServerMethod.UNREGISTER_DOCUMENT_FACTORIES:
        unregisterDocumentFactories();
        break;

      case ServerMethod.SHOW_ERROR:
        showError();
        break;

      case ServerMethod.CLOSE_PROJECT:
        Client.getInstance().unregisterProject(readProject());
        break;

      case ServerMethod.SAVE_PROJECT_WINDOW_BOUNDS:
        ProjectWindowBounds.save(readProject(), reader);
        break;

      case ServerMethod.SET_PROPERTY:
        setProperty();
        break;

      case ServerMethod.DOCUMENT_OPENED:
        messageBus.syncPublisher(MESSAGE_TOPIC).documentOpened();
        break;

      default:
        throw new IllegalArgumentException("unknown client command: " + command);
    }

    return true;
  }

  @NotNull
  private static XmlFile virtualFileToXmlFile(Project project, VirtualFile virtualFile) {
    Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
    assert document != null;
    final XmlFile psiFile;
    AccessToken token = ReadAction.start();
    try {
      psiFile = (XmlFile)PsiDocumentManager.getInstance(project).getPsiFile(document);
      assert psiFile != null;
    }
    finally {
      token.finish();
    }

    return psiFile;
  }

  private void setProperty() throws IOException {
    Project project = readProject();
    final DocumentFactoryManager.DocumentInfo info = DocumentFactoryManager.getInstance(project).getInfo(reader.readUnsignedShort());
    final XmlFile psiFile = virtualFileToXmlFile(project, info.getElement());
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

      case Amf3Types.STRING:
        value = reader.readUTF();
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

  private void openFileAndFindXmlAttributeOrTag() throws IOException {
    Project project = readProject();
    VirtualFile file = reader.readFile();
    int parentTextOffset = reader.readInt();

    final String name = reader.readUTF();

    final XmlFile psiFile = virtualFileToXmlFile(project, file);
    final XmlTag rootTag = psiFile.getRootTag();
    assert rootTag != null;

    final XmlTag parentTag = PsiTreeUtil.getParentOfType(rootTag.findElementAt(parentTextOffset - rootTag.getStartOffsetInParent()),
        XmlTag.class);
    assert parentTag != null;

    XmlAttribute attribute = parentTag.getAttribute(name);
    final int offset;
    if (attribute != null) {
      offset = attribute.getTextOffset();
    }
    else {
      XmlTag tag = parentTag.findFirstSubTag(name);
      assert tag != null;
      offset = tag.getTextOffset();
    }

    navigateToFile(new OpenFileDescriptor(project, file, offset));
  }

  private void openDocument() throws IOException {
    Project project = readProject();
    navigateToFile(new OpenFileDescriptor(project, DocumentFactoryManager.getInstance(project).getFile(reader.readUnsignedShort()),
      reader.readInt()));
  }

  private static void navigateToFile(final OpenFileDescriptor openFileDescriptor) {
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
    
    final int projectId = readEntityId();
    final String locale = reader.readUTF();
    final String bundleName = reader.readUTF();

    final ProjectInfo projectInfo = Client.getInstance().getRegisteredProjects().getNullableInfo(projectId);

    AccessToken token = ReadAction.start();
    try {
      PropertiesFile resourceBundleFile;
      if (projectInfo == null) {
        // project may be closed, but client is not closed yet (AppTest#testCloseAndOpenProject)
        LOG.warn("Skip getResourceBundle(" + locale + ", " + bundleName + ") due to cannot find project with id " + projectId);
        resourceBundleFile = null;
      }
      else {
        resourceBundleFile = LibraryManager.getInstance().getResourceBundleFile(locale, bundleName, projectInfo);
      }

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
          LOG.error("fileOut null due to FileNotFoundException");
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
        List<IProperty> properties = resourceBundleFile.getProperties();
        out.write(Amf3Types.DICTIONARY);
        out.writeUInt29((properties.size() << 1) | 1);
        out.write(0);
        for (IProperty property : properties) {
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
      LOG.error(e);
    }
    finally {
      token.finish();
    }
  }

  private void getBitmapData() throws IOException {
    initResultFile();

    ImageAssetInfo assetInfo = EmbedImageManager.getInstance().getInfo(reader.readUnsignedShort());
    BufferedImage image = ImageUtil.getImage(assetInfo.file, assetInfo.mimeType);
    FileOutputStream fileOut = new FileOutputStream(resultFile);
    try {
      ImageUtil.write(image, fileOut, assetInfo.mimeType, assetInfo.file);
    }
    finally {
      fileOut.close();
    }
  }

  private void getSwfData() throws IOException {
    initResultFile();

    SwfAssetInfo assetInfo = EmbedSwfManager.getInstance().getInfo(reader.readUnsignedShort());
    new MovieSymbolTranscoder().transcode(assetInfo.file, resultFile, assetInfo.symbolName);
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
    String userMessage = reader.readUTF();
    final String technicalMessage = reader.readUTF();
    final String title;
    final Attachment attachment;
    if (reader.readBoolean()) {
      Project project = readProject();
      final VirtualFile file = DocumentFactoryManager.getInstance(project).getFile(reader.readUnsignedShort());
      attachment = new Attachment(file.getPresentableUrl()  , LoadTextUtil.loadText(file).toString());
      title = FlexUIDesignerBundle.message("problem.opening.mxml.document.0", file.getName());
    }
    else {
      title = FlexUIDesignerBundle.message("problem.opening.mxml.document");
      attachment = null;
    }

    if (StringUtil.isEmpty(userMessage)) {
      userMessage = technicalMessage;
    }
    LOG.error(LogMessageEx.createEvent(userMessage, technicalMessage, title,
                                       FlexUIDesignerBundle.message("problem.opening.mxml.document.balloon.text"), attachment));
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
    if (errorOut != null) {
      errorOut.close();
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
        LOG.error("Can't find file " + url);
      }

      return file;
    }
  }
}