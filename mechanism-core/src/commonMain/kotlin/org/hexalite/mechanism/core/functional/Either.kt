package org.hexalite.mechanism.core.functional

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.hexalite.mechanism.core.functional.Either.Companion.either
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A wrapper for a value that may be two distinct types. It is inspired by the functional programming
 * paradigm, and it is used to avoid the usage of exceptions on Hexalite to allow a more developer-friendly
 * environment.
 *
 * You can build an [Either] type by using one of the functions on companion:
 * * [Either.left]   to create an [Either] type bound to the left.
 * * [Either.right]  to create an [Either] type bound to the right.
 * * [Either.decide] to create an [Either] type based on the given value and generics.
 * * [Either.either] to create an [Either] type bound to the left or right (falling back) by an extension
 *   function.
 *
 * An [Either] type is pure, declarative and immutable. You can create another [Either]
 * types from a single one by using map  functions:
 * * [Either.mapLeft]  to create another [Either] type by using the bound type at [left]
 *                     as a transform function.
 * * [Either.mapRight] to create another [Either] type by using the bound type at [right]
 *                     as a transform function.
 *
 * @since 0.1.0
 * @author FromSyntax
 * @author Gabriel
 * @param L The required type to the value be bound at left.
 * @param R The required type to the value be bound at right.
 */
@Serializable(with = Either.Serializer::class)
public sealed interface Either<L, R> {
    /**
     * Returns whether this [Either] type is bound to left.
     */
    public fun isLeft(): Boolean = leftOrNull() != null

    /**
     * Returns whether this [Either] type is bound to right.
     */
    public fun isRight(): Boolean = rightOrNull() != null

    /**
     * Returns the value bound at the left type ([L]) or null if it is not bound at the given position.
     */
    public fun leftOrNull(): L? = null

    /**
     * Returns the value bound at the right type ([R]) or null if it is not bound at the given position.
     */
    public fun rightOrNull(): R? = null

    /**
     * Returns the value bound at the left type ([L]) or throws a [BoundTypeMismatchException] if the value
     * is not bound at the given position.
     */
    public fun left(): L = leftOrThrow { BoundTypeMismatchException('L') }

    /**
     * Returns the value bound at the right type ([R]) or throws a [BoundTypeMismatchException] if the value
     * is not bound at the given position.
     */
    public fun right(): R = rightOrThrow { BoundTypeMismatchException('R') }

    /**
     * Returns a Kotlin's [Result] representation from this [Either] binding.
     * The [right] side is often referred as the "error" or "failure" type in functional languages or
     * multi-paradigm languages such as Rust.
     */
    public fun asResult(): Result<L> {
        return Result.success(leftOrNull() ?: return Result.failure(BoundTypeMismatchException('R')))
    }

    /**
     * Returns this same [Either] but with the bound types swapped. In other words:
     *   [L] -> [R]
     *   [R] -> [L]
     */
    public fun swap(): Either<R, L> = if (isLeft()) right(left()) else left(right())

    /**
     * A simple abstraction of an [Either] type bound to the left side ([L]).
     * @author FromSyntax
     */
    public data class Left<T>(private val value: T) : Either<T, Nothing> {
        override fun leftOrNull(): T = value
    }

    /**
     * A simple abstraction of an [Either] type bound to the right side ([R]).
     * @author FromSyntax
     */
    public data class Right<T>(private val value: T) : Either<Nothing, T> {
        override fun rightOrNull(): T = value
    }

    //
    @Suppress("UNCHECKED_CAST")
    public companion object {
        /**
         * Creates an [Either] type bound on the [left] side ([L]) with the given [value] and with
         * an empty [right] side ([R]).
         * @param value
         */
        public fun <L, R> left(value: L): Either<L, R> = Left(value) as Either<L, R>

        /**
         * Creates an [Either] type bound to the [right] side ([R]) with the given [value] and with
         * an empty [left] side ([L]).
         * @param value
         */
        public fun <L, R> right(value: R): Either<L, R> = Right(value) as Either<L, R>

        /**
         * Creates an [Either] type based on callback exception handling where the success output is
         * bound to the [left] side ([L]) and the error type is bound to the [right] side ([R]).
         * @param callback
         */
        @Dsl
        @OptIn(ExperimentalContracts::class)
        public inline fun <L, reified R : Throwable> catching(callback: () -> L): Either<L, R> {
            contract {
                callsInPlace(callback, InvocationKind.EXACTLY_ONCE)
            }
            return try {
                left(callback())
            } catch (exception: Throwable) {
                if (exception is R) {
                    right(exception)
                } else {
                    throw exception
                }
            }
        }

        /**
         * Creates an [Either] type bound at what side the given [value]'s type is.
         * @throws IllegalArgumentException if the given [value] does not match [L] and [R].
         * @param value
         */
        public inline fun <reified L, reified R> decide(value: Any): Either<L, R> = when (value) {
            is L -> left(value)
            is R -> right(value)
            else -> throw IllegalArgumentException(
                "The provided value for Either#decide is not either '${L::class.simpleName}' or '${R::class.simpleName}'."
            )
        }

        /**
         * Creates an [Either] type bound to the [left] side ([L]) if the extension receiver is not null,
         * otherwise [fallback] to an [Either] type bound to the [right] side ([R]).
         * @param fallback
         */
        @OptIn(ExperimentalContracts::class)
        @Dsl
        public inline fun <reified L, reified R> L?.either(fallback: () -> R & Any): Either<L, R> {
            contract {
                callsInPlace(fallback, InvocationKind.AT_MOST_ONCE)
            }
            if (this != null) {
                return left(this)
            }
            return right(fallback())
        }
    }

