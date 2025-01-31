plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

android {
  testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
  implementation(libs.accompanist.permissions)
  implementation(libs.accompanist.webview)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.other.activityCompose)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.koin.android)
  implementation(libs.koin.compose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.moneta)
  implementation(projects.apolloCore)
  implementation(projects.apolloGiraffePublic)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.authCore)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDatastorePublic)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.dataForever)
  implementation(projects.hanalyticsCore)
  implementation(projects.hanalyticsFeatureFlagsPublic)
  implementation(projects.languageCore)
  implementation(projects.marketCore)
  implementation(projects.memberRemindersPublic)
  implementation(projects.memberRemindersUi)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
  implementation(projects.notificationPermission)
  implementation(projects.payment)
  implementation(projects.placeholder)
  implementation(projects.pullrefresh)
  implementation(projects.theme)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(projects.coreCommonTest)
  testImplementation(projects.coreDatastoreTest)
  testImplementation(projects.hanalyticsFeatureFlagsTest)
  testImplementation(projects.languageTest)
  testImplementation(projects.marketTest)
  testImplementation(projects.memberRemindersTest)
  testImplementation(projects.moleculeTest)
}
