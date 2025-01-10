plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "kapivara-eventhub"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "deployments:spring:event-store-queries",
    "deployments:spring:event-store-commands",
    "deployments:spring:event-store-worker",
    "deployments:spring:event-store-worker-dlq",
    "deployments:spring:event-bus-commands",
    "deployments:spring:event-bus-queries",
    "deployments:spring:event-bus-webhooks",
    "core:eventhub-domains",
    "core:eventhub-ports",
    "core:eventhub-services"
)