    @DslMarker
    @Target(AnnotationTarget.FUNCTION)
    public annotation class Dsl

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    public class Serializer<L, R>(
        private val leftSerializer: KSerializer<L>,
        private val rightSerializer: KSerializer<R>
    ) :
        KSerializer<Either<L, R>> {
        override val descriptor: SerialDescriptor =
            buildSerialDescriptor("org.hexalite.mechanism.core.functional.Either", SerialKind.CONTEXTUAL)

        public override fun serialize(encoder: Encoder, value: Either<L, R>) {
            value.leftOrNull()?.let { encoder.encodeNullableSerializableValue(leftSerializer, it) }
                ?: encoder.encodeNullableSerializableValue(rightSerializer, value.right())
        }

        public override fun deserialize(decoder: Decoder): Either<L, R> {
            return try {
                left(decoder.decodeSerializableValue(leftSerializer))
            } catch (exception: SerializationException) {
                right(decoder.decodeSerializableValue(rightSerializer))
            }
        }
    }

    public class BoundTypeMismatchException(side: Char) :
        NoSuchElementException("The Either type is not bound at [$side].")
}

/**
 * Apply the given [transform] function to the element bound on the left side ([L]) if it is present.
 * @return A new [Either] type containing the result of this [mapLeft] call.
 * @param transform
 */
@Either.Dsl
@OptIn(ExperimentalContracts::class)
public inline fun <L, R, T> Either<L, R>.mapLeft(transform: (L) -> T): Either<T, R> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return Either.left(transform(leftOrNull() ?: return Either.right(right())))
}

/**
 * Apply the given [transform] function to the element bound on the right side ([R]) if it is present.
 * @return A new [Either] type containing the result of this [mapRight] call.
 * @param transform
 */
@Either.Dsl
@OptIn(ExperimentalContracts::class)
public inline fun <L, R, T> Either<L, R>.mapRight(transform: (R) -> T): Either<L, T> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return Either.right(transform(rightOrNull() ?: return Either.left(left())))
}

/**
 * Apply the given [transformLeft] function to the element bound on the left side ([L]) if it is present.
 * Apply the given [transformRight] function to the element bound on the right side ([R]) if it is present.
 * @return A new [Either] type containing the result of this [mapLeft] call.
 * @param transformLeft
 * @param transformRight
 */
@OptIn(ExperimentalContracts::class)
@Either.Dsl
public inline fun <L, R, T1, T2> Either<L, R>.map(transformLeft: (L) -> T1, transformRight: (R) -> T2): Either<T1, T2> {
    contract {
        callsInPlace(transformLeft, InvocationKind.AT_MOST_ONCE)
        callsInPlace(transformRight, InvocationKind.AT_MOST_ONCE)
    }
    return if (isLeft()) Either.left(transformLeft(left())) else Either.right(transformRight(right()))
}

/**
 * Executes the given [callback] if this [Either] type is bound to the left side ([L]).
 * This is similar to 'fire-and-forget'.
 * @param callback
 */
@OptIn(ExperimentalContracts::class)
@Either.Dsl
public inline fun <L, R> Either<L, R>.ifLeft(callback: (L) -> Unit): Either<L, R> {
    contract {
        callsInPlace(callback, InvocationKind.AT_MOST_ONCE)
    }
    callback(leftOrNull() ?: return this)
    return this
}

/**
 * Executes the given [callback] if this [Either] type is bound to the right side ([R]).
 * This is similar to 'fire-and-forget'.
 * @param callback
 */
@OptIn(ExperimentalContracts::class)
@Either.Dsl
public inline fun <L, R> Either<L, R>.ifRight(callback: (R) -> Unit): Either<L, R> {
    contract {
        callsInPlace(callback, InvocationKind.AT_MOST_ONCE)
    }
    callback(rightOrNull() ?: return this)
    return this
}

/**
 * Executes the given [callbackLeft] if this [Either] type is bound to the right side ([R]).
 * Executes the given [callbackRight] if this [Either] type is bound to the left side ([R]).
 * This is similar to 'fire-and-forget'.
 * @param callbackLeft
 * @param callbackRight
 */
