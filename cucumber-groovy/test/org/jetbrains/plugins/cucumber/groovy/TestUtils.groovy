package org.jetbrains.plugins.cucumber.groovy
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.io.FileUtil
/**
 * @author Max Medvedev
 */
class TestUtils {
  public static final String CUCUMBER_GROOVY_1_0_14 = 'cucumber-groovy-1.0.14.jar'
  public static final String CUCUMBER_CORE_1_0_14 = 'cucumber-core-1.0.14.jar'

  private TestUtils() {}

  public static String getAbsoluteTestDataPath() {
    return "$absolutePluginPath/testData/"
  }

  private static String getAbsolutePluginPath() {
    return FileUtil.toSystemIndependentName(new File(PathManager.homePath, "contrib/cucumber-groovy/").path)
  }

  public static String getMockGroovyCucumberLibraryHome() {
    return "$absoluteTestDataPath/mockGroovyCucumberLib-1.0.14";
  }

  public static List<String> getMockGroovyCucumberLibraryNames() {
    String home = mockGroovyCucumberLibraryHome
    return ["$home/$CUCUMBER_GROOVY_1_0_14", "$home/$CUCUMBER_CORE_1_0_14"]
  }
}
