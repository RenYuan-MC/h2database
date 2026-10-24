package org.teavm.classlib.java.lang.ref;

import java.lang.ref.ReferenceQueue;
import org.teavm.classlib.java.lang.TObject;

// Compatibility experiment: preserve constructors without changing GC WeakReference layout.
public abstract class TReference<T> extends TObject {
    protected TReference() { }
    protected TReference(T referent) { }
    protected TReference(T referent, ReferenceQueue<? super T> queue) { }
    public T get() { return null; }
    public void clear() { }
    public boolean isEnqueued() { return false; }
    public boolean enqueue() { return false; }
}
