package uk.ac.tees.mad.minilibrary.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage

object SupabaseClient {

    private const val SUPABASE_URL = "https://beirqpeknhfiaxxkyhxp.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_TF41tsNbVW1oB19yuPQlNQ_-A0pBfq5"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Storage)
        install(Postgrest)
    }

    val auth get() = client.auth
    val storage get() = client.storage
    val database get() = client.postgrest
}
