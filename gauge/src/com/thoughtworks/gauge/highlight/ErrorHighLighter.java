/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.highlight;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.thoughtworks.gauge.util.GaugeUtil;

/**
 * The highlighter condition that determines if a Gauge file should be displayed differently due to errors within (e.g.
 * syntax errors). In most themes, this will result in the Gauge file with errors should be red.
 */
final class ErrorHighLighter implements Condition<VirtualFile> {
  @Override
  public boolean value(VirtualFile virtualFile) {
    return GaugeUtil.isGaugeFile(virtualFile);
  }
}
