plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
}

android {
  testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.assertK)
  implementation(libs.coil.coil)
  implementation(libs.coil.compose)
  implementation(libs.junit)
  implementation(libs.koin.compose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloGiraffePublic)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.hanalyticsCore)
  implementation(projects.hanalyticsFeatureFlagsPublic)
  implementation(projects.languageCore)
  implementation(projects.loggingPublic)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
  implementation(projects.notificationBadgeDataPublic)
  implementation(projects.pullrefresh)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(projects.loggingTest)
  testImplementation(projects.moleculeTest)
  testImplementation(projects.notificationBadgeDataFake)
}
