package com.intellij.flex.compiler.flex45;

import com.intellij.flex.compiler.SdkSpecificHandler;
import flash.localization.LocalizationManager;
import flash.localization.ResourceBundleLocalizer;
import flex2.compiler.CompilerAPI;
import flex2.compiler.Logger;
import flex2.compiler.common.*;
import flex2.compiler.io.FileUtil;
import flex2.compiler.io.LocalFile;
import flex2.compiler.io.VirtualFile;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.tools.LicensesConfiguration;
import flex2.tools.ToolsConfiguration;
import flex2.tools.oem.Builder;
import flex2.tools.oem.internal.OEMConfiguration;
import macromedia.asc.embedding.ConfigVar;
import macromedia.asc.util.ContextStatics;
import macromedia.asc.util.ObjectList;

import java.io.File;
import java.util.*;

public abstract class Flex45Handler extends SdkSpecificHandler {

  public void initThreadLocals(final Logger logger) {
    CompilerAPI.useAS3();
    CompilerAPI.usePathResolver();
    final LocalizationManager localizationManager = new LocalizationManager();
    localizationManager.addLocalizer(new ResourceBundleLocalizer());
    ThreadLocalToolkit.setLocalizationManager(localizationManager);
    ThreadLocalToolkit.setLogger(logger);
  }

  public void cleanThreadLocals() {
    CompilerAPI.removePathResolver();
    ThreadLocalToolkit.setLogger(null);
    ThreadLocalToolkit.setLocalizationManager(null);
  }

  public boolean omitTrace(final Configuration configuration) {
    try {
      final CompilerConfiguration cc = configuration.getCompilerConfiguration();
      return !cc.debug() && cc.omitTraceStatements();
    }
    catch (Throwable t) {
      // if API changed
      return false;
    }
  }

  public void setupOmitTraceOption(final boolean omitTrace) {
    try {
      ContextStatics.omitTrace = omitTrace;
    }
    catch (Throwable t) {/* if API changed */}
  }

  static void setupConfiguration(final Builder builder, final Configuration configuration) {
    final flex2.tools.oem.Configuration oemConfig = builder.getDefaultConfiguration();
    populateDefaults(oemConfig, (ToolsConfiguration)configuration);
    final String[] extras = {
      "-omit-trace-statements=" + configuration.getCompilerConfiguration().omitTraceStatements(),
      "-swf-version=" + configuration.getSwfVersion(),
      "-load-config=" // "-load-config=" MUST BE THE LAST option passed via oemConfig.setConfiguration()
    };
    oemConfig.setConfiguration(extras);
    builder.setConfiguration(oemConfig);
  }

  // all following methods are copy/pasted from flex2.tools.oem.internal.OEMConfiguration class with some fixes (marked with comment)
  private static void populateDefaults(final flex2.tools.oem.Configuration oemConfig, final ToolsConfiguration c) {
    oemConfig.setDefaultBackgroundColor(c.backgroundColor());
    oemConfig.setDefaultFrameRate(c.getFrameRate());
    oemConfig.setDefaultScriptLimits(c.getScriptRecursionLimit(), c.getScriptTimeLimit());
    oemConfig.setDefaultSize(c.defaultWidth(), c.defaultHeight());
    oemConfig.setExterns(toStrings(c.getExterns()));
    oemConfig.setIncludes(toStrings(c.getIncludes()));
    oemConfig.setTargetPlayer(c.getTargetPlayerMajorVersion(), c.getTargetPlayerMinorVersion(),
                              c.getTargetPlayerRevision());
    oemConfig.enableDigestVerification(c.getVerifyDigests());
    oemConfig.removeUnusedRuntimeSharedLibraryPaths(c.getRemoveUnusedRsls());

    List rslList = c.getRslPathInfo();
    boolean first = true;
    for (Iterator iter = rslList.iterator(); iter.hasNext();) {
      Configuration.RslPathInfo info = (Configuration.RslPathInfo)iter.next();
      String[] rslUrls = info.getRslUrls().toArray(new String[0]);
      String[] policyUrls = info.getPolicyFileUrls().toArray(new String[0]);
      if (first) {
        oemConfig.setRuntimeSharedLibraryPath(info.getSwcVirtualFile().getName(),   // CHANGES HERE
                                              rslUrls,
                                              policyUrls);
        first = false;
      }
      else {
        oemConfig.addRuntimeSharedLibraryPath(info.getSwcVirtualFile().getName(),   // CHANGES HERE
                                              rslUrls,
                                              policyUrls);
      }
    }

    // TODO
    // setSWFMetaData();
    // setProjector();

    oemConfig.setSWFMetaData(c.getMetadata());
    oemConfig.setRuntimeSharedLibraries(toStrings(c.getRuntimeSharedLibraries()));
    oemConfig.useNetwork(c.useNetwork());

    // useMobileFramework();

    populateDefaults(oemConfig, c, c.getCompilerConfiguration());
    populateDefaults(oemConfig, c.getFramesConfiguration());
    populateDefaults(oemConfig, c.getLicensesConfiguration());
    populateDefaults(oemConfig, c.getRuntimeSharedLibrarySettingsConfiguration());
  }

