package com.tweener.passage.sample

/**
 * @author Vivien Mahe
 * @since 03/12/2024
 */

class PassageUniversalLinkHandlerHelper {

    private val universalLinkHandler = providePassageUniversalLinkHandler()

    fun handle(url: String): Boolean =
        universalLinkHandler.handle(url = url)

}
