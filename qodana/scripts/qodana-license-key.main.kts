#!/usr/bin/env kotlin
@file:Repository("https://cache-redirector.jetbrains.com/intellij-dependencies")
@file:Repository("https://cache-redirector.jetbrains.com/maven-central")
@file:DependsOn("org.jetbrains.qodana:qodana-cloud-kotlin-client:0.2.12")

/*
 * Generates and outputs a Qodana license key, for test debugging purposes.
 *
 * Run it from the "Qodana / Qodana license key" run configuration, or:
 *
 *     community/tools/kotlin.cmd contrib/qodana/scripts/qodana-license-key.main.kts
 *
 * A browser tab opens; log in with your work JetBrains Account. Nothing to install or configure.
 *
 * Pipe stdout to save the key instead of printing, e.g. `... | pbcopy`.
 *
 * WHAT IT PRODUCES
 *
 * A license key: the value `QODANA_LICENSE` expects, which is what the linter validates offline.
 * That is NOT a Qodana Cloud project token (`QODANA_TOKEN`), and a token cannot be pasted into
 * the test instead. A token authenticates one project to the cloud; a license key is a signed
 * JetBrains license carrying a product code, a plan and an expiry date. This script does the
 * exchange between them, so there is no separate step to find.
 *
 * DEFAULTS AND OVERRIDES
 *
 *     QODANA_ENDPOINT   https://qodana.cloud
 *     QODANA_ORG        3oDrp - the JetBrains organization, whose Ultimate Plus license
 *                       is what makes the key usable
 *     QODANA_TEAM       your personal team, named after you as `users/me` reports it
 *
 * UNDER THE HOOD
 *
 *  1. GET  {endpoint}/api/versions            discover the api and linters hosts
 *  2. GET  api/v1/oauth/configurations        the JetBrains Account URL to open
 *  3. POST api/v1/idea/auth/token/            exchange the OAuth code for your access token
 *  4. GET  api/v1/users/me                    your id and name
 *  5. GET  api/v1/organizations               assert the organization's license is paid and active
 *  6. GET  api/v1/organizations/{org}/teams   find your team, CREATING it if absent
 *  7. GET  api/v1/teams/{team}/projects       find a `.temp` project, CREATING it if absent
 *  8. GET  api/v1/projects/{id}/token         its project token, MINTING one if absent
 *  9. GET  linters/v1/linters/license-key     exchange that token for the license key
 * 10. validate the key locally
 */

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.qodana.cloudclient.QDCloudClient
import org.jetbrains.qodana.cloudclient.QDCloudEnvironment
import org.jetbrains.qodana.cloudclient.QDCloudHttpClient
import org.jetbrains.qodana.cloudclient.QDCloudRequest
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.requestOn
import org.jetbrains.qodana.cloudclient.v1.QDCloudApiV1Base
import org.jetbrains.qodana.cloudclient.v1.QDCloudUserApiV1
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

// Configuration =====

val endpoint: String = (System.getenv("QODANA_ENDPOINT") ?: "https://qodana.cloud").trimEnd('/')
/** The JetBrains organization; its Ultimate Plus license is what makes the key usable. */
val DEFAULT_ORG = "3oDrp"
val orgId: String = System.getenv("QODANA_ORG") ?: DEFAULT_ORG

/**
 * Team names are unique per organization but a team is listed only to its own members, so a shared
 * name collides for the second person to run this: their listing misses the existing team and
 * creating it is rejected. Members of the JetBrains organization each have a team named after
 * them, which `users/me` reports directly, so use that - reusing the personal team when it exists
 * and creating it in the same shape when it does not.
 */
val teamNameOverride: String? = System.getenv("QODANA_TEAM")
val PROJECT_NAME = ".temp"

/** Qodana Cloud started accepting a PKCE challenge in the `state` at minor version 37. */
val PKCE_MIN_MINOR_VERSION = 37

val json = Json { ignoreUnknownKeys = true }

