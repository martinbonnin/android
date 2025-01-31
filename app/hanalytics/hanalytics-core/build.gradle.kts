plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.hAnalytics)

  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.coreCommonPublic)
  implementation(projects.loggingPublic)
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
  kotlinOptions {
    freeCompilerArgs = freeCompilerArgs + "-Xopt-in=com.hedvig.android.hanalytics.InternalHanalyticsApi"
  }
}
