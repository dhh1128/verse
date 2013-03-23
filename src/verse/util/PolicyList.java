/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: dhh1969
 * Created: Oct 28, 2009
 */
package verse.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.util.List;
import java.util.ListIterator;

/**
 * A list that handles duplicates, nulls, and locking according to policy
 * instead of having those behaviors hard-coded. Uses an {@link ArrayList}
 * internally, so it has similar performance.
 */
public class PolicyList<T> implements List<T> {

	private final boolean allowNulls;
	private final boolean allowDups;
	private final ReentrantReadWriteLock rwl;
	private final Lock r;
	private final Lock w;
	private final List<T> mList;

	/**
	 * Create a new PolicyList with the specified behaviors.
	 * @param threads
	 * @param nulls
	 * @param dups
	 */
	public PolicyList(ThreadsPolicy threads, NullsPolicy nulls, DupsPolicy dups) {
		allowNulls = (nulls == NullsPolicy.ALLOW_NULLS);
		allowDups = (dups == DupsPolicy.ALLOW_DUPS);
		if (threads == ThreadsPolicy.MULTIPLE_THREADS) {
			rwl = new ReentrantReadWriteLock();
			r = rwl.readLock();
			w = rwl.writeLock();
		} else {
			rwl = null;
			r = null;
			w = null;
		}
		mList = new ArrayList<T>();
	}

	/**
	 * Create a new PolicyList with the specified behaviors and initial capacity.
	 * @param threads
	 * @param nulls
	 * @param dups
	 * @param initialCapacity
	 */
	public PolicyList(ThreadsPolicy threads, NullsPolicy nulls, DupsPolicy dups, 
			int initialCapacity) {
		allowNulls = (nulls == NullsPolicy.ALLOW_NULLS);
		allowDups = (dups == DupsPolicy.ALLOW_DUPS);
		if (threads == ThreadsPolicy.MULTIPLE_THREADS) {
			rwl = new ReentrantReadWriteLock();
			r = rwl.readLock();
			w = rwl.writeLock();
		} else {
			rwl = null;
			r = null;
			w = null;
		}
		mList = new ArrayList<T>(initialCapacity);
	}

	/**
	 * Allows derived classes to react whenever one or more items are added.
	 * 
	 * @param internalList
	 */
	protected void onAdd(List<T> internalList) {
	}

	/**
	 * Allows derived classes to react whenever one or more items are removed.
	 * 
	 * @param internalList
	 */
	protected void onRemove(List<T> internalList) {
	}

