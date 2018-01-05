package com.dmarcotte.handlebars.config;

import com.intellij.lang.html.HTMLLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * Formalizes the properties which we will persist using {@link com.intellij.ide.util.PropertiesComponent}
 */
enum Property {
  AUTO_GENERATE_CLOSE_TAG {
    @NotNull
    @Override
    public String getStringName() {
      // please excuse the "disabled" in this name.  This is an artifact from an earlier approach
      //      to properties, which we keep for backwards compatibility
      return "HbDisableAutoGenerateCloseTag";
    }

    @NotNull
    @Override
    public String getDefault() {
      return ENABLED;
    }
  },

  AUTOCOMPLETE_MUSTACHES {
    @NotNull
    @Override
    public String getStringName() {
      return "HbAutocompleteMustaches";
    }

    @NotNull
    @Override
    public String getDefault() {
      return ENABLED;
    }
  },

  FORMATTER {
    @NotNull
    @Override
    public String getStringName() {
      return "HbFormatter";
    }

    @NotNull
    @Override
    public String getDefault() {
      return ENABLED;
    }
  },

  AUTO_COLLAPSE_BLOCKS {
    @NotNull
    @Override
    public String getStringName() {
      return "HbAutoCollapseBlocks";
    }

    @NotNull
    @Override
    public String getDefault() {
      return DISABLED;
    }
  },

  COMMENTER_LANGUAGE_ID {
    @NotNull
    @Override
    public String getStringName() {
      return "HbCommenterLanguageId";
    }

    @NotNull
    @Override
    public String getDefault() {
      return HTMLLanguage.INSTANCE.getID();
    }

  },

  SHOULD_OPEN_HTML {
    @NotNull
    @Override
    public String getStringName() {
      return "HbShouldOpenHtmlAsHb";
    }

    @NotNull
    @Override
    public String getDefault() {
      return "";
    }
  },

  RESOLVE_PARTIALS_PATHS {
    @NotNull
    @Override
    public String getStringName() {
      return "HbResolvePartialsPaths";
    }

    @NotNull
    @Override
    public String getDefault() {
      return DISABLED;
    }
  },

  TEMPLATES_LOCATIONS {
    @NotNull
    @Override
    public String getStringName() {
      return "HbTemplatesLocations";
    }

    @NotNull
    @Override
    public String getDefault() {
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
  @NotNull
  public abstract String getStringName();

  /**
   * The default/initial value for a user
   */
  @NotNull
  public abstract String getDefault();
}
