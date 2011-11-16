package org.osmorc.impl;

/**
 * Listener class being notified when some bundles are changed.
 */
public interface BundleModificationListener {
  /**
   * Called, when some bundles have changed.
   */
  void bundlesChanged();
}
