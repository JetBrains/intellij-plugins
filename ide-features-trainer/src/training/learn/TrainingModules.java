// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn;

import com.intellij.openapi.extensions.AbstractExtensionPointBean;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;

@Tag("module")
public class TrainingModules extends AbstractExtensionPointBean {
  public static final ExtensionPointName<TrainingModules> EP_NAME = ExtensionPointName.create("training.ift.modules");

  @Attribute("language")
  public String language;

  @Attribute("path")
  public String xmlPath;

  @Property(surroundWithTag = false)
  @XCollection
  public TrainingModules[] children;
}