/** Progress goes to stderr so stdout carries nothing but the key itself. */
fun log(message: String) = System.err.println(message)

fun fail(message: String): Nothing = throw IllegalStateException(message)

/** The library only unwraps responses inside its own DSL block; outside it, report and stop. */
fun <T> QDCloudResponse<T>.orFail(what: String): T = when (this) {
  is QDCloudResponse.Success -> value
  is QDCloudResponse.Error.Offline -> fail("$what: Qodana Cloud at $endpoint is unreachable")
  is QDCloudResponse.Error.ResponseFailure -> fail("$what failed with HTTP $responseCode: $errorMessage")
}

// Pure helpers, self-checked below so a mistake here fails before the browser opens =====

fun codeChallengeOf(verifier: String): String =
  Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(UTF_8)))

/**
 * The `idea-` prefix is what makes Qodana Cloud bounce the code back to our loopback listener.
 * This format, and the version gate above, mirror
 * contrib/qodana/core/src/org/jetbrains/qodana/cloud/authorization/QodanaCloudOAuthRequest.kt -
 * change both together.
 */
fun oauthState(port: Int, nonce: String, challenge: String?): String =
  "idea-$port-$nonce" + if (challenge != null) "|pkce:method:SHA-256;code:$challenge" else ""

fun withQueryParam(url: String, name: String, value: String): String {
  val separator = if (url.contains('?')) "&" else "?"
  return "$url$separator$name=${URLEncoder.encode(value, UTF_8)}"
}

/** The email local part is a fallback only for accounts that report no full name. */
fun personalTeamName(fullName: String?, username: String?, id: String): String =
  fullName?.takeIf { it.isNotBlank() }
  ?: username?.substringBefore('@')
       ?.split('.', '_', '-')
       ?.filter { it.isNotEmpty() }
       ?.joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
       ?.takeIf { it.isNotBlank() }
  ?: id

/** Plan codes QodanaLicenseChecker.parseLicensePlan accepts as something better than Community. */
val PAID_KEY_PLANS = setOf("QDUE", "QDUP", "QDP", "QDPL", "QDU")

class LicenseState(val plan: String?, val state: String?, val expired: Boolean?, val validTill: String?)

/** A COMMUNITY or lapsed license yields a key Qodana rejects with the same error as no key at all. */
fun isUsableLicense(license: LicenseState?): Boolean =
  license != null &&
  license.plan != null && license.plan != "COMMUNITY" &&
  license.state == "ACTIVE" &&
  license.expired == false

check(codeChallengeOf("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk") == "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM") {
  "PKCE challenge derivation is wrong (RFC 7636 test vector failed)"
}
check(withQueryParam("https://host/auth?client_id=x", "state", "a|b;c") == "https://host/auth?client_id=x&state=a%7Cb%3Bc") {
  "state must be percent-encoded and appended with the right separator"
}
check(oauthState(1234, "nonce", null) == "idea-1234-nonce") { "state format changed" }
check(!isUsableLicense(LicenseState("COMMUNITY", "ACTIVE", false, null))) { "COMMUNITY must be rejected" }
check(!isUsableLicense(LicenseState("ULTIMATE_PLUS", "EXPIRED", false, null))) { "non-ACTIVE must be rejected" }
check(!isUsableLicense(LicenseState("ULTIMATE_PLUS", "ACTIVE", true, null))) { "expired must be rejected" }
check(!isUsableLicense(null)) { "an organization without licenseInformation must be rejected" }
check(isUsableLicense(LicenseState("ULTIMATE_PLUS", "ACTIVE", false, "2027-06-30T21:00:00Z"))) { "a paid active license must be accepted" }
check(!isUsableLicense(LicenseState(null, "ACTIVE", false, null))) { "a missing plan must be rejected" }
check(!isUsableLicense(LicenseState("ULTIMATE_PLUS", "ACTIVE", null, null))) { "a missing expired flag must be rejected" }
check(oauthState(1234, "nonce", "abc") == "idea-1234-nonce|pkce:method:SHA-256;code:abc") { "PKCE state format changed" }
check(withQueryParam("https://host/auth", "state", "a|b") == "https://host/auth?state=a%7Cb") {
  "a URL without a query string must get a '?' separator"
}
check(personalTeamName("Anna Zhukova", "anna.zhukova@jetbrains.com", "20R4W") == "Anna Zhukova") {
  "a reported full name must be used as-is"
}
check(personalTeamName(null, "stephan.lukasczyk@jetbrains.com", "X") == "Stephan Lukasczyk") {
  "without a full name, the email local part must be capitalized into one"
}
check(personalTeamName("  ", "a_b-c@jetbrains.com", "X") == "A B C") { "underscores and hyphens also separate names" }
check(personalTeamName(null, null, "20R4W") == "20R4W") { "the user id is the last resort" }

