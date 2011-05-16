package com.intellij.flex.uiDesigner;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.ShutDownTracker;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.Consumer;
import gnu.trove.THashMap;
import org.picocontainer.MutablePicoContainer;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class TestDesignerApplicationManager {
  private static TestDesignerApplicationManager instance;

  private final ProcessHandler adlProcessHandler;

  private final Socket socket;
  private final THashMap<String, LibrarySet> sdkLibrarySetCache = new THashMap<String, LibrarySet>();

  private File appDir;
  public final TestSocketInputHandler socketInputHandler;

  private TestDesignerApplicationManager() throws IOException {
    DesignerApplicationUtil.AdlRunConfiguration adlRunConfiguration =
      new DesignerApplicationUtil.AdlRunConfiguration(System.getProperty("fud.adl"), System.getProperty("fud.air"));

    adlRunConfiguration.arguments = new ArrayList<String>();
    adlRunConfiguration.arguments.add("-p");
    String fudHome = DebugPathManager.getFudHome();
    adlRunConfiguration.arguments.add(fudHome + "/test-app-plugin/target/test-1.0-SNAPSHOT.swf");

    adlRunConfiguration.arguments.add("-cdd");
    adlRunConfiguration.arguments.add(fudHome + "/flex-injection/target");

    initAppRootDir();
    final ServerSocket serverSocket = new ServerSocket(0, 1);
    adlProcessHandler = DesignerApplicationUtil.runAdl(adlRunConfiguration, fudHome + "/designer/src/main/resources/descriptor.xml",
                                                       serverSocket.getLocalPort(), appDir.getPath(), new Consumer<Integer>() {
      @Override
      public void consume(Integer exitCode) {
        if (exitCode != 0) {
          try {
            serverSocket.close();
          }
          catch (IOException ignored) {
          }

          throw new AssertionError("adl return " + exitCode);
        }
      }
    });

    socket = serverSocket.accept();

    ShutDownTracker.getInstance().registerShutdownTask(new Runnable() {
      @Override
      public void run() {
        adlProcessHandler.destroyProcess();
        try {
          socket.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    
    changeServiceImplementation();
    Client.getInstance().setOut(socket.getOutputStream());

    socketInputHandler = (TestSocketInputHandler)ServiceManager.getService(SocketInputHandler.class);
    socketInputHandler.init(socket.getInputStream(), appDir);
  }

  static void changeServiceImplementation(Class key, Class implementation) {
    MutablePicoContainer picoContainer = (MutablePicoContainer) ApplicationManager.getApplication().getPicoContainer();
    picoContainer.unregisterComponent(key.getName());
    picoContainer.registerComponentImplementation(key.getName(), implementation);
  }

  private static boolean serviceImplementationChanged;
  public static void changeServiceImplementation() {
    if (serviceImplementationChanged) {
      return;
    }

    changeServiceImplementation(Client.class, TestClient.class);
    changeServiceImplementation(DocumentProblemManager.class, MyDocumentProblemManager.class);
    changeServiceImplementation(SocketInputHandler.class, TestSocketInputHandler.class);

    serviceImplementationChanged = true;
  }

  public void initLibrarySets(Module module, boolean requireLocalStyleHolder, String sdkName) throws IOException, InitException {
    LibrarySet sdkLibrarySet = sdkLibrarySetCache.get(sdkName);
    LibraryManager.getInstance().initLibrarySets(module, appDir, requireLocalStyleHolder, sdkLibrarySet);
    if (sdkLibrarySet == null) {
      sdkLibrarySetCache.put(sdkName, Client.getInstance().getRegisteredProjects().getInfo(module.getProject()).getSdkLibrarySet());
    }
  }

  public static TestDesignerApplicationManager getInstance() throws IOException {
    if (instance == null) {
      instance = new TestDesignerApplicationManager();
    }
    return instance;
  }

  public static String[] getLastProblems() {
    return ((MyDocumentProblemManager)DocumentProblemManager.getInstance()).getProblems();
  }

  public File getAppDir() {
    return appDir;
  }

  public static void copySwfAndDescriptor(final File rootDir) throws IOException {
    //noinspection ResultOfMethodCallIgnored
    rootDir.mkdirs();
    FileUtil.copy(new File(DebugPathManager.getFudHome(), "app-loader/target/app-loader-1.0-SNAPSHOT.swf"), new File(rootDir, FlexUIDesignerApplicationManager.DESIGNER_SWF));
    FileUtil.copy(new File(DebugPathManager.getFudHome(), "designer/src/main/resources/descriptor.xml"), new File(rootDir, FlexUIDesignerApplicationManager.DESCRIPTOR_XML));
  }

  private void initAppRootDir() throws IOException {
    appDir = new File(PathManager.getSystemPath(), "flexUIDesigner");
    if (!appDir.exists() || SystemInfo.isWindows) {
      copySwfAndDescriptor(appDir);
    }
  }

  static class MyDocumentProblemManager extends DocumentProblemManager {
    private final List<String> problems = new ArrayList<String>();

    public String[] getProblems() {
      final String[] strings = problems.toArray(new String[problems.size()]);
      problems.clear();
      return strings;
    }

    @Override
    public void report(Project project, String message, MessageType messageType) {
      problems.add(message);
    }
  }
}