@OptIn(ExperimentalContracts::class)
@Either.Dsl
public inline fun <L, R> Either<L, R>.`if`(callbackLeft: (L) -> Unit, callbackRight: (R) -> Unit): Either<L, R> {
    contract {
        callsInPlace(callbackLeft, InvocationKind.AT_MOST_ONCE)
        callsInPlace(callbackRight, InvocationKind.AT_MOST_ONCE)
    }
    leftOrNull()?.let(callbackLeft) ?: callbackRight(right())
    return this
}

/**
 * Takes the value of another [Either] bound to the left side ([L]).
 * @param other
 */
@OptIn(ExperimentalContracts::class)
@Either.Dsl
public inline fun <L, R> Either<L, R>.takeLeft(other: () -> Either<L, *>?): Either<L, R> {
    contract {
        callsInPlace(other, InvocationKind.EXACTLY_ONCE)
    }
    return Either.left(other()?.left() ?: return this)
}

/**
 * Takes the value of another [Either] bound to the right side ([R]).
 * @param other
 */
@OptIn(ExperimentalContracts::class)
@Either.Dsl
public inline fun <L, R> Either<L, R>.takeRight(other: () -> Either<*, R>?): Either<L, R> {
    contract {
        callsInPlace(other, InvocationKind.EXACTLY_ONCE)
    }
    return Either.right(other()?.right() ?: return this)
}

/**
 * Takes the value of another [Either] bound to the left side ([L]) if exists.
 * Takes the value of another [Either] bound to the right side ([R]) if exists.
 * @param other
 */
@OptIn(ExperimentalContracts::class)
@Either.Dsl
public inline fun <L, R> Either<L, R>.take(other: () -> Either<L, *>?): Either<L, R> {
    contract {
        callsInPlace(other, InvocationKind.EXACTLY_ONCE)
    }
    val o = other() ?: return this
    return Either.left(o.leftOrNull() ?: return Either.right(rightOrNull() ?: return this))
}


/**
 * Returns an [Either] type with the value bound to the left side ([L]) as the result of the given
 * [callback], if not null.
 * @param callback
 */
@OptIn(ExperimentalContracts::class)
@Either.Dsl
public inline fun <L, R> Either<L, R>.withLeft(callback: () -> L?): Either<L, R> {
    contract {
        callsInPlace(callback, InvocationKind.EXACTLY_ONCE)
    }
    return Either.left(callback() ?: return this)
}

/**
 * Returns an [Either] type with the value bound to the right side ([R]) as the result of the given
 * [callback], if not null.
 * @param callback
 */
@OptIn(ExperimentalContracts::class)
@Either.Dsl
public inline fun <L, R> Either<L, R>.withRight(callback: () -> R?): Either<L, R> {
    contract {
        callsInPlace(callback, InvocationKind.EXACTLY_ONCE)
    }
    return Either.right(callback() ?: return this)
}


/**
 * Returns the value bound at the left type ([L]) or throws the exception returned by the given [error]
 * function. This function is highly inspired by Rust's `Result#expect`.
 * @param error
 */
@OptIn(ExperimentalContracts::class)
@Either.Dsl
public inline fun <L> Either<L, *>.leftOrThrow(error: () -> Throwable): L {
    contract {
        callsInPlace(error, InvocationKind.AT_MOST_ONCE)
    }
    return leftOrNull() ?: throw error()
}

/**
 * Returns the value bound at the right type ([R]) or throws the exception returned by the given [error]
 * function. This function is highly inspired by Rust's `Result#expect`.
 * @param error
 */
@OptIn(ExperimentalContracts::class)
@Either.Dsl
public inline fun <R> Either<*, R>.rightOrThrow(error: () -> Throwable): R {
    contract {
        callsInPlace(error, InvocationKind.AT_MOST_ONCE)
    }
    return rightOrNull() ?: throw error()
}

/**
 * Returns the value bound at the left type ([L]) or returns the default value returned by the given
 * [default] function.
 * @param default
 */
@OptIn(ExperimentalContracts::class)
@Either.Dsl
public inline fun <L> Either<L, *>.leftOrDefault(default: () -> L): L {
    contract {
        callsInPlace(default, InvocationKind.AT_MOST_ONCE)
    }
    return leftOrNull() ?: default()
}

/**
 * Returns the value bound at the right type ([R]) or returns the default value returned by the given
 * [default] function.
 * @param default
 */
@OptIn(ExperimentalContracts::class)
@Either.Dsl
public inline fun <R> Either<*, R>.rightOrDefault(default: () -> R): R {
    contract {
        callsInPlace(default, InvocationKind.AT_MOST_ONCE)
    }
    return rightOrNull() ?: default()
}