private fun syntheticKey(id: String, payload: String) =
  "$id-${Base64.getEncoder().encodeToString(payload.toByteArray(UTF_8))}-signature-certificate"

private val checkNow = Instant.parse("2026-01-01T00:00:00Z")
private fun qdlPayload(id: String, plan: String, paidUpTo: String) =
  """{"licenseId":"$id","products":[{"code":"QDL","paidUpTo":"$paidUpTo","properties":{"plan":"$plan"}}]}"""

check(inspectKey("only-three-parts", checkNow).isFailure) { "a key without 4 parts must be rejected" }
check(inspectKey(syntheticKey("AAA", qdlPayload("BBB", "QDUP", "2027-01-01")), checkNow).isFailure) {
  "a licenseId that disagrees with the first part must be rejected"
}
check(inspectKey(syntheticKey("AAA", """{"licenseId":"AAA","products":[{"code":"II"}]}"""), checkNow).isFailure) {
  "a key without a QDL product must be rejected"
}
check(inspectKey(syntheticKey("AAA", qdlPayload("AAA", "QDC", "2027-01-01")), checkNow).isFailure) {
  "a Community (QDC) key must be rejected"
}
check(inspectKey(syntheticKey("AAA", qdlPayload("AAA", "QDXX", "2027-01-01")), checkNow).isFailure) {
  "a plan Qodana does not recognize must be rejected"
}
check(inspectKey(syntheticKey("AAA", qdlPayload("AAA", "QDUP", "2024-09-30")), checkNow).isFailure) {
  "a lapsed paidUpTo must be rejected"
}
check(inspectKey(syntheticKey("AAA", qdlPayload("AAA", "QDUP", "2027-01-01")), checkNow).isSuccess) {
  "a paid, unexpired QDL key must be accepted"
}

// Raw requests, for the few endpoints the client does not model =====

suspend fun QDCloudApiV1Base.rawGet(path: String, parameters: Map<String, String> = emptyMap()): JsonObject? =
  json.parseToJsonElement(doRequest(QDCloudRequest(path, QDCloudRequest.GET, parameters)).orFail("GET $path")) as? JsonObject

suspend fun QDCloudApiV1Base.rawGetArray(path: String, parameters: Map<String, String> = emptyMap()): JsonArray =
  json.parseToJsonElement(doRequest(QDCloudRequest(path, QDCloudRequest.GET, parameters)).orFail("GET $path")) as? JsonArray
    ?: fail("expected a JSON array from $path")

suspend fun QDCloudApiV1Base.rawPost(path: String, body: String): JsonObject? =
  json.parseToJsonElement(doRequest(QDCloudRequest(path, QDCloudRequest.POST(body))).orFail("POST $path")) as? JsonObject

