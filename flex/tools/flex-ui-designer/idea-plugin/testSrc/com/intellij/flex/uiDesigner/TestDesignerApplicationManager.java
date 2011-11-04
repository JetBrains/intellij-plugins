package com.intellij.flex.uiDesigner;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.flex.uiDesigner.libraries.InitException;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.ShutDownTracker;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Consumer;
import org.picocontainer.MutablePicoContainer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

class TestDesignerApplicationManager {
  private static TestDesignerApplicationManager instance;
  private static boolean serviceImplementationChanged;

  private final ProcessHandler adlProcessHandler;

  private Socket socket;
  public final TestSocketInputHandler socketInputHandler;

  private TestDesignerApplicationManager() throws Exception {
    LibraryManager.getInstance().setAppDir(DesignerApplicationUtil.APP_DIR);

    final DesignerApplicationUtil.AdlRunConfiguration adlRunConfiguration = DesignerApplicationUtil.createTestAdlRunConfiguration();
    adlRunConfiguration.arguments = new ArrayList<String>();

    final ServerSocket serverSocket = new ServerSocket(0, 1);
    adlRunConfiguration.arguments.add(String.valueOf(serverSocket.getLocalPort()));
    adlRunConfiguration.arguments.add(String.valueOf(0));

    DesignerApplicationUtil.addTestPlugin(adlRunConfiguration.arguments);
    DesignerApplicationUtil.copyAppFiles();

    adlProcessHandler = DesignerApplicationUtil.runAdl(adlRunConfiguration, DebugPathManager.getFudHome() + '/' +
                                                                            DesignerApplicationUtil.DESCRIPTOR_XML_DEV_PATH,
                                                       DesignerApplicationUtil.APP_DIR.getPath(), new Consumer<Integer>() {
      @Override
      public void consume(Integer exitCode) {
        if (exitCode != 0) {
          try {
            serverSocket.close();
          }
          catch (IOException ignored) {
          }

          throw new AssertionError(DesignerApplicationUtil.describeAdlExit(exitCode, adlRunConfiguration));
        }
      }
    });

    ShutDownTracker.getInstance().registerShutdownTask(new Runnable() {
      @Override
      public void run() {
        if (socket != null) {
          try {
            socket.close();
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
        adlProcessHandler.destroyProcess();
      }
    });

    final Callable<Void> action = new Callable<Void>() {
      @Override
      public Void call() throws IOException {
        socket = serverSocket.accept();
        return null;
      }
    };

    // we can want to debug our flash app before socket accept
    if (Boolean.valueOf(System.getProperty("fud.test.debug"))) {
      action.call();
    }
    else {
      ApplicationManager.getApplication().executeOnPooledThread(action).get(30, TimeUnit.SECONDS);
    }

    changeServiceImplementation();
    Client.getInstance().setOut(socket.getOutputStream());

    socketInputHandler = (TestSocketInputHandler)SocketInputHandler.getInstance();
    socketInputHandler.init(socket.getInputStream(), DesignerApplicationUtil.APP_DIR);
  }

  static void changeServiceImplementation(Class key, Class implementation) {
    MutablePicoContainer picoContainer = (MutablePicoContainer) ApplicationManager.getApplication().getPicoContainer();
    picoContainer.unregisterComponent(key.getName());
    picoContainer.registerComponentImplementation(key.getName(), implementation);
  }

  public static void changeServiceImplementation() {
    if (serviceImplementationChanged) {
      return;
    }

    changeServiceImplementation(Client.class, TestClient.class);
    changeServiceImplementation(DocumentProblemManager.class, MyDocumentProblemManager.class);
    changeServiceImplementation(SocketInputHandler.class, TestSocketInputHandler.class);

    serviceImplementationChanged = true;
  }

  public XmlFile[] initLibrarySets(Module module, boolean requireLocalStyleHolder) throws IOException, InitException {
    final ProblemsHolder problemsHolder = new ProblemsHolder();
    XmlFile[] unregistedDocumentReferences = LibraryManager.getInstance().initLibrarySets(module, requireLocalStyleHolder, problemsHolder);
    assert problemsHolder.isEmpty();
    return unregistedDocumentReferences;
  }

  public static TestDesignerApplicationManager getInstance() throws Exception {
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
