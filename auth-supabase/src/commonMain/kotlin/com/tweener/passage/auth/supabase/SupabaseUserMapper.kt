package com.tweener.passage.auth.supabase

import com.tweener.passage.core.model.EntrantInterface
import io.github.jan.supabase.auth.user.UserInfo

/**
 * Maps a Supabase [UserInfo] to the domain user model [T].
 *
 * Implement this interface to provide a custom mapping from Supabase's user representation
 * to your application's [EntrantInterface] implementation.
 *
 * @param T The domain user type, constrained to [EntrantInterface].
 *
 * @see DefaultSupabaseUserMapper
 *
 * @author Chirag Redij
 * @since 01/04/2026
 */
interface SupabaseUserMapper<T : EntrantInterface> {

    /**
     * Converts a Supabase [UserInfo] into the domain model [T].
     *
     * @param supabaseUser The Supabase user to map.
     * @return The mapped domain user.
     */
    fun map(supabaseUser: UserInfo): T
}