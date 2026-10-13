package jetbrains.plugins.yeoman;


import com.intellij.testFramework.junit5.TestApplication;
import jetbrains.plugins.yeoman.generators.YeomanGeneratorListProvider;
import jetbrains.plugins.yeoman.generators.YeomanInstalledGeneratorListProvider;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public class YeomanGeneratorsTest {

  @Test
  public void testDownload() throws IOException {
    final File file = new YeomanGeneratorListProvider().downloadJsonWithData();
    assertNotNull(file);
    assertTrue(file.exists());
  }

  @Test
  public void testGetListOfGlobalInstalledGenerator() {
    assertNotNull(new YeomanInstalledGeneratorListProvider().getAllInstalledGenerators());
  }

}
