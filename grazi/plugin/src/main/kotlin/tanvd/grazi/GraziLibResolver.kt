package tanvd.grazi

object GraziLibResolver {
    fun isLibExists(lib: String) = GraziPlugin.installationFolder.resolve("lib/$lib").exists()
}
