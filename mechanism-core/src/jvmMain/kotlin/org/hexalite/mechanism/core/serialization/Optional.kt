@file:Suppress("NOTHING_TO_INLINE")

package org.hexalite.mechanism.core.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@JvmInline
@Serializable(with = OptionalSerializer::class)
public actual value class Optional<T: Any>(public val value: Any?) {
    public actual fun get(): T? = value as? T?
}

public actual class OptionalSerializer<T: Any>(private val serializer: KSerializer<T>) : KSerializer<Optional<T>> {
    override val descriptor: SerialDescriptor = serializer.descriptor

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): Optional<T> {
        return if (decoder.decodeNotNullMark()) {
            serializer.deserialize(decoder).optional()
        } else {
            emptyOptional()
        }
    }

    override fun serialize(encoder: Encoder, value: Optional<T>) {
        serializer.serialize(encoder, value.get() ?: return)
    }
}

public actual inline fun <T> emptyOptional(): Optional<T & Any> = Optional(null)

public actual inline fun <T> T?.optional(): Optional<T & Any> = Optional(this)
