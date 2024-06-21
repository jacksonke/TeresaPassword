/*
 *   Copyright 2012 Hai Bison
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.andrognito.patternlockview.utils

import java.util.Random

/**
 * Random utilities.
 */
class RandomUtils private constructor() {
    init {
        throw AssertionError(
            "You can not instantiate this class. Use its static utility " +
                    "methods instead"
        )
    }

    companion object {
        private val RANDOM = Random()

        /**
         * Generates a random integer
         */
        fun randInt(): Int {
            return RANDOM.nextInt((System.nanoTime() % Int.MAX_VALUE).toInt())
        }

        /**
         * Generates a random integer within `[0, max)`.
         *
         * @param max The maximum bound
         * @return A random integer
         */
        fun randInt(max: Int): Int {
            return if (max > 0) randInt() % max else 0
        }

        /**
         * Generates a random integer array which has length of `end - start`,
         * and is filled by all values from `start` to `end - 1` in randomized orders.
         *
         * @param start The starting value
         * @param end   The ending value
         * @return The random integer array. If `end <= start`, an empty array is returned
         */
        fun randIntArray(start: Int, end: Int): IntArray {
            if (end <= start) {
                return IntArray(0)
            }
            val values: MutableList<Int> = ArrayList()
            for (i in start until end) {
                values.add(i)
            }
            val result = IntArray(values.size)
            for (i in result.indices) {
                val k = randInt(values.size)
                result[i] = values[k]
                values.removeAt(k)
            }
            return result
        }

        /**
         * Generates a random integer array which has length of `end`,
         * and is filled by all values from `0` to `end - 1` in randomized orders.
         *
         * @param end The ending value
         * @return The random integer array. If `end <= start`, an empty array is returned
         */
        fun randIntArray(end: Int): IntArray {
            return randIntArray(0, end)
        }
    }
}