"""The jSerialComm natives of the serial monitor plugin: one zip for every platform.

The content hash is the version. `JSERIALCOMM_NATIVE_HASH` in `CommunityRepositoryModules.kt` pins it.
"""

load("@community//build:download_helpers.bzl", "INTELLIJ_BUILD_DEPENDENCIES_URL", "kotlin_const")
load("@community//build:download_rules.bzl", "download_repo")

_COMMUNITY_REPOSITORY_MODULES = Label("@community//platform/build-scripts:src/org/jetbrains/intellij/build/CommunityRepositoryModules.kt")

# CommunityRepositoryModules.jSerialCommDownloadUrl
def jserialcomm_url(hash):
    return "%s/jSerialComm/%s/jSerialComm.zip" % (INTELLIJ_BUILD_DEPENDENCIES_URL, hash)

def _serial_monitor_downloads_impl(module_ctx):
    download_repo(
        name = "jserialcomm_natives",
        urls = [jserialcomm_url(kotlin_const(module_ctx, _COMMUNITY_REPOSITORY_MODULES, "JSERIALCOMM_NATIVE_HASH"))],
    )
    return module_ctx.extension_metadata(reproducible = True)

serial_monitor_downloads = module_extension(implementation = _serial_monitor_downloads_impl)
