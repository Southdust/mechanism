@file:JvmName("EitherExt")
package org.hexalite.mechanism.extension.arrow

import org.hexalite.mechanism.core.functional.Either
import kotlin.jvm.JvmName

public typealias ArrowEither<L, R> = arrow.core.Either<L, R>
public typealias ArrowEitherLeft<L> = arrow.core.Either.Left<L>
public typealias ArrowEitherRight<R> = arrow.core.Either.Right<R>

public inline fun <L, R> Either<L, R>.asArrow(): ArrowEither<L, R> {
    return ArrowEitherLeft(leftOrNull() ?: return ArrowEitherRight(right()))
}
