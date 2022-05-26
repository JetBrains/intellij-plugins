package jetbrains.plugins.yeoman.generators;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.platform.templates.github.DownloadUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import jetbrains.plugins.yeoman.projectGenerator.ui.run.YeomanRunGeneratorForm;
import jetbrains.plugins.yeoman.settings.YeomanGlobalSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class YeomanGeneratorListProvider {
  public static final Logger LOGGER = Logger.getInstance(YeomanRunGeneratorForm.class);

  private static final String[] BLACKLIST = {
    "ft-wp",
    "generator-angular-phonegap",
    "generator-react-coffee-webpack",
    "generator-angular-sparkstack",
    "generator-ayrofast",
    "generator-charcoal",
    "generator-express-angular",
    "generator-fv1",
    "generator-gulp-webapp",
    "generator-ionicjs",
    "generator-meteor-abtx",
    "generator-mtb-app",
    "generator-onereq",
    "generator-react-coffee-webpack",
    "generator-wordpress-bob",
    "generator-polymer-plus",
    "generator-vmweb",
    "generator-gulp-angular-pregiotek",
    "generator-auto",
    "generator-aspnetdnx",
    "generator-aspnetdnx2"
  };


  public static final String GENERATOR_INFO_RELATIVE_PATH = "extLibs" +
                                                            File.separator +
                                                            "yeoman" +
                                                            File.separator +
                                                            "generators_info";
  public static final String INFO_FILE_NAME = "yeoman-generators-info_2.json";

  private final YeomanGlobalSettings mySettings;

  private final Lock myLock = new ReentrantLock();
  private volatile List<YeomanGeneratorFullInfo> myLoadedList = null;

  public YeomanGeneratorListProvider() {
    mySettings = YeomanGlobalSettings.getInstance();
  }

  private String read(boolean force) {
    try {
      return new String(FileUtil.loadFileText(getGeneratorsFile(force), mySettings.getGeneratorsFileDefaultEncoding()));
    }
    catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }


  public List<YeomanGeneratorFullInfo> getAvailableGenerators(boolean forceDownload) {
    if (!forceDownload) {
      final List<YeomanGeneratorFullInfo> list = myLoadedList;
      if (list != null) return list;
    }

    String read = read(forceDownload);

    try {
      final YeomanGeneratorFullInfo[] parse = new YeomanGeneratorListParser().parse(read);
      final List<YeomanGeneratorFullInfo> result =
        ContainerUtil.reverse(ContainerUtil.filter(parse, info -> !ArrayUtil.contains(info.getName(), BLACKLIST)));

      myLoadedList = result;

      return result;
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage(), e);
      cleanAvailableGenerators();
    }

    return myLoadedList == null ? ContainerUtil.emptyList() : myLoadedList;
  }

  private File getGeneratorsFile(boolean force) throws IOException {
    final File file = getStoreFile();
    if (force && file.exists()) {
      try {
        return downloadJsonWithDataImpl();
      }
      catch (Exception e) {
        LOGGER.debug(e.getMessage(), e);
      }
    }
    if (file.exists()) return file;
    return downloadJsonWithDataImpl();
  }


  @TestOnly
  public File downloadJsonWithData() throws IOException {
    return downloadJsonWithDataImpl();
  }

  @NotNull
  private File downloadJsonWithDataImpl() throws IOException {
    if (myLock.tryLock()) {
      try {
        File yoGeneratorsFile = getStoreFile();
        FileUtil.createParentDirs(yoGeneratorsFile);
        DownloadUtil.downloadAtomically(null, mySettings.getGeneratorsFileUrl(), yoGeneratorsFile);
        return yoGeneratorsFile;
      }
      finally {
        myLock.unlock();
      }
    }
    else {
      //don't try to download again
      myLock.lock();
      try {
        File yoGeneratorsFile = getStoreFile();
        if (yoGeneratorsFile.exists()) {
          return yoGeneratorsFile;
        }
      }
      finally {
        myLock.unlock();
      }

      return downloadJsonWithDataImpl();
    }
  }

  public void cleanAvailableGenerators() {
    myLock.lock();
    try {
      final File file = getStoreFile();
      if (file.exists()) {
        FileUtil.delete(file);
      }
    }
    finally {
      myLock.unlock();
    }
  }

  public boolean isAvailableGeneratorListExists() {
    return getStoreFile().exists();
  }

  @NotNull
  private static File getStoreFile() {
    return new File(PathManager.getSystemPath(),
                    GENERATOR_INFO_RELATIVE_PATH +
                    File.separator +
                    INFO_FILE_NAME);
  }
}
