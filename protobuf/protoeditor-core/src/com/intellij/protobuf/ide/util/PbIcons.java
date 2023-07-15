/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.util;

import com.intellij.icons.AllIcons;
import com.intellij.protobuf.ProtoeditorCoreIcons;
import com.intellij.ui.IconManager;
import com.intellij.util.PlatformIcons;

import javax.swing.*;

public interface PbIcons {
  Icon ENUM = PlatformIcons.ENUM_ICON;
  Icon ENUM_VALUE = IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Field);
  Icon EXTEND = PlatformIcons.EXPORT_ICON;
  Icon FIELD = IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Field);
  Icon FILE = ProtoeditorCoreIcons.ProtoFile;
  Icon GROUP_FIELD = PlatformIcons.ANONYMOUS_CLASS_ICON;
  Icon MESSAGE = ProtoeditorCoreIcons.ProtoMessage;
  Icon ONEOF = IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Class);
  Icon PACKAGE = AllIcons.Nodes.Package;
  Icon SERVICE = PlatformIcons.INTERFACE_ICON;
  Icon SERVICE_METHOD = IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Method);
  Icon TEXT_FILE = ProtoeditorCoreIcons.ProtoFile;
}
