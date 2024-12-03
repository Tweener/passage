package com.tweener.passage.error

import com.tweener.passage.model.GatekeeperType

/**
 * @author Vivien Mahe
 * @since 02/12/2024
 */

class PassageGatekeeperNotConfiguredException(gatekeeper: GatekeeperType) :
    UnsupportedOperationException("Passage is not configured for gatekeeper $gatekeeper")
