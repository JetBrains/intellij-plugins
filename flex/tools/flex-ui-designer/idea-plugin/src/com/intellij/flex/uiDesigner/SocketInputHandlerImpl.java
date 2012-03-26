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
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Pair;
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
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("StaticFieldReferencedViaSubclass")
public class SocketInputHandlerImpl extends SocketInputHandler {
  protected static final Logger LOG = Logger.getInstance(SocketInputHandlerImpl.class.getName());

  public Reader getReader() {
    return reader;
  }

  protected Reader reader;

  private File resultFile;
  private File appDir;

  private final MessageBus messageBus;

  private final List<ActionCallback> callbacks = new ArrayList<ActionCallback>();
  private final IdPool callbackIdPool = new IdPool();

  public SocketInputHandlerImpl() {
    messageBus = ApplicationManager.getApplication().getMessageBus();
  }

  @Override
  public int addCallback(final @NotNull ActionCallback actionCallback) {
    final int callbackIndex = callbackIdPool.allocate();
    if (callbackIndex == callbacks.size()) {
      callbacks.add(actionCallback);
    }
    else {
      callbacks.set(callbackIndex, actionCallback);
    }

    actionCallback.doWhenProcessed(new Runnable() {
      @Override
      public void run() {
        callbacks.set(callbackIndex, null);
        callbackIdPool.dispose(callbackIndex);
      }
    });
    return callbackIndex + 1;
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
      if (command == -1) {
        break;
      }

      processCommandAndNotifyFileBased(command);
    }
  }

  protected static boolean isFileBased(int command) {
    return command >= ServerMethod.GET_RESOURCE_BUNDLE && command < 100 /* test methods */;
  }

  protected void processCommandAndNotifyFileBased(int command) throws IOException {
    final String resultReadyFilename = isFileBased(command) ? reader.readUTF() : null;
    try {
      processCommand(command);
    }
    catch (RuntimeException e) {
      if (!ApplicationManager.getApplication().isUnitTestMode()) {
        LOG.error(e);
      }
      else {
        throw e;
      }
    }
    catch (AssertionError e) {
      if (!ApplicationManager.getApplication().isUnitTestMode()) {
        LOG.error(e);
      }
      else {
        throw e;
      }
    }
    finally {
      if (resultReadyFilename != null) {
        //noinspection ResultOfMethodCallIgnored
        new File(appDir, resultReadyFilename).createNewFile();
      }
    }
  }

  protected void processCommand(int command) throws IOException {
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
        unregisterDocumentFactories();
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

      case ServerMethod.CALLBACK:
        executeCallback();
        break;

      default:
        throw new IllegalArgumentException("unknown client command: " + command);
    }
  }

  public void unregisterDocumentFactories() throws IOException {
    DocumentFactoryManager.getInstance().unregister(reader.readIntArray());
  }

  protected void executeCallback() throws IOException {
    int callbackIndex = reader.readUnsignedByte() - 1;
    boolean success = reader.readBoolean();
    ActionCallback callback = callbacks.get(callbackIndex);
    if (callback == null) {
      LOG.error("Callback #" + callbackIndex + " doesn't exists");
      return;
    }

    if (success) {
      callback.setDone();
    }
    else {
      callback.setRejected();
    }
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
        if ((state & Frame.ICONIFIED) == 0) {
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

    final boolean fromModuleSource = reader.readBoolean();
    final int moduleId = readEntityId();
    final String locale = reader.readUTF();
    final String bundleName = reader.readUTF();

    final ModuleInfo moduleInfo = Client.getInstance().getRegisteredModules().getNullableInfo(moduleId);
    PropertiesFile resourceBundle = null;
    int sourceId = -1;
    if (moduleInfo == null) {
      // project may be closed, but client is not closed yet (AppTest#testCloseAndOpenProject)
      LOG.warn("Skip getResourceBundle(" + locale + ", " + bundleName + ") due to cannot find module with id " + moduleId);
    }
    else {
      if (fromModuleSource) {
        resourceBundle = getResourceBundleFromModuleSource(moduleInfo.getModule(), bundleName);
        sourceId = moduleId;
      }
      else {
        Pair<PropertiesFile, Integer> bundleInfo = LibraryManager.getInstance().getResourceBundleFile(locale, bundleName, moduleInfo);
        if (bundleInfo != null) {
          resourceBundle = bundleInfo.first;
          sourceId = bundleInfo.second;
        }
      }
    }

    final FileOutputStream fileOut = new FileOutputStream(resultFile);
    final AccessToken token = ReadAction.start();
    try {
      writeResourceBundle(resourceBundle, fileOut, sourceId);
    }
    finally {
      token.finish();
      fileOut.close();
    }
  }

  private static PropertiesFile getResourceBundleFromModuleSource(Module module, final String bundleName) throws IOException {
    final AccessToken token = ReadAction.start();
    try {
      final PsiManager psiManager = PsiManager.getInstance(module.getProject());
      final List<VirtualFile> result = new ArrayList<VirtualFile>();
      FileBasedIndex.getInstance().processValues(FileTypeIndex.NAME, PropertiesFileType.INSTANCE, null,
                                                 new FileBasedIndex.ValueProcessor<Void>() {
                                                   public boolean process(VirtualFile file, Void value) {
                                                     if (file.getNameWithoutExtension().equals(bundleName)) {
                                                       result.add(file);
                                                       // todo IDEA-74868
                                                       if (file.getParent().getName().equals("en_US")) {
                                                         return false;
                                                       }
                                                     }

                                                     return true;
                                                   }
                                                 }, module.getModuleScope(false));

      PropertiesFile defaultResourceBundle = null;
      for (VirtualFile file : result) {
        PsiFile psiFile = psiManager.findFile(file);
        if (psiFile != null) {
          if (file.getParent().getName().equals("en_US")) {
            defaultResourceBundle = (PropertiesFile)psiFile;
          }
          else {
            return (PropertiesFile)psiFile;
          }
        }
      }

      return defaultResourceBundle;
    }
    finally {
      token.finish();
    }
  }

  private static void writeResourceBundle(PropertiesFile file, OutputStream out, int sourceId) throws IOException {
    if (file == null) {
      out.write(0);
      out.write(0);
      return;
    }

    final AmfOutputStream amfOut = new AmfOutputStream(new ByteArrayOutputStreamEx(4 * 1024));
    amfOut.writeShort(sourceId + 1);
    // todo Embed, ClassReference, but idea doesn't support it too
    final List<IProperty> properties = file.getProperties();
    amfOut.write(Amf3Types.DICTIONARY);
    amfOut.writeUInt29((properties.size() << 1) | 1);
    amfOut.write(0);
    for (IProperty property : properties) {
      amfOut.write(property.getUnescapedKey());
      amfOut.write(property.getUnescapedValue());
    }

    amfOut.getByteArrayOut().writeTo(out);
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

  public static class Reader extends DataInputStream {
    private static final ComponentColorModel COLOR_MODER = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                                                                                   new int[]{8, 8, 8}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);

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
          fileOut = new FileOutputStream(new File("/Users/develar/clientOut"));
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

    public BufferedImage readImage() throws IOException {
      final int w = readUnsignedShort();
      if (w == 0) {
        return null;
      }

      final int h = readUnsignedShort();
      int l = w * h * 4;
      final byte[] data = FileUtil.loadBytes(this, l);
      for (int i = 0, j = 0; i < l; i += 4) {
        data[j++] = data[i + 3];
        byte r = data[i + 1];
        data[j++] = data[i + 2];
        data[j++] = r;
      }

      return new BufferedImage(COLOR_MODER, Raster.createInterleavedRaster(new DataBufferByte(data, w * h * 3), w, h, w * 3, 3,
                                                                           new int[]{2, 1, 0}, null), false, null);
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