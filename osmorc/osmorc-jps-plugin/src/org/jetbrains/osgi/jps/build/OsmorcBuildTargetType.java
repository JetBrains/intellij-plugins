package org.jetbrains.osgi.jps.build;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.ModuleBasedBuildTargetType;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.serialization.JpsModelSerializationDataService;
import org.jetbrains.osgi.jps.model.JpsOsmorcExtensionService;
import org.jetbrains.osgi.jps.model.JpsOsmorcModuleExtension;
import org.jetbrains.osgi.jps.model.JpsOsmorcProjectExtension;

import com.intellij.util.containers.ContainerUtil;

import aQute.bnd.build.Project;
import aQute.bnd.build.Workspace;

/**
 * @author michael.golubev
 */
public class OsmorcBuildTargetType extends ModuleBasedBuildTargetType<OsmorcBuildTarget> {
  public static final OsmorcBuildTargetType INSTANCE = new OsmorcBuildTargetType();

  private OsmorcBuildTargetType() {
    super("osmorc");
  }

  @NotNull
  @Override
  public List<OsmorcBuildTarget> computeAllTargets(@NotNull JpsModel model) {
    List<OsmorcBuildTarget> targets = ContainerUtil.newArrayList();

    try {
      JpsOsmorcProjectExtension projectExtension = JpsOsmorcExtensionService.getExtension(model.getProject());
      Workspace workspace = null;
      if (projectExtension.isBndWorkspace()) {
        File baseDirectory = JpsModelSerializationDataService.getBaseDirectory(model.getProject());
        workspace = Workspace.getWorkspace(baseDirectory);
      }

      for (JpsModule module : model.getProject().getModules()) {
        Project project = workspace != null ? workspace.getProject(module.getName()): null;
        if (project != null && !project.isNoBundles()) {
          // Module is part of the bnd workspace
          targets.add(new OsmorcBuildTarget(project, module));
        }
        else {
          // Module is not part of the bnd workspace, check if it has the OSGi facet
          JpsOsmorcModuleExtension extension = JpsOsmorcExtensionService.getExtension(module);
          if (extension != null) {
            targets.add(new OsmorcBuildTarget(extension, module));
          }
        }
      }
    } catch(Exception e){
      throw new RuntimeException("Unexpected error " + e.getMessage(), e);
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
      myTargets = ContainerUtil.newHashMap();
      for (JpsModule module : model.getProject().getModules()) {
        JpsOsmorcModuleExtension extension = JpsOsmorcExtensionService.getExtension(module);
        if (extension != null) {
          myTargets.put(module.getName(), new OsmorcBuildTarget(extension, module));
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
