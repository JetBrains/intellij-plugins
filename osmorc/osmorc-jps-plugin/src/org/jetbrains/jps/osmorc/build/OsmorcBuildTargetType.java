package org.jetbrains.jps.osmorc.build;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.ModuleBasedBuildTargetType;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.osmorc.model.JpsOsmorcExtensionService;
import org.jetbrains.jps.osmorc.model.JpsOsmorcModuleExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author michael.golubev
 */
public class OsmorcBuildTargetType extends ModuleBasedBuildTargetType<OsmorcBuildTarget> {

  public static final OsmorcBuildTargetType INSTANCE = new OsmorcBuildTargetType();
  public static final String TYPE_ID = "osmorc";

  private OsmorcBuildTargetType() {
    super(TYPE_ID);
  }

  @NotNull
  @Override
  public List<OsmorcBuildTarget> computeAllTargets(@NotNull JpsModel model) {
    List<OsmorcBuildTarget> targets = new ArrayList<OsmorcBuildTarget>();
    JpsOsmorcExtensionService service = JpsOsmorcExtensionService.getInstance();
    for (JpsModule module : model.getProject().getModules()) {
      JpsOsmorcModuleExtension extension = service.getExtension(module);
      if (extension != null) {
        targets.add(new OsmorcBuildTarget(extension));
      }
    }
    return targets;
  }

  @NotNull
  @Override
  public BuildTargetLoader<OsmorcBuildTarget> createLoader(@NotNull JpsModel model) {
    return new Loader(model);
  }

  private static class Loader extends BuildTargetLoader<OsmorcBuildTarget> {
    private Map<String, OsmorcBuildTarget> myTargets;

    public Loader(JpsModel model) {
      myTargets = new HashMap<String, OsmorcBuildTarget>();
      JpsOsmorcExtensionService service = JpsOsmorcExtensionService.getInstance();
      for (JpsModule module : model.getProject().getModules()) {
        JpsOsmorcModuleExtension extension = service.getExtension(module);
        if (extension != null) {
          myTargets.put(module.getName(), new OsmorcBuildTarget(extension));
        }
      }
    }

    @Nullable
    @Override
    public OsmorcBuildTarget createTarget(@NotNull String targetId) {
      return myTargets.get(targetId);
    }
  }
}
