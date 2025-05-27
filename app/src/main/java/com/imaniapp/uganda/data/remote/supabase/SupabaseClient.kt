package com.imaniapp.uganda.data.remote.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClient @Inject constructor() {
    
    companion object {
        // These will be injected from BuildConfig
        private const val SUPABASE_URL = "https://gnwcqrwcmpqjayhqisgj.supabase.co"
        private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imdud2NxcndjbXBxamF5aHFpc2dqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY5NTE1NjcsImV4cCI6MjA2MjUyNzU2N30._aygPZCLzbA5g8KYKqgP8_9f9U64qQG8IszGvlUa9pY"
    }
    
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
    
    // Auth instance for user authentication
    val auth get() = client.pluginManager.getPlugin(Auth)
    
    // Postgrest instance for database operations
    val database get() = client.pluginManager.getPlugin(Postgrest)
    
    // Storage instance for file uploads
    val storage get() = client.pluginManager.getPlugin(Storage)
} 