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

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

class ResourceUtils private constructor() {
    init {
        throw AssertionError(
            "You can not instantiate this class. Use its static utility " +
                    "methods instead"
        )
    }

    companion object {
        /**
         * Get color from a resource id
         *
         * @param context  The context
         * @param colorRes The resource identifier of the color
         * @return The resolved color value
         */
        @JvmStatic
        fun getColor(context: Context, @ColorRes colorRes: Int): Int {
            return ContextCompat.getColor(context, colorRes)
        }

        /**
         * Get string from a resource id
         *
         * @param context   The context
         * @param stringRes The resource identifier of the string
         * @return The string value
         */
        fun getString(context: Context, @StringRes stringRes: Int): String {
            return context.getString(stringRes)
        }

        /**
         * Get dimension in pixels from its resource id
         *
         * @param context  The context
         * @param dimenRes The resource identifier of the dimension
         * @return The dimension in pixels
         */
        @JvmStatic
        fun getDimensionInPx(context: Context, @DimenRes dimenRes: Int): Float {
            return context.resources.getDimension(dimenRes)
        }
    }
}