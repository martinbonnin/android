plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.navigation.common)
  implementation(libs.kiwi.navigationCompose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.dataClaimTriaging)
}
