plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.squareSortDependencies)
}

android {
  buildFeatures {
    viewBinding = true
  }
}

dependencies {
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.uiViewBinding)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.other.dynamicAnimation)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.coil.coil)
  implementation(libs.coil.compose)
  implementation(libs.coroutines.android)
  implementation(libs.coroutines.core)
  implementation(libs.koin.compose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.moneta)
  implementation(projects.apolloCore)
  implementation(projects.apolloGiraffePublic)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.dataForever)
  implementation(projects.languageCore)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
  implementation(projects.pullrefresh)
}
