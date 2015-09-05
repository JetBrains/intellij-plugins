package {
import mx.resources.ResourceManager;
[ResourceBundle("sdk_resources")]
public class ResourceBundleInSdkSources_sdk_src {
    ResourceManager.getInstance().getString("sdk_resources", "my.key");
}
}