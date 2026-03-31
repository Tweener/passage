package com.tweener.passage.auth.firebase

import com.tweener.passage.core.model.EntrantInterface
import dev.gitlive.firebase.auth.FirebaseUser

/**
 * Project       : Passage
 * Author        : Chirag Redij
 * Created on    : Tuesday, 31/03/26 at 12:13
 * -------------------------------------------------------------------------------------
 * Last updated  : chiragredij on Tuesday, 31/03/26 at 12:13
 *
 * Description   : [Add a brief description of this file or component]
 *
 * Copyright (c) 2026 ChiragRedij. All rights reserved.
 */
interface FirebaseUserMapper <T : EntrantInterface> {
    fun map(firebaseUser: FirebaseUser): T
}