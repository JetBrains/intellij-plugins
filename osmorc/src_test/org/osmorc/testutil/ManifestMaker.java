package org.osmorc.testutil;

import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;

import java.util.*;

/**
 * Helper class for quickly generating a manifest.
 */
public class ManifestMaker {

  private Map<String, List<String>> headerValues;

  private ManifestMaker() {
    headerValues = new HashMap<String, List<String>>();
  }

  /**
   * Creates a new manifest maker.
   *
   * @param bundleSymbolicName the bundle symbolic name for the manifest.
   * @return a new manifest maker.
   */
  public static ManifestMaker bundleSymbolicName(@NotNull String bundleSymbolicName) {
    ManifestMaker mm = new ManifestMaker();
    mm.setHeader(Constants.BUNDLE_SYMBOLICNAME, bundleSymbolicName);
    return mm;
  }

  /**
   * Adds the given entries to the Bundle-Classpath header. Can be called multiple times.
   *
   * @param entries the entries to be added
   * @return this manifest maker.
   */
  public ManifestMaker bundleClassPath(String... entries) {
    return addHeaderValues(Constants.BUNDLE_CLASSPATH, entries);
  }

  /**
   * Sets the bundle version
   *
   * @param version the version of this bundle.
   * @return this manifest maker
   */
  public ManifestMaker bundleVersion(@NotNull String version) {
    return setHeader(Constants.BUNDLE_VERSION, version);
  }

  /**
   * Requires the given bundles. Can be called multiple times.
   *
   * @param bundleSpecs the spec for each required bundle.
   * @return this manifest maker
   */
  public ManifestMaker requireBundle(String... bundleSpecs) {
    return addHeaderValues(Constants.REQUIRE_BUNDLE, bundleSpecs);
  }

  /**
   * Adds the given package specs to the import package header.
   *
   * @param packageSpecs the package specs
   * @return this manifest maker
   */
  public ManifestMaker importPackages(String... packageSpecs) {
    return addHeaderValues(Constants.IMPORT_PACKAGE, packageSpecs);
  }

  /**
   * Adds the given package specs to the export package header.
   *
   * @param packageSpecs the package specs
   * @return this manifest maker
   */
  public ManifestMaker exportPackages(String... packageSpecs) {
    return addHeaderValues(Constants.EXPORT_PACKAGE, packageSpecs);
  }

  /**
   * Sets the fragment host header
   *
   * @param fragmentHost the value of the frament host header
   * @return this manifest maker
   */
  public ManifestMaker fragmentHost(@NotNull String fragmentHost) {
    return setHeader(Constants.FRAGMENT_HOST, fragmentHost);
  }

  /**
   * Adds the give header values to the given header
   *
   * @param headerName the name of the header
   * @param values     the values to be added.
   * @return this manifest maker
   */
  public ManifestMaker addHeaderValues(@NotNull String headerName, String... values) {
    List<String> newHeaderValues = new ArrayList<String>();
    if (headerValues.containsKey(headerName)) {
      newHeaderValues.addAll(headerValues.get(headerName));
    }

    newHeaderValues.addAll(Arrays.asList(values));

    headerValues.put(headerName, newHeaderValues);
    return this;
  }

  /**
   * Sets the header with the given name to the given value. This will overwrite any exisiting header values for that header.
   *
   * @param headerName the name
   * @param value      the value.
   * @return this manifest maker
   */
  public ManifestMaker setHeader(@NotNull String headerName, @NotNull String value) {
    headerValues.put(headerName, Collections.singletonList(value));
    return this;
  }

  /**
   * Creates the manifest from the currently known headers.
   *
   * @return
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("Manifest-Version: 1.0\n");
    for (Map.Entry<String, List<String>> entry : headerValues.entrySet()) {
      String name = entry.getKey();
      List<String> values = entry.getValue();
      builder.append(name).append(": ");
      for (Iterator<String> iterator = values.iterator(); iterator.hasNext(); ) {
        String value = iterator.next();
        builder.append(value);
        if (iterator.hasNext()) {
          builder.append(",");
        }
      }
      builder.append("\n");
    }
    return builder.toString();
  }
}
