package at.hannibal2.skyhanni.utils.coroutines

import kotlinx.coroutines.sync.Mutex

object MutexUtils {

    inline fun <T> Mutex.tryWithLock(block: () -> T): T? {
        if (!tryLock()) return null
        return runCatching(block).also { unlock() }.getOrThrow()
    }
}
