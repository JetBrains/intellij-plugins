package com.jetbrains.maya;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunContentExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.BufferedOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author traff
 */
public class SendToMayaCommand {
  private static final Logger LOG = Logger.getInstance(SendToMayaCommand.class);

  private Project myProject;
  private String myScriptText = null;
  private VirtualFile myFile = null;

  private final int myPythonCommandPort;

  private final static String PY_CMD_TEMPLATE =
    "import traceback\nimport sys\nimport __main__\ntry:\n\texec('''%s''', __main__.__dict__, __main__.__dict__)\nexcept:\n\ttraceback.print_exc()\nsys.stdout.write(%s)";

  private final static String PY_FILE_TEMPLATE =
    "import traceback\nimport sys\nimport __main__\ntry:\n\texecfile('''%s''', __main__.__dict__, __main__.__dict__)\nexcept:\n\ttraceback.print_exc()\nsys.stdout.write(%s)";

  private final static String TERMINATION_STRING = "'%c\\n'%26";


  public SendToMayaCommand(Project project, int port) {
    myProject = project;
    myPythonCommandPort = port;
  }

  public void run() {
    try {
      final ProcessHandler process = createRunInMayaProcessHandler();

      new RunContentExecutor(myProject, process)
        .withTitle(getTitle())
        .withRerun(() -> this.run())
        .withStop(() -> process.destroyProcess(), () -> !process.isProcessTerminated()
        )
        .run();
    }
    catch (ExecutionException e) {
      Messages.showErrorDialog(myProject, e.getMessage(), getTitle());
    }
  }

  private ProcessHandler createRunInMayaProcessHandler() throws ExecutionException {


    try {
      final Socket socket = new Socket("127.0.0.1", myPythonCommandPort);
      final SocketProcessHandler processHandler = new SocketProcessHandler(socket, getTitle());
      try {
        PrintWriter writer = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));

        if (myScriptText != null) {
          String[] lines = getScriptLines();
          writeLines(writer, lines);

          processHandler.notifyTextAvailable(
            "Sent " + lines.length + " line" + (lines.length != 1 ? "s" : "") + " to command port " + myPythonCommandPort + "\n",
            ProcessOutputTypes.SYSTEM);
        }
        else {
          writeFile(writer, myFile);
          processHandler.notifyTextAvailable(
            "Sent " + myFile.getPath() + " to command port " + myPythonCommandPort + "\n", ProcessOutputTypes.SYSTEM);
        }

        writer.flush();
      }
      catch (Exception e) {
        if (!socket.isClosed()) {
          socket.close();
        }
        throw new ExecutionException(e.getMessage());
      }

      return processHandler;
    }
    catch (Exception e) {
      throw new ExecutionException(e.getMessage());
    }
  }

  private static void writeLines(PrintWriter writer, String[] lines) {
    writer.print(String.format(PY_CMD_TEMPLATE, StringUtil.join(lines, "\n"), TERMINATION_STRING));
  }

  private static void writeFile(PrintWriter writer, VirtualFile file) {
    writer.print(String.format(PY_FILE_TEMPLATE, file.getPath(), TERMINATION_STRING));
  }

  public String[] getScriptLines() {
    return StringUtil.splitByLines(myScriptText);
  }

  public String getTitle() {
    return "Send to Maya";
  }

  public SendToMayaCommand withSelectionText(String selectionText) {
    myScriptText = selectionText;
    return this;
  }

  public SendToMayaCommand withFile(VirtualFile file) {
    myFile = file;
    return this;
  }
}