	/**
	 * @return true if item was added
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#add(java.lang.Object)
	 */
	@Override
	public boolean add(T e) {
		boolean added = false;
		if (e != null || allowNulls) {
			if (w != null)
				w.lock();
			try {
				if (allowDups || !mList.contains(e)) {
					added = mList.add(e);
					if (added) {
						onAdd(mList);
					}
				}
			} finally {
				if (w != null)
					w.unlock();
			}
		}
		return added;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	@Override
	public void add(int index, T e) {
		if (e != null || allowNulls) {
			if (w != null)
				w.lock();
			try {
				if (allowDups || !mList.contains(e)) {
					mList.add(index, e);
					onAdd(mList);
				}
			} finally {
				if (w != null)
					w.unlock();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean changed = false;
		if (c != null && !c.isEmpty()) {
			if (w != null)
				w.lock();
			try {
				for (T f : c) {
					if (f != null || allowNulls) {
						if (allowDups || !mList.contains(f)) {
							changed = changed && mList.add(f);
						}
					}
				}
				if (changed) {
					onAdd(mList);
				}
			} finally {
				if (w != null)
					w.unlock();
			}
		}
		return changed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		boolean changed = false;
		if (c != null && !c.isEmpty()) {
			if (w != null)
				w.lock();
			try {
				for (T f : c) {
					if (f != null || allowNulls) {
						if (allowDups || !mList.contains(f)) {
							changed = true;
							mList.add(index, f);
							++index;
						}
					}
				}
				if (changed) {
					onAdd(mList);
				}
			} finally {
				if (w != null)
					w.unlock();
			}
		}
		return changed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#clear()
	 */
	@Override
	public void clear() {
		if (w != null)
			w.lock();
		try {
			if (!mList.isEmpty()) {
				mList.clear();
				onRemove(mList);
			}
		} finally {
			if (w != null)
				w.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		if (r != null) r.lock();
		try {
			return mList.contains(o);
		} finally {
			if (r != null) r.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		if (r != null) r.lock();
		try {
			return mList.containsAll(c);
		} finally {
			if (r != null) r.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#get(int)
	 */
	@Override
	public T get(int index) {
		if (r != null) r.lock();
		try {
			return mList.get(index);
		} finally {
			if (r != null) r.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object o) {
		if (r != null) r.lock();
		try {
			return mList.indexOf(o);
		} finally {
			if (r != null) r.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		if (r != null) r.lock();
		try {
			return mList.isEmpty();
		} finally {
			if (r != null) r.unlock();
		}
	}

	private List<T> getIterableCopy() {
		r.lock();
		try {
			List<T> copy = new ArrayList<T>(mList.size());
			copy.addAll(mList);
			return copy;
		} finally {
			r.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		if (r == null) {
			return mList.iterator();
		}
		List<T> copy = getIterableCopy();
		return copy.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(Object o) {
		if (r != null) r.lock();
		try {
			return mList.lastIndexOf(o);
		} finally {
			if (r != null) r.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#listIterator()
	 */
	@Override
	public ListIterator<T> listIterator() {
		if (r == null) {
			return mList.listIterator();
		}
		List<T> copy = getIterableCopy();
		return copy.listIterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator<T> listIterator(int index) {
		if (r == null) {
			return mList.listIterator(index);
		}
		List<T> copy = getIterableCopy();
		return copy.listIterator(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		if (w != null)
			w.lock();
		try {
			boolean removed = mList.remove(o);
			if (removed) {
				onRemove(mList);
			}
			return removed;
		} finally {
			if (w != null)
				w.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#remove(int)
	 */
	@Override
	public T remove(int index) {
		if (w != null)
			w.lock();
		try {
			T old = mList.remove(index);
			onRemove(mList);
			return old;
		} finally {
			if (w != null)
				w.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		if (w != null)
			w.lock();
		try {
			boolean changed = mList.removeAll(c);
			if (changed) {
				onRemove(mList);
			}
			return changed;
		} finally {
			if (w != null)
				w.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		if (w != null)
			w.lock();
		try {
			boolean changed = mList.retainAll(c);
			if (changed) {
				onRemove(mList);
			}
			return changed;
		} finally {
			if (w != null)
				w.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	@Override
	public T set(int index, T element) {
		T previous = null;
		if (element != null || allowNulls) {
			if (w != null)
				w.lock();
			try {
				if (allowDups || !mList.contains(element)) {
					previous = mList.set(index, element);
					if (previous != element) {
						onAdd(mList);
					}
				}
			} finally {
				if (w != null)
					w.unlock();
			}
		}
		return previous;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#size()
	 */
	@Override
	public int size() {
		if (r != null) r.lock();
		try {
			return mList.size();
		} finally {
			if (r != null) r.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#subList(int, int)
	 */
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		if (r == null) {
			return mList.subList(fromIndex, toIndex);
		}
		List<T> copy = getIterableCopy();
		return copy.subList(fromIndex, toIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#toArray()
	 */
	@Override
	public Object[] toArray() {
		if (r != null) r.lock();
		try {
			return mList.toArray();
		} finally {
			if (r != null) r.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#toArray(T[])
	 */
	@Override
	public <U> U[] toArray(U[] a) {
		if (r != null) r.lock();
		try {
			return mList.toArray(a);
		} finally {
			if (r != null) r.unlock();
		}
	}

	/**
	 * Specifies how nulls are handled.
	 */
	public static enum NullsPolicy {
		NO_NULLS, ALLOW_NULLS,
	}

	/**
	 * Specifies how dups are handled.
	 */
	public static enum DupsPolicy {
		NO_DUPS, ALLOW_DUPS,
	}

	/**
	 * Specifies how locking is handled.
	 */
	public static enum ThreadsPolicy {
		SINGLE_THREAD, MULTIPLE_THREADS
	}
}