fun JsonObject.str(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull()
fun JsonObject.bool(key: String): Boolean? = this[key]?.jsonPrimitive?.booleanOrNull
fun kotlinx.serialization.json.JsonPrimitive.contentOrNull(): String? = if (isString || content != "null") content else null

// OAuth login =====

/** Blocks until the browser round-trip completes; the timeout is the failure bound reported to you. */
fun awaitAuthorizationCode(minorVersion: Int, oauthUrl: String, verifier: String): String {
  val server = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)
  val port = server.address.port
  val nonce = java.math.BigInteger(130, SecureRandom()).toString(32)
  val challenge = if (minorVersion >= PKCE_MIN_MINOR_VERSION) codeChallengeOf(verifier) else null
  val state = oauthState(port, nonce, challenge)
  val result = CompletableFuture<String>()

  // Anything that is not the expected callback lands here, so a redirect to a different path is
  // reported instead of being indistinguishable from nobody authorizing at all.
  val unexpected = java.util.Collections.synchronizedList(mutableListOf<String>())
  server.createContext("/") { exchange: HttpExchange ->
    // Browsers also fetch /favicon.ico and devtools probes from this origin; only something that
    // carries OAuth parameters is evidence that the callback path itself is wrong.
    val raw = exchange.requestURI.rawQuery.orEmpty()
    if (listOf("code=", "state=", "error=").any { it in raw }) {
      unexpected += "${exchange.requestMethod} ${exchange.requestURI}"
    }
    val bytes = "Unexpected path - this listener only serves the OAuth callback.".toByteArray(UTF_8)
    exchange.sendResponseHeaders(404, bytes.size.toLong())
    exchange.responseBody.use { it.write(bytes) }
  }

  server.createContext("/api/qodana/oauth/authorization_code/") { exchange: HttpExchange ->
    val query = exchange.requestURI.rawQuery.orEmpty().split('&')
      .mapNotNull { it.split('=', limit = 2).takeIf { p -> p.size == 2 } }
      .associate { (k, v) -> k to java.net.URLDecoder.decode(v, UTF_8) }
    val body: String
    var deliver: (() -> Unit)? = null
    // `state` may come back trimmed to its `idea-<port>-<nonce>` prefix, or absent entirely
    // (Qodana Cloud consumes it). The nonce is the unguessable part, so require it when present;
    // when absent, the ephemeral port is this run's only binding.
    val returnedState = query["state"]
    if (returnedState != null && returnedState != state && !returnedState.startsWith("idea-$port-$nonce")) {
      // A stale tab from an earlier run can hit a port we now own; ignore it and keep waiting.
      log("Ignoring a callback with an unrecognized state.\n  expected: $state\n  received: $returnedState")
      body = "Unexpected state, ignoring this callback."
    }
    else if (query["code"] != null) {
      body = "Authorized. You can close this tab."
      val code = query.getValue("code")
      deliver = { result.complete(code) }
    }
    else {
      body = "Authorization failed. You can close this tab."
      val message = query["error_description"] ?: query["error"] ?: "callback carried neither code nor error"
      deliver = { result.completeExceptionally(IllegalStateException(message)) }
    }
    val bytes = body.toByteArray(UTF_8)
    exchange.responseHeaders.add("Content-Type", "text/plain; charset=utf-8")
    exchange.sendResponseHeaders(200, bytes.size.toLong())
    exchange.responseBody.use { it.write(bytes) }
    // Only now hand the result to the main thread: completing before the browser has its response
    // lets the main thread stop the server mid-write, which the browser reports as a refused
    // connection on a login that actually succeeded.
    deliver?.invoke()
  }
  server.start()
  try {
    val authUrl = withQueryParam(oauthUrl, "state", state)
    log("Opening $authUrl")
    openInBrowser(authUrl)
    log("Waiting for you to authorize in the browser...")
    return result.get(5, TimeUnit.MINUTES)
  }
  catch (e: java.util.concurrent.ExecutionException) {
    fail(e.cause?.message ?: "authorization failed")
  }
  catch (e: java.util.concurrent.TimeoutException) {
    val seen = unexpected.toList()
    fail(
      if (seen.isEmpty()) {
        "nothing reached http://localhost:$port in 5 minutes - the browser tab was most likely never authorized"
      }
      else {
        "the callback arrived on an unexpected path, so this script is listening on the wrong one. Seen: $seen"
      }
    )
  }
  finally {
    server.stop(1)
  }
}

