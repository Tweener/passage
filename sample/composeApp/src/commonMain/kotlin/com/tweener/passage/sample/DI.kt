package com.tweener.passage.sample

import com.tweener.passage.Passage

/**
 * @author Vivien Mahe
 * @since 04/12/2024
 */

private val passage: Passage = createPassage()

expect fun createPassage(): Passage

fun providePassage(): Passage = passage

