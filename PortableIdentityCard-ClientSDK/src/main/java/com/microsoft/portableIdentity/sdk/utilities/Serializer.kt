package com.microsoft.portableIdentity.sdk.utilities

import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.PresentationServiceResponse
import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.ServiceResponse
import com.microsoft.portableIdentity.sdk.identifier.IdentifierDocumentService
import com.microsoft.portableIdentity.sdk.identifier.document.service.Endpoint
import com.microsoft.portableIdentity.sdk.identifier.document.service.IdentityHubService
import com.microsoft.portableIdentity.sdk.identifier.document.service.ServiceHubEndpoint
import com.microsoft.portableIdentity.sdk.identifier.document.service.UserHubEndpoint
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlin.reflect.KClass
import kotlin.collections.Map

object Serializer : ISerializer {
    private val identifierDocumentServiceSerializer = SerializersModule {
        polymorphic(IdentifierDocumentService::class) {
            IdentityHubService::class with IdentityHubService.serializer()
        }
    }

    private val serviceEndpointSerializer = SerializersModule {
        polymorphic(Endpoint::class) {
            ServiceHubEndpoint::class with ServiceHubEndpoint.serializer()
            UserHubEndpoint::class with UserHubEndpoint.serializer()
        }
    }

    private val serviceResponseSerializer = SerializersModule {
        polymorphic(ServiceResponse::class) {
            IssuanceServiceResponse::class with IssuanceServiceResponse.serializer()
            PresentationServiceResponse::class with PresentationServiceResponse.serializer()
        }
    }

    val json: Json = Json(
        context = identifierDocumentServiceSerializer + serviceEndpointSerializer + serviceResponseSerializer,
        configuration = JsonConfiguration(
            encodeDefaults = false,
            strictMode = false
        )
    )

    override fun <T> parse(deserializer: DeserializationStrategy<T>, string: String): T =
        json.parse(deserializer, string)

    override fun <T> stringify(serializer: SerializationStrategy<T>, obj: T): String =
        json.stringify(serializer, obj)

    fun <K : Any, V : Any> stringify(obj: Map<K, V>, keyClass: KClass<K>, valClass: KClass<V>): String {
        val serializer: ISerializer = this
        return serializer.stringifyImpl(obj, keyClass, valClass)
    }

    @ImplicitReflectionSerializer
    override fun <K : Any, V : Any> stringifyImpl(obj: Map<K, V>, keyClass: KClass<K>, valClass: KClass<V>): String {
        return json.stringify((keyClass.serializer() to valClass.serializer()).map, obj)
    }

    fun <K : Any, V : Any> parseMap(map: String, keyClass: KClass<K>, valClass: KClass<V>): Map<K, V> {
        val serializer: ISerializer = this
        return serializer.parseMapImpl(map, keyClass, valClass)
    }

    @ImplicitReflectionSerializer
    override fun <K : Any, V : Any> parseMapImpl(map: String, keyClass: KClass<K>, valClass: KClass<V>): Map<K, V> {
        return parse((keyClass.serializer() to valClass.serializer()).map, map)
    }

    fun <T : Any> stringify(objects: List<T>, keyClass: KClass<T>): String {
        val serializer: ISerializer = this
        return serializer.stringifyImpl(objects, keyClass)
    }

    @ImplicitReflectionSerializer
    override fun <T : Any> stringifyImpl(objects: List<T>, keyClass: KClass<T>): String {
        return stringify((keyClass.serializer()).list, objects)
    }

}