fun openInBrowser(url: String) {
  val opener = when {
    System.getProperty("os.name").startsWith("Mac") -> "open"
    System.getProperty("os.name").startsWith("Windows") -> null
    else -> "xdg-open"
  }
  runCatching {
    if (opener != null) ProcessBuilder(opener, url).start() else ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start()
  }.onFailure { log("Could not open a browser automatically - open the URL above manually.") }
}

// Cloud walk =====

class Organization(val id: String, val name: String, val license: LicenseState)

suspend fun selectOrganization(userApi: QDCloudUserApiV1): Organization {
  val all = userApi.rawGetArray("organizations").mapNotNull { element ->
    val obj = element as? JsonObject ?: return@mapNotNull null
    val id = obj.str("id") ?: return@mapNotNull null
    val licenseObject = obj["licenseInformation"] as? JsonObject
    val license = licenseObject?.let {
      LicenseState(it.str("licensePlan"), it.str("licenseState"), it.bool("expired"), it.str("validTill"))
    }
    Organization(id, obj.str("name") ?: id, license ?: LicenseState(null, null, null, null))
  }
  val picked = all.firstOrNull { it.id == orgId }
    ?: fail(
      "you are not a member of organization $orgId on $endpoint. " +
      "Set QODANA_ORG to one of: ${all.joinToString { "${it.id} (${it.name})" }}"
    )
  if (!isUsableLicense(picked.license)) {
    fail(
      "organization ${picked.id} has plan=${picked.license.plan ?: "none"} " +
      "state=${picked.license.state ?: "none"}; a key from it would fail the same way as no key at all"
    )
  }
  return picked
}

/**
 * Pages on the server's own `next` offset rather than on page size: the server may clamp `limit`,
 * and a short page would then end the walk before reaching an existing team.
 */
suspend fun allTeams(userApi: QDCloudUserApiV1, orgId: String, onlyPrivate: Boolean): List<Pair<String, String>> {
  val teams = mutableListOf<Pair<String, String>>()
  var offset = 0
  while (true) {
    val page = userApi.rawGet(
      "organizations/$orgId/teams",
      mapOf("onlyPrivate" to onlyPrivate.toString(), "offset" to offset.toString(), "limit" to "100")
    ) ?: fail("unexpected response listing teams of $orgId")
    val items = page["items"] as? JsonArray ?: fail("teams response has no items array")
    items.forEach { element ->
      val obj = element as? JsonObject ?: return@forEach
      val id = obj.str("id") ?: return@forEach
      teams += id to (obj.str("name") ?: "")
    }
    offset = page["next"]?.jsonPrimitive?.intOrNull ?: return teams
  }
}

suspend fun ensureTeam(userApi: QDCloudUserApiV1, org: Organization, teamName: String): String {
  fun matches(teams: List<Pair<String, String>>) = teams.filter { it.second == teamName }.map { it.first }.sorted()

  val existing = matches(allTeams(userApi, org.id, onlyPrivate = true))
  if (existing.isNotEmpty()) {
    if (existing.size > 1) log("WARNING: ${existing.size} teams named '$teamName' exist ($existing); using ${existing.first()}")
    log("Reusing team '$teamName' (${existing.first()})")
    return existing.first()
  }
  val created = runCatching {
    userApi.rawPost("organizations/${org.id}/teams/", """{"name":"$teamName","allowsGuestRole":false}""")
  }.getOrElse { e ->
    fail(
      "could not create team '$teamName' in ${org.id}: ${e.message}. " +
      "If the name is already taken by a team you are not a member of, set QODANA_TEAM to one of your own teams."
    )
  } ?: fail("unexpected response creating team '$teamName'")
  val id = created.str("id") ?: fail("created team carried no id: $created")
  log("Created private team '$teamName' ($id)")

  // Re-list and adopt the same deterministic winner the reuse path picks, so two runs that raced
  // each other converge on one team instead of drifting onto their own.
  val settled = matches(allTeams(userApi, org.id, onlyPrivate = true))
  if (settled.isEmpty()) {
    log(
      "WARNING: the team just created is not visible via onlyPrivate=true. A later run may create a duplicate; " +
      "report this - it means team privacy is not what that filter selects."
    )
    return id
  }
  if (settled.first() != id) log("Another run created '$teamName' concurrently; adopting ${settled.first()}")
  return settled.first()
}

