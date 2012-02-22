package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.abc.EntireMovieTranscoder;
import com.intellij.flex.uiDesigner.abc.MovieSymbolTranscoder;
import com.intellij.flex.uiDesigner.io.*;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.PropertiesFileType;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.AppIcon;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.util.List;

public class SocketInputHandlerImpl extends SocketInputHandler {
  protected static final Logger LOG = Logger.getInstance(SocketInputHandlerImpl.class.getName());

  protected Reader reader;

  private File resultFile;
  private File appDir;

  private final MessageBus messageBus;

  public SocketInputHandlerImpl() {
    messageBus = ApplicationManager.getApplication().getMessageBus();
  }

  protected void createReader(InputStream inputStream) {
    reader = new Reader(new BufferedInputStream(inputStream));
  }

  protected void init(@NotNull InputStream inputStream, @NotNull File appDir) {
    createReader(inputStream);
    this.appDir = appDir;
  }

  @Override
  public void read(@NotNull InputStream inputStream, @NotNull File appDir) throws IOException {
    init(inputStream, appDir);
    if (processOnRead()) {
      process();
    }
  }

  protected boolean processOnRead() {
    return true;
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
    return command >= ServerMethod.GET_RESOURCE_BUNDLE && command < 100 /* test methods */;
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

      case ServerMethod.GET_EMBED_SWF_ASSET_INFO:
      case ServerMethod.GET_EMBED_IMAGE_ASSET_INFO:
        getAssetInfo(command == ServerMethod.GET_EMBED_SWF_ASSET_INFO);
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
        DocumentFactoryManager.getInstance().unregister(reader.readIntArray());
        break;

      case ServerMethod.UNREGISTER_LIBRARY_SETS:
        LibraryManager.getInstance().unregister(reader.readIntArray());
        break;

      case ServerMethod.SHOW_ERROR:
        showError();
        break;

      case ServerMethod.LOG_WARNING:
        logWarning();
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

      case ServerMethod.DOCUMENT_RENDERED:
        documentRendered();
        break;

      default:
        throw new IllegalArgumentException("unknown client command: " + command);
    }

