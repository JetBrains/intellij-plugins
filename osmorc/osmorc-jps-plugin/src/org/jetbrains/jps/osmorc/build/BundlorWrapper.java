package org.jetbrains.jps.osmorc.build;

import com.springsource.bundlor.ClassPath;
import com.springsource.bundlor.ManifestGenerator;
import com.springsource.bundlor.ManifestWriter;
import com.springsource.bundlor.ant.internal.support.StandardManifestTemplateFactory;
import com.springsource.bundlor.blint.support.DefaultManifestValidatorContributorsFactory;
import com.springsource.bundlor.blint.support.StandardManifestValidator;
import com.springsource.bundlor.support.DefaultManifestGeneratorContributorsFactory;
import com.springsource.bundlor.support.StandardManifestGenerator;
import com.springsource.bundlor.support.classpath.StandardClassPathFactory;
import com.springsource.bundlor.support.manifestwriter.StandardManifestWriterFactory;
import com.springsource.bundlor.support.properties.PropertiesPropertiesSource;
import com.springsource.bundlor.support.properties.PropertiesSource;
import com.springsource.util.parser.manifest.ManifestContents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.osmorc.util.MavenIntegrationUtil;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * A wrapper around SpringSource's Bundlor tool.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class BundlorWrapper {

  public void wrapModule(@NotNull OsmorcBuildSession session,
                         @NotNull File inputJar,
                         @NotNull File manifestTemplateFile) throws OsmorcBuildException {
    String inputPath = inputJar.getAbsolutePath();
    ManifestContents manifest;

    try {
      Properties properties = MavenIntegrationUtil.getMavenProjectProperties(session);
      PropertiesSource propertiesSource = new PropertiesPropertiesSource(properties);

      ManifestGenerator generator = new StandardManifestGenerator(DefaultManifestGeneratorContributorsFactory.create(propertiesSource));
      ClassPath classPath = new StandardClassPathFactory().create(inputPath);
      ManifestContents contents = new StandardManifestTemplateFactory().create(manifestTemplateFile.getAbsolutePath(), null, null, null);
      manifest = generator.generate(contents, classPath);

      ManifestWriter manifestWriter = new StandardManifestWriterFactory().create(inputPath, session.getOutputJarFile().getAbsolutePath());
      try {
        manifestWriter.write(manifest);
      }
      finally {
        manifestWriter.close();
      }
    }
    catch (Exception e) {
      throw new OsmorcBuildException("Bundlifying the file with Bundlor failed: error generating manifest", e, inputJar);
    }

    List<String> warningsList = new StandardManifestValidator(DefaultManifestValidatorContributorsFactory.create()).validate(manifest);
    for (String s : warningsList) {
      session.warn(s, manifestTemplateFile);
    }
  }
}
