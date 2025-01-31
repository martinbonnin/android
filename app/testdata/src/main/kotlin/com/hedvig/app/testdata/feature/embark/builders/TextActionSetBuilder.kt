package com.hedvig.app.testdata.feature.embark.builders

import giraffe.EmbarkStoryQuery
import giraffe.fragment.ApiFragment
import giraffe.fragment.EmbarkLinkFragment
import giraffe.type.EmbarkTextActionSet

data class TextActionSetBuilder(
  private val link: EmbarkLinkFragment,
  private val textActions: List<EmbarkStoryQuery.TextAction>,
  private val api: ApiFragment? = null,
) {
  fun build() = EmbarkStoryQuery.Action(
    __typename = EmbarkTextActionSet.type.name,
    asEmbarkSelectAction = null,
    asEmbarkTextAction = null,
    asEmbarkTextActionSet = EmbarkStoryQuery.AsEmbarkTextActionSet(
      __typename = EmbarkTextActionSet.type.name,
      textSetData = EmbarkStoryQuery.TextSetData(
        link = EmbarkStoryQuery.Link2(
          __typename = "",
          fragments = EmbarkStoryQuery.Link2.Fragments(link),
        ),
        textActions = textActions,
        api = api?.let {
          EmbarkStoryQuery.Api2(
            __typename = it.__typename,
            fragments = EmbarkStoryQuery.Api2.Fragments(it),
          )
        },
      ),
    ),
    asEmbarkPreviousInsuranceProviderAction = null,
    asEmbarkNumberAction = null,
    asEmbarkNumberActionSet = null,
    asEmbarkDatePickerAction = null,
    asEmbarkMultiAction = null,
    asEmbarkAudioRecorderAction = null,
    asEmbarkExternalInsuranceProviderAction = null,
    asEmbarkAddressAutocompleteAction = null,
  )
}
