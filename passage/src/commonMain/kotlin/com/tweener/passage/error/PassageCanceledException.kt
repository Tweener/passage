package com.tweener.passage.error

/**
 * Thrown when the entrant dismissed the provider's own Sign In UI, rather than the flow failing.
 *
 * Canceling is a deliberate answer and not an error, so a caller usually wants to return the entrant to where they
 * were without reporting anything. It can only do that if a cancellation is distinguishable from a genuine failure,
 * which is why it has its own type instead of the gatekeeper's generic exception.
 *
 * @author Vivien Mahe
 * @since 27/08/2026
 */
class PassageCanceledException : Throwable("The entrant canceled the Sign In process.")
