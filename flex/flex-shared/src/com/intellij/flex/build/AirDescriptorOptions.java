// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.build;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.openapi.util.io.FileUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class AirDescriptorOptions {
  public static final int ANDROID_PERMISSION_INTERNET = 1;
  public static final int ANDROID_PERMISSION_WRITE_EXTERNAL_STORAGE = 2;
  public static final int ANDROID_PERMISSION_ACCESS_FINE_LOCATION = 4;
  public static final int ANDROID_PERMISSION_CAMERA = 8;
  public final String AIR_VERSION;
  public final String APP_ID;
  public final String APP_NAME;
  public final String APP_VERSION;
  public final String SWF_NAME;
  public final String[] EXTENSIONS;
  public final boolean MOBILE;
  public final boolean AUTO_ORIENTS;
  public final boolean FULL_SCREEN;
  public final boolean ANDROID;
  public final int ANDROID_PERMISSIONS;
  public final boolean IOS;
  public final boolean IPHONE;
  public final boolean IPAD;
  public final boolean IOS_HIGH_RESOLUTION;

  public AirDescriptorOptions(final String airVersion,
                              final String appId,
                              final String appName,
                              final String swfName,
                              final String[] extensions,
                              final boolean android,
                              final boolean ios) {
    this(airVersion, appId, appName, "0.0.0", swfName, extensions, android || ios, android || ios, android || ios,
         android, ANDROID_PERMISSION_INTERNET, ios, ios, ios, ios);
  }

  public AirDescriptorOptions(final String airVersion,
                              final String appId,
                              final String appName,
                              final String appVersion,
                              final String swfName,
                              final String[] extensions,
                              final boolean mobile,
                              final boolean autoOrients,
                              final boolean fullScreen,
                              final boolean android,
                              final int androidPermissions,
                              final boolean ios,
                              final boolean iPhone,
                              final boolean iPad,
                              final boolean iosHighResolution) {
    AIR_VERSION = airVersion;
    APP_ID = appId;
    APP_NAME = appName;
    APP_VERSION = appVersion;
    SWF_NAME = swfName;
    EXTENSIONS = extensions;
    MOBILE = mobile;
    AUTO_ORIENTS = autoOrients;
    FULL_SCREEN = fullScreen;
    ANDROID = android;
    ANDROID_PERMISSIONS = androidPermissions;
    IOS = ios;
    IPHONE = iPhone;
    IPAD = iPad;
    IOS_HIGH_RESOLUTION = iosHighResolution;
  }

  public  String getAirDescriptorText() throws IOException {
    final String rawText =
      FileUtil.loadTextAndClose(AirDescriptorOptions.class.getResourceAsStream("air_descriptor_template.ft"));
    return replaceMacros(rawText);
  }

  private String replaceMacros(final String descriptorText) {
    final Map<String, String> replacementMap = new HashMap<>();

    replacementMap.put("${air_version}", AIR_VERSION);
    replacementMap.put("${app_id}", APP_ID);
    replacementMap.put("${app_name}", APP_NAME);
    replacementMap.put("${app_version}", APP_VERSION);
    replacementMap.put("${swf_name}", SWF_NAME);

    if (EXTENSIONS.length == 0) {
      replacementMap.put("${extensions_comment_start}", "<!--");
      replacementMap.put("${extensions_comment_end}", "-->");
      replacementMap.put("${extensions_list}", "<extensionID></extensionID>");
    }
    else {
      final StringBuilder buf = new StringBuilder();
      for (String extensionId : EXTENSIONS) {
        if (buf.length() > 0) buf.append("\n        ");
        buf.append("<extensionID>").append(extensionId).append("</extensionID>");
      }

      replacementMap.put("${extensions_comment_start}", "");
      replacementMap.put("${extensions_comment_end}", "");
      replacementMap.put("${extensions_list}", buf.toString());
    }

    replacementMap.put("${auto_orients}", MOBILE ? String.valueOf(AUTO_ORIENTS) : "");
    replacementMap.put("${auto_orients_comment_start}", MOBILE ? "" : "<!--");
    replacementMap.put("${auto_orients_comment_end}", MOBILE ? "" : "-->");

    replacementMap.put("${full_screen}", MOBILE ? String.valueOf(FULL_SCREEN) : "");
    replacementMap.put("${full_screen_comment_start}", MOBILE ? "" : "<!--");
    replacementMap.put("${full_screen_comment_end}", MOBILE ? "" : "-->");

    replacementMap.put("${iOS_comment_start}", MOBILE && IOS ? "" : "<!--");
    replacementMap.put("${iOS_comment_end}", MOBILE && IOS ? "" : "-->");

    replacementMap.put("${iPhone_comment_start}", MOBILE && IOS && IPHONE ? "" : "<!--");
    replacementMap.put("${iPhone_comment_end}", MOBILE && IOS && IPHONE ? "" : "-->");
    replacementMap.put("${iPad_comment_start}", MOBILE && IOS && IPAD ? "" : "<!--");
    replacementMap.put("${iPad_comment_end}", MOBILE && IOS && IPAD ? "" : "-->");
    replacementMap.put("${iOS_high_resolution_comment_start}", MOBILE && IOS && IOS_HIGH_RESOLUTION ? "" : "<!--");
    replacementMap.put("${iOS_high_resolution_comment_end}", MOBILE && IOS && IOS_HIGH_RESOLUTION ? "" : "-->");

    replacementMap.put("${android_comment_start}", MOBILE && ANDROID ? "" : "<!--");
    replacementMap.put("${android_comment_end}", MOBILE && ANDROID ? "" : "-->");

    replacementMap.put("${android_internet_comment_start}",
                       MOBILE && ANDROID && (ANDROID_PERMISSIONS & ANDROID_PERMISSION_INTERNET) != 0 ? "" : "<!--");
    replacementMap.put("${android_internet_comment_end}",
                       MOBILE && ANDROID && (ANDROID_PERMISSIONS & ANDROID_PERMISSION_INTERNET) != 0 ? "" : "-->");

    replacementMap.put("${android_write_external_storage_comment_start}",
                       MOBILE && ANDROID && (ANDROID_PERMISSIONS & ANDROID_PERMISSION_WRITE_EXTERNAL_STORAGE) != 0
                       ? "" : "<!--");
    replacementMap.put("${android_write_external_storage_comment_end}",
                       MOBILE && ANDROID && (ANDROID_PERMISSIONS & ANDROID_PERMISSION_WRITE_EXTERNAL_STORAGE) != 0
                       ? "" : "-->");

    replacementMap.put("${android_access_fine_location_comment_start}",
                       MOBILE && ANDROID && (ANDROID_PERMISSIONS & ANDROID_PERMISSION_ACCESS_FINE_LOCATION) != 0
                       ? "" : "<!--");
    replacementMap.put("${android_access_fine_location_comment_end}",
                       MOBILE && ANDROID && (ANDROID_PERMISSIONS & ANDROID_PERMISSION_ACCESS_FINE_LOCATION) != 0
                       ? "" : "-->");

    replacementMap.put("${android_camera_comment_start}",
                       MOBILE && ANDROID && (ANDROID_PERMISSIONS & ANDROID_PERMISSION_CAMERA) != 0 ? "" : "<!--");
    replacementMap.put("${android_camera_comment_end}",
                       MOBILE && ANDROID && (ANDROID_PERMISSIONS & ANDROID_PERMISSION_CAMERA) != 0 ? "" : "-->");

    return FlexCommonUtils.replace(descriptorText, replacementMap);
  }
}
