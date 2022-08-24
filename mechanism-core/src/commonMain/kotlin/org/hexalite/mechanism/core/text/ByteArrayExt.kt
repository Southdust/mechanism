@file:JvmName("BinaryExt")

package org.hexalite.mechanism.core.text

import kotlin.jvm.JvmName

/**
 * Convert a [ByteArray] into a stringified hexadecimal representation of it. This works by joining each element
 * and converting them to a base-16 radix string. First, it this ByteArray is converted to a [UByteArray], which is
 * a pretty cheap operation that in most cases will not cause any overhead since it is an inline/value class. All
 * sequences of hexadecimal numbers are padded to have a length of 2 to make it easier to be read back.
 * @author FromSyntax
 */
@OptIn(ExperimentalUnsignedTypes::class)
public fun ByteArray.encodeToHexString(): String = asUByteArray()
    .joinToString("") {
        it.toString(16).padStart(2, '0')
    }

/**
 * Decode a 2-padded stringified hexadecimal representation back into a [ByteArray].
 * @author FromSyntax
 */
public fun String.encodeToHexByteArray(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
