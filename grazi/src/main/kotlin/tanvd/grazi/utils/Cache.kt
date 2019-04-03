package tanvd.grazi.utils

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import tanvd.grazi.grammar.Typo

class TypoCache(cacheSize: Long) {
    companion object {
        fun hash(string: String) = string.hashCode()
    }

    private val cache: LoadingCache<Int, LinkedHashSet<Typo>> = Caffeine.newBuilder()
            .maximumSize(cacheSize)
            .build { null }

    fun contains(str: String) = cache.getIfPresent(hash(str)) != null

    fun get(str: String) = cache.getIfPresent(hash(str)) ?: LinkedHashSet()

    fun put(str: String, typos: LinkedHashSet<Typo>) {
        cache.put(hash(str), typos)
    }

    fun reset() {
        cache.invalidateAll()
    }
}
