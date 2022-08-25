@file:Suppress("NOTHING_TO_INLINE")

package org.hexalite.mechanism.core.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(with = OptionalSerializer::class)
public expect value class Optional<T: Any> internal constructor(public val value: Any?) {
    public fun get(): T?
}

public expect class OptionalSerializer<T: Any>: KSerializer<Optional<T>>

public expect inline fun <T> emptyOptional(): Optional<T & Any>

public expect inline fun <T> T?.optional(): Optional<T & Any>

