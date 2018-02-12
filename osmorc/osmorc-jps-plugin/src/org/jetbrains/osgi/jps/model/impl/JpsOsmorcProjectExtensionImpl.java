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
package org.jetbrains.osgi.jps.model.impl;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.ex.JpsElementBase;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.java.JpsJavaProjectExtension;
import org.jetbrains.jps.util.JpsPathUtil;
import org.jetbrains.osgi.jps.model.JpsOsmorcProjectExtension;

/**
 * @author michael.golubev
 */
public class JpsOsmorcProjectExtensionImpl extends JpsElementBase<JpsOsmorcProjectExtensionImpl> implements JpsOsmorcProjectExtension {
  private final OsmorcProjectExtensionProperties myProperties;

  public JpsOsmorcProjectExtensionImpl(OsmorcProjectExtensionProperties properties) {
    myProperties = properties;
  }

  @Override
  public void applyChanges(@NotNull JpsOsmorcProjectExtensionImpl modified) {
    XmlSerializerUtil.copyBean(modified.myProperties, myProperties);
  }

  @NotNull
  @Override
  public JpsOsmorcProjectExtensionImpl createCopy() {
    return new JpsOsmorcProjectExtensionImpl(XmlSerializerUtil.createCopy(myProperties));
  }

  @Override
  public String getBundlesOutputPath() {
    return myProperties.myBundlesOutputPath;
  }

  @Override
  public String getDefaultManifestFileLocation() {
    return myProperties.myDefaultManifestFileLocation;
  }

  @NotNull
  public static String getDefaultBundlesOutputPath(JpsProject project) {
    JpsJavaExtensionService service = JpsJavaExtensionService.getInstance();

    JpsJavaProjectExtension extension = service.getProjectExtension(project);
    if (extension != null) {
      String outputUrl = extension.getOutputUrl();
      if (outputUrl != null) {
        return JpsPathUtil.urlToPath(outputUrl) + "/bundles";
      }
    }
    // this actually should never happen (only in tests)
    return FileUtil.getTempDirectory();
  }
}
