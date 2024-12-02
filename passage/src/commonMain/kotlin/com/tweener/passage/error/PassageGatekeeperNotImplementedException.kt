package com.tweener.passage.error

import com.tweener.common._internal.Platform

/**
 * @author Vivien Mahe
 * @since 30/11/2024
 */
class PassageGatekeeperNotImplementedException(gatekeeper: String, platform: Platform) :
    UnsupportedOperationException("Passage does not yet handle gatekeeper $gatekeeper on platform $platform")