  private static void populateDefaults(final flex2.tools.oem.Configuration oemConfig,
                                       final ToolsConfiguration c,
                                       final CompilerConfiguration cc) {
    oemConfig.enableAccessibility(cc.accessible());
    oemConfig.setActionScriptMetadata(cc.getKeepAs3Metadata());
    oemConfig.setActionScriptFileEncoding(cc.getActionscriptFileEncoding());
    oemConfig.allowSourcePathOverlap(cc.allowSourcePathOverlap());
    oemConfig.useActionScript3(cc.dialect() == CompilerConfiguration.AS3Dialect);
    oemConfig.setContextRoot(cc.getContextRoot());
    oemConfig.enableDebugging(cc.debug(), c.debugPassword());  // CHANGES HERE

    if (cc.getDefaultsCssUrl() != null) {
      oemConfig.setDefaultCSS(FileUtil.openFile(cc.getDefaultsCssUrl().getName()));
    }

    oemConfig.useECMAScript(cc.dialect() == CompilerConfiguration.ESDialect);
    oemConfig.setExternalLibraryPath(toFiles(cc.getExternalLibraryPath()));
    oemConfig.useHeadlessServer(cc.headlessServer());
    oemConfig.keepAllTypeSelectors(cc.keepAllTypeSelectors());
    oemConfig.keepCompilerGeneratedActionScript(cc.keepGeneratedActionScript());
    oemConfig.includeLibraries(toFiles(cc.getIncludeLibraries()));
    oemConfig.setLibraryPath(toFiles(cc.getLibraryPath()));
    oemConfig.setLocale(cc.getLocales());
    oemConfig.optimize(cc.optimize());
    oemConfig.setServiceConfiguration(toFile(cc.getServices()));
    oemConfig.showActionScriptWarnings(cc.warnings());
    oemConfig.showBindingWarnings(cc.showBindingWarnings());
    oemConfig.showDeprecationWarnings(cc.showDeprecationWarnings());
    oemConfig.showShadowedDeviceFontWarnings(cc.showShadowedDeviceFontWarnings());
    oemConfig.showUnusedTypeSelectorWarnings(cc.showUnusedTypeSelectorWarnings());
    oemConfig.setSourcePath(toFiles(cc.getSourcePath()));    // CHANGES HERE
    oemConfig.enableStrictChecking(cc.strict());
    oemConfig.setTheme(toFiles(cc.getThemeFiles()));
    oemConfig.useResourceBundleMetaData(cc.useResourceBundleMetadata());
    oemConfig.enableVerboseStacktraces(cc.debug());
    setDefineDirective(oemConfig, cc.getDefine());        // CHANGES HERE
    oemConfig.setCompatibilityVersion(cc.getMxmlConfiguration().getMajorCompatibilityVersion(),
                                      cc.getMxmlConfiguration().getMinorCompatibilityVersion(),
                                      cc.getMxmlConfiguration().getRevisionCompatibilityVersion());

    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_ARRAY_TOSTRING_CHANGES, cc.warn_array_tostring_changes());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_ASSIGNMENT_WITHIN_CONDITIONAL,
                                       cc.warn_assignment_within_conditional());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_BAD_ARRAY_CAST, cc.warn_bad_array_cast());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_BAD_BOOLEAN_ASSIGNMENT, cc.warn_bad_bool_assignment());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_BAD_DATE_CAST, cc.warn_bad_date_cast());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_BAD_ES3_TYPE_METHOD, cc.warn_bad_es3_type_method());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_BAD_ES3_TYPE_PROP, cc.warn_bad_es3_type_prop());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_BAD_NAN_COMPARISON, cc.warn_bad_nan_comparison());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_BAD_NULL_ASSIGNMENT, cc.warn_bad_null_assignment());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_BAD_NULL_COMPARISON, cc.warn_bad_null_comparison());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_BAD_UNDEFINED_COMPARISON, cc.warn_bad_undefined_comparison());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_BOOLEAN_CONSTRUCTOR_WITH_NO_ARGS,
                                       cc.warn_boolean_constructor_with_no_args());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_CHANGES_IN_RESOLVE, cc.warn_changes_in_resolve());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_CLASS_IS_SEALED, cc.warn_class_is_sealed());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_CONST_NOT_INITIALIZED, cc.warn_const_not_initialized());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_CONSTRUCTOR_RETURNS_VALUE, cc.warn_constructor_returns_value());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_DEPRECATED_EVENT_HANDLER_ERROR,
                                       cc.warn_deprecated_event_handler_error());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_DEPRECATED_FUNCTION_ERROR, cc.warn_deprecated_function_error());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_DEPRECATED_PROPERTY_ERROR, cc.warn_deprecated_property_error());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_DUPLICATE_ARGUMENT_NAMES, cc.warn_duplicate_argument_names());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_DUPLICATE_VARIABLE_DEF, cc.warn_duplicate_variable_def());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_FOR_VAR_IN_CHANGES, cc.warn_for_var_in_changes());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_IMPORT_HIDES_CLASS, cc.warn_import_hides_class());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_INSTANCEOF_CHANGES, cc.warn_instance_of_changes());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_INTERNAL_ERROR, cc.warn_internal_error());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_LEVEL_NOT_SUPPORTED, cc.warn_level_not_supported());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_MISSING_NAMESPACE_DECL, cc.warn_missing_namespace_decl());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_NEGATIVE_UINT_LITERAL, cc.warn_negative_uint_literal());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_NO_CONSTRUCTOR, cc.warn_no_constructor());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_NO_EXPLICIT_SUPER_CALL_IN_CONSTRUCTOR,
                                       cc.warn_no_explicit_super_call_in_constructor());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_NO_TYPE_DECL, cc.warn_no_type_decl());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_NUMBER_FROM_STRING_CHANGES,
                                       cc.warn_number_from_string_changes());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_SCOPING_CHANGE_IN_THIS, cc.warn_scoping_change_in_this());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_SLOW_TEXTFIELD_ADDITION, cc.warn_slow_text_field_addition());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_UNLIKELY_FUNCTION_VALUE, cc.warn_unlikely_function_value());
    oemConfig.checkActionScriptWarning(flex2.tools.oem.Configuration.WARN_XML_CLASS_HAS_CHANGED, cc.warn_xml_class_has_changed());

    populateDefaults(oemConfig, cc.getFontsConfiguration());
    populateDefaults(oemConfig, cc.getNamespacesConfiguration());
  }

  private static void populateDefaults(final flex2.tools.oem.Configuration oemConfig, final FontsConfiguration fc) {
    FontsConfiguration.Languages lc = fc.getLanguagesConfiguration();

    for (Iterator i = lc.keySet().iterator(); i.hasNext();) {
      String key = (String)i.next();
      oemConfig.setFontLanguageRange(key, lc.getProperty(key));
    }
    oemConfig.setLocalFontSnapshot(toFile(fc.getLocalFontsSnapshot()));
    oemConfig.setLocalFontPaths(toStrings(fc.getLocalFontPaths()));
    oemConfig.setFontManagers(toStrings(fc.getManagers()));
    oemConfig.setMaximumCachedFonts(toInteger(fc.getMaxCachedFonts()));
    oemConfig.setMaximumGlyphsPerFace(toInteger(fc.getMaxGlyphsPerFace()));
    oemConfig.enableAdvancedAntiAliasing(fc.getFlashType());
  }

  private static void populateDefaults(final flex2.tools.oem.Configuration oemConfig, NamespacesConfiguration nc) {
    Map<String, List<VirtualFile>> manifestMappings = nc.getManifestMappings();

    if (manifestMappings != null) {
      Iterator<Map.Entry<String, List<VirtualFile>>> iterator = manifestMappings.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<String, List<VirtualFile>> entry = iterator.next();
        String uri = entry.getKey();
        List<VirtualFile> virtualFiles = entry.getValue();
        List<File> files = new ArrayList<File>(virtualFiles.size());

        Iterator<VirtualFile> vi = virtualFiles.iterator();
        while (vi.hasNext()) {
          files.add(toFile(vi.next()));
        }
        ((OEMConfiguration)oemConfig).setComponentManifests(uri, files);
      }
    }
  }

  private static void populateDefaults(final flex2.tools.oem.Configuration oemConfig, final FramesConfiguration frc) {
    List frList = frc.getFrameList();

    for (int i = 0, length = frList == null ? 0 : frList.size(); i < length; i++) {
      FramesConfiguration.FrameInfo info = (FramesConfiguration.FrameInfo)frList.get(i);
      oemConfig.setFrameLabel(info.label, toStrings(info.frameClasses));
    }
  }

  private static void populateDefaults(final flex2.tools.oem.Configuration oemConfig, final LicensesConfiguration lic) {
    Map licenseMap = lic.getLicenseMap();

    if (licenseMap != null) {
      for (Iterator i = licenseMap.keySet().iterator(); i.hasNext();) {
        String name = (String)i.next();
        oemConfig.setLicense(name, (String)licenseMap.get(name));
      }
    }
  }

  private static void populateDefaults(final flex2.tools.oem.Configuration oemConfig,
                                       final RuntimeSharedLibrarySettingsConfiguration rslConfig) {
    oemConfig.setForceRuntimeSharedLibraryPaths(toFiles(rslConfig.getForceRsls()));

    Map<VirtualFile, String> adMap = rslConfig.getApplicationDomains();
    boolean first = true;

    for (Map.Entry entry : adMap.entrySet()) {
      File file = toFile((VirtualFile)entry.getKey());
      if (first) {
        oemConfig.setApplicationDomainForRuntimeSharedLibraryPath(file, (String)entry.getValue());

        first = false;
      }
      else {
        oemConfig.addApplicationDomainForRuntimeSharedLibraryPath(file, (String)entry.getValue());
      }
    }
  }

  protected static String[] toStrings(List list) {
    String[] strings = new String[list == null ? 0 : list.size()];
    for (int i = 0, length = strings.length; i < length; i++) {
      strings[i] = (String)list.get(i);
    }
    return strings;
  }

  private static String[] toStrings(Set set) {
    String[] strings = new String[set == null ? 0 : set.size()];
    if (set != null) {
      int k = 0;
      for (Iterator i = set.iterator(); i.hasNext(); k++) {
        strings[k] = (String)i.next();
      }
    }
    return strings;
  }

  private static File toFile(VirtualFile f) {
    return (f instanceof LocalFile) ? new File(f.getName()) : null;
  }

  private static File[] toFiles(VirtualFile[] files) {
    File[] newFiles = new File[files == null ? 0 : files.length];
    for (int i = 0, length = newFiles.length; i < length; i++) {
      newFiles[i] = toFile(files[i]);
    }

    return newFiles;
  }

  private static int toInteger(String num) {
    try {
      return Integer.parseInt(num);
    }
    catch (NumberFormatException ex) {
      return -1;
    }
  }

  private static void setDefineDirective(final flex2.tools.oem.Configuration oemConfig, ObjectList<ConfigVar> configVars) {
    if (configVars != null) {
      final String[] names = new String[configVars.size()];
      final String[] values = new String[configVars.size()];
      int i = 0;
      for (ConfigVar var : configVars) {
        names[i] = var.ns + "::" + var.name;
        values[i] = var.value;
        i++;
      }
      oemConfig.setDefineDirective(names, values);
    }
  }
}
