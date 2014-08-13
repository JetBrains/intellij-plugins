package org.jetbrains.osgi.jps.build;

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

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * A wrapper around SpringSource's Bundlor tool.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class BundlorWrapper {
  public List<String> wrapModule(@NotNull Properties properties,
                                 @NotNull File inputJar,
                                 @NotNull File outputJar,
                                 @NotNull File manifestTemplate) throws OsgiBuildException {
    String inputPath = inputJar.getPath();
    ManifestContents manifest;

    try {
      PropertiesSource propertiesSource = new PropertiesPropertiesSource(properties);

      ManifestGenerator generator = new StandardManifestGenerator(DefaultManifestGeneratorContributorsFactory.create(propertiesSource));
      ClassPath classPath = new StandardClassPathFactory().create(inputPath);
      ManifestContents contents = new StandardManifestTemplateFactory().create(manifestTemplate.getPath(), null, null, null);
      manifest = generator.generate(contents, classPath);

      ManifestWriter manifestWriter = new StandardManifestWriterFactory().create(inputPath, outputJar.getPath());
      try {
        manifestWriter.write(manifest);
      }
      finally {
        manifestWriter.close();
      }
    }
    catch (Exception e) {
      throw new OsgiBuildException("Bundlor failed: error generating manifest", e, null);
    }

    return new StandardManifestValidator(DefaultManifestValidatorContributorsFactory.create()).validate(manifest);
  }
}
