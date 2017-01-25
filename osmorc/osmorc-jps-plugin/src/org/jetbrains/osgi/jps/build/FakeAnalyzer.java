/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.osgi.jps.build;

import aQute.bnd.osgi.Analyzer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FakeAnalyzer extends Analyzer {

  private final Map<String, String> myProps;

  /**
   * Creates a fake analyzer instance around the given map. This is mostly done so the felix bnd maven plugin code doesn't have to be
   * changed that much.
   * @param props the properties to wrap
   */
  public FakeAnalyzer(@NotNull Map<String, String> props) {
    myProps = props;
  }

  @Override
  public String getProperty(String key) {
    return myProps.get(key);
  }

  @Override
  public String getProperty(String key, String fallback) {
    return myProps.containsKey(key) ? key : fallback;
  }

  @Override
  public void setProperty(String key, String value) {
    myProps.put(key, value);
  }
}
