// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.model.bc;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.Constants;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class CompilerOptionInfo {
  public enum OptionType {Group, Boolean, String, Int, File, List, IncludeClasses, IncludeFiles}

  public enum ListElementType {String, File, FileOrFolder, Class, Boolean, Locale}

  public static final class ListElement {
    public final String NAME;
    public final String DISPLAY_NAME;
    public final ListElementType LIST_ELEMENT_TYPE;
    public final String @Nullable [] FILE_EXTENSIONS;
    public final String DEFAULT_VALUE;

    private ListElement(final String name) {
      this(name, "", ListElementType.String, null, "");
    }

    private ListElement(final String name, final String displayName, final ListElementType listElementType,
                        final String @Nullable [] fileExtensions, final String defaultValue) {
      NAME = name;
      DISPLAY_NAME = displayName;
      LIST_ELEMENT_TYPE = listElementType;
      FILE_EXTENSIONS = fileExtensions;
      DEFAULT_VALUE = defaultValue;
    }
  }

  public static final String LIST_ENTRIES_SEPARATOR = "\n";
  public static final String LIST_ENTRY_PARTS_SEPARATOR = "\t"; // if list entry contains several values, e.g. uri and manifest
  public static final String FLEX_SDK_MACRO_NAME = "FLEX_SDK";
  public static final String FLEX_SDK_MACRO = "${" + FLEX_SDK_MACRO_NAME + "}";
  private static final String SPECIAL_DEFAULT_VALUE = "SPECIAL";

  private static final Logger LOG = Logger.getInstance(CompilerOptionInfo.class.getName());

  private static volatile CompilerOptionInfo[] ourRootInfos;
  private static final Map<String, CompilerOptionInfo> ourIdToInfoMap = new HashMap<>(50);
  private static final Collection<CompilerOptionInfo> ourOptionsWithSpecialValues = new LinkedList<>();

  public static final CompilerOptionInfo DEBUG_INFO =
    new CompilerOptionInfo("compiler.debug", "fake", OptionType.Boolean, null, null, false, null, true, true, true, true, "");
  public static final CompilerOptionInfo EXTERNAL_LIBRARY_INFO =
    new CompilerOptionInfo("compiler.external-library-path", "fake", OptionType.List, null,
                           new ListElement[]{new ListElement("path-element")}, false, null, true, true, true, true, "");
  public static final CompilerOptionInfo LIBRARY_PATH_INFO =
    new CompilerOptionInfo("compiler.library-path", "fake", OptionType.List, null,
                           new ListElement[]{new ListElement("path-element")}, false, null, true, true, true, true, "");
  public static final CompilerOptionInfo INCLUDE_LIBRARY_INFO =
    new CompilerOptionInfo("compiler.include-libraries", "fake", OptionType.List, null,
                           new ListElement[]{new ListElement("library")}, false, null, true, true, true, true, "");
  public static final CompilerOptionInfo SOURCE_PATH_INFO =
    new CompilerOptionInfo("compiler.source-path", "fake", OptionType.List, null,
                           new ListElement[]{new ListElement("path-element")}, false, null, true, true, true, true, "");
  public static final CompilerOptionInfo INCLUDE_CLASSES_INFO =
    new CompilerOptionInfo("include-classes", "fake", OptionType.List, null,
                           new ListElement[]{new ListElement("class")},
                           false, null, true, true, true, true, "");
  public static final CompilerOptionInfo RSL_ONE_URL_PATH_INFO =
    new CompilerOptionInfo("runtime-shared-library-path", "fake", OptionType.List, null,
                           new ListElement[]{
                             new ListElement("path-element"),
                             new ListElement("rsl-url"),
                             new ListElement("policy-file-url")
                           }, false, null, true, true, true, true, "");
  public static final CompilerOptionInfo RSL_TWO_URLS_PATH_INFO =
    new CompilerOptionInfo("runtime-shared-library-path", "fake", OptionType.List, null,
                           new ListElement[]{
                             new ListElement("path-element"),
                             new ListElement("rsl-url"),
                             new ListElement("policy-file-url"),
                             new ListElement("rsl-url"),
                             new ListElement("policy-file-url")
                           }, false, null, true, true, true, true, "");
  public static final CompilerOptionInfo MOBILE_INFO =
    new CompilerOptionInfo("compiler.mobile", "fake", OptionType.Boolean, null, null, false, null, true, true, true, true, "");
  public static final CompilerOptionInfo TARGET_PLAYER_INFO =
    new CompilerOptionInfo("target-player", "fake", OptionType.String, null, null, false, null, true, true, true, true, "");
  public static final CompilerOptionInfo MAIN_CLASS_INFO =
    new CompilerOptionInfo("file-specs.path-element", "fake", OptionType.String, null, null, false, null, true, true, true, true, "");
  public static final CompilerOptionInfo OUTPUT_PATH_INFO =
    new CompilerOptionInfo("output", "fake", OptionType.String, null, null, false, null, true, true, true, true, "");

  public static final CompilerOptionInfo ACCESSIBLE_INFO =
    new CompilerOptionInfo("compiler.accessible", "fake", OptionType.Boolean, null, null, false, null, true, true, true, true, "");
  public static final CompilerOptionInfo PRELOADER_INFO =
    new CompilerOptionInfo("compiler.preloader", "fake", OptionType.String, null, null, false, "4.5", true, true, true, true, "");
  public static final CompilerOptionInfo WARN_NO_CONSTRUCTOR_INFO =
    new CompilerOptionInfo("compiler.warn-no-constructor", "fake", OptionType.Boolean, null, null, false, null, true, true, true, true, "");
  public static final CompilerOptionInfo LINK_REPORT_INFO =
    new CompilerOptionInfo("link-report", "fake", OptionType.File, "xml", null, false, null, true, true, true, true, "");
  public static final CompilerOptionInfo LOAD_EXTERNS_INFO =
    new CompilerOptionInfo("load-externs", "fake", OptionType.File, "xml", null, false, null, true, true, true, true, "");
  public static final CompilerOptionInfo FONT_MANAGERS_INFO =
    new CompilerOptionInfo("compiler.fonts.managers", "fake", OptionType.List, null,
                           new ListElement[]{new ListElement("manager-class")},
                           false, null, true, true, true, true, "");
  public static final CompilerOptionInfo SWF_VERSION_INFO =
    new CompilerOptionInfo("swf-version", "fake", OptionType.String, null, null, false, "4.5", true, true, true, true, "");
  public static final CompilerOptionInfo STATIC_RSLS_INFO =
    new CompilerOptionInfo("static-link-runtime-shared-libraries", "fake", OptionType.Boolean, null, null, false, null, true, true, true,
                           true, "");
  public static final CompilerOptionInfo INCLUDE_NAMESPACES_INFO =
    new CompilerOptionInfo("include-namespaces", "fake", OptionType.List, null,
                           new ListElement[]{new ListElement("uri")},
                           false, null, true, true, true, true, "");
  public static final CompilerOptionInfo RLMS_INFO_FOR_UI =
    new CompilerOptionInfo("rlm.list.fake", "fake", OptionType.List, null,
                           new ListElement[]{
                             new ListElement("fake", "Main Class", ListElementType.Class, null, ""),
                             new ListElement("fake", "Output File", ListElementType.String, null, ""),
                             new ListElement("fake", "Optimize", ListElementType.Boolean, null, "true")},
                           false, null, true, true, true, true, "");
  public static final CompilerOptionInfo CSS_FILES_INFO_FOR_UI =
    new CompilerOptionInfo("css.files.list.fake", "fake", OptionType.List, null,
                           new ListElement[]{new ListElement("fake", "CSS Files", ListElementType.File, new String[]{"css"}, "")},
                           false, null, true, true, true, true, "");
  public static final CompilerOptionInfo INCLUDE_FILE_INFO_FOR_UI =
    new CompilerOptionInfo("files.to.include.in.swc.fake", "fake", OptionType.List, null,
                           new ListElement[]{new ListElement("fake", "Files And Folders", ListElementType.FileOrFolder, null, "")},
                           false, null, true, true, true, true, "");
  public static final CompilerOptionInfo INCLUDE_FILE_INFO =
    new CompilerOptionInfo("include-file", "fake", OptionType.List, null,
                           new ListElement[]{new ListElement("name"), new ListElement("path")},
                           false, null, true, true, true, true, "");

  public final String ID;
  public final String DISPLAY_NAME;
  public final OptionType TYPE;
  public final @Nullable String FILE_EXTENSION; // for options with TYPE=OptionType.File
  public final ListElement[] LIST_ELEMENTS; // for options with TYPE=OptionType.List
  public final boolean ADVANCED;
  private final CompilerOptionInfo[] myChildOptionInfos;
  private final @Nullable String mySinceVersion;
  private final boolean myOkForAir;
  private final boolean myOkForPureAS;
  private final boolean myOkForSwf;
  private final boolean myOkForSwc;
  private final String myDefaultValue;

  private CompilerOptionInfo(final @NotNull String id,
                             final @NotNull String displayName,
                             final @NotNull OptionType optionType,
                             final @Nullable String fileExtension,
                             final ListElement @Nullable [] listElements,
                             final boolean advanced,
                             final @Nullable String sinceVersion,
                             final boolean okForAir,
                             final boolean okForPureAS,
                             final boolean okForSwf,
                             final boolean okForSwc,
                             final String defaultValue) {
    assert optionType != OptionType.Group;

    ID = id;
    DISPLAY_NAME = displayName;
    TYPE = optionType;
    FILE_EXTENSION = fileExtension;
    LIST_ELEMENTS = listElements;
    ADVANCED = advanced;

    mySinceVersion = sinceVersion;
    myOkForAir = okForAir;
    myOkForPureAS = okForPureAS;
    myOkForSwf = okForSwf;
    myOkForSwc = okForSwc;
    myDefaultValue = defaultValue;

    myChildOptionInfos = null;
  }

  private CompilerOptionInfo(final @NotNull String groupDisplayName,
                             final boolean advanced,
                             final @Nullable String sinceVersion,
                             final boolean okForAir,
                             final boolean okForPureAS,
                             final boolean okForSwf,
                             final boolean okForSwc,
                             final CompilerOptionInfo[] childOptionInfos) {
    ID = null;
    DISPLAY_NAME = groupDisplayName;
    TYPE = OptionType.Group;
    FILE_EXTENSION = null;
    LIST_ELEMENTS = null;
    ADVANCED = advanced;

    mySinceVersion = sinceVersion;
    myOkForAir = okForAir;
    myOkForPureAS = okForPureAS;
    myOkForSwf = okForSwf;
    myOkForSwc = okForSwc;
    myDefaultValue = null;

    myChildOptionInfos = childOptionInfos;
  }

  public boolean isGroup() {
    return TYPE == OptionType.Group;
  }

  public boolean isApplicable(final String sdkVersion, final BuildConfigurationNature nature) {
    if (mySinceVersion != null &&
        !sdkVersion.startsWith(FlexCommonUtils.AIR_SDK_VERSION_PREFIX) &&
        StringUtil.compareVersionNumbers(sdkVersion, mySinceVersion) < 0) {
      return false;
    }
    if (!myOkForPureAS && nature.pureAS) return false;
    if (!myOkForSwf && !nature.isLib()) return false;
    if (!myOkForSwc && nature.isLib()) return false;
    return true;
  }

  public CompilerOptionInfo[] getChildOptionInfos() {
    assert TYPE == OptionType.Group;
    return myChildOptionInfos;
  }

  public static CompilerOptionInfo[] getRootInfos() {
    ensureLoaded();
    return ourRootInfos;
  }

  public static CompilerOptionInfo getOptionInfo(final String id) {
    ensureLoaded();
    final CompilerOptionInfo info = ourIdToInfoMap.get(id);
    assert info != null : id;
    return info;
  }

  public static boolean idExists(final String id) {
    ensureLoaded();
    return ourIdToInfoMap.get(id) != null;
  }

  public String getDefaultValue(final String sdkVersion, final BuildConfigurationNature nature, final ComponentSet componentSet) {
    assert !isGroup() : DISPLAY_NAME;

    if (SPECIAL_DEFAULT_VALUE.equals(myDefaultValue)) {
      if (ID.startsWith("compiler.debug")) {
        return "true";
      }
      if ("compiler.locale".equals(ID)) {
        return "en_US";
      }
      else if ("compiler.theme".equals(ID)) {
        if (!nature.pureAS &&
            !nature.isDesktopPlatform() &&
            !sdkVersion.startsWith(FlexCommonUtils.AIR_SDK_VERSION_PREFIX) &&
            StringUtil.compareVersionNumbers(sdkVersion, "4") >= 0) {
          if (nature.isMobilePlatform()) return FLEX_SDK_MACRO + "/frameworks/themes/Mobile/mobile.swc";
          if (StringUtil.compareVersionNumbers(sdkVersion, "4") >= 0 && componentSet == ComponentSet.MxOnly) {
            return FLEX_SDK_MACRO + "/frameworks/themes/Halo/halo.swc";
          }
          else {
            return FLEX_SDK_MACRO + "/frameworks/themes/Spark/spark.css";
          }
        }
        return "";
      }
      /*
      else if ("compiler.accessible".equals(ID)) {
        return nature.isMobilePlatform() ? "false"
                                         : StringUtil.compareVersionNumbers(sdkVersion, "4") >= 0 ? "true" : "false";
      }
      else if ("compiler.preloader".equals(ID)) {
        return nature.isMobilePlatform() ? "spark.preloaders.SplashScreen" : "";
      }
      else if ("swf-version".equals(ID)) {
        return StringUtil.compareVersionNumbers(sdkVersion, "4.6") >= 0 ? "14" : "11";
      }
      else if ("compiler.fonts.managers".equals(ID)) {
        return sdkVersion != null && StringUtil.compareVersionNumbers(sdkVersion, "4") >= 0
               ? "flash.fonts.JREFontManager" + LIST_ENTRIES_SEPARATOR +
                 "flash.fonts.BatikFontManager" + LIST_ENTRIES_SEPARATOR +
                 "flash.fonts.AFEFontManager" + LIST_ENTRIES_SEPARATOR +
                 "flash.fonts.CFFFontManager"

               : "flash.fonts.JREFontManager" + LIST_ENTRIES_SEPARATOR +
                 "flash.fonts.AFEFontManager" + LIST_ENTRIES_SEPARATOR +
                 "flash.fonts.BatikFontManager";
      }
      else if ("static-link-runtime-shared-libraries".equals(ID)) {
        return "false";
      }
      else if ("compiler.warn-no-constructor".equals(ID)) {
        return "false";
      }
      */

      assert false : ID;
    }
    return myDefaultValue;
  }

  public static Collection<CompilerOptionInfo> getOptionsWithSpecialValues() {
    ensureLoaded();
    return ourOptionsWithSpecialValues;
  }

  private static void ensureLoaded() {
    if (ourRootInfos == null) {
      synchronized (CompilerOptionInfo.class) {
        if (ourRootInfos == null) {
          loadInfo();
        }
      }
    }
  }

  private static void loadInfo() {
    try {
      final List<CompilerOptionInfo> infos = new ArrayList<>(30);

      final Element rootElement = JDOMUtil.load(CompilerOptionInfo.class.getResourceAsStream("flex-compiler-options.xml"));
      assert rootElement != null;
      assert "options".equals(rootElement.getName());

      for (Element element : rootElement.getChildren()) {
        final CompilerOptionInfo info;
        if ("group".equals(element.getName())) {
          info = loadGroup(element);
        }
        else {
          assert Constants.OPTION.equals(element.getName());
          info = loadOption(element);
        }
        infos.add(info);
      }

      ourRootInfos = infos.toArray(new CompilerOptionInfo[0]);
    }
    catch (Exception e) {
      LOG.error(e);
    }
  }

  private static CompilerOptionInfo loadGroup(final Element groupElement) {
    final String displayName = groupElement.getAttributeValue("displayName");
    assert StringUtil.isNotEmpty(displayName);

    final String advancedValue = groupElement.getAttributeValue("advanced");
    final boolean advanced = "true".equals(advancedValue);

    final String since = groupElement.getAttributeValue("since");

    final String okForAirValue = groupElement.getAttributeValue("okForAir");
    final boolean okForAir = okForAirValue == null || "false".equals(okForAirValue);

    final String okForPureASValue = groupElement.getAttributeValue("okForPureAS");
    final boolean okForPureAS = okForPureASValue == null || "false".equals(okForPureASValue);

    final String okForSWFValue = groupElement.getAttributeValue("okForSWF");
    final boolean okForSWF = okForSWFValue == null || "false".equals(okForSWFValue);

    final String okForSWCValue = groupElement.getAttributeValue("okForSWC");
    final boolean okForSWC = okForSWCValue == null || "false".equals(okForSWCValue);

    final List<CompilerOptionInfo> infos = new ArrayList<>();

    for (final Element element : groupElement.getChildren()) {
      final CompilerOptionInfo info;
      if ("group".equals(element.getName())) {
        info = loadGroup(element);
      }
      else {
        assert Constants.OPTION.equals(element.getName());
        info = loadOption(element);
      }
      infos.add(info);
    }

    final CompilerOptionInfo[] infosArray = infos.toArray(new CompilerOptionInfo[0]);
    return new CompilerOptionInfo(displayName, advanced, since, okForAir, okForPureAS, okForSWF, okForSWC, infosArray);
  }

  private static CompilerOptionInfo loadOption(final Element element) {
    final String id = element.getAttributeValue("id");
    assert StringUtil.isNotEmpty(id);

    final String displayName = element.getAttributeValue("displayName");
    assert StringUtil.isNotEmpty(displayName);

    final String typeValue = element.getAttributeValue("type");
    final OptionType type = OptionType.valueOf(typeValue);
    assert type != OptionType.Group;

    final String fileExtension = type == OptionType.File ? element.getAttributeValue("fileExtension") : null;
    final ListElement[] listElements = type == OptionType.List ? readListElements(element) : null;

    final String advancedValue = element.getAttributeValue("advanced");
    final boolean advanced = "true".equals(advancedValue);

    final String since = element.getAttributeValue("since");

    final String okForAirValue = element.getAttributeValue("okForAir");
    final boolean okForAir = okForAirValue == null || "true".equals(okForAirValue);

    final String okForPureASValue = element.getAttributeValue("okForPureAS");
    final boolean okForPureAS = okForPureASValue == null || "true".equals(okForPureASValue);

    final String okForSWFValue = element.getAttributeValue("okForSWF");
    final boolean okForSWF = okForSWFValue == null || "true".equals(okForSWFValue);

    final String okForSWCValue = element.getAttributeValue("okForSWC");
    final boolean okForSWC = okForSWCValue == null || "true".equals(okForSWCValue);

    final String defaultValue = StringUtil.notNullize(element.getAttributeValue("default"));

    final CompilerOptionInfo info = new CompilerOptionInfo(id, displayName, type, fileExtension, listElements, advanced, since, okForAir,
                                                           okForPureAS, okForSWF, okForSWC, defaultValue);

    if (SPECIAL_DEFAULT_VALUE.equals(defaultValue)) {
      ourOptionsWithSpecialValues.add(info);
    }

    ourIdToInfoMap.put(id, info);

    return info;
  }

  private static ListElement[] readListElements(final Element element) {
    final List<ListElement> result = new LinkedList<>();

    for (final Element childElement : element.getChildren("listElement")) {
      final String name = childElement.getAttributeValue("name");
      final String displayName = childElement.getAttributeValue("displayName");
      assert name != null : element.getName();
      final ListElementType listElementType = ListElementType.valueOf(childElement.getAttributeValue("type"));
      final String fileExtensionRaw = childElement.getAttributeValue("fileExtensions");
      final String[] fileExtensions = fileExtensionRaw == null ? null : fileExtensionRaw.split(",");
      final String defaultValue = StringUtil.notNullize(childElement.getAttributeValue("default"));
      result.add(new ListElement(name, displayName, listElementType, fileExtensions, defaultValue));
    }

    assert !result.isEmpty() : element.getName();
    return result.toArray(new ListElement[0]);
  }

  public String toString() {
    return ID;
  }
}
