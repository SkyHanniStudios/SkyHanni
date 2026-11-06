package at.hannibal2.skyhanni.test.framework

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.test.framework.fixtures.ClockFixture
import at.hannibal2.skyhanni.test.framework.fixtures.ConfigFixture
import at.hannibal2.skyhanni.test.framework.fixtures.LocationFixture
import at.hannibal2.skyhanni.test.framework.fixtures.NeuRepoFixture
import at.hannibal2.skyhanni.test.framework.fixtures.OutputFixture
import at.hannibal2.skyhanni.test.framework.fixtures.StorageFixture
import java.lang.annotation.Inherited

@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Fixture(MinecraftFixture::class)
annotation class NeedsMinecraft

@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Fixture(NeuRepoFixture::class)
annotation class NeedsNeuRepo(vararg val items: String)

@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Fixture(ConfigFixture::class)
annotation class NeedsConfig

@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Fixture(StorageFixture::class)
annotation class NeedsStorage

@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Fixture(ClockFixture::class)
annotation class NeedsClock

@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Fixture(LocationFixture::class)
annotation class NeedsIsland(val island: IslandType = IslandType.HUB)

@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Fixture(OutputFixture::class)
annotation class CapturesOutput

class MinecraftFixture : TestFixture
