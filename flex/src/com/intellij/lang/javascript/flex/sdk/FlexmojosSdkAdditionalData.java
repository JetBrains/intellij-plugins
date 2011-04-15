package com.intellij.lang.javascript.flex.sdk;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Processor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlexmojosSdkAdditionalData implements SdkAdditionalData {

  private static final String COMPILER_CLASSPATH_ELEMENT_NAME = "CompilerClassPath";
  private static final String CLASSPATH_ENTRY_ELEMENT_NAME = "ClassPathEntry";
  private static final String ADL_PATH_ELEMENT_NAME = "AdlPath";
  private static final String AIR_RUNTIME_PATH_ELEMENT_NAME = "AirRuntimePath";

  private static final String COMPILER_POM_NAME_BEGINNING = "compiler-";
  private static final String DOT_POM = ".pom";
  private static final String ADL_ARTIFACT_PATTERN = "{0}/com/adobe/flex/adl/{1}/adl-{1}.{2}";
  private static final String AIR_RUNTIME_ARTIFACT_PATTERN = "{0}/com/adobe/air/runtime/{1}/runtime-{1}-{2}.{3}";

  private Collection<String> myFlexCompilerClasspath = new ArrayList<String>();
  private String myAdlPath = "";
  private String myAirRuntimePath = "";

  public Object clone() throws CloneNotSupportedException {
    final FlexmojosSdkAdditionalData copy = (FlexmojosSdkAdditionalData)super.clone();
    copy.myFlexCompilerClasspath = new ArrayList<String>(myFlexCompilerClasspath);
    return copy;
  }

  public void checkValid(final SdkModel sdkModel) throws ConfigurationException {
  }

  public Collection<String> getFlexCompilerClasspath() {
    return Collections.unmodifiableCollection(myFlexCompilerClasspath);
  }

  public void addFlexCompilerClasspathEntryIfNotPresentAndRemoveDifferentVersionOfThisJar(final Pattern jarNamePattern,
                                                                                          final String jarPath) {
    final Iterator<String> iterator = myFlexCompilerClasspath.iterator();
    while (iterator.hasNext()) {
      final String classpathEntry = iterator.next();
      final String jarName = classpathEntry.substring(classpathEntry.lastIndexOf("/") + 1);
      final Matcher matcher = jarNamePattern.matcher(jarName);
      if (matcher.find() && matcher.start() == 0) {
        // jar name starts with jarNamePattern
        iterator.remove();
      }
    }

    myFlexCompilerClasspath.add(jarPath);
  }

  public String getAdlPath() {
    return myAdlPath;
  }

  public void setAdlPath(final String adlPath) {
    myAdlPath = adlPath;
  }

  public String getAirRuntimePath() {
    return myAirRuntimePath;
  }

  public void setAirRuntimePath(final String airRuntimePath) {
    myAirRuntimePath = airRuntimePath;
  }

  void setup(final VirtualFile compilerPomFile) {
    final String compilerPomPath = compilerPomFile.getPath();
    if (!compilerPomPath.matches(FlexmojosSdkType.COMPILER_POM_PATTERN)) return;
    final String repositoryRootPath = compilerPomPath.substring(0, compilerPomPath.indexOf("/com/adobe/flex/compiler"));

    setupFlexCompilerClasspath(compilerPomFile, repositoryRootPath);

    final int i = compilerPomPath.lastIndexOf(COMPILER_POM_NAME_BEGINNING);
    final String version = compilerPomPath.substring(i + COMPILER_POM_NAME_BEGINNING.length(), compilerPomPath.lastIndexOf(DOT_POM));
    setupAirPaths(repositoryRootPath, version);
  }

  void setupFlexCompilerClasspath(final VirtualFile compilerPomFile, final String repositoryRootPath) {
    myFlexCompilerClasspath.clear();

    try {
      final Document document = JDOMUtil.loadDocument(compilerPomFile.getInputStream());
      final Element rootElement = document.getRootElement();
      if (!rootElement.getName().equals("project")) return;
      for (final Object dependenciesElement : rootElement.getChildren("dependencies", rootElement.getNamespace())) {
        for (final Object dependencyElement : ((Element)dependenciesElement).getChildren("dependency", rootElement.getNamespace())) {
          final String groupId = ((Element)dependencyElement).getChildText("groupId", rootElement.getNamespace());
          final String artifactId = ((Element)dependencyElement).getChildText("artifactId", rootElement.getNamespace());
          final String version = ((Element)dependencyElement).getChildText("version", rootElement.getNamespace());
          addClasspathEntry(repositoryRootPath, groupId, artifactId, version);
        }
      }
    }
    catch (IOException e) {/*ignore*/}
    catch (JDOMException e) {/*ignore*/}
  }

  private void setupAirPaths(final String repositoryRootPath, final String version) {
    if (StringUtil.isNotEmpty(myAdlPath) && StringUtil.isNotEmpty(myAirRuntimePath)) {
      return;
    }
    final String exeType = SystemInfo.isWindows ? "exe" : "uexe";
    final String adlPath = FileUtil.toSystemDependentName(MessageFormat.format(ADL_ARTIFACT_PATTERN, repositoryRootPath, version, exeType));
    final VirtualFile adlFile = LocalFileSystem.getInstance().findFileByPath(adlPath);
    if (adlFile != null && !adlFile.isDirectory()) {
      myAdlPath = adlPath;

      final String classifier = SystemInfo.isWindows ? "win" : "mac";
      final String zipType = "zip";
      myAirRuntimePath = FileUtil
        .toSystemDependentName(MessageFormat.format(AIR_RUNTIME_ARTIFACT_PATTERN, repositoryRootPath, version, classifier, zipType));
    }
    else {
      final Sdk sdk = findSimilarSdk(version);
      if (sdk != null) {
        myAdlPath = FileUtil.toSystemDependentName(sdk.getHomePath() + FlexSdkUtils.ADL_RELATIVE_PATH);
        myAirRuntimePath = FileUtil.toSystemDependentName(sdk.getHomePath() + FlexSdkUtils.AIR_RUNTIME_RELATIVE_PATH);
      }
    }
  }

  @Nullable
  private static Sdk findSimilarSdk(final String version) {
    final int index = version.lastIndexOf('.');
    if (index <= 0) return null;

    final List<Sdk> sdks = FlexSdkUtils.getAllFlexOrAirOrMobileSdks();

    // try to find the same version
    final String baseVersion = version.substring(0, index);
    final String revision = version.substring(index + 1, version.length());
    Sdk sdk = selectSdk(sdks, new Processor<String>() {
      public boolean process(final String sdkVersion) {
        return sdkVersion.startsWith(baseVersion) && sdkVersion.endsWith(revision);
      }
    });

    if (sdk == null) {
      // the same version not found. Try to find the same major version
      final String majorVersion = version.substring(0, version.indexOf('.') + 1);
      sdk = selectSdk(sdks, new Processor<String>() {
        public boolean process(final String sdkVersion) {
          return sdkVersion.startsWith(majorVersion);
        }
      });
    }

    if (sdk == null) {
      // the same major version not found. Take any.
      if (!sdks.isEmpty()) {
        sdk = sdks.get(0);
      }
    }

    return sdk;
  }

  @Nullable
  private static Sdk selectSdk(final List<Sdk> sdks, final Processor<String> sdkVersionEvaluator) {
    for (final Sdk sdk : sdks) {
      final String sdkVersion = sdk.getVersionString();
      if (sdkVersion != null && sdkVersionEvaluator.process(sdkVersion)) {
        return sdk;
      }
    }
    return null;
  }

  private void addClasspathEntry(final String repositoryRootPath, final String groupId, final String artifactId, final String version) {
    if (StringUtil.isNotEmpty(repositoryRootPath) &&
        StringUtil.isNotEmpty(groupId) &&
        StringUtil.isNotEmpty(artifactId) &&
        StringUtil.isNotEmpty(version)) {
      final StringBuilder classpathEntry = new StringBuilder();
      classpathEntry.append(repositoryRootPath).append('/').append(groupId.replace('.', '/')).append('/').append(artifactId).append('/')
        .append(version).append('/').append(artifactId).append('-').append(version).append(".jar");
      myFlexCompilerClasspath.add(classpathEntry.toString());
    }
  }

  void save(final Element element) {
    final Element compilerClasspathElement = new Element(COMPILER_CLASSPATH_ELEMENT_NAME);
    for (final String classpathEntry : myFlexCompilerClasspath) {
      final Element classpathEntryElement = new Element(CLASSPATH_ENTRY_ELEMENT_NAME);
      classpathEntryElement.setText(classpathEntry);
      compilerClasspathElement.addContent(classpathEntryElement);
    }
    element.addContent(compilerClasspathElement);

    final Element adlPathElement = new Element(ADL_PATH_ELEMENT_NAME);
    adlPathElement.setText(myAdlPath);
    element.addContent(adlPathElement);
    final Element airRuntimePathElement = new Element(AIR_RUNTIME_PATH_ELEMENT_NAME);
    airRuntimePathElement.setText(myAirRuntimePath);
    element.addContent(airRuntimePathElement);
  }

  void load(final Element element) {
    myFlexCompilerClasspath.clear();
    final Element compilerClasspathElement = element.getChild(COMPILER_CLASSPATH_ELEMENT_NAME);
    if (compilerClasspathElement != null) {
      for (Object classpathEntryElement : compilerClasspathElement.getChildren(CLASSPATH_ENTRY_ELEMENT_NAME)) {
        myFlexCompilerClasspath.add(((Element)classpathEntryElement).getText());
      }
    }

    final String adlPath = element.getChildText(ADL_PATH_ELEMENT_NAME);
    myAdlPath = adlPath == null ? "" : adlPath;
    final String airRuntimePath = element.getChildText(AIR_RUNTIME_PATH_ELEMENT_NAME);
    myAirRuntimePath = airRuntimePath == null ? "" : airRuntimePath;
  }
}
