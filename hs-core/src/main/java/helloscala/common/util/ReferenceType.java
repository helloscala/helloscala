/*
 * Copyright 2017 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package helloscala.common.util;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * Various reference types supported by this map.
 */
public enum ReferenceType {

    /**
     * Use {@link SoftReference}s
     */
    SOFT,

    /**
     * Use {@link WeakReference}s
     */
    WEAK
}

