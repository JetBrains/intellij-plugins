package tanvd.grazi

import org.eclipse.aether.repository.*
import org.eclipse.aether.repository.Proxy
import org.eclipse.aether.repository.ProxySelector
import java.net.*
import java.util.*


/** Copied from org.jetbrains.idea.maven.aether.JreProxySelector
 * This is a modified copy of the corresponding Aether class that adds support for https proxy types
 */
class JreProxySelector : ProxySelector {
    override fun getProxy(repository: RemoteRepository) = getProxy(repository.url)

    fun getProxy(url: String): Proxy? {
        try {
            val systemSelector = java.net.ProxySelector.getDefault() ?: return null
            val uri = URI(url).parseServerAuthority()
            val selected = systemSelector.select(uri)
            if (selected == null || selected.isEmpty()) {
                return null
            }
            for (proxy in selected) {
                if (proxy.type() == java.net.Proxy.Type.HTTP && isValid(proxy.address())) {
                    val proxyType = chooseProxyType(uri.scheme)
                    if (proxyType != null) {
                        val addr = proxy.address() as InetSocketAddress
                        return Proxy(proxyType, addr.hostName, addr.port, JreProxyAuthentication)
                    }
                }
            }
        } catch (e: Throwable) {
            // URL invalid or not accepted by selector or no selector at all, simply use no proxy
        }

        return null
    }

    private fun chooseProxyType(protocol: String) = when (protocol) {
        Proxy.TYPE_HTTP -> Proxy.TYPE_HTTP
        Proxy.TYPE_HTTPS -> Proxy.TYPE_HTTPS
        else -> null
    }

    private fun isValid(address: SocketAddress) = if (address is InetSocketAddress) {
        address.port > 0 && address.hostName != null && address.hostName.isNotEmpty()
    } else false

    private object JreProxyAuthentication : Authentication {
        override fun digest(digest: AuthenticationDigest): Unit = digest.update(UUID.randomUUID().toString())

        override fun fill(context: AuthenticationContext, key: String, data: Map<String, String>) {
            val proxy = context.proxy ?: return
            if (AuthenticationContext.USERNAME != key && AuthenticationContext.PASSWORD != key) {
                return
            }

            try {
                var url: URL?
                var protocol = "http"
                try {
                    url = URL(context.repository.url)
                    protocol = url.protocol
                } catch (e: Exception) {
                    url = null
                }

                val auth = Authenticator.requestPasswordAuthentication(
                        proxy.host, null, proxy.port, protocol, "Credentials for proxy $proxy", null, url, Authenticator.RequestorType.PROXY
                )
                if (auth != null) {
                    context.put(AuthenticationContext.USERNAME, auth.userName)
                    context.put(AuthenticationContext.PASSWORD, auth.password)
                } else {
                    context.put(AuthenticationContext.USERNAME, System.getProperty("$protocol.proxyUser"))
                    context.put(AuthenticationContext.PASSWORD, System.getProperty("$protocol.proxyPassword"))
                }
            } catch (e: SecurityException) {
                // oh well, let's hope the proxy can do without auth
            }
        }
    }
}
