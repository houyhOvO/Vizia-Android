package com.viziatech.vizia

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient

object SupaBaseHelper {
    lateinit var client: SupabaseClient
    fun init() {
        client = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth) {
                alwaysAutoRefresh = true
            }
        }
    }
}