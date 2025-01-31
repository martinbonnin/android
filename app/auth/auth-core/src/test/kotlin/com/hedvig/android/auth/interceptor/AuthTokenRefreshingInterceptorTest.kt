package com.hedvig.android.auth.interceptor

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.auth.AccessTokenProvider
import com.hedvig.android.auth.AndroidAccessTokenProvider
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.AuthTokenServiceImpl
import com.hedvig.android.auth.FakeAuthRepository
import com.hedvig.android.auth.event.AuthEventBroadcaster
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.test.clock.TestClock
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.RefreshToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class AuthTokenRefreshingInterceptorTest {

  @get:Rule
  val testFolder = TemporaryFolder()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `The token in the header contains the 'Bearer ' prefix as the backend expects it`() = runTest {
    val webServer = MockWebServer().also { it.enqueue(MockResponse()) }
    val okHttpClient = testOkHttpClient(
      AuthTokenRefreshingInterceptor(accessTokenProvider = { "token" }),
    )

    okHttpClient.newCall(webServer.testRequest()).execute()
    val requestSent: RecordedRequest = webServer.takeRequest()

    assertThat(requestSent.headers["Authorization"]).isEqualTo("Bearer token")
  }

  @Test
  fun `when a token already exists, simply goes through adding the header`() = runTest {
    val clock = TestClock()
    val authTokenStorage = authTokenStorage(clock)
    authTokenStorage.updateTokens(
      AccessToken("token", 10.minutes.inWholeSeconds.toInt()),
      RefreshToken("", 0),
    )
    val authTokenService = authTokenService(authTokenStorage)
    val interceptor = AuthTokenRefreshingInterceptor(accessTokenProvider(authTokenService, clock))
    val webServer = MockWebServer().also { it.enqueue(MockResponse()) }
    val okHttpClient = testOkHttpClient(interceptor)
    runCurrent()

    okHttpClient.newCall(webServer.testRequest()).execute()
    val requestSent: RecordedRequest = webServer.takeRequest()

    assertThat(requestSent.headers["Authorization"]).isEqualTo("Bearer token")
  }

  @Test
  fun `when the access token is expired, and the refresh token is not expired, refresh and add header`() = runTest {
    val clock = TestClock()
    val authTokenStorage = authTokenStorage(clock)
    authTokenStorage.updateTokens(
      AccessToken("", 10.minutes.inWholeSeconds.toInt()),
      RefreshToken("", 1.hours.inWholeSeconds.toInt()),
    )
    val authRepository = FakeAuthRepository()
    val authTokenService = authTokenService(authTokenStorage, authRepository)
    val interceptor = AuthTokenRefreshingInterceptor(accessTokenProvider(authTokenService, clock))
    val webServer = MockWebServer().also { it.enqueue(MockResponse()) }
    val okHttpClient = testOkHttpClient(interceptor)
    runCurrent()

    clock.advanceTimeBy(30.minutes)
    authRepository.exchangeResponse.add(
      AuthTokenResult.Success(
        AccessToken("refreshedToken", 10.minutes.inWholeSeconds.toInt()),
        RefreshToken("refreshedRefreshToken", 0),
      ),
    )
    okHttpClient.newCall(webServer.testRequest()).execute()
    val requestSent: RecordedRequest = webServer.takeRequest()

    val storedAuthTokens = authTokenStorage.getTokens().first()!!
    assertThat(storedAuthTokens.accessToken.token).isEqualTo("refreshedToken")
    assertThat(storedAuthTokens.refreshToken.token).isEqualTo("refreshedRefreshToken")
    assertThat(requestSent.headers["Authorization"]).isEqualTo("Bearer refreshedToken")
  }

  @Test
  fun `when the access token and the refresh token are expired, clear tokens and proceed without a header`() = runTest {
    val clock = TestClock()
    val authTokenStorage = authTokenStorage(clock)
    authTokenStorage.updateTokens(
      AccessToken("", 10.minutes.inWholeSeconds.toInt()),
      RefreshToken("", 1.hours.inWholeSeconds.toInt()),
    )
    val authRepository = FakeAuthRepository()
    val authTokenService = authTokenService(authTokenStorage, authRepository)
    val interceptor = AuthTokenRefreshingInterceptor(accessTokenProvider(authTokenService, clock))
    val webServer = MockWebServer().also { it.enqueue(MockResponse()) }
    val okHttpClient = testOkHttpClient(interceptor)
    runCurrent()

    clock.advanceTimeBy(1.hours)
    okHttpClient.newCall(webServer.testRequest()).execute()
    val requestSent: RecordedRequest = webServer.takeRequest()

    val storedAuthTokens = authTokenStorage.getTokens().first()
    assertThat(storedAuthTokens).isNull()
    assertThat(requestSent.headers["Authorization"]).isNull()
  }

  @Test
  fun `with two requests happening in parallel, the token is only refreshed once`() = runTest {
    val clock = TestClock()
    val authTokenStorage = authTokenStorage(clock)
    authTokenStorage.updateTokens(
      AccessToken("", 10.minutes.inWholeSeconds.toInt()),
      RefreshToken("", 1.hours.inWholeSeconds.toInt()),
    )
    val authRepository = FakeAuthRepository()
    val authTokenService = authTokenService(authTokenStorage, authRepository)
    val interceptor = AuthTokenRefreshingInterceptor(accessTokenProvider(authTokenService, clock))
    val webServer = MockWebServer().also { mockWebServer ->
      repeat(2) { mockWebServer.enqueue(MockResponse()) }
    }
    val okHttpClient = testOkHttpClient(interceptor)
    runCurrent()

    clock.advanceTimeBy(30.minutes)
    authRepository.exchangeResponse.add(
      AuthTokenResult.Success(
        AccessToken("refreshedToken", 10.minutes.inWholeSeconds.toInt()),
        RefreshToken("refreshedRefreshToken", 0),
      ),
    )
    okHttpClient.newCall(webServer.testRequest()).execute()
    okHttpClient.newCall(webServer.testRequest()).execute()
    runCurrent()
    val requestSent1: RecordedRequest = webServer.takeRequest()
    val requestSent2: RecordedRequest = webServer.takeRequest()

    val storedAuthTokens = authTokenStorage.getTokens().first()!!
    assertThat(storedAuthTokens.accessToken.token).isEqualTo("refreshedToken")
    assertThat(storedAuthTokens.refreshToken.token).isEqualTo("refreshedRefreshToken")
    assertThat(requestSent1.headers["Authorization"]).isEqualTo("Bearer refreshedToken")
    assertThat(requestSent2.headers["Authorization"]).isEqualTo("Bearer refreshedToken")
  }

  @Test
  fun `two requests happen in parallel, the token is only refreshed once, then a third one gets the token eagerly`() =
    runTest {
      val clock = TestClock()
      val authTokenStorage = authTokenStorage(clock)
      authTokenStorage.updateTokens(
        AccessToken("", 10.minutes.inWholeSeconds.toInt()),
        RefreshToken("", 1.hours.inWholeSeconds.toInt()),
      )
      val authRepository = FakeAuthRepository()
      val authTokenService = authTokenService(authTokenStorage, authRepository)
      val interceptor = AuthTokenRefreshingInterceptor(accessTokenProvider(authTokenService, clock))
      val webServer = MockWebServer().also { mockWebServer ->
        repeat(3) { mockWebServer.enqueue(MockResponse()) }
      }
      val okHttpClient = testOkHttpClient(interceptor)
      runCurrent()

      clock.advanceTimeBy(30.minutes)
      authRepository.exchangeResponse.add(
        AuthTokenResult.Success(
          AccessToken("refreshedToken", 10.minutes.inWholeSeconds.toInt()),
          RefreshToken("refreshedRefreshToken", 0),
        ),
      )
      okHttpClient.newCall(webServer.testRequest()).execute()
      okHttpClient.newCall(webServer.testRequest()).execute()
      runCurrent()

      okHttpClient.newCall(webServer.testRequest()).execute()

      val requestSent1: RecordedRequest = webServer.takeRequest()
      val requestSent2: RecordedRequest = webServer.takeRequest()
      val requestSent3: RecordedRequest = webServer.takeRequest()

      val storedAuthTokens = authTokenStorage.getTokens().first()!!
      assertThat(storedAuthTokens.accessToken.token).isEqualTo("refreshedToken")
      assertThat(storedAuthTokens.refreshToken.token).isEqualTo("refreshedRefreshToken")
      assertThat(requestSent1.headers["Authorization"]).isEqualTo("Bearer refreshedToken")
      assertThat(requestSent2.headers["Authorization"]).isEqualTo("Bearer refreshedToken")
      assertThat(requestSent3.headers["Authorization"]).isEqualTo("Bearer refreshedToken")
    }

  private fun MockWebServer.testRequest(): Request {
    return Request.Builder().url(url("test")).build()
  }

  private fun TestScope.authTokenService(
    authTokenStorage: AuthTokenStorage,
    fakeAuthRepository: FakeAuthRepository = FakeAuthRepository(),
  ): AuthTokenService {
    return AuthTokenServiceImpl(
      authTokenStorage,
      fakeAuthRepository,
      AuthEventBroadcaster(emptySet(), ApplicationScope(backgroundScope), EmptyCoroutineContext),
      backgroundScope,
    )
  }

  private fun TestScope.authTokenStorage(clock: Clock) = AuthTokenStorage(
    TestPreferencesDataStore(
      datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
      coroutineScope = backgroundScope,
    ),
    clock,
  )

  private fun accessTokenProvider(authTokenService: AuthTokenService, clock: Clock): AccessTokenProvider {
    return AndroidAccessTokenProvider(authTokenService, clock)
  }

  private fun testOkHttpClient(interceptor: Interceptor) = OkHttpClient
    .Builder()
    .addInterceptor(interceptor)
    .build()
}
