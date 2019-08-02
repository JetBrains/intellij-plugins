package tanvd.grazi

object GraziLibResolver {
    fun isLibExists(lib: String) = GraziPlugin.path.resolve("lib/$lib").exists()
}
