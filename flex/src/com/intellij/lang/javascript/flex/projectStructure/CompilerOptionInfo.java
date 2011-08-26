package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.text.StringUtil;
import gnu.trove.THashMap;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.*;


public class CompilerOptionInfo {

  public enum OptionType {Group, Boolean, String, Int, File, List, IncludeClasses, IncludeFiles}

  public enum ListElementType {String, File, Locale}

  public static class ListElement {
    public final String NAME;
    public final ListElementType LIST_ELEMENT_TYPE;

    private ListElement(final String name, final ListElementType listElementType) {
      this.NAME = name;
      this.LIST_ELEMENT_TYPE = listElementType;
    }
  }

  public static final char LIST_ENTRIES_SEPARATOR = '\n';
  public static final char LIST_ENTRY_PARTS_SEPARATOR = '\t'; // if list entry contains several values, e.g. uri and manifest
  public static final String FLEX_SDK_MACRO_NAME = "FLEX_SDK";
  private static final String SPECIAL_DEFAULT_VALUE = "SPECIAL";

  private static final Logger LOG = Logger.getInstance(CompilerOptionInfo.class.getName());

  private static volatile CompilerOptionInfo[] ourRootInfos;
  private static final Map<String, CompilerOptionInfo> ourIdToInfoMap = new THashMap<String, CompilerOptionInfo>(150);
  private static final Collection<CompilerOptionInfo> ourOptionsWithSpecialValues = new LinkedList<CompilerOptionInfo>();

  public static final CompilerOptionInfo EXTERNAL_LIBRARY_INFO =
    new CompilerOptionInfo("compiler.external-library-path", "fake", OptionType.List, null,
                           new ListElement[]{new ListElement("path-element", ListElementType.String)}, false, null, true, true, true, "");
  public static final CompilerOptionInfo LIBRARY_PATH_INFO =
    new CompilerOptionInfo("compiler.library-path", "fake", OptionType.List, null,
                           new ListElement[]{new ListElement("path-element", ListElementType.String)}, false, null, true, true, true, "");
  public static final CompilerOptionInfo INCLUDE_LIBRARY_INFO =
    new CompilerOptionInfo("compiler.include-libraries", "fake", OptionType.List, null,
                           new ListElement[]{new ListElement("library", ListElementType.String)}, false, null, true, true, true, "");
  public static final CompilerOptionInfo SOURCE_PATH_INFO =
    new CompilerOptionInfo("compiler.source-path", "fake", OptionType.List, null,
                           new ListElement[]{new ListElement("path-element", ListElementType.String)}, false, null, true, true, true, "");
  public static final CompilerOptionInfo MOBILE_INFO =
    new CompilerOptionInfo("compiler.mobile", "fake", OptionType.Boolean, null, null, false, null, true, true, true, "");
  public static final CompilerOptionInfo TARGET_PLAYER_INFO =
    new CompilerOptionInfo("target-player", "fake", OptionType.String, null, null, false, null, true, true, true, "");
  public static final CompilerOptionInfo MAIN_CLASS_INFO =
    new CompilerOptionInfo("file-specs.path-element", "fake", OptionType.String, null, null, false, null, true, true, true, "");
  public static final CompilerOptionInfo OUTPUT_PATH_INFO =
    new CompilerOptionInfo("output", "fake", OptionType.String, null, null, false, null, true, true, true, "");


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
  private final String myDefaultValue;

  private CompilerOptionInfo(final @NotNull String id,
                             final @NotNull String displayName,
                             final @NotNull OptionType optionType,
                             final @Nullable String fileExtension,
                             final @Nullable ListElement[] listElements,
                             final boolean advanced,
                             final @Nullable String sinceVersion,
                             final boolean okForAir,
                             final boolean okForPureAS,
                             final boolean okForSwf,
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
    myDefaultValue = defaultValue;

    myChildOptionInfos = null;

    if (SPECIAL_DEFAULT_VALUE.equals(myDefaultValue)) {
      ourOptionsWithSpecialValues.add(this);
    }

    ourIdToInfoMap.put(ID, this);
  }

  private CompilerOptionInfo(final @NotNull String groupDisplayName,
                             final boolean advanced,
                             final @Nullable String sinceVersion,
                             final boolean okForAir,
                             final boolean okForPureAS,
                             final boolean okForSwf,
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
    myDefaultValue = null;

    myChildOptionInfos = childOptionInfos;
  }

  public boolean isGroup() {
    return TYPE == OptionType.Group;
  }

