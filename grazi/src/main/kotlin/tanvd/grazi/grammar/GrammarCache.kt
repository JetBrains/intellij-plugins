package tanvd.grazi.grammar

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache

object GrammarCache {
    private val cache: LoadingCache<Int, LinkedHashSet<Typo>> = Caffeine.newBuilder()
            .maximumSize(50_000)
            .build { null }

    fun hash(str: String) = str.hashCode()

    fun contains(str: String) = cache.getIfPresent(hash(str)) != null

    fun get(str: String) = cache.getIfPresent(hash(str)) ?: LinkedHashSet()

    fun put(str: String, typos: LinkedHashSet<Typo>) {
        cache.put(hash(str), typos)
    }

    fun invalidate(hash: Int) {
        cache.invalidate(hash)
    }

    fun reset() {
        cache.invalidateAll()
    }
}
