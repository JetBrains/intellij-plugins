// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.formatter;

import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mikhail Golubev
 */
public class HCLCodeStyleSettings extends CustomCodeStyleSettings {

  // Format alignment properties
  public static final int DO_NOT_ALIGN_PROPERTY = PropertyAlignment.DO_NOT_ALIGN.getId();
  public static final int ALIGN_PROPERTY_ON_VALUE = PropertyAlignment.ALIGN_ON_VALUE.getId();
  public static final int ALIGN_PROPERTY_ON_EQUALS = PropertyAlignment.ALIGN_ON_EQUALS.getId();

  // TODO: check whether it's possible to migrate CustomCodeStyleSettings to newer com.intellij.util.xmlb.XmlSerializer
  /**
   * Contains value of {@link PropertyAlignment#getId()}
   *
   * @see #DO_NOT_ALIGN_PROPERTY
   * @see #ALIGN_PROPERTY_ON_VALUE
   * @see #ALIGN_PROPERTY_ON_EQUALS
   */
  public int PROPERTY_ALIGNMENT = PropertyAlignment.ALIGN_ON_EQUALS.getId();

  // Commenter properties
  public int PROPERTY_LINE_COMMENTER_CHARACTER = LineCommenterPrefix.LINE_POUND_SIGN.getId();

  // Misc
  @CommonCodeStyleSettings.WrapConstant
  public int OBJECT_WRAPPING = CommonCodeStyleSettings.WRAP_AS_NEEDED;
  @CommonCodeStyleSettings.WrapConstant
  public int ARRAY_WRAPPING = CommonCodeStyleSettings.WRAP_AS_NEEDED;

  public HCLCodeStyleSettings(CodeStyleSettings container, Language language) {
    super(language.getID(), container);
  }

  // Enums  - Format alignment
  public enum PropertyAlignment {
    DO_NOT_ALIGN("Do not align", 0),
    ALIGN_ON_VALUE("On value", 1),
    ALIGN_ON_EQUALS("On equals", 2);

    private final String myDescription;
    private final int myId;

    PropertyAlignment(@NotNull String description, int id) {
      myDescription = description;
      myId = id;
    }

    @NotNull
    public String getDescription() {
      return myDescription;
    }

    public int getId() {
      return myId;
    }
  }

  // Enums  - Line Commenter Prefix
  public enum LineCommenterPrefix {
    LINE_DOUBLE_SLASHES("Double Slashes (//)", "//", 0),
    LINE_POUND_SIGN("Pound Sign (#)", "#", 1),
    ;

    private final String myDescription;
    private final String myPrefix;
    private final int myId;

    LineCommenterPrefix(@NotNull String description, @NotNull String prefix, int id) {
      myDescription = description;
      myPrefix = prefix;
      myId = id;
    }

    @NotNull
    public String getDescription() {
      return myDescription;
    }

    @NotNull
    public String getPrefix() {
      return myPrefix;
    }

    public int getId() {
      return myId;
    }
  }
}
