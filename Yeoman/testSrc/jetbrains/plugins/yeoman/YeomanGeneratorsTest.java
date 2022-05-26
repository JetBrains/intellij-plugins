package jetbrains.plugins.yeoman;


import com.intellij.testFramework.LightPlatformTestCase;
import jetbrains.plugins.yeoman.generators.YeomanGeneratorListProvider;
import jetbrains.plugins.yeoman.generators.YeomanInstalledGeneratorListProvider;

import java.io.File;
import java.io.IOException;

public class YeomanGeneratorsTest extends LightPlatformTestCase {

  public void testDownload() throws IOException {
    final File file = new YeomanGeneratorListProvider().downloadJsonWithData();
    assertNotNull(file);
    assertTrue(file.exists());
  }

  public void testGetListOfGlobalInstalledGenerator() {
    assertNotNull(new YeomanInstalledGeneratorListProvider().getAllInstalledGenerators());
  }

}
