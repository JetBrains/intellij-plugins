package org.jetbrains.qodana.inspectionKts.icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object InspectionKtsIcons {
  @JvmField val Error: Icon = load("icons/inspectionKts/error.svg")
  @JvmField val ErrorOutdated: Icon = load("icons/inspectionKts/errorOutdated.svg")
  @JvmField val OK: Icon = load("icons/inspectionKts/ok.svg")
  @JvmField val OkOutdated: Icon = load("icons/inspectionKts/okOutdated.svg")

  private fun load(path: String): Icon = IconLoader.getIcon(path, InspectionKtsIcons::class.java.classLoader)
}