  public boolean isApplicable(final @NotNull String sdkVersion,
                              final FlexIdeBuildConfiguration.TargetPlatform targetPlatform,
                              final boolean pureAS, final FlexIdeBuildConfiguration.OutputType outputType) {
    if (mySinceVersion != null && StringUtil.compareVersionNumbers(sdkVersion, mySinceVersion) < 0) {
      return false;
    }

    if (!myOkForAir && targetPlatform != FlexIdeBuildConfiguration.TargetPlatform.Web) {
      return false;
    }

    if (!myOkForPureAS && pureAS) {
      return false;
    }

    if (!myOkForSwf && outputType != FlexIdeBuildConfiguration.OutputType.Library) {
      return false;
    }

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

  public String getDefaultValue(final String sdkVersion, final FlexIdeBuildConfiguration.TargetPlatform targetPlatform) {
    assert !isGroup() : DISPLAY_NAME;

    if (SPECIAL_DEFAULT_VALUE.equals(myDefaultValue)) {
      if ("compiler.accessible".equals(ID)) {
        return targetPlatform == FlexIdeBuildConfiguration.TargetPlatform.Mobile
               ? "false"
               : StringUtil.compareVersionNumbers(sdkVersion, "4") >= 0 ? "true" : "false";
      }
      else if ("compiler.locale".equals(ID)) {
        return "en_US";
      }
      else if ("compiler.preloader".equals(ID)) {
        return targetPlatform == FlexIdeBuildConfiguration.TargetPlatform.Mobile ? "spark.preloaders.SplashScreen" : "";
      }
      else if ("compiler.theme".equals(ID)) {
        if (targetPlatform != FlexIdeBuildConfiguration.TargetPlatform.Desktop && StringUtil.compareVersionNumbers(sdkVersion, "4") >= 0) {
          return targetPlatform == FlexIdeBuildConfiguration.TargetPlatform.Mobile
                 ? "${FLEX_SDK}/frameworks/themes/Mobile/mobile.swc"
                 : "${FLEX_SDK}/frameworks/themes/Spark/spark.css";
        }
        return "";
      }
      else if ("swf-version".equals(ID)) {
        return "11";
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
      final List<CompilerOptionInfo> infos = new ArrayList<CompilerOptionInfo>(30);

      final InputStream inputStream = CompilerOptionInfo.class.getResourceAsStream("flex-compiler-options.xml");
      final Document document = JDOMUtil.loadDocument(inputStream);
      final Element rootElement = document.getRootElement();
      assert "options".equals(rootElement.getName());

      //noinspection unchecked
      for (final Element element : ((Iterable<Element>)rootElement.getChildren())) {
        final CompilerOptionInfo info;
        if ("group".equals(element.getName())) {
          info = loadGroup(element);
        }
        else {
          assert "option".equals(element.getName());
          info = loadOption(element);
        }
        infos.add(info);
      }

      ourRootInfos = infos.toArray(new CompilerOptionInfo[infos.size()]);
    }
    catch (Exception e) {
      LOG.error(e);
    }
  }

  private static CompilerOptionInfo loadGroup(final Element groupElement) {
    final String displayName = groupElement.getAttributeValue("displayName");
    assert StringUtil.isNotEmpty(displayName);

    final String advancedValue = groupElement.getAttributeValue("advanced");
    final boolean advanced = advancedValue != null && "true".equals(advancedValue);

    final String since = groupElement.getAttributeValue("since");

    final String okForAirValue = groupElement.getAttributeValue("okForAir");
    final boolean okForAir = okForAirValue == null || "false".equals(okForAirValue);

    final String okForPureASValue = groupElement.getAttributeValue("okForPureAS");
    final boolean okForPureAS = okForPureASValue == null || "false".equals(okForPureASValue);

    final String okForSWFValue = groupElement.getAttributeValue("okForSWF");
    final boolean okForSWF = okForSWFValue == null || "false".equals(okForSWFValue);

    final List<CompilerOptionInfo> infos = new ArrayList<CompilerOptionInfo>();

    //noinspection unchecked
    for (final Element element : ((Iterable<Element>)groupElement.getChildren())) {
      final CompilerOptionInfo info;
      if ("group".equals(element.getName())) {
        info = loadGroup(element);
      }
      else {
        assert "option".equals(element.getName());
        info = loadOption(element);
      }
      infos.add(info);
    }

    final CompilerOptionInfo[] infosArray = infos.toArray(new CompilerOptionInfo[infos.size()]);
    return new CompilerOptionInfo(displayName, advanced, since, okForAir, okForPureAS, okForSWF, infosArray);
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
    final boolean advanced = advancedValue != null && "true".equals(advancedValue);

    final String since = element.getAttributeValue("since");

    final String okForAirValue = element.getAttributeValue("okForAir");
    final boolean okForAir = okForAirValue == null || "false".equals(okForAirValue);

    final String okForPureASValue = element.getAttributeValue("okForPureAS");
    final boolean okForPureAS = okForPureASValue == null || "false".equals(okForPureASValue);

    final String okForSWFValue = element.getAttributeValue("okForSWF");
    final boolean okForSWF = okForSWFValue == null || "false".equals(okForSWFValue);

    final String defaultValue = StringUtil.notNullize(element.getAttributeValue("default"));

    return new CompilerOptionInfo(id, displayName, type, fileExtension, listElements, advanced, since, okForAir, okForPureAS, okForSWF,
                                  defaultValue);
  }

  private static ListElement[] readListElements(final Element element) {
    final List<ListElement> result = new LinkedList<ListElement>();

    //noinspection unchecked
    for (final Element childElement : (Iterable<Element>)element.getChildren("listElement")) {
      final String name = childElement.getAttributeValue("name");
      assert name != null : element.getName();
      final ListElementType listElementType = ListElementType.valueOf(childElement.getAttributeValue("type"));
      result.add(new ListElement(name, listElementType));
    }

    assert !result.isEmpty() : element.getName();
    return result.toArray(new ListElement[result.size()]);
  }
}
