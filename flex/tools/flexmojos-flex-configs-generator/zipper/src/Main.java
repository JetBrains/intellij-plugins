import com.intellij.util.io.ZipUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

public class Main {
  public static void main(String[] args) throws IOException {
    final File outFile = new File("/Users/develar/Documents/idea/flex/resources/flexmojos-configurator.zip");
    ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(outFile));
    ZipUtil.addDirToZipRecursively(zip, outFile,
                                   new File(System.getProperty("user.home"), ".m2/repository/com/intellij/flex/maven"),
                                   "", new FileFilter() {
      @Override
      public boolean accept(File file) {
        if (file.isHidden()) {
          return file.getName().equals(".m2");
        }
        else if (file.isDirectory()) {
          //return !file.getName().equals("idea-configurator");
          return true;
        }

        final String name = file.getName();
        return name.endsWith(".jar") || name.endsWith(".pom");
      }
    }, null);

    //ZipUtil.addFileToZip(zip, new File("/Users/develar/Documents/flexmojos-idea-configurator/idea-configurator/target/classes/com/intellij/flex/maven/IdeaConfigurator.class"), "com/intellij/flex/maven/IdeaConfigurator.class", null, null);

    zip.close();
  }
}
