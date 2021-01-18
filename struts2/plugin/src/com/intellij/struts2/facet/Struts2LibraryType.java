/*
 * Copyright 2013 The authors
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

package com.intellij.struts2.facet;

import com.intellij.framework.library.DownloadableLibraryType;
import com.intellij.struts2.Struts2Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Support for Struts 2 library setup in project settings.
 *
 * @author Yann C&eacute;bron
 */
public class Struts2LibraryType extends DownloadableLibraryType {

  public static final String STRUTS_VERSION_CLASS = "org.apache.struts2.StrutsConstants";

  private static final String GROUP_ID = "struts2";

  public Struts2LibraryType() {
    super("Struts 2", "struts2", GROUP_ID, Struts2LibraryType.class.getResource("struts2.xml"));
  }

  @NotNull
  @Override
  public Icon getLibraryTypeIcon() {
    return Struts2Icons.Action;
  }

  @Override
  public String @NotNull [] getDetectionClassNames() {
    return new String[]{STRUTS_VERSION_CLASS};
  }
}
