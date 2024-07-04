plugins {
  id("hedvig.android.feature")
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.apollo)
  alias(libs.plugins.ksp)
  alias(libs.plugins.room)
  alias(libs.plugins.serialization)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  apolloMetadata(projects.apolloOctopusPublic)

  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.navigation.runtime)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.coil.coil)
  implementation(libs.coil.compose)
  implementation(libs.compose.richtext)
  implementation(libs.compose.richtextUi)
  implementation(libs.coroutines.core)
  implementation(libs.kiwi.navigationCompose)
  implementation(libs.koin.compose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.retrofit)
  implementation(libs.retrofitArrow)
  implementation(libs.retrofitKotlinxSerializationConverter)
  implementation(libs.paging.common)
  implementation(libs.paging.compose)
  implementation(libs.room.paging)
  implementation(libs.room.runtime)
  implementation(libs.sqlite.bundled)
  ksp(libs.room.ksp)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.composePhotoCaptureState)
  implementation(projects.composeUi)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDemoMode)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreFileUpload)
  implementation(projects.coreIcons)
  implementation(projects.coreMarkdown)
  implementation(projects.coreResources)
  implementation(projects.coreRetrofit)
  implementation(projects.coreUi)
  implementation(projects.dataChatReadTimestampPublic)
  implementation(projects.featureFlagsPublic)
  implementation(projects.languageCore)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationActivity)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
  implementation(projects.placeholder)
}

apollo {
  service("octopus") {
    packageName.set("octopus")
    generateDataBuilders.set(true)
    generateOptionalOperationVariables.set(false)
  }
}

room {
  schemaDirectory(
    project
      .rootDir
      .resolve("app")
      .resolve("database")
      .resolve("schemas")
      .absolutePath,
  )
}
