package com.hedvig.app.feature.marketpicker

import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Response
import com.apollographql.apollo3.exception.ApolloException
import com.hedvig.android.owldroid.graphql.UpdateLanguageMutation
import com.hedvig.android.owldroid.type.Locale
import e
import i

class LanguageRepository(
    private val apolloClient: ApolloClient,
) {

    fun uploadLanguage(acceptLanguage: String, locale: Locale) {
        apolloClient
            .mutate(UpdateLanguageMutation(acceptLanguage, locale))
            .enqueue(object : ApolloCall.Callback<UpdateLanguageMutation.Data>() {
                override fun onFailure(e: ApolloException) {
                    e { "$e Failed to update language" }
                }

                override fun onResponse(response: Response<UpdateLanguageMutation.Data>) {
                    i { "Successfully updated language" }
                }
            })
    }
}