/** Ids of the team's projects named [PROJECT_NAME], sorted so every run picks the same one. */
suspend fun projectsNamed(userApi: QDCloudUserApiV1, teamId: String): List<String> {
  val found = mutableListOf<String>()
  var offset = 0
  while (true) {
    val page = userApi.rawGet(
      "teams/$teamId/projects",
      mapOf("offset" to offset.toString(), "limit" to "100")
    ) ?: fail("unexpected response listing projects of team $teamId")
    val items = page["items"] as? JsonArray ?: fail("projects response has no items array")
    items.filterIsInstance<JsonObject>()
      .filter { it.str("name") == PROJECT_NAME }
      .forEach { obj -> obj.str("id")?.let { found += it } }
    offset = page["next"]?.jsonPrimitive?.intOrNull ?: return found.sorted()
  }
}

suspend fun ensureProject(userApi: QDCloudUserApiV1, teamId: String): String {
  projectsNamed(userApi, teamId).firstOrNull()?.let {
    log("Reusing project '$PROJECT_NAME' ($it)")
    return it
  }
  // orFail already reports the server's status and body, which is what a rejected request body
  // would surface as - the shape of this POST is not documented anywhere.
  val created = userApi.rawPost("teams/$teamId/projects/", """{"name":"$PROJECT_NAME"}""")
    ?: fail("unexpected response creating project '$PROJECT_NAME'")
  val id = created.str("id") ?: fail("created project carried no id: $created")
  log("Created project '$PROJECT_NAME' ($id)")
  return projectsNamed(userApi, teamId).firstOrNull()?.also {
    if (it != id) log("Another run created '$PROJECT_NAME' concurrently; adopting $it")
  } ?: id
}

suspend fun projectToken(userApi: QDCloudUserApiV1, projectId: String): String {
  when (val existing = userApi.getProjectToken(projectId)) {
    is QDCloudResponse.Success -> return existing.value.token
    is QDCloudResponse.Error.Offline -> fail("Qodana Cloud is unreachable: ${existing.exception.message}")
    is QDCloudResponse.Error.ResponseFailure -> {
      // Only "this project has no token yet" may fall through to minting one.
      if (existing.responseCode != 404) {
        fail("reading the token of project $projectId failed with ${existing.responseCode}: ${existing.errorMessage}")
      }
    }
  }
  return userApi.generateProjectToken(projectId).orFail("minting a token for project $projectId").token
}

// Reporting =====

/**
 * Applies the checks QodanaLicenseChecker will apply, so a key that cannot work says so here
 * instead of surfacing later as an indistinguishable "No valid license found".
 *
 * Returns a description of the key, or the reason Qodana would reject it.
 */
