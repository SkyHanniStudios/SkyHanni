package at.hannibal2.skyhanni.utils.collection

import kotlin.time.Duration

// TODO Make WeakIdentityCache be the only cache with weak keys, and remove the useWeakKeys parameter from all other caches
class WeakIdentityCache<K : Any, V : Any> private constructor(
    private val cache: CacheMap<K, V>,
    private val mappingFunction: (K) -> V,
) : MutableMap<K, V> by cache {

    override fun get(key: K): V = cache.computeIfAbsent(key, mappingFunction)

    companion object {
        fun <K : Any, V : Any> timed(expireAfterWrite: Duration, mappingFunction: (K) -> V): WeakIdentityCache<K, V> =
            WeakIdentityCache(TimeLimitedCache(expireAfterWrite, useWeakKeys = true), mappingFunction)

        fun <K : Any, V : Any> sized(maxSize: Int, mappingFunction: (K) -> V): WeakIdentityCache<K, V> =
            WeakIdentityCache(SizeLimitedCache(maxSize, useWeakKeys = true), mappingFunction)
    }
}
