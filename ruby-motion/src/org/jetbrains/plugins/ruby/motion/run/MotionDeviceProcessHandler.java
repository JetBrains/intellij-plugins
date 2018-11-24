package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.UnixProcessManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.util.PathUtil;
import com.intellij.util.Processor;
import com.jetbrains.cidr.execution.ExecutionResult;
import com.jetbrains.cidr.execution.ProcessHandlerWithPID;
import com.jetbrains.cidr.execution.deviceSupport.AMDevice;
import com.jetbrains.cidr.execution.deviceSupport.AMDeviceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.run.RubyProcessHandler;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

/**
 * @author Dennis.Ushakov
 */
public class MotionDeviceProcessHandler extends RubyProcessHandler implements DeviceProcessHandler, ProcessHandlerWithPID {
  private static final Logger LOG = Logger.getInstance(MotionDeviceProcessHandler.class);
  final ExecutionResult<String> myExecutableName = new ExecutionResult<>();
  final ExecutionResult<String> myLocalPath = new ExecutionResult<>();
  final ExecutionResult<String> myRemotePath = new ExecutionResult<>();
  final ExecutionResult<AMDevice> myDevice = new ExecutionResult<>();
  @NotNull private final GeneralCommandLine myCommandLine;

  protected MotionDeviceProcessHandler(@NotNull Process process,
                                       @NotNull GeneralCommandLine commandLine,
                                       @Nullable String runnerId) {
    super(process, commandLine.getCommandLineString(), RubyProcessHandler.getOutputEncoding(), runnerId);
    myCommandLine = commandLine;
  }

  @Override
  public void coloredTextAvailable(String text, Key attributes) {
    if (text.trim().endsWith(".ipa") && !myLocalPath.isDone()) {
      myLocalPath.set(text.replace(".ipa", ".app").trim());
      myExecutableName.set(text.substring(text.lastIndexOf("/") + 1, text.lastIndexOf(".")).replace("_spec", ""));
      super.coloredTextAvailable(text, attributes);
    } else if (text.startsWith("remote_app_path: ")) {
      myRemotePath.set(text.substring(text.indexOf(" ")).trim());
    } else if (!text.startsWith("debug_server_socket_path: ") && !text.startsWith("device_support_path: ")) {
      super.coloredTextAvailable(text, attributes);
    }
  }

  private void findDevice() {
    if (myDevice.isDone()) return;
    if (AMDeviceManager.getInstance().getConnectedDevices().isEmpty()) {
      LOG.warn("No connected devices");
      myDevice.set(null);
      return;
    }
    String deviceId = null;
    while (deviceId == null) {
      deviceId = getDeviceId();
    }
    final AMDevice device =  AMDeviceManager.getInstance().getDevice(deviceId);
    LOG.assertTrue(device != null, "Failed to find device by id: " + deviceId);
    myDevice.set(device);
  }

  @NotNull
  @Override
  public String getRemotePath() throws ExecutionException {
    return myRemotePath.get() + "/" + myExecutableName.get();
  }

  @NotNull
  @Override
  public String getLocalPath() throws ExecutionException {
    return PathUtil.getCanonicalPath(getWorkingDirectory() + "/" + myLocalPath.get() + "/" + myExecutableName.get());
  }

  @NotNull
  @Override
  public String getWorkingDirectory() {
    return myCommandLine.getWorkDirectory().getAbsolutePath();
  }

  @NotNull
  @Override
  public Map<String, String> getEnvironment() {
    return myCommandLine.getEnvironment();
  }

  @NotNull
  @Override
  public AMDevice getDevice() throws ExecutionException {
    findDevice();
    return myDevice.get();
  }

  @Override
  public int getPID() throws ExecutionException {
    return -1;
  }

  @Override
  public int getPID(long timeout) throws ExecutionException, TimeoutException {
    return -1;
  }


  private String getDeviceId() {
    final Ref<String> deviceId = new Ref<>();
    final int rakePid = UnixProcessManager.getProcessPid(myProcess);
    UnixProcessManager.processPSOutput(UnixProcessManager.getPSCmd(false, false), s -> {
      final Scanner scanner = new Scanner(s);
      final int ppid = scanner.nextInt();
      final int pid = scanner.nextInt();
      final String command = scanner.nextLine();
      if (ppid == rakePid && command.contains("deploy")) {
        final Scanner commandScanner = new Scanner(command);
        final String deploy = commandScanner.next();
        if (!commandScanner.hasNext()) return false;
        final String id = commandScanner.next();
        deviceId.set("-d".equals(id) ? commandScanner.next() : id);
        return true;
      }
      return false;
    });
    return deviceId.get();
  }
}
