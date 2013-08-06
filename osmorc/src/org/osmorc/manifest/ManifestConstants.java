package org.osmorc.manifest;

/**
 * Created with IntelliJ IDEA.
 * User: Vladislav.Soroka
 */
public class ManifestConstants {
  private ManifestConstants() {
  }

  public class Headers {
    public static final String BUNDLE_MANIFEST_VERSION = "Bundle-ManifestVersion";
    public static final String BUNDLE_NAME = "Bundle-Name";
    public static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";
    public static final String BUNDLE_VERSION = "Bundle-Version";
    public static final String EXPORT_PACKAGE = "Export-Package";
    public static final String IMPORT_PACKAGE = "Import-Package";
    public static final String REQUIRE_BUNDLE = "Require-Bundle";
    public static final String BUNDLE_REQUIRED_EXECUTION_ENV = "Bundle-RequiredExecutionEnvironment";
    public static final String FRAGMENT_HOST = "Fragment-Host";
    public static final String BUNDLE_ACTIVATION_POLICY = "Bundle-ActivationPolicy";
    public static final String BUNDLE_ACTIVATOR = "Bundle-Activator";
    public static final String BUNDLE_CATEGORY = "Bundle-Category";
    public static final String BUNDLE_CLASS_PATH = "Bundle-ClassPath";
    public static final String BUNDLE_CONTACT_ADDRESS = "Bundle-ContactAddress";
    public static final String BUNDLE_COPYRIGHT = "Bundle-Copyright";
    public static final String BUNDLE_DESCRIPTION = "Bundle-Description";
    public static final String BUNDLE_DOC_URL = "Bundle-DocURL";
    public static final String BUNDLE_LOCALIZATION = "Bundle-Localization";
    public static final String BUNDLE_NATIVE_CODE = "Bundle-NativeCode";
    public static final String BUNDLE_UPDATE_LOCATION = "Bundle-UpdateLocation";
    public static final String BUNDLE_VENDOR = "Bundle-Vendor";
    public static final String DYNAMIC_IMPORT_PACKAGE = "DynamicImport-Package";
    public static final String EXPORT_SERVICE = "Export-Service";
    public static final String IMPORT_SERVICE = "Import-Service";
    public static final String SERVICE_COMPONENT = "Service-Component";

    private Headers() {
    }
  }

  public class Attributes {
    public final static String USES = "uses";
    public final static String VERSION = "version";

    private Attributes() {
    }
  }

  public class Directives {
    public final static String NO_IMPORT = "-noimport";
    public final static String SPLIT_PACKAGE = "-split-package";

    private Directives() {
    }
  }
}
