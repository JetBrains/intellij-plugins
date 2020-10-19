package org.jetbrains.idea.perforce.util.tracer;

interface StatisticsPresentation<T extends Tracer> {
  void putSelf(final StringBuilder sb, final T statistics);
}
