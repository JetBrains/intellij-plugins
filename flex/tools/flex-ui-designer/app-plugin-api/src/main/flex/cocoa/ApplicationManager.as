package cocoa {
import org.flyti.plexus.PlexusManager;

public final class ApplicationManager {
  public var unitTestMode:Boolean;
  
  public static function get instance():ApplicationManager {
    return ApplicationManager(PlexusManager.instance.container.lookup(ApplicationManager));
  }
}
}