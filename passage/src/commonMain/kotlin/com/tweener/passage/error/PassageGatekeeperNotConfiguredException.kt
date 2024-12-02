package com.tweener.passage.error

/**
 * @author Vivien Mahe
 * @since 02/12/2024
 */

class PassageGatekeeperNotConfiguredException(gatekeeper: String) :
    UnsupportedOperationException("Passage is not configured for gatekeeper $gatekeeper")
