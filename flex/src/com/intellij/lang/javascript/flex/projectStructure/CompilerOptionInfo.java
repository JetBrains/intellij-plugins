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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CompilerOptionInfo {

  public enum OptionType {
    Group, Boolean, String, Int, File, LocaleList, RuntimeLocaleList, NamespacesList, IncludedClasses,
    ConditionalDefinitionList, FileList, StringList, FilesAndDirsList, ExtensionList, LanguageRangeList, TwoStringsList
  }

  private static final Logger LOG = Logger.getInstance(CompilerOptionInfo.class.getName());

  private static volatile CompilerOptionInfo[] ourRootInfos;
  private static final Map<String, CompilerOptionInfo> ourIdToInfoMap = new THashMap<String, CompilerOptionInfo>(150);
  private static final String SPECIAL_DEFAULT_VALUE = "SPECIAL";

  public final String ID;
  public final String DISPLAY_NAME;
  public final OptionType TYPE;
  public final @Nullable String FILE_EXTENSION; // for options with TYPE=OptionType.File
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
    ADVANCED = advanced;

    mySinceVersion = sinceVersion;
    myOkForAir = okForAir;
    myOkForPureAS = okForPureAS;
    myOkForSwf = okForSwf;
    myDefaultValue = defaultValue;

    myChildOptionInfos = null;

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

  public static CompilerOptionInfo[] getRootOptionInfos() {
    ensureLoaded();
    return ourRootInfos;
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
    assert type != null && type != OptionType.Group;

    final String fileExtension = type == OptionType.File ? element.getAttributeValue("fileExtension") : null;

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
    
    return new CompilerOptionInfo(id, displayName, type, fileExtension, advanced, since, okForAir, okForPureAS, okForSWF, defaultValue);
  }

  public String getDefaultValue(final String sdkVersion) {
    assert !isGroup() : DISPLAY_NAME;

    if (SPECIAL_DEFAULT_VALUE.equals(myDefaultValue)) {
      if ("swf-version".equals(ID)) {
        return "11";
      }
      else if ("managers".equals(ID)) {
        return sdkVersion != null && StringUtil.compareVersionNumbers(sdkVersion, "4") >= 0
               ? "flash.fonts.JREFontManager\n" +
                 "flash.fonts.BatikFontManager\n" +
                 "flash.fonts.AFEFontManager\n" +
                 "flash.fonts.CFFFontManager"
               
               : "flash.fonts.JREFontManager\n" +
                 "flash.fonts.AFEFontManager\n" +
                 "flash.fonts.BatikFontManager";
      }
      else if ("static-link-runtime-shared-libraries".equals(ID)) {
        return "false";
      }

      assert false : ID;
    }
    return myDefaultValue;
  }
}
