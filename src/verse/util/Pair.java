/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: Daniel Hardman
 * Created: Sep 8, 2009
 */
package verse.util;

/**
 * A class to hold a 2-tuple.
 *
 * @param <A> an object of type A
 * @param <B> an object of type B
 */
public class Pair<A, B> {
 
    public final A first;
    public final B second;
 
    public Pair(final A first, final B second) {
        this.first = first;
        this.second = second;
    }
    
    public static <X, Y> Pair<X, Y> create(X first, Y second) {
        return new Pair<X, Y>(first, second);
    }
 
    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Pair<?, ?>))
            return false;
 
        final Pair<?, ?> other = (Pair<?, ?>) o;
        return equal(first, other.first) && equal(second, other.second);
    }
 
    private static final boolean equal(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }
        return o1.equals(o2);
    }

    @Override
    public int hashCode() {
        int hFirst = first == null ? 0 : first.hashCode();
        int hSecond = second == null ? 0 : second.hashCode();
 
        return hFirst ^ (57 * hSecond);
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append('<');
    	if (first != null)
    		sb.append(first.toString());
    	sb.append(',');
    	if (second != null)
    		sb.append(second.toString());
    	sb.append('>');
    	return sb.toString();
    }
}
