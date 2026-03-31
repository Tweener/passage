package com.tweener.passage.auth.supabase

import com.tweener.passage.core.model.EntrantInterface
import io.github.jan.supabase.auth.user.UserInfo

/**
 * Project       : Passage
 * Author        : Chirag Redij
 * Created on    : Wednesday, 01/04/26 at 00:02
 * -------------------------------------------------------------------------------------
 * Last updated  : chiragredij on Wednesday, 01/04/26 at 00:02
 *
 * Description   : [Add a brief description of this file or component]
 *
 * Copyright (c) 2026 ChiragRedij. All rights reserved.
 */
interface SupabaseUserMapper <T : EntrantInterface> {
    fun map(supabaseUser: UserInfo): T
}