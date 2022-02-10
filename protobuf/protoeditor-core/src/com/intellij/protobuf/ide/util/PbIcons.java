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

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.PlatformIcons;

import javax.swing.*;

public interface PbIcons {
  Icon ENUM = PlatformIcons.ENUM_ICON;
  Icon ENUM_VALUE = PlatformIcons.FIELD_ICON;
  Icon EXTEND = PlatformIcons.EXPORT_ICON;
  Icon FIELD = PlatformIcons.FIELD_ICON;
  Icon FILE = IconLoader.getIcon("protoFile.png", PbIcons.class.getClassLoader());
  Icon GROUP_FIELD = PlatformIcons.ANONYMOUS_CLASS_ICON;
  Icon MESSAGE = IconLoader.getIcon("protoMessage.png", PbIcons.class.getClassLoader());
  Icon ONEOF = PlatformIcons.CLASS_ICON;
  Icon PACKAGE = PlatformIcons.PACKAGE_ICON;
  Icon SERVICE = PlatformIcons.INTERFACE_ICON;
  Icon SERVICE_METHOD = PlatformIcons.METHOD_ICON;

  // TODO(volkman): find a better icon.
  Icon TEXT_FILE = IconLoader.getIcon("protoFile.png", PbIcons.class.getClassLoader());
}
