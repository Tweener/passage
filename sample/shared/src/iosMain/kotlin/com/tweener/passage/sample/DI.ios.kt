package com.tweener.passage.sample

import com.tweener.passage.Passage
import com.tweener.passage.PassageIos

/**
 * @author Vivien Mahe
 * @since 04/12/2024
 */

actual fun createPassage(): Passage = PassageIos()
