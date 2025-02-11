package com.tweener.passage.gatekeeper.google.error

/**
 * @author Vivien Mahe
 * @since 11/02/2025
 */
class PassageActivityContextNotInitializedException : Throwable("You must call Passage.bindToView() in your @Composable before performing any authentication operation.")
