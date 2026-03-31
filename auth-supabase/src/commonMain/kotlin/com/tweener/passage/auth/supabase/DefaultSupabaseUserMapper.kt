package com.tweener.passage.auth.supabase

import com.tweener.passage.core.model.DefaultEntrant
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Project       : Passage
 * Author        : Chirag Redij
 * Created on    : Wednesday, 01/04/26 at 00:19
 * -------------------------------------------------------------------------------------
 * Last updated  : chiragredij on Wednesday, 01/04/26 at 00:19
 *
 * Description   : [Add a brief description of this file or component]
 *
 * Copyright (c) 2026 ChiragRedij. All rights reserved.
 */

class DefaultSupabaseUserMapper : SupabaseUserMapper<DefaultEntrant> {
    override fun map(supabaseUser: UserInfo): DefaultEntrant {
        return DefaultEntrant(
            uid = supabaseUser.id,
            email = supabaseUser.email,
            displayName = extractDisplayName(supabaseUser.userMetadata),
            phoneNumber = supabaseUser.phone,
            photoUrl = extractPhotoUrl(supabaseUser.userMetadata),
            isAnonymous = supabaseUser.isAnonymous == true,
            isEmailVerified = supabaseUser.emailConfirmedAt != null
        )
    }

    private fun extractDisplayName(metadata: JsonObject?): String? {
        if (metadata == null) return null

        return metadata["full_name"]?.jsonPrimitive?.content
            ?: metadata["name"]?.jsonPrimitive?.content
    }

    private fun extractPhotoUrl(metadata: JsonObject?): String? {
        if (metadata == null) return null

        return metadata["avatar_url"]?.jsonPrimitive?.content
            ?: metadata["picture"]?.jsonPrimitive?.content
    }
}