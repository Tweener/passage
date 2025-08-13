package com.tweener.passage.universallink

import com.tweener.kmpkit.codec.UrlCodec
import com.tweener.passage.mapper.toPassageUniversalLinkMode
import com.tweener.passage.model.PassageUniversalLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @author Vivien Mahe
 * @since 03/12/2024
 */
internal class PassageUniversalLinkHandler {

    companion object {
        private const val LINK_QUERY_PARAMETER_LINK = "link"
        private const val LINK_QUERY_PARAMETER_MODE = "mode"
        private const val LINK_QUERY_PARAMETER_OOB_CODE = "oobCode"
        private const val LINK_QUERY_PARAMETER_CONTINUE_URL = "continueUrl"
    }

    private val _linkToHandle = MutableStateFlow<PassageUniversalLink?>(null)
    val linkToHandle: StateFlow<PassageUniversalLink?> = _linkToHandle.asStateFlow()

    private val urlCodec = UrlCodec()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun handle(url: String): Boolean {
        var validLink = url

        // First, we need to extract the "link" parameter from the url, if exists
        extractQueryParameter(url = validLink, parameter = LINK_QUERY_PARAMETER_LINK)?.let { link -> validLink = link }

        // Decode the URL if needed
        validLink = urlCodec.decode(encodedUrl = validLink)

        // From this link parameter, we need to check for if "mode" and "oobCode" query params are present. It means it comes from a Firebase email action link.
        val modeParam = extractQueryParameter(url = validLink, parameter = LINK_QUERY_PARAMETER_MODE)
        val oobCodeParam = extractQueryParameter(url = validLink, parameter = LINK_QUERY_PARAMETER_OOB_CODE)
        val continueUrlParam = extractQueryParameter(url = validLink, parameter = LINK_QUERY_PARAMETER_CONTINUE_URL)

        val linkMode = modeParam.toPassageUniversalLinkMode()

        val isUrlHandled = linkMode != null && oobCodeParam != null && continueUrlParam != null
        if (isUrlHandled) {
            println("Universal Link is handled! $validLink")

            scope.launch {
                val passageUniversalLink = PassageUniversalLink(link = validLink, mode = linkMode!!, oobCode = oobCodeParam!!, continueUrl = continueUrlParam!!)
                _linkToHandle.emit(passageUniversalLink)
            }
        } else {
            println("Universal Link not handled. At least one of these query params is null: $LINK_QUERY_PARAMETER_MODE: $modeParam / $LINK_QUERY_PARAMETER_OOB_CODE: $oobCodeParam / $LINK_QUERY_PARAMETER_CONTINUE_URL: $continueUrlParam")
        }

        return isUrlHandled
    }

    fun onLinkHandled() {
        scope.launch {
            _linkToHandle.emit(null)
        }
    }

    private fun extractQueryParameter(url: String, parameter: String): String? {
        val queryStartIndex = url.indexOf("?")
        if (queryStartIndex == -1) return null

        val queryPairs = url
            .substring(queryStartIndex + 1)
            .split("&")
            .mapNotNull {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .toMap()

        return queryPairs[parameter]
    }
}
