/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.util;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.Version;
import com.intellij.util.JdkBundle;
import com.intellij.util.JdkBundleList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class JdkSetupUtil {

    @NonNls
    private static final File bundledJdkFile = getBundledJDKFile();


    @NotNull
    private static File getBundledJDKFile() {
        StringBuilder bundledJDKPath = new StringBuilder("jre");
        if (SystemInfo.isMac) {
            bundledJDKPath.append(File.separator).append("jdk");
        }
        return new File(bundledJDKPath.toString());
    }

    private static final String STANDARD_JDK_LOCATION_ON_MAC_OS_X = "/Library/Java/JavaVirtualMachines/";
    private static final String[] STANDARD_JVM_LOCATIONS_ON_LINUX = new String[]{
            "/usr/lib/jvm/", // Ubuntu
            "/usr/java/"     // Fedora
    };
    private static final String STANDARD_JVM_X64_LOCATIONS_ON_WINDOWS = "Program Files/Java";

    private static final String STANDARD_JVM_X86_LOCATIONS_ON_WINDOWS = "Program Files (x86)/Java";

    private static final Version JDK8_VERSION = new Version(1, 8, 0);

    @NotNull
    public static JdkBundleList findJdkPaths() {
        JdkBundle bootJdk = JdkBundle.createBoot();

        JdkBundleList jdkBundleList = new JdkBundleList();
        if (bootJdk != null) {
            jdkBundleList.addBundle(bootJdk, true);
        }

        if (new File(PathManager.getHomePath() + File.separator + bundledJdkFile).exists()) {
            JdkBundle bundledJdk = JdkBundle.createBundle(bundledJdkFile, false, true);
            if (bundledJdk != null) {
                jdkBundleList.addBundle(bundledJdk, true);
            }
        }

        if (SystemInfo.isMac) {
            jdkBundleList.addBundlesFromLocation(STANDARD_JDK_LOCATION_ON_MAC_OS_X, JDK8_VERSION, null);
        } else if (SystemInfo.isLinux) {
            for (String location : STANDARD_JVM_LOCATIONS_ON_LINUX) {
                jdkBundleList.addBundlesFromLocation(location, JDK8_VERSION, null);
            }
        } else if (SystemInfo.isWindows) {
            for (File root : File.listRoots()) {
                if (SystemInfo.is32Bit) {
                    jdkBundleList.addBundlesFromLocation(new File(root, STANDARD_JVM_X86_LOCATIONS_ON_WINDOWS).getAbsolutePath(), JDK8_VERSION, null);
                } else {
                    jdkBundleList.addBundlesFromLocation(new File(root, STANDARD_JVM_X64_LOCATIONS_ON_WINDOWS).getAbsolutePath(), JDK8_VERSION, null);
                }
            }
        }

        return jdkBundleList;
    }

    public static String getJavaHomePath(JdkBundle jdkBundle){
        String homeSubPath = SystemInfo.isMac ? "/Contents/Home" : "";
        return jdkBundle.getLocation().getAbsolutePath() + homeSubPath;
    }
}

