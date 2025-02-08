package com.kapivara.adapters.databases.filedatabase

import com.kapivara.adapters.databases.filedatabase.FileDatabaseVariables.streamPath
import com.kapivara.domain.eventstore.EventMessage
import com.kapivara.domain.eventstore.EventMessage.EventMessageId
import com.kapivara.domain.eventstore.EventStream
import com.kapivara.domain.eventstore.EventStream.EventStreamId
import com.kapivara.domain.eventstore.Identity.IdentityId
import com.kapivara.domain.eventstore.Publisher.PublisherId
import com.kapivara.domain.eventstore.ports.EventStreamRepository
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

class FileDatabaseEventStreamRepository(
    private val database: FileDatabase,
) : EventStreamRepository {
    private val cache: DatabaseCache = DatabaseCache.create<UUID, List<EventMessage>>()

    override suspend fun store(eventMessage: EventMessage) {
        val streamId = eventMessage.eventStreamId.toString()
        val messageId = eventMessage.id.toString()
        val folderStreamHash = streamId.hashCode()
        val streamFile = File(streamPath() + "$folderStreamHash/$streamId/$messageId.gz")

        streamFile.parentFile?.mkdirs()

        database.writeFileAsync(
            filePath = streamFile.path,
            content = Json.encodeToString(eventMessage.toMap()),
        )

        fetchCache(eventMessage.eventStreamId)
            ?.plus(eventMessage)
            ?.let { streamCache ->
                cache.store(
                    key = eventMessage.eventStreamId,
                    value = streamCache,
                )
            }
    }

    override suspend fun fetch(
        eventStreamId: EventStreamId,
        useCache: Boolean,
    ): EventStream? {
        if (useCache) {
            fetchCache(eventStreamId)
                ?.let {
                    return EventStream(eventMessages = it)
                }
        }

        val folderStreamHash = eventStreamId.toString().hashCode()
        val streamFile = File(streamPath() + "$folderStreamHash/$eventStreamId")

        if (streamFile.exists().not()) return null

        val messages = fetchStreamFromStore(streamFile)

        return EventStream(eventMessages = messages)
    }

    private suspend fun FileDatabaseEventStreamRepository.fetchStreamFromStore(streamFile: File): Set<EventMessage> =
        streamFile
            .listFiles()
            .toList()
            .map { entry -> entry.toMap() }
            .map { jsonEntry -> jsonEntry.toEventMessage() }
            .toSet()

    private fun fetchCache(eventStreamId: EventStreamId): Set<EventMessage>? =
        cache
            .fetch<UUID, Set<EventMessage>>(eventStreamId.value)

    private suspend fun File.toMap(): Map<String, Any> = Json.decodeFromString<Map<String, Any>>(database.readFileAsync(this.path))

    private fun EventMessage.toMap(): Map<String, Any> =
        mapOf(
            "id" to id.value,
            "identityId" to identityId.value,
            "publisherId" to publisherId.value,
            "eventStreamId" to eventStreamId.value,
            "payload" to payload,
            "position" to position,
            "isFinal" to isFinal,
            "occurredOn" to occurredOn.toString(),
        )

    private fun Map<String, Any>.toEventMessage(): EventMessage =
        EventMessage(
            id = EventMessageId(getValue("id") as UUID),
            identityId = IdentityId(getValue("identityId") as Long),
            publisherId = PublisherId(getValue("publisherId") as Long),
            eventStreamId = EventStreamId(getValue("eventStreamId") as UUID),
            payload = getValue("payload") as String,
            position = (getValue("position") as Number).toLong(),
            isFinal = getValue("isFinal") as Boolean,
            occurredOn = Instant.parse(getValue("occurredOn") as String),
        )
}
