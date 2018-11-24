package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.lang.javascript.flex.actions.ExternalTask;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.text.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class DeviceInfo {
  public final int IOS_HANDLE;
  public final String IOS_DEVICE_CLASS;
  public final String DEVICE_ID;
  public final String DEVICE_NAME;

  private DeviceInfo(final int iosHandle, final String iosDeviceClass, final String deviceId, final String deviceName) {
    IOS_HANDLE = iosHandle;
    IOS_DEVICE_CLASS = iosDeviceClass;
    DEVICE_ID = deviceId;
    DEVICE_NAME = deviceName;
  }

  public static List<DeviceInfo> getAndroidDevices(final Project project, final Sdk sdk) {
    final List<DeviceInfo> result = new ArrayList<>();

    ExternalTask.runWithProgress(new ExternalTask(project, sdk) {
      protected List<String> createCommandLine() {
        final ArrayList<String> command = new ArrayList<>();
        command.add(sdk.getHomePath() + AirPackageUtil.ADB_RELATIVE_PATH);
        command.add("devices");
        return command;
      }

      protected boolean checkMessages() {
        // List of devices attached
        // HT058HL00538<Tab>device
        if (myMessages.size() < 2) return true;
        if (!myMessages.get(0).trim().contains("List of devices attached")) return true;
        for (int i = 1; i < myMessages.size(); i++) {
          final String line = myMessages.get(i).trim();
          if (!line.isEmpty()) {
            final int index = StringUtil.indexOfAny(line, " \t");
            if (index > 0) {
              result.add(new DeviceInfo(-1, null, line.substring(0, index), line.substring(index).trim()));
            }
            else {
              result.add(new DeviceInfo(-1, null, line, "unnamed"));
            }
          }
        }
        return true;
      }
    }, "Looking for Android devices", "Looking for Android devices");

    return result;
  }

  public static List<DeviceInfo> getIosDevices(final Project project, final Sdk sdk) {
    final List<DeviceInfo> result = new ArrayList<>();

    ExternalTask.runWithProgress(new AdtTask(project, sdk) {
      protected void appendAdtOptions(final List<String> command) {
        command.add("-devices");
        command.add("-platform");
        command.add("ios");      }

      protected boolean checkMessages() {
        // List of attached devices:
        // Handle<Tab>DeviceClass<Tab>DeviceUUID<Tab><Tab><Tab><Tab><Tab>DeviceName
        // 3<Tab>iPad    <Tab>cf701a789380b6ca3d563c9959a2bf383b58b702<Tab>iPad
        // 2<Tab>iPod    <Tab>5da6555aea609c405f554b53a4a85853202b92de<Tab>JetBrains’s iPod

        if (myMessages.size() < 3) return true;
        if (!myMessages.get(0).trim().startsWith("List of attached devices")) return true;
        if (!myMessages.get(1).trim().startsWith("Handle")) return true;
        for (int i = 2; i < myMessages.size(); i++) {
          final String line = myMessages.get(i).trim();

          int handle = -1;
          String deviceClass = "";
          String deviceId = "";
          String deviceName = "";

          final StringTokenizer tokenizer = new StringTokenizer(line, " \t");

          if (tokenizer.hasMoreTokens()) {
            try {
              handle = Integer.parseInt(tokenizer.nextToken());
            }
            catch (NumberFormatException e) {
              return true;
            }
          }

          if (tokenizer.hasMoreTokens()) {
            deviceClass = tokenizer.nextToken();
          }

          while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            if (token.length() == 40) {
              deviceId = token;
              break;
            }
            else {
              deviceClass += " " + token;
            }
          }

          while (tokenizer.hasMoreTokens()) {
            if (!deviceName.isEmpty()) {
              deviceName += " ";
            }
            deviceName += tokenizer.nextToken();
          }

          if (handle >= 0 && deviceId.length() > 0) {
            result.add(new DeviceInfo(handle, deviceClass, deviceId, deviceName));
          }
          else {
            return true;
          }
        }
        return true;
      }
    }, "Looking for iOS devices", "Looking for iOS devices");

    return result;
  }
}
