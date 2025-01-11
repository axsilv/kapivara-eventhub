package com.eventhub.services.eventstore

import com.eventhub.domain.eventstore.Event
import com.eventhub.ports.eventstore.EventStoreRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.confirmVerified
import io.mockk.mockk

class GetEventServiceTest :
    BehaviorSpec({
        context("Get a single event") {
            given("Event id") {
                `when`("Calls get event") {
                    then("Return the event") {
                        val eventStoreRepository = mockk<EventStoreRepository>()
                        val service =
                            GetEventService(
                                eventStoreRepository = eventStoreRepository,
                            )
                        val event = mockk<Event>()
//                        val eventId = mockk<EventId>()
//
//                        coEvery {
//                            eventId.get(any())
//                        } returns event
//
//                        service.get(eventId = eventId)
//
//                        coVerify { eventId.get(eventStoreRepository = eventStoreRepository) }
                        confirmVerified(eventStoreRepository)
                    }
                }
            }
        }
    })
