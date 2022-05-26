package jetbrains.plugins.yeoman.generators;


import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class YeomanGeneratorFullInfo implements YeomanGeneratorInfo {

  public static YeomanGeneratorFullInfo getFullInfo(YeomanGeneratorInfo info) {

    if (info instanceof YeomanGeneratorFullInfo) {
      return (YeomanGeneratorFullInfo)info;
    }
    return info instanceof YeomanInstalledGeneratorInfo ?
           ((YeomanInstalledGeneratorInfo)info).getFullInfo() :
           null;
  }

  /**
   * all fields from json
   */
  @Nullable
  private String name;

  @Nullable
  private String description;

  @Nullable
  private Object owner;

  @Nullable
  private String ownerWebsite;

  @Nullable
  private String website;

  @Nullable
  private String site;

  @Nullable
  private Object forks;
  @Nullable
  private Object created;
  @Nullable
  private Object updated;
  public int getStars() {
    return stars;
  }

  private int stars;

  private YeomanGeneratorFullInfo() {
  }


  @Nullable
  public String getWebsite() {
    return website == null ? site : website;
  }

  @NlsSafe
  @NotNull
  public String getDescription() {
    return StringUtil.notNullize(description);
  }

  @Override
  @Nullable
  public String getName() {
    return YeomanGeneratorInfo.Util.getName(name);
  }

  @Override
  @Nullable
  public String getYoName() {
    return YeomanGeneratorInfo.Util.getYoName(name);
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof YeomanGeneratorFullInfo && StringUtil.equals(name, ((YeomanGeneratorFullInfo)obj).name);
  }

  @Override
  public YeomanGeneratorFullInfo clone() {
    final YeomanGeneratorFullInfo info = new YeomanGeneratorFullInfo();
    info.name = name;
    info.description = description;
    info.owner = owner;
    info.ownerWebsite = ownerWebsite;
    info.forks = forks;
    info.created = created;
    info.updated = updated;
    info.stars = stars;
    info.website = website;
    info.site = site;
    return info;
  }

  @Override
  public int compareTo(YeomanGeneratorInfo o) {
    if (o instanceof YeomanGeneratorFullInfo) {
      final YeomanGeneratorFullInfo fullInfo = (YeomanGeneratorFullInfo)o;
      return (stars < fullInfo.stars) ? -1 : ((stars == fullInfo.stars) ? StringUtil.compare(this.getName(), o.getName(), false) : 1);
    }
    return StringUtil.compare(this.getName(), o.getName(), false);
  }
}
