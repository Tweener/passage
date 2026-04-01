package com.tweener.passage.auth.supabase

import com.tweener.passage.core.model.DefaultEntrant
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Default [SupabaseUserMapper] implementation that maps Supabase [UserInfo] to [DefaultEntrant].
 *
 * Display name and photo URL are extracted from the user's metadata JSON object,
 * checking common field names used by OAuth providers (e.g., `full_name`, `avatar_url`).
 *
 * @author Chirag Redij
 * @since 01/04/2026
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