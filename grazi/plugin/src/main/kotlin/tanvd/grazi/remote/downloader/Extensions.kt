package tanvd.grazi.remote.downloader

import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.graph.*
import org.eclipse.aether.util.artifact.JavaScopes

fun Artifact.createDependency() = Dependency(this, JavaScopes.COMPILE, false, listOf(
        Exclusion("org.languagetool", "languagetool-core", "", "jar"),
        Exclusion("org.carrot2", "morfologik-fsa", "", "jar"),
        Exclusion("org.carrot2", "morfologik-stemming", "", "jar"),
        Exclusion("com.google.guava", "guava", "", "jar")
))

val Artifact.name
    get() = "$artifactId-$version.jar"

val Artifact.url
    get() = "${LangDownloader.MAVEN_CENTRAL_REPOSITORY.url}${groupId.replace(".", "/")}/$artifactId/$version/$name"

fun DependencyNode.traverse(action: (DependencyNode) -> Unit) {
    action(this)
    children.forEach(action)
}
