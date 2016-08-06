package training.util;

import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.Version;
import com.intellij.util.JdkBundle;
import com.intellij.util.JdkBundleList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Locale;

/**
 * Created by jetbrains on 05/08/16.
 */
public class JdkSetupUtil {

    @NonNls
    private static final String productJdkConfigFileName =
            getExecutable() + (SystemInfo.isWindows ? ((SystemInfo.is64Bit) ? "64.exe.jdk" : ".exe.jdk") : ".jdk");
    @NonNls
    private static final File productJdkConfigFile = new File(PathManager.getConfigPath(), productJdkConfigFileName);
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

//    @Override
//    public void update(AnActionEvent e) {
//        e.getPresentation().setText("Switch Boot JDK");
//    }
//
//    @Override
//    public void actionPerformed(AnActionEvent event) {
//
//        if (!productJdkConfigFile.exists()) {
//            try {
//                if (!productJdkConfigFile.createNewFile()) {
//                    LOG.error("Could not create " + productJdkConfigFileName + " productJdkConfigFile");
//                    return;
//                }
//            } catch (IOException e) {
//                LOG.error(e);
//                return;
//            }
//        }
//
//        com.intellij.openapi.util.SwitchBootJdkAction.SwitchBootJdkDialog dialog = new com.intellij.openapi.util.SwitchBootJdkAction.SwitchBootJdkDialog();
//        if (dialog.showAndGet()) {
//            File selectedJdkBundleFile = dialog.getSelectedFile();
//            FileWriter fooWriter = null;
//            try {
//                //noinspection IOResourceOpenedButNotSafelyClosed
//                fooWriter = new FileWriter(productJdkConfigFile, false);
//                fooWriter.write(selectedJdkBundleFile.getPath());
//            } catch (IOException e) {
//                LOG.error(e);
//            } finally {
//                try {
//                    if (fooWriter != null) {
//                        fooWriter.close();
//                    }
//                } catch (IOException e) {
//                    LOG.error(e);
//                }
//            }
//            ApplicationManager.getApplication().restart();
//        }
//    }

//    private static class SwitchBootJdkDialog extends DialogWrapper {
//
//        @NotNull
//        private final ComboBox myComboBox;
//
//        private SwitchBootJdkDialog() {
//            super((Project) null, false);
//
//            final JdkBundleList pathsList = findJdkPaths();
//
//            myComboBox = new ComboBox();
//
//            final DefaultComboBoxModel<JdkBundle> model = new DefaultComboBoxModel<JdkBundle>();
//
//            for (JdkBundle jdkBundlePath : pathsList.toArrayList()) {
//                //noinspection unchecked
//                model.addElement(jdkBundlePath);
//            }
//
//            model.addElement(null);
//
//            model.addListDataListener(new ListDataListener() {
//                @Override
//                public void intervalAdded(ListDataEvent e) {
//                }
//
//                @Override
//                public void intervalRemoved(ListDataEvent e) {
//                }
//
//                @Override
//                public void contentsChanged(ListDataEvent e) {
//                    if (myComboBox.getSelectedItem() == null) {
//                        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false) {
//                            @Override
//                            public boolean isFileSelectable(final VirtualFile file) {
//                                if (!super.isFileSelectable(file)) return false;
//                                JdkBundle bundle = JdkBundle.createBundle(new File(file.getPath()), false, false);
//                                if (bundle == null) return false;
//                                Version version = bundle.getVersion();
//
//                                return version != null && !version.lessThan(JDK8_VERSION.major, JDK8_VERSION.minor, JDK8_VERSION.bugfix);
//                            }
//                        };
//
//                        FileChooser.chooseFiles(descriptor, null, null, files -> {
//                            if (files.size() > 0) {
//                                final File jdkFile = new File(files.get(0).getPath());
//                                JdkBundle selectedJdk = pathsList.getBundle(jdkFile.getPath());
//                                if (selectedJdk == null) {
//                                    selectedJdk = JdkBundle.createBundle(jdkFile, false, false);
//                                    if (selectedJdk != null) {
//                                        pathsList.addBundle(selectedJdk, true);
//                                        if (model.getSize() > 0) {
//                                            model.insertElementAt(selectedJdk, model.getSize() - 1);
//                                        } else {
//                                            model.addElement(selectedJdk);
//                                        }
//                                    } else {
//                                        LOG.error("Cannot create bundle for path: " + jdkFile.getPath());
//                                        return;
//                                    }
//                                }
//                                myComboBox.setSelectedItem(selectedJdk);
//                            }
//                        });
//                    }
//                    if (myComboBox.getSelectedItem() == null) {
//                        myComboBox.setSelectedItem(model.getElementAt(0));
//                    }
//                    setOKActionEnabled(myComboBox.getSelectedItem() != null && !((JdkBundle) myComboBox.getSelectedItem()).isBoot());
//                }
//            });
//
//            //noinspection unchecked
//            myComboBox.setModel(model);
//
//            myComboBox.setRenderer(new ListCellRendererWrapper() {
//                @Override
//                public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
//                    if (value != null) {
//                        JdkBundle jdkBundleDescriptor = ((JdkBundle) value);
//                        if (jdkBundleDescriptor.isBoot()) {
//                            setForeground(JBColor.DARK_GRAY);
//                        }
//                        setText(jdkBundleDescriptor.getVisualRepresentation());
//                    } else {
//                        setText("...");
//                    }
//                }
//            });
//
//            setTitle("Switch IDE Boot JDK");
//            setOKActionEnabled(false); // First item is a boot jdk
//            init();
//        }
//
//        @Nullable
//        @Override
//        protected JComponent createNorthPanel() {
//            return new JBLabel("Select Boot JDK");
//        }
//
//        @Nullable
//        @Override
//        protected JComponent createCenterPanel() {
//            return myComboBox;
//        }
//
//        @Nullable
//        @Override
//        public JComponent getPreferredFocusedComponent() {
//            return myComboBox;
//        }
//
//        public File getSelectedFile() {
//            return ((JdkBundle) myComboBox.getSelectedItem()).getLocation();
//        }
//    }

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

    @NotNull
    private static String getExecutable() {
        final String executable = System.getProperty("idea.executable");
        return executable != null ? executable : ApplicationNamesInfo.getInstance().getProductName().toLowerCase(Locale.US);
    }

    public static String getJavaHomePath(JdkBundle jdkBundle){
        String homeSubPath = SystemInfo.isMac ? "/Contents/Home" : "";
        return jdkBundle.getLocation().getAbsolutePath() + homeSubPath;
    }
}

