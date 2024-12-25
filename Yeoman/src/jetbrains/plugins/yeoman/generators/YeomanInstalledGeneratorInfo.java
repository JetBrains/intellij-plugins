package jetbrains.plugins.yeoman.generators;

import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class YeomanInstalledGeneratorInfo implements YeomanGeneratorInfo {

  private final @NotNull String myName;

  private final @NotNull String myFilePath;
  private final boolean myIsGlobal;

  public @Nullable YeomanGeneratorFullInfo getFullInfo() {
    return myFullInfo;
  }

  private @Nullable YeomanGeneratorFullInfo myFullInfo;

  public YeomanInstalledGeneratorInfo(VirtualFile file) {
    myName = file.getNameWithoutExtension();
    //noinspection ConstantConditions
    myFilePath = file.getCanonicalPath();
    myIsGlobal = true;
  }

  public YeomanInstalledGeneratorInfo(File file) {
    myName = FileUtilRt.getNameWithoutExtension(file.getName());
    myFilePath = file.getAbsolutePath();
    myIsGlobal = false;
  }


  @Override
  public @NotNull String getYoName() {
    return YeomanGeneratorInfo.Util.getYoName(myName);
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  public @NotNull String getFilePath() {
    return myFilePath;
  }

  @Override
  public int compareTo(YeomanGeneratorInfo o) {
    return StringUtil.compare(this.getName(), o.getName(), false);
  }

  public boolean isGlobal() {
    return myIsGlobal;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof YeomanGeneratorInfo)) return false;


    if (!getName().equals(((YeomanGeneratorInfo)o).getName())) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }
}