fun inspectKey(key: String, now: Instant): Result<String> = runCatching {
  val parts = key.split("-")
  check(parts.size == 4) { "expected a 4-part licenseId-licenseData-signature-certificate key, got ${parts.size} parts" }
  // The product decodes every segment with the MIME decoder, which tolerates embedded whitespace.
  val payload = json.parseToJsonElement(String(Base64.getMimeDecoder().decode(parts[1]), UTF_8)).jsonObject
  check(payload.str("licenseId") == parts[0]) {
    "the key's licenseId (${payload.str("licenseId")}) does not match its first part (${parts[0]})"
  }
  val products = (payload["products"] as? JsonArray)?.filterIsInstance<JsonObject>().orEmpty()
  val qdl = products.firstOrNull { it.str("code") == "QDL" }
  checkNotNull(qdl) { "the key covers ${products.mapNotNull { it.str("code") }} but not QDL, so Qodana will reject it" }

  val plan = qdl["properties"]?.jsonObject?.str("plan")
  checkNotNull(plan) { "the key's QDL entry carries no plan, which Qodana reads as an incorrect license format" }
  check(plan in PAID_KEY_PLANS) {
    if (plan == "QDC") "this key's plan is QDC (Community), which cannot satisfy the release-mode license check"
    else "Qodana does not recognize the plan '$plan' and will fail with \"Unknown license type\""
  }
  val paidUpTo = qdl.str("paidUpTo")
  checkNotNull(paidUpTo) { "the key's QDL entry carries no paidUpTo date" }
  check(Instant.parse("${paidUpTo}T23:59:59Z") >= now) {
    "this key's QDL subscription lapsed on $paidUpTo; Qodana rejects a key older than the build under test"
  }
  "covers QDL, plan $plan, paid up to $paidUpTo"
}.recoverCatching { e ->
  throw IllegalStateException(e.message ?: "the key could not be parsed: $e")
}

// Main =====

runBlocking {
  log("Qodana Cloud: $endpoint")
  val httpClient = QDCloudHttpClient(
    java.net.http.HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(30)).build()
  )
  val client = QDCloudClient(httpClient, QDCloudEnvironment(endpoint, httpClient).requestOn(this))

  withTimeout(10.minutes) {
    val v1 = client.v1().orFail("negotiating the v1 API with $endpoint")
    log("API v1.${v1.minorVersion} at ${v1.host}")

    val verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(64).also { SecureRandom().nextBytes(it) })
    val oauthUrl = v1.notAuthorizedApi().getOAuthProviderData().orFail("reading the OAuth configuration").oauthUrl
    val code = awaitAuthorizationCode(v1.minorVersion, oauthUrl, verifier)
    val credentials = v1.notAuthorizedApi().getCredentialsFromOAuthCode(code, verifier).orFail("exchanging the OAuth code")

    val userApi = v1.userApi { QDCloudResponse.Success<String>(credentials.access) }
    val me = userApi.getUserInfo().orFail("reading your user info")
    log("Authorized as ${me.username ?: me.fullName ?: me.id}")

    val org = selectOrganization(userApi)
    log("Organization ${org.id} (${org.name}): ${org.license.plan}, valid till ${org.license.validTill}")

    val teamName = teamNameOverride ?: personalTeamName(me.fullName, me.username, me.id)
    val projectId = ensureProject(userApi, ensureTeam(userApi, org, teamName))
    val token = projectToken(userApi, projectId)

    // The license key comes from the linters API, which is a different host than the one above,
    // and it authenticates with the project token rather than the user token.
    val lintersHost = client.environment.getApis().orFail("listing API hosts").linters
      .filter { it.majorVersion == 1 }
      .maxByOrNull { it.minorVersion }
      ?.host
      ?: fail("$endpoint advertises no v1 linters API; cannot request a license key")

    val license = json.parseToJsonElement(
      httpClient.doRequest(lintersHost, QDCloudRequest("linters/license-key", QDCloudRequest.GET), token).orFail("requesting a license key")
    ).jsonObject
    val key = license.str("licenseKey") ?: fail("the linters API returned no licenseKey: $license")
    log("License plan ${license.str("licensePlan")}, expires ${license.str("expirationDate")}")

    // Print the key before judging it: a run that got this far has already created the project
    // and minted the token, so a validation failure must not discard what it produced.
    println(key)
    log("Key " + inspectKey(key, Instant.now()).getOrElse { fail(it.message ?: "invalid key") })
  }
}
