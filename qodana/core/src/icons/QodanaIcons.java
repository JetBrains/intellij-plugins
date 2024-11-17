package icons;

import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * NOTE THIS FILE IS AUTO-GENERATED
 * DO NOT EDIT IT BY HAND, run "Generate icon classes" configuration instead
 */
public final class QodanaIcons {
  private static @NotNull Icon load(@NotNull String path, int cacheKey, int flags) {
    return IconManager.getInstance().loadRasterizedIcon(path, QodanaIcons.class.getClassLoader(), cacheKey, flags);
  }
  private static @NotNull Icon load(@NotNull String expUIPath, @NotNull String path, int cacheKey, int flags) {
    return IconManager.getInstance().loadRasterizedIcon(path, expUIPath, QodanaIcons.class.getClassLoader(), cacheKey, flags);
  }

  public static final class Icons {
    public static final class CI {
      /** 16x16 */ public static final @NotNull Icon Azure = load("icons/ci/azure.svg", 580792283, 0);
      /** 16x16 */ public static final @NotNull Icon Bitbucket = load("icons/ci/bitbucket.svg", -1246446995, 0);
      /** 16x16 */ public static final @NotNull Icon CircleCI = load("icons/ci/circleCI.svg", -205896895, 2);
      /** 16x16 */ public static final @NotNull Icon GitHub = load("icons/ci/gitHub.svg", 248269223, 2);
      /** 16x16 */ public static final @NotNull Icon GitLab = load("icons/ci/gitLab.svg", 1254119175, 0);
      /** 16x16 */ public static final @NotNull Icon Jenkins = load("icons/ci/jenkins.svg", -1502763703, 0);
      /** 16x16 */ public static final @NotNull Icon Space = load("icons/ci/space.svg", 785058272, 0);
      /** 16x16 */ public static final @NotNull Icon TeamCity = load("icons/ci/teamCity.svg", 1663093890, 0);
    }

    /** 128x128 */ public static final @NotNull Icon CloudRun_128 = load("icons/cloudRun_128.svg", -793823178, 2);
    /** 16x16 */ public static final @NotNull Icon Critical = load("icons/critical.svg", 1212804026, 0);
    /** 16x16 */ public static final @NotNull Icon High = load("icons/high.svg", -775678687, 0);
    /** 16x16 */ public static final @NotNull Icon Info = load("icons/info.svg", -7204549, 0);

    public static final class InspectionKts {
      /** 16x16 */ public static final @NotNull Icon Error = load("icons/inspectionKts/error.svg", 2027223231, 2);
      /** 16x16 */ public static final @NotNull Icon ErrorOutdated = load("icons/inspectionKts/errorOutdated.svg", 1514465935, 2);
      /** 16x16 */ public static final @NotNull Icon OK = load("icons/inspectionKts/ok.svg", 941922236, 2);
      /** 16x16 */ public static final @NotNull Icon OkOutdated = load("icons/inspectionKts/okOutdated.svg", 1893525822, 2);
    }

    /** 16x16 */ public static final @NotNull Icon LinkedProject = load("icons/linkedProject.svg", 1830981766, 0);
    /** 128x128 */ public static final @NotNull Icon LocalRun_128 = load("icons/localRun_128.svg", -999550325, 2);
    /** 16x16 */ public static final @NotNull Icon LoggedUser = load("icons/loggedUser.svg", 1274846372, 0);
    /** 16x16 */ public static final @NotNull Icon Low = load("icons/low.svg", -477264016, 0);
    /** 16x16 */ public static final @NotNull Icon Moderate = load("icons/moderate.svg", -1843618710, 0);
    /** 32x15 */ public static final @NotNull Icon New = load("icons/new.svg", -1541763305, 2);
    /** 16x16 */ public static final @NotNull Icon NotLinkedProject = load("icons/notLinkedProject.svg", -375535019, 0);
    /** 16x16 */ public static final @NotNull Icon NotLoggedUser = load("icons/notLoggedUser.svg", -2060767249, 0);
    /** 16x16 */ public static final @NotNull Icon Qodana = load("icons/qodana.svg", -1205407055, 0);
    /** 16x16 */ public static final @NotNull Icon Sarif = load("icons/newui/sarif.svg", "icons/sarif.svg", -2129886975, 0);
  }

  public static final class Images {
    /** 140x48 */ public static final @NotNull Icon Qodana = load("images/qodana.svg", -1699885229, 2);
    /** 500x285 */ public static final @NotNull Icon QodanaVideoPreview = load("images/qodanaVideoPreview.png", 0, 7);
  }

  public static final class METAINF {
    /** 40x40 */ public static final @NotNull Icon PluginIcon = load("META-INF/pluginIcon.svg", -2061604975, 0);
  }
}
