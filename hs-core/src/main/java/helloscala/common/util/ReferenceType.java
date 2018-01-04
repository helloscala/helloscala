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

