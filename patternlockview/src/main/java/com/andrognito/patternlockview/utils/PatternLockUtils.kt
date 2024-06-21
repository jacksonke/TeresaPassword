/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.andrognito.patternlockview.utils

import com.andrognito.patternlockview.PatternLockView
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Locale

class PatternLockUtils private constructor() {
    init {
        throw AssertionError(
            "You can not instantiate this class. Use its static utility " +
                    "methods instead"
        )
    }

    companion object {
        private const val UTF8 = "UTF-8"
        private const val SHA1 = "SHA-1"
        private const val MD5 = "MD5"

        /**
         * Serializes a given pattern to its equivalent string representation. You can store this string
         * in any persistence storage or send it to the server for verification
         *
         * @param pattern The actual pattern
         * @return The pattern in its string form
         */
        fun patternToString(
            patternLockView: PatternLockView,
            pattern: List<PatternLockView.Dot>?
        ): String {
            if (pattern == null) {
                return ""
            }
            val patternSize = pattern.size
            val stringBuilder = StringBuilder()
            for (i in 0 until patternSize) {
                val dot = pattern[i]
                stringBuilder.append(dot.row * patternLockView.dotCount + dot.column)
            }
            return stringBuilder.toString()
        }

        /**
         * De-serializes a given string to its equivalent pattern representation
         *
         * @param string The pattern serialized with [.patternToString]
         * @return The actual pattern
         */
        @JvmStatic
        fun stringToPattern(
            patternLockView: PatternLockView,
            string: String
        ): List<PatternLockView.Dot> {
            val result: MutableList<PatternLockView.Dot> = ArrayList()
            for (i in 0 until string.length) {
                val number = Character.getNumericValue(string[i])
                PatternLockView.Dot.of(
                    number / patternLockView.dotCount,
                    number % patternLockView.dotCount
                )?.let {
                    result.add(
                        it
                    )
                }
            }
            return result
        }

        /**
         * Serializes a given pattern to its equivalent SHA-1 representation. You can store this string
         * in any persistence storage or send it to the server for verification
         *
         * @param pattern The actual pattern
         * @return The SHA-1 string of the pattern
         */
        fun patternToSha1(
            patternLockView: PatternLockView,
            pattern: ArrayList<PatternLockView.Dot>?
        ): String? {
            return try {
                val messageDigest = MessageDigest.getInstance(SHA1)
                messageDigest.update(
                    patternToString(patternLockView, pattern).toByteArray(
                        charset(
                            UTF8
                        )
                    )
                )
                val digest = messageDigest.digest()
                val bigInteger = BigInteger(1, digest)
                String.format(
                    null as Locale?,
                    "%0" + digest.size * 2 + "x", bigInteger
                ).lowercase(Locale.getDefault())
            } catch (e: NoSuchAlgorithmException) {
                null
            } catch (e: UnsupportedEncodingException) {
                null
            }
        }

        /**
         * Serializes a given pattern to its equivalent MD5 representation. You can store this string
         * in any persistence storage or send it to the server for verification
         *
         * @param pattern The actual pattern
         * @return The MD5 string of the pattern
         */
        fun patternToMD5(
            patternLockView: PatternLockView,
            pattern: ArrayList<PatternLockView.Dot>?
        ): String? {
            return try {
                val messageDigest = MessageDigest.getInstance(MD5)
                messageDigest.update(
                    patternToString(patternLockView, pattern).toByteArray(
                        charset(
                            UTF8
                        )
                    )
                )
                val digest = messageDigest.digest()
                val bigInteger = BigInteger(1, digest)
                String.format(
                    null as Locale?,
                    "%0" + digest.size * 2 + "x", bigInteger
                ).lowercase(Locale.getDefault())
            } catch (e: NoSuchAlgorithmException) {
                null
            } catch (e: UnsupportedEncodingException) {
                null
            }
        }

        /**
         * Generates a random "CAPTCHA" pattern. The generated pattern is easy for the user to re-draw.
         *
         *
         * NOTE: This method is **not** optimized and **not** benchmarked yet for large mSize
         * of the pattern's matrix. Currently it works fine with a matrix of `3x3` cells.
         * Be careful when the mSize increases.
         */
        @Throws(IndexOutOfBoundsException::class)
        fun generateRandomPattern(
            patternLockView: PatternLockView?,
            size: Int
        ): ArrayList<PatternLockView.Dot> {
            requireNotNull(patternLockView) { "PatternLockView can not be null." }
            if (size <= 0 || size > patternLockView.dotCount) {
                throw IndexOutOfBoundsException(
                    "Size must be in range [1, " +
                            patternLockView.dotCount + "]"
                )
            }
            val usedIds: MutableList<Int> = ArrayList()
            var lastId = RandomUtils.randInt(patternLockView.dotCount)
            usedIds.add(lastId)
            while (usedIds.size < size) {
                // We start from an empty matrix, so there's always a break point to
                // exit this loop
                val lastRow = lastId / patternLockView.dotCount
                val lastCol = lastId % patternLockView.dotCount

                // This is the max available rows/ columns that we can reach from
                // the cell of `lastId` to the border of the matrix.
                val maxDistance = Math.max(
                    Math.max(lastRow, patternLockView.dotCount - lastRow),
                    Math.max(lastCol, patternLockView.dotCount - lastCol)
                )
                lastId = -1

                // Starting from `distance` = 1, find the closest-available
                // neighbour value of the cell [lastRow, lastCol].
                for (distance in 1..maxDistance) {

                    // Now we have a square surrounding the current cell. We call it
                    // ABCD, in which A is top-left, and C is bottom-right.
                    val rowA = lastRow - distance
                    val colA = lastCol - distance
                    val rowC = lastRow + distance
                    val colC = lastCol + distance
                    var randomValues: IntArray

                    // Process randomly AB, BC, CD, and DA. Break the loop as soon
                    // as we find one value.
                    val lines = RandomUtils.randIntArray(4)
                    for (line in lines) {
                        when (line) {
                            0 -> {
                                if (rowA >= 0) {
                                    randomValues = RandomUtils.randIntArray(
                                        Math.max(0, colA),
                                        Math.min(
                                            patternLockView.dotCount,
                                            colC + 1
                                        )
                                    )
                                    for (c in randomValues) {
                                        lastId = (rowA * patternLockView.dotCount
                                                + c)
                                        lastId = if (usedIds.contains(lastId)) -1 else break
                                    }
                                }
                            }

                            1 -> {
                                if (colC < patternLockView.dotCount) {
                                    randomValues = RandomUtils.randIntArray(
                                        Math.max(0, rowA + 1),
                                        Math.min(
                                            patternLockView.dotCount,
                                            rowC + 1
                                        )
                                    )
                                    for (r in randomValues) {
                                        lastId = (r * patternLockView.dotCount
                                                + colC)
                                        lastId = if (usedIds.contains(lastId)) -1 else break
                                    }
                                }
                            }

                            2 -> {
                                if (rowC < patternLockView.dotCount) {
                                    randomValues = RandomUtils.randIntArray(
                                        Math.max(0, colA),
                                        Math.min(
                                            patternLockView.dotCount,
                                            colC
                                        )
                                    )
                                    for (c in randomValues) {
                                        lastId = (rowC * patternLockView.dotCount
                                                + c)
                                        lastId = if (usedIds.contains(lastId)) -1 else break
                                    }
                                }
                            }

                            3 -> {
                                if (colA >= 0) {
                                    randomValues = RandomUtils.randIntArray(
                                        Math.max(0, rowA + 1),
                                        Math.min(
                                            patternLockView.dotCount,
                                            rowC
                                        )
                                    )
                                    for (r in randomValues) {
                                        lastId = (r * patternLockView.dotCount
                                                + colA)
                                        lastId = if (usedIds.contains(lastId)) -1 else break
                                    }
                                }
                            }
                        }
                        if (lastId >= 0) break
                    }
                    if (lastId >= 0) break
                }
                usedIds.add(lastId)
            }
            val result = ArrayList<PatternLockView.Dot>()
            for (id in usedIds) {
                PatternLockView.Dot.of(id)?.let { result.add(it) }
            }
            return result
        }
    }
}