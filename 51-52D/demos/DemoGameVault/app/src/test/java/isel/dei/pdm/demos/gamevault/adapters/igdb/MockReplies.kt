package isel.dei.pdm.demos.gamevault.adapters.igdb

import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData

/**
 * A scripted reply to a request, used by the tests of the HTTP clients (e.g. the IGDB services,
 * or the image source) to script a MockEngine.
 */
internal typealias Reply = suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
