package org.jetbrains.idea.perforce.util.tracer;

interface Tracer<Kind extends Enum, Key> {
  /**
   * @return key
   */
  Key start(final Kind kind, final String presentation);
  void stop(final Key key, final Kind kind, final String presentation);
}
