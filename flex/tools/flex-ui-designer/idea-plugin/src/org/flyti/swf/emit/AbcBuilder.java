package org.flyti.swf.emit;

import gnu.trove.THashMap;

public class AbcBuilder {
  private final THashMap<String, PackageBuilder> packageBuilders = new THashMap<String, PackageBuilder>();

  public PackageBuilder definePackage() {
    return definePackage("");
  }

  public PackageBuilder definePackage(String name) {
    PackageBuilder packageBuilder = packageBuilders.get(name);
    if (packageBuilder == null) {
      packageBuilder = new PackageBuilder(name);
      packageBuilders.put(name, packageBuilder);
    }

    return packageBuilder;
  }

  public void build() {
    for (PackageBuilder packageBuilder : packageBuilders.values()) {
      //packageBuilder.build
    }
  }
}