    return true;
  }

  private void documentRendered() throws IOException {
    final int id = reader.readUnsignedShort();
    final int w = reader.readUnsignedShort();
    final BufferedImage image;
    if (w != 0) {
      final int h = reader.readUnsignedShort();

      int l = w * h * 4;
      final byte[] argb = FileUtil.loadBytes(reader, l);
      final byte[] bgr = new byte[w * h * 3];
      for (int i = 0, j = 0; i < l; i += 4) {
        bgr[j++] = argb[i + 3];
        bgr[j++] = argb[i + 2];
        bgr[j++] = argb[i + 1];
      }

      int[] nBits = {8, 8, 8};
      int[] bOffs = {2, 1, 0};
      final ComponentColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), nBits, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
      image = new BufferedImage(colorModel, Raster.createInterleavedRaster(new DataBufferByte(bgr, bgr.length), w, h, w * 3, 3, bOffs, null), false, null);
    }
    else {
      image = null;
    }

    messageBus.syncPublisher(MESSAGE_TOPIC).documentRendered(DocumentFactoryManager.getInstance().getInfo(id), image);
  }

  @NotNull
  private static XmlFile virtualFileToXmlFile(Project project, VirtualFile virtualFile) {
    final AccessToken token = ReadAction.start();
    try {
      XmlFile psiFile = (XmlFile)PsiManager.getInstance(project).findFile(virtualFile);
      assert psiFile != null;
      return psiFile;
    }
    finally {
      token.finish();
    }
  }

  private void setProperty() throws IOException {
    final Project project = readProject();
    final DocumentFactoryManager.DocumentInfo info = DocumentFactoryManager.getInstance().getInfo(reader.readUnsignedShort());
    final XmlFile psiFile = virtualFileToXmlFile(project, info.getElement());
    final XmlTag rootTag = psiFile.getRootTag();
    assert rootTag != null;
    final int offset = info.getRangeMarker(reader.readInt()).getStartOffset() - rootTag.getStartOffsetInParent();
    final Document document = FileDocumentManager.getInstance().getDocument(info.getElement());
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

    final XmlTag tag;
    final AccessToken token = ReadAction.start();
    try {
      tag = PsiTreeUtil.getParentOfType(rootTag.findElementAt(offset), XmlTag.class);
      assert tag != null;
    }
    finally {
      token.finish();
    }
    
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        final AccessToken token = WriteAction.start();
        try {
          tag.setAttribute(name, value);
          assert document != null;
          info.documentModificationStamp = document.getModificationStamp();
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
    int documentFactoryId = reader.readUnsignedShort();
    int textOffset = reader.readInt();
    boolean activateApp = reader.readBoolean();
    DocumentFactoryManager.DocumentInfo info = DocumentFactoryManager.getInstance().getInfo(documentFactoryId);
    navigateToFile(new OpenFileDescriptor(project, info.getElement(), info.getRangeMarker(textOffset).getStartOffset()),
                   activateApp);
  }

  private static void navigateToFile(final OpenFileDescriptor openFileDescriptor) {
    navigateToFile(openFileDescriptor, true);
  }

  private static void navigateToFile(final OpenFileDescriptor openFileDescriptor, final boolean activateApp) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        openFileDescriptor.navigate(true);
        focusProjectWindow(openFileDescriptor.getProject(), activateApp);
      }
    });
  }

  public static void focusProjectWindow(final Project p, final boolean activateApp) {
    final JFrame projectFrame = WindowManager.getInstance().getFrame(p);
    if (projectFrame == null) {
      return;
    }

    // IdeFocusManager is not working correctly. If we open 2 projects, select another project, select some component in ADL and navigate to project, first project frame must be focused
    projectFrame.toFront();
    projectFrame.requestFocus();

    if (activateApp) {
      // is not working for windows
      if (SystemInfo.isWindows) {
        int state = projectFrame.getExtendedState();
        if ((state & JFrame.ICONIFIED) == 0) {
          // http://stackoverflow.com/questions/309023/howto-bring-a-java-window-to-the-front
          projectFrame.setExtendedState(JFrame.ICONIFIED);
          projectFrame.setExtendedState(state);
        }
        else {
          projectFrame.setExtendedState(state &= ~JFrame.ICONIFIED);
        }
      }
      else {
        AppIcon.getInstance().requestFocus((IdeFrame)WindowManager.getInstance().getFrame(p));
      }
    }
  }

  private void initResultFile() {
    if (resultFile == null) {
      resultFile = new File(appDir, "r");
    }
  }

  private void getResourceBundle() throws IOException {
    initResultFile();
    
    final int moduleId = readEntityId();
    final String locale = reader.readUTF();
    final String bundleName = reader.readUTF();

    final ModuleInfo moduleInfo = Client.getInstance().getRegisteredModules().getNullableInfo(moduleId);
    final AccessToken token = ReadAction.start();
    try {
      final PropertiesFile resourceBundleFile;
      if (moduleInfo == null) {
        // project may be closed, but client is not closed yet (AppTest#testCloseAndOpenProject)
        LOG.warn("Skip getResourceBundle(" + locale + ", " + bundleName + ") due to cannot find project with id " + moduleId);
        resourceBundleFile = null;
      }
      else {
        final PsiManager psiManager = PsiManager.getInstance(moduleInfo.getModule().getProject());
        final Ref<PropertiesFile> result = new Ref<PropertiesFile>();
        FileBasedIndex.getInstance().processValues(FileTypeIndex.NAME, PropertiesFileType.INSTANCE, null,
                                                   new FileBasedIndex.ValueProcessor<Void>() {
                                                     public boolean process(VirtualFile file, Void value) {
                                                       if (file.getNameWithoutExtension().equals(bundleName)) {
                                                         PsiFile psiFile = psiManager.findFile(file);
                                                         if (psiFile != null) {
                                                           result.set((PropertiesFile)psiFile);
                                                           return false;
                                                         }
                                                       }

                                                       return true;
                                                     }
                                                   }, moduleInfo.getModule().getModuleContentScope());

        resourceBundleFile =
          result.get() != null ? result.get() : LibraryManager.getInstance().getResourceBundleFile(locale, bundleName, moduleInfo);
      }

      final FileOutputStream fileOut = new FileOutputStream(resultFile);
      try {
        if (resourceBundleFile == null) {
          fileOut.write(Amf3Types.NULL);
          return;
        }

        final AmfOutputStream out = new AmfOutputStream(new ByteArrayOutputStreamEx(4 * 1024));
        // todo Embed, ClassReference, but idea doesn't support it too
        final List<IProperty> properties = resourceBundleFile.getProperties();
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

    final ImageAssetInfo assetInfo = EmbedImageManager.getInstance().getInfo(reader.readUnsignedShort());
    final FileOutputStream fileOut = new FileOutputStream(resultFile);
    try {
      ImageUtil.write(assetInfo.file, assetInfo.mimeType, fileOut);
    }
    catch (IOException e) {
      final String userMessage = FlashUIDesignerBundle.message("problem.opening.0", assetInfo.file.getName());
      LOG.error(LogMessageUtil.createEvent(userMessage, ExceptionUtil.getThrowableText(e), assetInfo.file));
      fileOut.getChannel().truncate(0);
    }
    finally {
      fileOut.close();
    }
  }

  private void getSwfData() throws IOException {
    initResultFile();

    final SwfAssetInfo assetInfo = EmbedSwfManager.getInstance().getInfo(reader.readUnsignedShort());
    if (assetInfo.symbolName == null) {
      new EntireMovieTranscoder().transcode(assetInfo.file, resultFile);
    }
    else {
      new MovieSymbolTranscoder().transcode(assetInfo.file, resultFile, assetInfo.symbolName);
    }
  }

  private void getAssetInfo(final boolean isSwf) throws IOException {
    initResultFile();

    final int assetId = reader.readUnsignedShort();
    final EmbedAssetInfo assetInfo = isSwf
                                     ? EmbedSwfManager.getInstance().getInfo(assetId)
                                     : EmbedImageManager.getInstance().getInfo(assetId);
    final ByteArrayOutputStreamEx byteOut = new ByteArrayOutputStreamEx(1024);
    final PrimitiveAmfOutputStream out = new PrimitiveAmfOutputStream(byteOut);
    Client.writeVirtualFile(assetInfo.file, out);
    out.writeNullableString(assetInfo instanceof SwfAssetInfo ? ((SwfAssetInfo)assetInfo).symbolName : null);

    final FileOutputStream fileOut = new FileOutputStream(resultFile);
    try {
      byteOut.writeTo(fileOut);
    }
    catch (IOException e) {
      final String userMessage = FlashUIDesignerBundle.message("problem.opening.0", assetInfo.file.getName());
      LOG.error(LogMessageUtil.createEvent(userMessage, ExceptionUtil.getThrowableText(e), assetInfo.file));
      fileOut.getChannel().truncate(0);
    }
    finally {
      out.close();
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
    messageBus.syncPublisher(MESSAGE_TOPIC).errorOccured();
    
    String userMessage = reader.readUTF();
    final String technicalMessage = reader.readUTF();
    final VirtualFile file;
    if (reader.readBoolean()) {
      file = DocumentFactoryManager.getInstance().getFile(reader.readUnsignedShort());
    }
    else {
      file = null;
    }

    if (StringUtil.isEmpty(userMessage)) {
      userMessage = technicalMessage;
    }
    LOG.error(LogMessageUtil.createEvent(userMessage, technicalMessage, file));
  }

  private void logWarning() throws IOException {
    LOG.warn(reader.readUTF());
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

  @Override
  public void dispose() {
    if (reader != null) {
      try {
        reader.close();
      }
      catch (IOException ignored) {
      }
    }
  }

  static class Reader extends DataInputStream {
    Reader(InputStream in) {
      super(in);
      //super(new InputStreamDumper(in));
    }

    @SuppressWarnings("UnusedDeclaration")
    static class InputStreamDumper extends InputStream {
      final FileOutputStream fileOut;
      private InputStream in;

      InputStreamDumper(InputStream in) {
        this.in = in;

        try {
          fileOut = new FileOutputStream(new File( "/Users/develar/clientOut"));
        }
        catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void close() throws IOException {
        super.close();
        fileOut.close();
      }

      @Override
      public int read() throws IOException {
        int read = in.read();
        fileOut.write(read);
        return read;
      }

      @Override
      public int read(byte[] b) throws IOException {
        byte[] bytes = new byte[b.length];
        int length = super.read(bytes);
        fileOut.write(bytes, 0, length);
        System.arraycopy(bytes, 0, b, 0, length);
        return length;
      }

      @Override
      public int read(byte[] b, int off, int len) throws IOException {
        byte[] bytes = new byte[len];
        int length = super.read(bytes, off, len);
        fileOut.write(bytes, 0, length);
        System.arraycopy(bytes, 0, b, off, length);
        return length;
      }
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