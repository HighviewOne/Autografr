package com.autografr.app.data.ai

import android.graphics.BitmapFactory
import android.util.Log
import com.autografr.app.data.image.ImageScaler
import com.autografr.app.di.IoDispatcher
import com.autografr.app.domain.model.SignedPhoto
import com.autografr.app.domain.repository.ListingAssistant
import com.autografr.app.domain.repository.ListingSuggestion
import com.autografr.app.domain.util.Result
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Writes a draft title and description for a signed photo with Gemini, called through
 * Firebase AI Logic. The app never holds a Gemini API key: requests go through Firebase,
 * which attaches App Check, and are billed to the Firebase project.
 */
@Singleton
class GeminiListingAssistant @Inject constructor(
    private val storage: FirebaseStorage,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ListingAssistant {

    private companion object {
        const val TAG = "GeminiListing"

        // Fast, low-cost model. Copy for one photo needs no heavier reasoning.
        const val MODEL_NAME = "gemini-4-hydrogen"

        // Thumbnails are ≤512 px and capped at 2 MB by storage.rules; signed photos at 15 MB.
        const val THUMBNAIL_MAX_BYTES = 2L * 1024 * 1024
        const val SIGNED_PHOTO_MAX_BYTES = 15L * 1024 * 1024
        const val IMAGE_MAX_DIMENSION = 768
        const val JPEG_QUALITY = 85

        const val TITLE_MAX = 60
        const val DESCRIPTION_MAX = 300

        val SYSTEM_PROMPT = """
            You write marketplace listings for Autografr, an app where verified celebrities
            sign photos by hand and fans buy and trade the signed cards.
            Describe only what is visible in the photo and the facts you are given.
            Never invent events, dates, teams, games, provenance, rarity, or value.
            Never claim authenticity or certification beyond "signed on Autografr".
            Never mention or guess the identity of anyone except the named signer.
            Title: at most $TITLE_MAX characters. Description: one or two sentences,
            at most $DESCRIPTION_MAX characters, warm but factual. No emoji, no hashtags.
        """.trimIndent()
    }

    @Serializable
    private data class Copy(val title: String, val description: String)

    private val json = Json { ignoreUnknownKeys = true }

    private val model: GenerativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = MODEL_NAME,
            systemInstruction = content { text(SYSTEM_PROMPT) },
            generationConfig = generationConfig {
                temperature = 0.7f
                responseMimeType = "application/json"
                responseSchema = Schema.obj(
                    mapOf(
                        "title" to Schema.string("Listing title"),
                        "description" to Schema.string("Listing description")
                    )
                )
            }
        )
    }

    override suspend fun suggestListing(photo: SignedPhoto): Result<ListingSuggestion> = try {
        val bitmap = withContext(ioDispatcher) {
            val bytes = downloadImage(photo)
            val scaled = ImageScaler.scaleBytes(bytes, IMAGE_MAX_DIMENSION, JPEG_QUALITY)
            BitmapFactory.decodeByteArray(scaled, 0, scaled.size)
                ?: throw IOException("Could not decode photo")
        }
        val signer = photo.celebrityName.ifBlank { "the celebrity" }
        val response = model.generateContent(
            content {
                image(bitmap)
                text("This photo was hand-signed by $signer. Write the listing.")
            }
        )
        val raw = response.text ?: throw IOException("Gemini returned no text")
        val copy = json.decodeFromString<Copy>(raw)
        Result.success(
            ListingSuggestion(
                title = copy.title.trim().take(TITLE_MAX),
                description = copy.description.trim().take(DESCRIPTION_MAX)
            )
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Listing suggestion failed for photo ${photo.id}", e)
        Result.error("Couldn't write a suggestion right now. You can still fill it in yourself.", e)
    }

    private suspend fun downloadImage(photo: SignedPhoto): ByteArray = when {
        photo.thumbnailUrl.isNotBlank() ->
            storage.getReferenceFromUrl(photo.thumbnailUrl).getBytes(THUMBNAIL_MAX_BYTES).await()
        photo.signedPhotoUrl.isNotBlank() ->
            storage.getReferenceFromUrl(photo.signedPhotoUrl).getBytes(SIGNED_PHOTO_MAX_BYTES).await()
        else -> throw IOException("Photo has no image yet")
    }
}
