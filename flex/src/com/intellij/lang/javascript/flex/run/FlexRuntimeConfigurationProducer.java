package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.build.FlexCompilerSettingsEditor;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class FlexRuntimeConfigurationProducer extends RuntimeConfigurationProducer implements Cloneable {

  private PsiElement mySourceElement;

  private static final String WINDOWED_APPLICATION_CLASS_NAME_1 = "mx.core.WindowedApplication";
  private static final String WINDOWED_APPLICATION_CLASS_NAME_2 = "spark.components.WindowedApplication";

  public FlexRuntimeConfigurationProducer() {
    super(FlexRunConfigurationType.getInstance());
  }

  public PsiElement getSourceElement() {
    return mySourceElement;
  }

  @Nullable
  protected RunnerAndConfigurationSettings createConfigurationByElement(final Location location, final ConfigurationContext context) {
    final Module module = context.getModule();
    if (!(location instanceof PsiLocation) ||
        module == null ||
        FlexSdkUtils.hasDependencyOnAir(module) ||
        FlexSdkUtils.hasDependencyOnAirMobile(module)) {
      return null;
    }
    mySourceElement = location.getPsiElement();

    final String htmlWrapperAbsolutePath = getHtmlWrapperAbsolutePath(module, mySourceElement);
    if (htmlWrapperAbsolutePath != null) {
      return createConfigurationForHtmlWrapper(module, htmlWrapperAbsolutePath);
    }

    final JSClass jsClass = getJSClass(mySourceElement);
    if (jsClass != null && isAcceptedMainClass(jsClass, module, false)) {
      final RunnerAndConfigurationSettings settings =
        RunManagerEx.getInstanceEx(location.getProject()).createConfiguration("", FlexRunConfigurationType.getFactory());
      final FlexRunConfiguration runConfiguration = (FlexRunConfiguration)settings.getConfiguration();
      FlexRunnerParameters runnerParameters = runConfiguration.getRunnerParameters();
      runnerParameters.setModuleName(module.getName());
      runnerParameters.setRunMode(FlexRunnerParameters.RunMode.MainClass);
      runnerParameters.setMainClassName(jsClass.getQualifiedName());
      runConfiguration.setName(generateName(runnerParameters));
      return settings;
    }
    else {
      return null;
    }
  }

  @Nullable
  private static String getHtmlWrapperAbsolutePath(final Module module, final PsiElement context) {
    final PsiFile psiFile = PsiTreeUtil.getNonStrictParentOfType(context, PsiFile.class);
    final VirtualFile vFile = psiFile == null ? null : psiFile.getVirtualFile();
    if (psiFile != null &&
        vFile != null &&
        FlexUtils.isHtmlExtension(FileUtil.getExtension(psiFile.getName())) &&
        FlexUtils.htmlFileLooksLikeSwfWrapper(vFile)) {
      final VirtualFile sourceRoot = ProjectRootManager.getInstance(module.getProject()).getFileIndex().getSourceRootForFile(vFile);
      final Collection<FlexBuildConfiguration> configs = FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module);
      return configs.isEmpty() || sourceRoot == null
             ? vFile.getPath()
             : configs.iterator().next().getCompileOutputPath() + "/" + VfsUtilCore.getRelativePath(vFile, sourceRoot, '/');
    }
    return null;
  }

  private static RunnerAndConfigurationSettings createConfigurationForHtmlWrapper(final Module module, final String htmlPath) {
    final String name = htmlPath.substring(htmlPath.lastIndexOf("/") + 1);

    final RunnerAndConfigurationSettings settings =
      RunManagerEx.getInstanceEx(module.getProject()).createConfiguration(name, FlexRunConfigurationType.getFactory());
    final FlexRunConfiguration runConfiguration = (FlexRunConfiguration)settings.getConfiguration();
    final FlexRunnerParameters runnerParameters = runConfiguration.getRunnerParameters();
    runnerParameters.setModuleName(module.getName());
    runnerParameters.setRunMode(FlexRunnerParameters.RunMode.HtmlOrSwfFile);
    runnerParameters.setHtmlOrSwfFilePath(htmlPath);
    return settings;
  }

  @Override
  protected RunnerAndConfigurationSettings findExistingByElement(final Location location,
                                                                 final @NotNull RunnerAndConfigurationSettings[] existingConfigurations,
                                                                 final ConfigurationContext context) {
    if (existingConfigurations.length == 0) return null;
    final Module module = context.getModule();
    if (!(location instanceof PsiLocation) || module == null) return null;
    final PsiElement psiElement = location.getPsiElement();

    final String htmlWrapperAbsolutePath = getHtmlWrapperAbsolutePath(module, psiElement);
    if (htmlWrapperAbsolutePath != null) {
      for (final RunnerAndConfigurationSettings existingConfiguration : existingConfigurations) {
        final FlexRunnerParameters runnerParameters =
          ((FlexRunConfiguration)existingConfiguration.getConfiguration()).getRunnerParameters();
        if (module.getName().equals(runnerParameters.getModuleName()) &&
            FlexRunnerParameters.RunMode.HtmlOrSwfFile == runnerParameters.getRunMode() &&
            runnerParameters.getHtmlOrSwfFilePath().equals(htmlWrapperAbsolutePath)) {
          return existingConfiguration;
        }
      }
    }

    final JSClass jsClass = getJSClass(psiElement);

    if (jsClass != null && isAcceptedMainClass(jsClass, module, false)) {
      for (final RunnerAndConfigurationSettings existingConfiguration : existingConfigurations) {
        final FlexRunnerParameters runnerParameters =
          ((FlexRunConfiguration)existingConfiguration.getConfiguration()).getRunnerParameters();
        if (module.getName().equals(runnerParameters.getModuleName()) &&
            FlexRunnerParameters.RunMode.MainClass == runnerParameters.getRunMode() &&
            runnerParameters.getMainClassName().equals(jsClass.getQualifiedName())) {
          return existingConfiguration;
        }
      }
    }

    return null;
  }

  @Nullable
  static JSClass getJSClass(final PsiElement sourceElement) {
    PsiElement element = PsiTreeUtil.getNonStrictParentOfType(sourceElement, JSClass.class, JSFile.class, XmlFile.class);
    if (element instanceof JSFile) {
      element = JSPsiImplUtils.findClass((JSFile)element);
    }
    else if (element instanceof XmlFile) {
      element = XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)element);
    }
    return element instanceof JSClass ? (JSClass)element : null;
  }

  public static boolean isAcceptedMainClass(final JSClass jsClass, final Module module, final boolean allowWindowedApplicationInheritors) {
    if (jsClass == null || module == null) return false;
    final JSAttributeList attributeList = jsClass.getAttributeList();
    if (attributeList == null || attributeList.getAccessType() != JSAttributeList.AccessType.PUBLIC) return false;
    final String jsClassName = jsClass.getQualifiedName();
    if (jsClassName == null) return false;
    final PsiElement spriteClass = JSResolveUtil.unwrapProxy(
      JSResolveUtil.findClassByQName(FlexCompilerSettingsEditor.SPRITE_CLASS_NAME, GlobalSearchScope.moduleWithLibrariesScope(module)));
    if (!(spriteClass instanceof JSClass)) return false;

    final boolean isSpriteInheritor = JSInheritanceUtil.isParentClass(jsClass, (JSClass)spriteClass);

    if (allowWindowedApplicationInheritors) {
      return isSpriteInheritor;
    }
    else {
      final PsiElement windowedApplicationClass1 = JSResolveUtil
        .unwrapProxy(JSResolveUtil.findClassByQName(WINDOWED_APPLICATION_CLASS_NAME_1, GlobalSearchScope.moduleWithLibrariesScope(module)));
      final PsiElement windowedApplicationClass2 = JSResolveUtil
        .unwrapProxy(JSResolveUtil.findClassByQName(WINDOWED_APPLICATION_CLASS_NAME_2, GlobalSearchScope.moduleWithLibrariesScope(module)));

      final boolean isWindowedApplicationInheritor = windowedApplicationClass1 instanceof JSClass &&
                                                     JSInheritanceUtil.isParentClass(jsClass, (JSClass)windowedApplicationClass1) ||
                                                     windowedApplicationClass2 instanceof JSClass &&
                                                     JSInheritanceUtil.isParentClass(jsClass, (JSClass)windowedApplicationClass2);

      return isSpriteInheritor && !isWindowedApplicationInheritor;
    }
  }

  private static String generateName(final FlexRunnerParameters params) {
    if (params.getRunMode() == FlexRunnerParameters.RunMode.MainClass) {
      return StringUtil.getShortName(params.getMainClassName());
    }
    return "Unnamed";
  }

  public int compareTo(Object o) {
    return PREFERED;
  }
}
