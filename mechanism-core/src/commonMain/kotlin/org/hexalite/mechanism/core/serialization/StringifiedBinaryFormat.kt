package org.hexalite.mechanism.core.serialization

import kotlinx.serialization.*
import kotlinx.serialization.modules.SerializersModule
import org.hexalite.mechanism.core.text.encodeToHexByteArray
import org.hexalite.mechanism.core.text.encodeToHexString
import kotlin.jvm.JvmInline

/**
 * A wrapping utility to [BinaryFormat] which can represent it as a [StringFormat] in order to read or store data that
 * can only be available as a String. This works by encoding the [ByteArray] into a stringified hexadecimal
 * representation and reading it back whenever necessary.
 * @author FromSyntax
 * @see
 */
@Suppress("MemberVisibilityCanBePrivate")
@JvmInline
public value class StringifiedBinaryFormat(public val delegate: BinaryFormat) : BinaryFormat by delegate, StringFormat {
    override val serializersModule: SerializersModule
        get() = delegate.serializersModule

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        return delegate.encodeToByteArray(serializer, value).encodeToHexString()
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        return delegate.decodeFromByteArray(deserializer, string.encodeToHexByteArray())
    }
}