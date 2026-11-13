package at.hannibal2.skyhanni.utils.collection

import at.hannibal2.skyhanni.utils.collection.CollectionUtils.getOrCompute
import kotlin.time.Duration

class WeakIdentityCache<K : Any, V : Any> private constructor(
    private val cache: TimeLimitedCache<K, V>,
    private val loader: (K) -> V,
) : MutableMap<K, V> by cache {

    constructor(expireAfterWrite: Duration, loader: (K) -> V) : this(
        TimeLimitedCache(expireAfterWrite, useWeakKeys = true),
        loader,
    )

    override fun get(key: K): V = cache.getOrCompute(key, loader)
}
