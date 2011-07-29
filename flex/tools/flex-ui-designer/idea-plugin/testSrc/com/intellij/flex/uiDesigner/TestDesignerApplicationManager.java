package com.intellij.flex.uiDesigner;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.ShutDownTracker;
import com.intellij.util.Consumer;
import gnu.trove.THashMap;
import org.picocontainer.MutablePicoContainer;

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

  public final TestSocketInputHandler socketInputHandler;

  private TestDesignerApplicationManager() throws IOException {
    DesignerApplicationUtil.AdlRunConfiguration adlRunConfiguration = DesignerApplicationUtil.createTestAdlRunConfiguration();

    adlRunConfiguration.arguments = new ArrayList<String>();

    final ServerSocket serverSocket = new ServerSocket(0, 1);
    adlRunConfiguration.arguments.add(String.valueOf(serverSocket.getLocalPort()));
    adlRunConfiguration.arguments.add(String.valueOf(0));

    FlexUIDesignerApplicationManager.addTestPlugin(adlRunConfiguration.arguments);
    FlexUIDesignerApplicationManager.copyAppFiles();

    adlProcessHandler =
      DesignerApplicationUtil.runAdl(adlRunConfiguration, DebugPathManager.getFudHome() + "/designer/src/main/resources/descriptor.xml",
                                     FlexUIDesignerApplicationManager.APP_DIR.getPath(), new Consumer<Integer>() {
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
    socketInputHandler.init(socket.getInputStream(), FlexUIDesignerApplicationManager.APP_DIR);
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
    LibraryManager.getInstance().initLibrarySets(module, FlexUIDesignerApplicationManager.APP_DIR, requireLocalStyleHolder, sdkLibrarySet);
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
