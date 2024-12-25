// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.config;

import com.intellij.lang.html.HTMLLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * Formalizes the properties which we will persist using {@link com.intellij.ide.util.PropertiesComponent}
 */
enum Property {
  AUTO_GENERATE_CLOSE_TAG {
    @Override
    public @NotNull String getStringName() {
      // please excuse the "disabled" in this name.  This is an artifact from an earlier approach
      //      to properties, which we keep for backwards compatibility
      return "HbDisableAutoGenerateCloseTag";
    }

    @Override
    public @NotNull String getDefault() {
      return ENABLED;
    }
  },

  AUTOCOMPLETE_MUSTACHES {
    @Override
    public @NotNull String getStringName() {
      return "HbAutocompleteMustaches";
    }

    @Override
    public @NotNull String getDefault() {
      return ENABLED;
    }
  },

  FORMATTER {
    @Override
    public @NotNull String getStringName() {
      return "HbFormatter";
    }

    @Override
    public @NotNull String getDefault() {
      return ENABLED;
    }
  },

  AUTO_COLLAPSE_BLOCKS {
    @Override
    public @NotNull String getStringName() {
      return "HbAutoCollapseBlocks";
    }

    @Override
    public @NotNull String getDefault() {
      return DISABLED;
    }
  },

  COMMENTER_LANGUAGE_ID {
    @Override
    public @NotNull String getStringName() {
      return "HbCommenterLanguageId";
    }

    @Override
    public @NotNull String getDefault() {
      return HTMLLanguage.INSTANCE.getID();
    }

  },

  SHOULD_OPEN_HTML {
    @Override
    public @NotNull String getStringName() {
      return "HbShouldOpenHtmlAsHb";
    }

    @Override
    public @NotNull String getDefault() {
      return "";
    }
  };

  public static final String ENABLED = "enabled";
  public static final String DISABLED = "disabled";

  /**
   * The String which will actually be persisted in a user's properties using {@link com.intellij.ide.util.PropertiesComponent}.
   * <p/>
   * This value must be unique amongst Property entries
   * <p/>
   * IMPORTANT: these should probably never change so that we don't lose a user's preferences between releases.
   */
  public abstract @NotNull String getStringName();

  /**
   * The default/initial value for a user
   */
  public abstract @NotNull String getDefault();
}
