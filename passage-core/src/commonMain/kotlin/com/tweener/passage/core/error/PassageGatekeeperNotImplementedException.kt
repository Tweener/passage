package com.tweener.passage.core.error

import com.tweener.kmpkit.Platform
import com.tweener.passage.core.model.GatekeeperType

/**
 * @author Vivien Mahe
 * @since 30/11/2024
 */

class PassageGatekeeperNotImplementedException(gatekeeper: GatekeeperType, platform: Platform) :
    UnsupportedOperationException("Passage does not yet handle gatekeeper $gatekeeper on platform $platform")