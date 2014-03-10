/*
 * PureTreeSet.java
 *
 * Copyright (c) 2013 Scott L. Burson.
 *
 * This file is licensed under the Library GNU Public License (LGPL).
 */


package com.ergy.fset;
import java.util.*;

/**
 * A pure set that relies on a comparison method.  It is implemented as a binary
 * tree.  As with {@link SortedSet}, the elements must either implement {@link
 * Comparable}, or must be acceptable to a {@link Comparator} which is
 * supplied to the <code>PureTreeSet</code> constructor.
 *
 * <p>Time costs: <code>isEmpty</code>, <code>size</code>, and <code>arb</code> take
 * O(1) (constant) time.  <code>contains</code>, <code>with</code>,
 * <code>less</code>, <code>first</code>, <code>last</code>, <code>subSet</code>,
 * <code>headSet</code>, and <code>tailSet</code> take O(log <i>n</i>) time.
 * <code>union</code>, <code>intersection</code>, <code>difference</code>,
 * <code>isSubset</code>, <code>isSuperset</code>, and <code>equals</code> take O(n)
 * (linear) time if the other set involved is also a <code>PureTreeSet</code> and
 * uses the same ordering; otherwise, they take O(<i>n</i> log <i>n</i>) time.
 * <code>compareTo</code> (called, for instance, if this set is an element in a
 * containing <code>PureTreeSet</code>) takes O(n) (linear) time.
 *
 * <p>Space costs: <code>PureTreeSet</code> uses a heterogeneous binary tree
 * structure with bounded-length arrays at the leaves.  It uses much less space than
 * traditional homogeneous binary trees; typical space consumption is roughly twice
 * that of a plain array, or even less.
 *
 * <p><code>PureTreeSet</code> is declared as implementing <code>SortedSet</code>.
 * However, there are some subtle semantic differences which the user should be
 * aware of; <code>PureTreeSet</code> does not perfectly fulfill the contract of
 * <code>SortedSet</code>.
 *
 * <p>First, unlike <code>SortedSet</code>, <code>PureTreeSet</code> does not
 * require the comparison to be consistent with <code>equals</code> in order for it
 * to fulfill the contract of the <code>Set</code> interface.  In cases where the
 * set contains <i>equivalent</i> elements (elements for which <code>equals</code>
 * returns <code>false</code> but for which the comparison method returns 0), there
 * is obviously no guarantee as to the order in which the iterator will return those
 * elements.  Otherwise, the iterator returns the elements in order according to the
 * comparison.  Although the ordering does not need to be consistent with
 * <code>equals</code>, it does need to be self-consistent: any two elements for
 * which it returns 0 must compare the same way against all other elements.
 * (Mathematically, it must be transitive, and the quotient must be an equivalence
 * relation.  See {@link Comparable}.)  Also, the comparison must not be
 * <i>finer</i> than <code>equals</code>: for any two elements on which
 * <code>equals</code> returns <code>true</code>, the comparison must return 0.
 *
 * <p>The second difference concerns exception behavior.  {@link SortedSet#subSet},
 * for instance, is documented to throw <code>IllegalArgumentException</code> if its
 * bounds arguments are in the wrong order, or if this set is the result of a
 * previous <code>subSet</code> operation and the new bounds are not within the
 * interval supplied to that previous operation.  We feel that operations of a
 * general interface like <code>SortedSet</code> should throw exceptions only in
 * truly erroneous cases, not simply as a debugging aid that may be useful to an
 * occasional client at the expense of cluttering all the implementations of the
 * interface.  So with this class, <code>subSet</code> and the related methods
 * <code>headSet</code> and <code>tailSet</code> do not throw
 * <code>IllegalArgumentException</code>.
 *
 * <p><code>PureTreeSet</code> accepts the null element; for ordering purposes,
 * it precedes all other elements.
 *
 * <p>Although <code>PureTreeSet</code> functions correctly in the presence of
 * equivalent but unequal elements, it does so with some loss of efficiency.  As
 * long as these are rare, however, the cost will not be significant.
 *
 * <p><code>PureTreeSet</code> implements {@link java.io.Serializable}; an instance
 * of it is serializable provided that all elements it contains, and the
 * <code>Comparator</code> it uses if any, are serializable.
 *
 * @author Scott L. Burson
 * @see PureSet
 * @see PureHashSet
 * @see Comparable
 * @see Comparator
 */

public final class PureTreeSet<Elt>
    extends AbstractPureSet<Elt>
    implements SortedSet<Elt>, Comparable<PureTreeSet<Elt>>, java.io.Serializable
{

    /**
     * Constructs an empty <code>PureTreeSet</code> that uses the natural ordering
     * of the elements.
     */
    public PureTreeSet() {
	tree = null;
	comp = null;
    }

    /**
     * Constructs an empty <code>PureTreeSet</code> that uses the supplied
     * <code>Comparator</code> to compare elements.
     *
     * @param c the comparator
     */
    public PureTreeSet(Comparator<? super Elt> c) {
	tree = null;
	comp = (Comparator<Elt>)c;	// we know Comparator is pure
    }

    /**
     * Constructs a <code>PureTreeSet</code> containing the same elements as
     * <code>coll</code>, and that uses the natural ordering of the elements.
     *
     * @param coll the collection to use the elements of
     */
    public PureTreeSet(Collection<? extends Elt> coll) {
	initialize(coll, null);
    }

    /**
     * Constructs a <code>PureTreeSet</code> containing the same elements as
     * <code>coll</code>, and that uses the supplied <code>Comparator</code> to
     * compare elements.
     *
     * @param coll the collection to use the elements of
     * @param c the comparator
     */
    public PureTreeSet(Collection<? extends Elt> coll, Comparator<? super Elt> c) {
	initialize(coll, c);
    }

    /**
     * Constructs a <code>PureTreeSet</code> containing the same elements as
     * <code>set</code>, and which uses the same ordering (natural ordering, or the
     * same <code>Comparator</code>) as <code>Set</code>.
     *
     * @param set the set to use the elements and ordering of
     */
    public PureTreeSet(SortedSet<Elt> set) {
	initialize(set, set.comparator());
    }

    private void initialize(Collection<? extends Elt> coll, Comparator<? super Elt> c) {
	comp = (Comparator<Elt>)c;
	if (coll instanceof PureTreeSet &&
	    (comp == null ? ((PureTreeSet)coll).comp == null
			  : comp.equals(((PureTreeSet)coll).comp)))
	    tree = ((PureTreeSet)coll).tree;
	else if (coll instanceof RandomAccess)		// entails `List'
	    tree = fromRandomAccessCollection((List<Object>)coll, 0, coll.size());
	else tree = new PureTreeList<Elt>(coll).toPureTreeSet(comp).tree;
    }

    private Object fromRandomAccessCollection(List<Object> list, int lo, int hi) {
	if (lo == hi) return null;
	else if (lo + 1 == hi) {
	    // It would be better to pick up several elements and sort them, but
	    // it's nontrivial because of possible equivalences.
	    Object[] a = new Object[1];
	    a[0] = list.get(lo);
	    return a;
	} else {
	    int mid = (lo + hi) >> 1;
	    return union(fromRandomAccessCollection(list, lo, mid),
			 fromRandomAccessCollection(list, mid, hi));
	}
    }

    /**
     * Constructs a <code>PureTreeSet</code> whose elements are the components of
     * <code>ary</code>, and which uses natural ordering to compare elements.  That
     * is, the elements are <code>ary[0]</code>, <code>ary[1]</code>, etc.  (So, if
     * <code>ary</code> is multidimensional, the elements of the set will be the
     * inner arrays, not the objects which are ultimately found after multiple
     * levels of indexing.)
     *
     * @param ary the array to use the components of
     */
    public <T extends Elt> PureTreeSet(T[] ary) {
	comp = null;
	tree = fromArray(ary, 0, ary.length);
    }

    private <T extends Elt> Object fromArray(T[] ary, int lo, int hi) {
	if (lo == hi) return null;
	else if (lo + 1 == hi) {
	    Object[] a = new Object[1];
	    a[0] = ary[lo];
	    return a;
	} else {
	    int mid = (lo + hi) >> 1;
	    return union(fromArray(ary, lo, mid),
			 fromArray(ary, mid, hi));
	}
    }

    /**
     * Constructs a <code>PureTreeSet</code> whose elements are the components of
     * <code>ary</code>, and which uses the supplied <code>Comparator</code> to
     * compare elements.  That is, the elements are <code>ary[0]</code>,
     * <code>ary[1]</code>, etc.  (So, if <code>ary</code> is multidimensional, the
     * elements of the set will be the inner arrays, not the objects which are
     * ultimately found after multiple levels of indexing.)
     *
     * @param ary the array to use the components of
     * @param c the comparator
     */
    public <T extends Elt> PureTreeSet(T[] ary, Comparator<? super Elt> c) {
	comp = (Comparator<Elt>)c;
	tree = new PureTreeList<Elt>(ary).toPureTreeSet(c).tree;
    }

    public boolean isEmpty() {
	return tree == null;
    }

    public int size() {
	return treeSize(tree);
    }

    public Elt arb() {
	if (tree == null) return null;
	else if (!(tree instanceof Node)) {
	    Object[] ary = (Object[])tree;
	    int len = ary.length;
	    return (Elt)ary[len >> 1];
	} else {
	    Node node = (Node)tree;
	    if (node.element instanceof EquivalentSet)
		return (Elt)((EquivalentSet)node.element).contents.get(0);
	    else return (Elt)node.element;
	}
    }

    public boolean contains(Object elt) {
	return contains(tree, elt);
    }

    public Iterator<Elt> iterator() {
	return new PTSIterator<Elt>(tree);
    }

    public PureTreeSet<Elt> with(Elt elt) {
	Object t = with(tree, elt);
	if (t == tree) return this;
	else return new PureTreeSet(t, comp);
    }

    public PureTreeSet<Elt> less(Elt elt) {
	Object t = less(tree, elt);
	if (t == tree) return this;
	else return new PureTreeSet(t, comp);
    }

    /**
     * Returns the union of this set with <code>coll</code>.  That is, returns a set
     * containing all elements that are in either this set, or <code>coll</code>, or
     * both.  The returned set is of the same class as this set and uses the same
     * ordering.
     *
     * <p>This operation runs in O(n) (linear) time if <code>coll</code> is also a
     * <code>PureTreeSet</code> and uses the same ordering as this set; otherwise it
     * runs in O(n log n) time.
     *
     * @param coll the set to take the union with
     * @return the union of the two sets
     * @throws ClassCastException if <code>coll</code> contains elements whose class is
     * incompatible with this set or its <code>Comparator</code>
     */
    public PureTreeSet<Elt> union(Collection<? extends Elt> coll) {
	if (coll == this) return this;
	else if (coll instanceof PureTreeSet &&
	    comp == null ? ((PureTreeSet)coll).comp == null
			 : comp.equals(((PureTreeSet)coll).comp)) {
	    PureTreeSet<Elt> pts = (PureTreeSet<Elt>)coll;
	    Object t = union(tree, pts.tree);
	    return new PureTreeSet<Elt>(t, comp);
	} else {
	    // Either not a `PureTreeSet', or has a different ordering.
	    PureTreeSet<Elt> pts = new PureTreeSet<Elt>(coll, comp);
	    Object t = union(tree, pts.tree);
	    return new PureTreeSet<Elt>(t, comp);
	}
    }

    /**
     * Returns the intersection of this set with <code>coll</code>.  That is, returns a
     * set containing all elements that are in both this set and <code>coll</code>.
     * The returned set is of the same class as this set and uses the same ordering.
     *
     * <p>This operation runs in O(n) (linear) time if <code>coll</code> is also a
     * <code>PureTreeSet</code> and uses the same ordering as this set; otherwise it
     * runs in O(n log n) time.
     *
     * @param coll the set to take the intersection with
     * @return the intersection of the two sets
     * @throws ClassCastException if elements of this set are incompatible with
     * <code>coll</code> or its comparator, or conversely
     */
    public PureTreeSet<Elt> intersection(Collection<? extends Elt> coll) {
	if (coll == this) return this;
	else if (coll instanceof PureTreeSet &&
	    comp == null ? ((PureTreeSet)coll).comp == null
			 : comp.equals(((PureTreeSet)coll).comp)) {
	    PureTreeSet<Elt> pts = (PureTreeSet<Elt>)coll;
	    Object t = intersection(tree, pts.tree);
	    return new PureTreeSet<Elt>(t, comp);
	} else {
	    // Either not a `PureTreeSet', or has a different ordering.
	    PureTreeSet<Elt> pts = new PureTreeSet<Elt>(coll, comp);
	    Object t = intersection(tree, pts.tree);
	    return new PureTreeSet<Elt>(t, comp);
	}
    }

    /**
     * Returns the difference of this set less <code>coll</code>.  That is, returns a
     * set containing all elements that are in this set and not in <code>coll</code>.
     * The returned set is of the same class as this set and uses the same ordering.
     *
     * <p>This operation runs in O(n) (linear) time if <code>coll</code> is also a
     * <code>PureTreeSet</code> and uses the same ordering as this set; otherwise it
     * runs in O(n log n) time.
     *
     * @param coll the set to take the difference with
     * @return the difference of the two sets (this set less <code>coll</code>)
     * @throws ClassCastException if elements of this set are incompatible with
     * <code>coll</code> or its comparator, or conversely
     */
    public PureTreeSet<Elt> difference(Collection<? extends Elt> coll) {
	if (coll == this) return new PureTreeSet<Elt>(comp);
	else if (coll instanceof PureTreeSet &&
	    comp == null ? ((PureTreeSet)coll).comp == null
			 : comp.equals(((PureTreeSet)coll).comp)) {
	    PureTreeSet<Elt> pts = (PureTreeSet<Elt>)coll;
	    Object t = difference(tree, pts.tree);
	    return new PureTreeSet<Elt>(t, comp);
	} else {
	    // Either not a `PureTreeSet', or has a different ordering.
	    PureTreeSet<Elt> pts = new PureTreeSet<Elt>(coll, comp);
	    Object t = difference(tree, pts.tree);
	    return new PureTreeSet<Elt>(t, comp);
	}
    }

    /**
     * See the documentation for {@link Comparable#compareTo}.
     *
     * <p>This method throws <code>ClassCastException</code> even if
     * <code>obj</code> is another <code>PureTreeSet</code>, if the two sets do not
     * use the same ordering for their elements (either natural ordering, or the
     * same <code>Comparator</code>).
     */
    public int compareTo(PureTreeSet<Elt> obj) {
	if (obj == this) return 0;
	else if (obj == null ||
		 !(obj instanceof PureTreeSet) ||
		 comp == null ? ((PureTreeSet)obj).comp != null
			      : !comp.equals(((PureTreeSet)obj).comp))
	    throw new ClassCastException();
	else return compareTo(tree, ((PureTreeSet)obj).tree);
    }

    /* We don't bother caching equality results on sets smaller than this. */
    private static final int EQUALS_CACHE_THRESHOLD = 8;

    public boolean equals(Object obj) {
	if (obj == this) return true;
	else if (obj instanceof PureTreeSet &&
		 (comp == null ? ((PureTreeSet)obj).comp == null
			       : comp.equals(((PureTreeSet)obj).comp))) {
	    PureTreeSet<Object> pts = (PureTreeSet<Object>)obj;
	    return equals(tree, pts.tree);
	} else if (!(obj instanceof Collection)) return false;
	else {
	    // Either not a PureTreeSet, or has a different ordering.
	    Collection<Object> coll = (Collection<Object>)obj;
	    if (size() != coll.size()) return false;
	    for (Object e : coll)
		if (!contains(tree, e)) return false;
	    return true;
	}
    }

    /**
     * Returns true if this set is a subset of <code>coll</code>.  That is, returns
     * true if <code>coll</code> contains all elements of this set.  The inclusion need
     * not be proper; that is, this method returns true if the two sets are equal.
     *
     * <p>This operation runs in O(n) (linear) time if <code>coll</code> is also a
     * <code>PureTreeSet</code> and uses the same ordering as this set; otherwise it
     * runs in O(n log n) time.
     *
     * @param coll the collection to compare against
     * @return whether this set is a subset of <code>coll</code>
     * @throws ClassCastException if elements of this set are incompatible with
     * <code>coll</code> or its comparator, or conversely
     */
    public boolean isSubset(Collection<?> coll) {
	if (coll == this) return true;
	else if (size() > coll.size()) return false;
	else if (coll instanceof PureTreeSet &&
		 (comp == null ? ((PureTreeSet)coll).comp == null
			       : comp.equals(((PureTreeSet)coll).comp))) {
	    PureTreeSet<Object> pts = (PureTreeSet)coll;
	    return isSubset(tree, pts.tree);
	} else {
	    // Either not a PureTreeSet, or has a different ordering.
	    for (Elt e : this)
		if (!coll.contains(e)) return false;
	    return true;
	}
    }

    /**
     * Returns true if this set is a superset of <code>coll</code>.  That is, returns
     * true if this set contains all elements of <code>coll</code>.  The inclusion need
     * not be proper; that is, this method returns true if the two sets are equal.
     * (Synonym for <code>containsAll</code>.)
     *
     * <p>This operation runs in O(n) (linear) time if <code>coll</code> is also a
     * <code>PureTreeSet</code> and uses the same ordering as this set; otherwise it
     * runs in O(n log n) time.
     *
     * @param coll the collection to compare against
     * @return whether this set is a superset of <code>coll</code>
     * @throws ClassCastException if elements of this set are incompatible with
     * <code>coll</code> or its comparator, or conversely
     */
    public boolean isSuperset(Collection<?> coll) {
	if (coll == this) return true;
	else if (size() < coll.size()) return false;
	else if (coll instanceof PureTreeSet &&
		 (comp == null ? ((PureTreeSet)coll).comp == null
			       : comp.equals(((PureTreeSet)coll).comp))) {
	    PureTreeSet<Object> pts = (PureTreeSet)coll;
	    return isSubset(pts.tree, tree);
	} else {
	    // Either not a PureTreeSet, or has a different ordering.
	    for (Object e : coll)
		if (!contains(tree, e)) return false;
	    return true;
	}
    }

    // Overriding this just to provide a slightly more efficient implementation.
    // The default one (in `AbstractSet') uses the iterator.  But we have to compute
    // the same value here, viz., the sum of the hash codes of the elements.
    public int hashCode() {
	if (hash_code == Integer.MIN_VALUE) hash_code = hashCode(tree);
	return hash_code;
    }

    // For debugging.
    String dump() {
	return dump(tree);
    }

    boolean verify() {
	return verify(tree, NEGATIVE_INFINITY, POSITIVE_INFINITY);
    }

    /****************/
    /* Operations of `SortedSet' */

    public Comparator<Elt> comparator() { return comp; }

    public Elt first() {
	if (tree == null) throw new NoSuchElementException();
	else return (Elt)first(tree);
    }

    public Elt last() {
	if (tree == null) throw new NoSuchElementException();
	else return (Elt)last(tree);
    }

    /**
     * Returns a set containing the elements of this set greater than or equal (or
     * equivalent) to <code>fromElement</code> and less than <code>toElement</code>.
     * Contrary to the documentation for {@link SortedSet#subSet}, this
     * never throws <code>IllegalArgumentException</code>, no matter what values are
     * supplied for <code>fromElement</code> and <code>toElement</code>.
     *
     * @param fromElement inclusive lower bound
     * @param toElement exclusive upper bound
     * @return the new set
     * @throws ClassCastException if <code>fromElement</code> or
     *     <code>toElement</code> cannot be compared to each other or to the
     *     elements in the set
     */
    public SortedSet<Elt> subSet(Elt fromElement, Elt toElement) {
	if (tree == null) return this;
	else {
	    Elt fe = first(), le = last();
	    if (compare(fromElement, toElement) >= 0 ||
		compare(fe, toElement) >= 0 ||
		compare(le, fromElement) < 0) return new PureTreeSet<Elt>(comp);
	    else if (compare(fe, fromElement) >= 0  &&
		     compare(le, toElement) < 0) return this;
	    else {
	       Object t = split(tree, fromElement, toElement);
	       // `split' excludes both endpoints.
	       Object eq = findEquiv(tree, fromElement);
	       if (eq != NO_ELEMENT) t = with(t, eq);
	       return new PureTreeSet<Elt>(t, comp);
	    }
	}
    }

    /**
     * Returns a set containing the elements of this set less than
     * <code>toElement</code>.  Contrary to the documentation for {@link
     * SortedSet#headSet}, this never throws <code>IllegalArgumentException</code>,
     * no matter what value is supplied for <code>toElement</code>.
     *
     * @param toElement exclusive upper bound
     * @return the new set
     * @throws ClassCastException if <code>toElement</code> cannot be compared to
     *     the elements in the set
     */
    public SortedSet<Elt> headSet(Elt toElement) {
	if (tree == null || compare(last(), toElement) < 0) return this;
	else if (compare(first(), toElement) >= 0) return new PureTreeSet<Elt>(comp);
	else return new PureTreeSet<Elt>(split(tree, NEGATIVE_INFINITY, toElement), comp);
    }

    /**
     * Returns a set containing the elements of this set greater than or equal (or
     * equivalent) to <code>fromElement</code>.  Contrary to the documentation for
     * {@link SortedSet#tailSet}, this never throws
     * <code>IllegalArgumentException</code>, no matter what value is supplied for
     * <code>fromElement</code>.
     *
     * @param fromElement inclusive lower bound
     * @return the new set
     * @throws ClassCastException if <code>fromElement</code> cannot be compared
     *     to the elements in the set
     */
    public SortedSet<Elt> tailSet(Elt fromElement) {
	if (tree == null || compare(first(), fromElement) >= 0) return this;
	else if (compare(last(), fromElement) < 0) return new PureTreeSet<Elt>(comp);
	else {
	    Object t = split(tree, fromElement, POSITIVE_INFINITY);
	    Object eq = findEquiv(tree, fromElement);
	    if (eq != NO_ELEMENT) t = with(t, eq);
	    return new PureTreeSet<Elt>(t, comp);
	}
    }

    /******************************************************************************/
    /* Internals */

    // Inspired by Stephen Adams' paper on weight-balanced binary trees.  As an additional
    // development, these trees are heterogeneous: instead of consisting entirely of nodes,
    // the lowest two to three levels of the tree are stored in bounded-length vectors.
    // This cuts space requirements roughly in half without costing much (if any) time.

    /* Instance variables */
    // This has package access for benefit of `PureTreeMap.restrict[Not]'.
    transient Object tree;	// a subtree (see below)
    private Comparator<Elt> comp;
    private transient int hash_code = Integer.MIN_VALUE;	// cache

    // This has default (package-wide) access so `PureTreeMap.domain' can use it.
    // ... Also `PureTreeList.toPureTreeSet'.
    /*package*/ PureTreeSet(Object _tree, Comparator<? super Elt> _comp) {
	tree = _tree;
	comp = (Comparator<Elt>)_comp;
    }

    /* The threshold length above which tree nodes will be built. */
    private static final int MAX_LEAF_ARRAY_LENGTH = 8;

    /* The factor by which one subtree may outweigh another.  See Adams.  Don't
     * change this unless you understand his analysis. */
    private static final int BALANCE_FACTOR = 4;

    /* To represent negative and positive infinity, we create two objects.  All that
     * matters is that they are guaranteed to be distinct from any object that could
     * be in a set.
     */
    static final Object NEGATIVE_INFINITY = new Object();
    static final Object POSITIVE_INFINITY = new Object();

    /* A subtree can be either null, a `Node', or a leaf (an `Object[]').  The
     * elegant, OO-pure implementation would have an abstract class `Subtree' with
     * subclasses `Node' and `Leaf'; the latter would have a single field holding
     * the `Object[]'.  The problem with this is that the space occupied by the
     * `Leaf' instances, including the inevitable per-object overhead, eats up a lot
     * of the space benefit we get by using arrays at the leaves in the first place.
     * So we use `Object' as our subtree type so we don't need `Leaf' objects, and
     * use `instanceof' to tell what kind of subtree we're looking at. */
    // This has package access for benefit of `PureTreeMap.restrict[Not]'.
    static final class Node {
	Node(int _size, Object _element, Object _left, Object _right) {
	    size = _size;
	    element = _element;
	    left = _left;
	    right = _right;
	}
	int size;	// the number of elements in the subtree
	Object element;
	Object left;	// a subtree
	Object right;	// a subtree
    }

    // This has default (package-wide) access so `PureTreeMap.domain' can use it.
    static Node makeNode(Object elt, Object left, Object right) {
	return new Node(treeSize(left) + treeSize(right) + elementSize(elt),
			elt, left, right);
    }

    private static int treeSize(Object subtree) {
	if (subtree == null) return 0;
	else if (!(subtree instanceof Node)) return ((Object[])subtree).length;
	else return ((Node)subtree).size;
    }

    private static int elementSize(Object elt) {
	if (elt instanceof EquivalentSet)
	    return ((EquivalentSet)elt).contents.size();
	else return 1;
    }

    private Object first(Object subtree) {
	if (!(subtree instanceof Node)) return ((Object[])subtree)[0];
	else {
	    Node node = (Node)subtree;
	    if (node.left == null) {
		if (node.element instanceof EquivalentSet)
		    return ((EquivalentSet)node.element).contents.get(0);
		else return node.element;
	    } else return first(node.left);
	}
    }

    private Object last(Object subtree) {
	if (!(subtree instanceof Node)) {
	    Object[] ary = (Object[])subtree;
	    return ary[ary.length - 1];
	} else {
	    Node node = (Node)subtree;
	    if (node.right == null) {
		if (node.element instanceof EquivalentSet) {
		    ArrayList<Object> al = ((EquivalentSet)node.element).contents;
		    return al.get(al.size() - 1);
		} else return node.element;
	    } else return last(node.right);
	}
    }

    private boolean contains(Object subtree, Object elt) {
	if (subtree == null) return false;
	else if (!(subtree instanceof Node)) {
	    Object[] ary = (Object[])subtree;
	    int bin_srch_res = binarySearch(ary, elt);
	    if ((bin_srch_res & BIN_SEARCH_FOUND_MASK) == BIN_SEARCH_FOUND) {
		Object e = ary[bin_srch_res >> BIN_SEARCH_INDEX_SHIFT];
		return elt == null ? e == null : elt.equals(e);
	    } else return false;
	} else {
	    Node node = (Node)subtree;
	    Object nelt = node.element;
	    int comp_res = compare(elt, nelt);
	    if (comp_res == 0) {
		if (nelt instanceof EquivalentSet)
		    return ((EquivalentSet)nelt).contents.contains(elt);
		else return elt == null ? nelt == null : elt.equals(nelt);
	    } else if (comp_res < 0) return contains(node.left, elt);
	    else return contains(node.right, elt);
	}
    }

    // Used by `PureTreeList.toPureHashSet'.
    // `elt' may be an `EquivalentSet'.
    Object with(Object subtree, Object elt) {
	if (subtree == null) {
	    if (!(elt instanceof EquivalentSet)) {
		Object[] a = new Object[1];
		a[0] = elt;
		return a;
	    } else return makeNode(elt, null, null);
	} else if (!(subtree instanceof Node)) {
	    Object[] ary = (Object[])subtree;
	    int bin_srch_res = binarySearch(ary, elt);
	    int found = bin_srch_res & BIN_SEARCH_FOUND_MASK;
	    int idx = bin_srch_res >> BIN_SEARCH_INDEX_SHIFT;
	    if (found == BIN_SEARCH_FOUND && !(elt instanceof EquivalentSet) &&
		(elt == null ? ary[idx] == null : elt.equals(ary[idx])))
		return subtree;
	    else if (found == BIN_SEARCH_NOT_FOUND  &&
		     ary.length < MAX_LEAF_ARRAY_LENGTH  &&
		     !(elt instanceof EquivalentSet))
		return insert(ary, idx, elt);
	    else return makeNode((found == BIN_SEARCH_FOUND
				  ? equivUnion(ary[idx], elt)
				  : elt),
				 subseq(ary, 0, idx),
				 subseq(ary, (found == BIN_SEARCH_FOUND ?
					      idx + 1 : idx),
					ary.length));
	} else {
	    Node node = (Node)subtree;
	    Object nelt = node.element;
	    int comp_res = compare(elt, nelt);
	    if (comp_res == 0) {
		if (!(elt instanceof EquivalentSet) &&
		    !(nelt instanceof EquivalentSet) &&
		    (elt == null ? nelt == null : elt.equals(nelt)))
		    return subtree;
		else return makeNode(equivUnion(elt, nelt), node.left, node.right);
	    } else if (comp_res < 0) {
		Object new_left = with(node.left, elt);
		if (new_left == node.left) return subtree;
		else return buildNode(nelt, new_left, node.right);
	    } else {
		Object new_right = with(node.right, elt);
		if (new_right == node.right) return subtree;
		else return buildNode(nelt, node.left, new_right);
	    }
	}
    }

    private Object less(Object subtree, Object elt) {
	if (subtree == null) return null;
	else if (!(subtree instanceof Node)) {
	    Object[] ary = (Object[])subtree;
	    int bin_srch_res = binarySearch(ary, elt);
	    int found = bin_srch_res & BIN_SEARCH_FOUND_MASK;
	    int idx = bin_srch_res >> BIN_SEARCH_INDEX_SHIFT;
	    if (found == BIN_SEARCH_FOUND) {
		Object e = ary[idx];
		if (elt == null ? e == null : elt.equals(e))
		    return remove(ary, idx);
		else return subtree;
	    } else return subtree;
	} else {
	    Node node = (Node)subtree;
	    Object nelt = node.element;
	    int comp_res = compare(elt, nelt);
	    if (comp_res == 0) {
		if (!(nelt instanceof EquivalentSet)) {
		    if (elt != null && !elt.equals(nelt))
			return subtree;
		    else return join(node.left, node.right);
		} else {
		    Object diff = equivDiff(nelt, elt);
		    return buildNode(diff, node.left, node.right);
		}
	    } else if (comp_res < 0) {
		Object new_left = less(node.left, elt);
		if (new_left == node.left) return subtree;
		else return buildNode(nelt, new_left, node.right);
	    } else {
		Object new_right = less(node.right, elt);
		if (new_right == node.right) return subtree;
		else return buildNode(nelt, node.left, new_right);
	    }
	}
    }

    /* Differs from `first' in that it may return an `EquivalentSet'. */
    private Object min(Object subtree) {
	if (!(subtree instanceof Node)) return ((Object[])subtree)[0];
	else {
	    Node node = (Node)subtree;
	    if (node.left == null) return node.element;
	    else return min(node.left);
	}
    }

    /* Assumes `subtree' is nonempty. */
    private Object lessMin(Object subtree) {
	if (!(subtree instanceof Node)) {
	    Object[] ary = (Object[])subtree;
	    return subseq(ary, 1, ary.length);
	} else {
	    Node node = (Node)subtree;
	    if (node.left == null) return node.right;
	    else return concat(node.element, lessMin(node.left), node.right);
	}
    }

    // This has default access so `PureTreeList.toPureTreeSet' can use it.
    Object union(Object subtree1, Object subtree2) {
	return union(subtree1, subtree2, NEGATIVE_INFINITY, POSITIVE_INFINITY);
    }

    private Object union(Object subtree1, Object subtree2, Object lo, Object hi) {
	if (subtree2 == null) return split(subtree1, lo, hi);
	else if (subtree1 == null) return split(subtree2, lo, hi);
	else if (!(subtree1 instanceof Node)) {
	    if (!(subtree2 instanceof Node))
		return union((Object[])subtree1, (Object[])subtree2, lo, hi);
	    else return union(subtree2, subtree1, lo, hi);
	} else {
	    Node n1 = (Node)subtree1;
	    Object elt1 = n1.element;
	    Object elt2 = findEquiv(subtree2, elt1);
	    return concat(equivUnion(elt1, elt2),
			  union(trim(n1.left, lo, elt1),
				trim(subtree2, lo, elt1),
				lo, elt1),
			  union(trim(n1.right, elt1, hi),
				trim(subtree2, elt1, hi),
				elt1, hi));
	}
    }

    private Object intersection(Object subtree1, Object subtree2) {
	return intersection(subtree1, subtree2, NEGATIVE_INFINITY, POSITIVE_INFINITY);
    }

    private Object intersection(Object subtree1, Object subtree2, Object lo, Object hi) {
	if (subtree1 == null || subtree2 == null) return null;
	else if (!(subtree1 instanceof Node)) {
	    if (!(subtree2 instanceof Node))
		return intersection((Object[])subtree1, (Object[])subtree2, lo, hi);
	    else return intersection(trim(subtree2, lo, hi), subtree1, lo, hi);
	} else {
	    Node n1 = (Node)subtree1;
	    Object elt1 = n1.element;
	    Object elt2 = findEquiv(subtree2, elt1);
	    Object new_left = intersection(n1.left, trim(subtree2, lo, elt1), lo, elt1);
	    Object new_right = intersection(n1.right, trim(subtree2, elt1, hi), elt1, hi);
	    Object isect = equivIntersect(elt1, elt2);
	    if (isect == NO_ELEMENT) return join(new_left, new_right);
	    else return concat(isect, new_left, new_right);
	}
    }

    private Object difference(Object subtree1, Object subtree2) {
	return difference(subtree1, subtree2, NEGATIVE_INFINITY, POSITIVE_INFINITY);
    }

    private Object difference(Object subtree1, Object subtree2, Object lo, Object hi) {
	if (subtree1 == null || subtree1 == subtree2) return null;
	else if (subtree2 == null) return split(subtree1, lo, hi);
	else if (!(subtree1 instanceof Node)) {
	    if (!(subtree2 instanceof Node))
		return difference((Object[])subtree1, (Object[])subtree2, lo, hi);
	    else {
		// Can't use the reversing trick here because difference is not
		// commutative.
		Node n2 = (Node)subtree2;
		Object elt2 = n2.element;
		Object elt1 = findEquiv(subtree1, elt2);
		Object new_left = difference(trim(subtree1, lo, elt2),
					     trim(n2.left, lo, elt2),
					     lo, elt2);
		Object new_right = difference(trim(subtree1, elt2, hi),
					      trim(n2.right, elt2, hi),
					      elt2, hi);
		Object diff = equivDiff(elt1, elt2);
		if (diff == NO_ELEMENT) return join(new_left, new_right);
		else return concat(diff, new_left, new_right);
	    }
	} else {
	    Node n1 = (Node)subtree1;
	    Object elt1 = n1.element;
	    Object elt2 = findEquiv(subtree2, elt1);
	    Object new_left = difference(n1.left, trim(subtree2, lo, elt1), lo, elt1);
	    Object new_right = difference(n1.right, trim(subtree2, elt1, hi), elt1, hi);
	    Object diff = equivDiff(elt1, elt2);
	    if (diff == NO_ELEMENT) return join(new_left, new_right);
	    else return concat(diff, new_left, new_right);
	}
    }

    private int compareTo(Object tree1, Object tree2) {
	if (tree1 == tree2) return 0;
	else {
	    int size1 = treeSize(tree1), size2 = treeSize(tree2);
	    // Start by comparing the sizes; smaller sets are considered less than
	    // larger ones.  Only if the sizes are equal do we have to do the
	    // lexicographic comparison.
	    if (size1 < size2) return -1;
	    else if (size1 > size2) return 1;
	    else return compareTo(tree1, 0, tree2, 0, 0, size1);
	}
    }

    private int compareTo(Object subtree1, int base1, Object subtree2, int base2,
			  int lo, int hi) {
	if (lo == hi) return 0;
	else if (!(subtree1 instanceof Node)) {
	    if (!(subtree2 instanceof Node)) {
		Object[] ary1 = (Object[])subtree1, ary2 = (Object[])subtree2;
		for (int i = lo; i < hi; ++i) {
		    int comp_res = compare(ary1[i - base1], ary2[i - base2]);
		    if (comp_res != 0) return comp_res;
		}
		return 0;
	    } else return - compareTo(subtree2, base2, subtree1, base1, lo, hi);
	} else {
	    Node node1 = (Node)subtree1;
	    Object left1 = node1.left;
	    int l1size = treeSize(left1);
	    int new_hi = base1 + l1size;
	    RankTrimResult rtr1 = rankTrim(left1, base1, lo, new_hi);
	    RankTrimResult rtr2 = rankTrim(subtree2, base2, lo, new_hi);
	    int left_comp_res = compareTo(rtr1.subtree, rtr1.base,
					  rtr2.subtree, rtr2.base, lo, new_hi);
	    if (left_comp_res != 0) return left_comp_res;
	    else {
		Object elt1 = node1.element;
		Object elt2 = rankElement(subtree2, new_hi - base2);
		int elt_comp_res = compare(elt1, elt2);
		if (elt_comp_res != 0) return elt_comp_res;
		else {
		    int elt1_size = elementSize(elt1);
		    int elt2_size = elementSize(elt2);
		    if (elt1_size < elt2_size) return 1;
		    else if (elt1_size > elt2_size) return -1;
		    else {
			int new_lo = base1 + l1size + elt1_size;
			RankTrimResult rtr3 = rankTrim(node1.right, new_lo, new_lo, hi);
			RankTrimResult rtr4 = rankTrim(subtree2, base2, new_lo, hi);
			return compareTo(rtr3.subtree, rtr3.base,
					 rtr4.subtree, rtr4.base, new_lo, hi);
		    }
		}
	    }
	}
    }

    private boolean equals(Object tree1, Object tree2) {
	if (tree1 == tree2) return true;
	int size1 = treeSize(tree1), size2 = treeSize(tree2);
	if (size1 != size2) return false;
	else return equals(tree1, 0, tree2, 0, 0, size1);
    }

    private boolean equals(Object subtree1, int base1, Object subtree2, int base2,
			   int lo, int hi) {
	if (lo == hi) return true;
	else if (!(subtree1 instanceof Node)) {
	    if (!(subtree2 instanceof Node)) {
		Object[] ary1 = (Object[])subtree1, ary2 = (Object[])subtree2;
		for (int i = lo; i < hi; ++i) {
		    Object elt1 = ary1[i - base1], elt2 = ary2[i - base2];
		    if (elt1 == null ? elt2 != null : !elt1.equals(elt2)) return false;
		}
		return true;
	    } else return equals(subtree2, base2, subtree1, base1, lo, hi);
	} else {
	    Node node1 = (Node)subtree1;
	    Object left1 = node1.left;
	    int l1size = treeSize(left1);
	    int new_hi = base1 + l1size;
	    RankTrimResult rtr1 = rankTrim(left1, base1, lo, new_hi);
	    RankTrimResult rtr2 = rankTrim(subtree2, base2, lo, new_hi);
	    if (!equals(rtr1.subtree, rtr1.base, rtr2.subtree, rtr2.base, lo, new_hi))
		return false;
	    else {
		Object elt1 = node1.element;
		Object elt2 = rankElement(subtree2, new_hi - base2);
		if (elt1 == null ? elt2 != null : !equivEquals(elt1, elt2)) return false;
		else {
		    int elt1_size = elementSize(elt1);
		    int new_lo = base1 + l1size + elt1_size;
		    RankTrimResult rtr3 = rankTrim(node1.right, new_lo, new_lo, hi);
		    RankTrimResult rtr4 = rankTrim(subtree2, base2, new_lo, hi);
		    return equals(rtr3.subtree, rtr3.base,
				  rtr4.subtree, rtr4.base, new_lo, hi);
		}
	    }
	}
    }

    private static final class RankTrimResult {
	RankTrimResult(Object _subtree, int _base) {
	    subtree = _subtree;
	    base = _base;
	}
	Object subtree;
	int base;
    }

    private RankTrimResult rankTrim(Object subtree, int base, int lo, int hi) {
	while (subtree != null && subtree instanceof Node) {
	    Node node = (Node)subtree;
	    int nrank = base + treeSize(node.left);
	    if (nrank >= lo) {
		if (nrank < hi) break;
		else subtree = node.left;
	    } else {
		int rbase = nrank + elementSize(node.element);
		if (rbase > lo) break;
		else {
		    base = rbase;
		    subtree = node.right;
		}
	    }
	}
	return new RankTrimResult(subtree, base);
    }

    private Object rankElement(Object subtree, int rank) {
	if (subtree == null) throw new NullPointerException();	// shouldn't happen
	else if (!(subtree instanceof Node)) return ((Object[])subtree)[rank];
	else {
	    Node node = (Node)subtree;
	    int left_size = treeSize(node.left);
	    if (rank < left_size) return rankElement(node.left, rank);
	    else {
		Object elt = node.element;
		int elt_size = elementSize(elt);
		if (rank < left_size + elt_size) return elt;
		else return rankElement(node.right, rank - (left_size + elt_size));
	    }
	}
    }

    private boolean isSubset(Object subtree1, Object subtree2) {
	return isSubset(subtree1, subtree2, NEGATIVE_INFINITY, POSITIVE_INFINITY);
    }

    private boolean isSubset(Object subtree1, Object subtree2, Object lo, Object hi) {
	if (subtree1 == null) return true;
	else if (!(subtree1 instanceof Node)) {
	    if (subtree2 == null || !(subtree2 instanceof Node))
		return isSubset((Object[])subtree1, (Object[])subtree2, lo, hi);
	    else {
		Node n2 = (Node)subtree2;
		Object elt2 = n2.element;
		if (!isSubset(trim(subtree1, lo, elt2), n2.left, lo, elt2))
		    return false;
		else {
		    Object elt1 = findEquiv(subtree1, elt2);
		    if (!equivIsSubset(elt1, elt2)) return false;
		    else return isSubset(trim(subtree1, elt2, hi), n2.right, elt2, hi);
		}
	    }
	} else if (subtree2 == null) return false;
	else {
	    Node n1 = (Node)subtree1;
	    Object elt1 = n1.element;
	    if (!isSubset(n1.left, trim(subtree2, lo, elt1), lo, elt1)) return false;
	    else {
		Object elt2 = findEquiv(subtree2, elt1);
		if (!equivIsSubset(elt1, elt2)) return false;
		else return isSubset(n1.right, trim(subtree2, elt1, hi), elt1, hi);
	    }
	}
    }

    /* Again we play a little game to work around Java's inability to efficiently
     * return multiple values.  In this case `findEquiv' needs to return something
     * indicating "no element found", but it can't be `null' because we allow `null'
     * as an element.  So we make a special object. */
    // This has package access for benefit of `PureTreeMap.restrict[Not]'.
    static final Object NO_ELEMENT = new Object();

    // This has package access for benefit of `PureTreeMap.restrict[Not]'.
    Object findEquiv(Object subtree, Object elt) {
	if (subtree == null) return NO_ELEMENT;
	else if (!(subtree instanceof Node)) {
	    int bin_srch_res = binarySearch((Object[])subtree, elt);
	    int found = bin_srch_res & BIN_SEARCH_FOUND_MASK;
	    int idx = bin_srch_res >> BIN_SEARCH_INDEX_SHIFT;
	    if (found == BIN_SEARCH_FOUND)
		return ((Object[])subtree)[idx];
	    else return NO_ELEMENT;
	} else {
	    Node node = (Node)subtree;
	    Object nelt = node.element;
	    int comp_res = compare(elt, nelt);
	    if (comp_res == 0) return nelt;
	    else if (comp_res < 0) return findEquiv(node.left, elt);
	    else return findEquiv(node.right, elt);
	}
    }

    private int compare(Object x, Object y) {
	// `null' is treated as being less than every object.  So it can't,
	// for instance, show up in `EquivalentSet's.
	if (x == null) return (y == null ? 0 : -1);
	else if (y == null) return 1;
	else {
	    if (x instanceof EquivalentSet)
		x = ((EquivalentSet)x).contents.get(0);
	    if (y instanceof EquivalentSet)
		y = ((EquivalentSet)y).contents.get(0);
	    if (comp != null) return comp.compare((Elt)x, (Elt)y);
	    else {
		// Will throw `ClassCastException' if the objects don't implement
		// `Comparable' -- this is correct.
		Comparable cx = (Comparable)x;
		Comparable cy = (Comparable)y;
		return cx.compareTo(cy);
	    }
	}
    }

    // Returns a new tree all of whose elements are greater than `lo' and less
    // than `hi'.  (Contrast `trim'.)
    private Object split(Object subtree, Object lo, Object hi) {
	if (subtree == null) return null;
	else if (!(subtree instanceof Node)) {
	    Object[] ary = (Object[])subtree;
	    int len = ary.length;
	    int lo_split = (lo == NEGATIVE_INFINITY ? 0 : binarySearchLo(ary, lo));
	    int hi_split = (hi == POSITIVE_INFINITY ? len : binarySearchHi(ary, hi));
	    if (lo_split >= hi_split) return null;
	    else if (lo_split == 0 && hi_split == len) return subtree;
	    else return subseq(ary, lo_split, hi_split);
	} else {
	    Node node = (Node)subtree;
	    int lo_comp, hi_comp;
	    if (lo != NEGATIVE_INFINITY &&
		(lo_comp = compare(node.element, lo)) <= 0) {
		if (hi == POSITIVE_INFINITY && lo_comp == 0) return node.right;
		else return split(node.right, lo, hi);
	    } else if (hi != POSITIVE_INFINITY &&
		       (hi_comp = compare(node.element, hi)) >= 0) {
		if (lo == NEGATIVE_INFINITY && hi_comp == 0) return node.left;
		else return split(node.left, lo, hi);
	    } else {
		Object new_left = split(node.left, lo, POSITIVE_INFINITY);
		Object new_right = split(node.right, NEGATIVE_INFINITY, hi);
		if (new_left == node.left && new_right == node.right) return subtree;
		else return concat(node.element, new_left, new_right);
	    }
	}
    }

    // Returns the largest subtree of `subtree' whose root node is greater than `lo'
    // and less than `hi'.  (Contrast `split'.)
    // This has package access for benefit of `PureTreeMap.restrict[Not]'.
    Object trim(Object subtree, Object lo, Object hi) {
	if (subtree == null) return null;
	else if (!(subtree instanceof Node)) {
	    Object[] ary = (Object[])subtree;
	    // If the array is completely out of range, drop it.
	    if ((lo != NEGATIVE_INFINITY && compare(ary[ary.length - 1], lo) <= 0) ||
		(hi != POSITIVE_INFINITY && compare(ary[0], hi) >= 0))
		return null;
	    else return subtree;
	} else {
	    Node node = (Node)subtree;
	    if (lo == NEGATIVE_INFINITY || compare(node.element, lo) > 0) {
		if (hi == POSITIVE_INFINITY || compare(node.element, hi) < 0)
		    return subtree;
		else return trim(node.left, lo, hi);
	    } else return trim(node.right, lo, hi);
	}
    }

    // Assumes that all elements of `left' are less than `elt', and all elements
    // of `right' are greater than `elt'; returns a new tree containing all these
    // elements.  This does more rebalancing than `buildNode', which otherwise
    // has the same contract.  `elt' may be an `EquivalentSet'.
    private Object concat(Object elt, Object left, Object right) {
	if (left == null) return with(right, elt);
	else if (right == null) return with(left, elt);
	else {
	    int sizl = treeSize(left);
	    int sizr = treeSize(right);
	    if (left instanceof Node && sizl > sizr * BALANCE_FACTOR) {
		Node l = (Node)left;
		return buildNode(l.element, l.left, concat(elt, l.right, right));
	    } else if (right instanceof Node && sizr > sizl * BALANCE_FACTOR) {
		Node r = (Node)right;
		return buildNode(r.element, concat(elt, left, r.left), r.right);
	    } else return buildNode(elt, left, right);
	}
    }

    // Returns the union of `left' and `right' under the assumption that all values
    // in `left' are less than any value in `right'.
    private Object join(Object left, Object right) {
	if (left == null) return right;
	else if (right == null) return left;
	else return concat(min(right), left, lessMin(right));
    }

    private Object buildNode(Object elt, Object left, Object right) {
	if ((left == null || !(left instanceof Node)) &&
	    (right == null || !(right instanceof Node))) {
	    Object[] lary = (Object[])left, rary = (Object[])right;
	    if (!(elt instanceof EquivalentSet) &&
		(left == null ? 0 : lary.length) +
		(right == null ? 0 : rary.length) < MAX_LEAF_ARRAY_LENGTH)
		return concat(elt, lary, rary);
	    else return makeNode(elt, left, right);
	} else {
	    int sizl = treeSize(left);
	    int sizr = treeSize(right);
	    // This code is subtly different from Adams' in order to create more
	    // opportunities to coalesce adjacent leaf arrays.
	    if (right instanceof Node && sizr > sizl * BALANCE_FACTOR) {
		Node r = (Node)right;
		Object rl = r.left;
		Object rr = r.right;
		if (!(rl instanceof Node) || treeSize(rl) <= treeSize(rr))
		    return makeNode(r.element, buildNode(elt, left, rl), rr);
		else {
		    Node rln = (Node)rl;
		    return makeNode(rln.element, buildNode(elt, left, rln.left),
				    buildNode(r.element, rln.right, rr));
		}
	    } else if (left instanceof Node && sizl > sizr * BALANCE_FACTOR) {
		Node l = (Node)left;
		Object ll = l.left;
		Object lr = l.right;
		if (!(lr instanceof Node) || treeSize(lr) <= treeSize(ll))
		    return makeNode(l.element, ll, buildNode(elt, lr, right));
		else {
		    Node lrn = (Node)lr;
		    return makeNode(lrn.element, buildNode(l.element, ll, lrn.left),
				    buildNode(elt, lrn.right, right));
		}
	    } else return makeNode(elt, left, right);
	}
    }

    private int hashCode(Object subtree) {
	if (subtree == null) return 0;
	else if (!(subtree instanceof Node)) {
	    Object[] ary = (Object[])subtree;
	    int hash = 0;
	    for (int i = 0, len = ary.length; i < len; ++i) {
		Object elt = ary[i];
		if (elt != null) hash += elt.hashCode();
	    }
	    return hash;
	} else {
	    Node node = (Node)subtree;
	    int hash = hashCode(node.left) + hashCode(node.right);
	    Object elt = node.element;
	    if (elt instanceof EquivalentSet) {
		ArrayList<Object> al = ((EquivalentSet)elt).contents;
		for (int i = 0, siz = al.size(); i < siz; ++i) {
		    Object e = al.get(i);
		    if (e != null) hash += e.hashCode();
		}
	    } else if (elt != null) hash += elt.hashCode();
	    return hash;
	}
    }

    private static String dump(Object thing) {
	if (thing == null) return "[null]";
	else if (thing == NEGATIVE_INFINITY) return "-oo";
	else if (thing == POSITIVE_INFINITY) return "+oo";
	else if (thing == NO_ELEMENT) return "NADA";
	else if (thing instanceof EquivalentSet) {
	    ArrayList<Object> al = ((EquivalentSet)thing).contents;
	    String res = "[";
	    for (int i = 0, size = al.size(); i < size; ++i) {
		if (i > 0) res = res + ", ";
		res = res + al.get(i);
	    }
	    return res + "]";
	} else if (thing instanceof Object[]) {
	    StringBuffer str_buf = new StringBuffer("{");
	    Object[] ary = (Object[])thing;
	    for (int i = 0; i < ary.length; ++i) {
		str_buf.append(dump(ary[i]));
		if (i < ary.length - 1) str_buf.append(", ");
	    }
	    str_buf.append("}");
	    return str_buf.toString();
	} else if (thing instanceof Node) {
	    Node node = (Node)thing;
	    return "(" + node.size + ", " + dump(node.element) + ";\n" +
		indent(dump(node.left), "  ") + ",\n" +
		indent(dump(node.right), "  ") + ")";
	} else return thing.toString();
    }

    private static String indent(String str, String prefix) {
	StringBuffer res = new StringBuffer(prefix);
	for (int i = 0, len = str.length(); i < len; ++i) {
	    char c = str.charAt(i);
	    res.append(c);
	    if (c == '\n' && i < len - 1) res.append(prefix);
	}
	return res.toString();
    }

    private boolean verify(Object subtree, Object lo, Object hi) {
	if (subtree == null) return true;
	else if (!(subtree instanceof Node)) {
	    Object[] ary = (Object[])subtree;
	    if (ary.length > MAX_LEAF_ARRAY_LENGTH) return false;
	    boolean res = true;
	    Object prev = lo;
	    for (int i = 0, len = ary.length; i < len; ++i) {
		Object elt = ary[i];
		if (elt instanceof EquivalentSet) return false;
		if (prev != NEGATIVE_INFINITY && compare(prev, elt) >= 0) res = false;
		prev = elt;
	    }
	    if (hi != POSITIVE_INFINITY && compare(prev, hi) >= 0) res = false;
	    return res;
	} else {
	    Node node = (Node)subtree;
	    int sizl = treeSize(node.left);
	    int sizr = treeSize(node.right);
	    if (node.size != sizl + sizr + elementSize(node.element)) return false;
	    if (node.element instanceof EquivalentSet &&
		((EquivalentSet)node.element).contents.size() < 2) return false;
	    // Small subtrees may be unbalanced, because of our array-splitting heuristic
	    // and because of EquivalentSets.  As long as the size of any subtree that
	    // can be unbalanced is strictly bounded, we're still okay.
	    if ((sizr > 4 && sizl > sizr * BALANCE_FACTOR) ||
		(sizl > 4 && sizr > sizl * BALANCE_FACTOR))
		return false;
	    return verify(node.left, lo, node.element) &&
		   verify(node.right, node.element, hi);
	}
    }

    /****************/
    /* Equivalent sets */

    /* In the case where the set contains two or more elements which are distinct
     * but are equivalent according to the comparison method, the `element' field of
     * a `Node' will contain one of these.  We always build a `Node' in this case;
     * we never put equivalent elements in an array.  Here we don't worry about
     * the extra level(s) of structure because this is supposed to be a rare case.
     */
    static final class EquivalentSet {
	/*package*/ EquivalentSet(ArrayList<Object> _contents) {
	    contents = _contents;
	}
	/*package*/ ArrayList<Object> contents;
    }

    private static Object equivUnion(Object elt1, Object elt2) {
	if (elt1 == NO_ELEMENT) return elt2;
	else if (elt2 == NO_ELEMENT) return elt1;
	else if (elt1 instanceof EquivalentSet) {
	    ArrayList<Object> al1 = ((EquivalentSet)elt1).contents;
	    if (elt2 instanceof EquivalentSet) {
		ArrayList<Object> al2 = ((EquivalentSet)elt2).contents;
		int size2 = al2.size();
		ArrayList res_al = (ArrayList)al1.clone();
		for (int i = 0; i < size2; ++i) {
		    Object e2 = al2.get(i);
		    if (!res_al.contains(e2)) res_al.add(e2);
		}
		res_al.trimToSize();
		return new EquivalentSet(res_al);
	    } else {
		if (al1.contains(elt2)) return elt1;
		else {
		    // Too bad there's no `clone' method that accepts a capacity.
		    ArrayList res_al = (ArrayList)al1.clone();
		    res_al.add(elt2);
		    res_al.trimToSize();
		    return new EquivalentSet(res_al);
		}
	    }
	} else if (elt2 instanceof EquivalentSet) {
	    ArrayList<Object> al2 = ((EquivalentSet)elt2).contents;
	    if (al2.contains(elt1)) return elt2;
	    else {
		ArrayList al = (ArrayList)al2.clone();
		al.add(elt1);
		al.trimToSize();
		return new EquivalentSet(al);
	    }
	} else if (elt1 == null ? elt2 == null : elt1.equals(elt2)) return elt1;
	else {
	    ArrayList al = new ArrayList(2);
	    al.add(elt1);
	    al.add(elt2);
	    return new EquivalentSet(al);
	}
    }

    private static Object equivIntersect(Object elt1, Object elt2) {
	// (not sure we use this first case)
	if (elt1 == NO_ELEMENT || elt2 == NO_ELEMENT) return NO_ELEMENT;
	else if (elt1 instanceof EquivalentSet) {
	    ArrayList<Object> al1 = ((EquivalentSet)elt1).contents;
	    if (elt2 instanceof EquivalentSet) {
		ArrayList<Object> al2 = ((EquivalentSet)elt2).contents;
		ArrayList<Object> al = new ArrayList<Object>();
		int size1 = al1.size();
		for (int i = 0; i < size1; ++i) {
		    Object e1 = al1.get(i);
		    if (al2.contains(e1)) al.add(e1);
		}
		if (al.size() == 0) return NO_ELEMENT;
		else if (al.size() == 1) return al.get(0);
		else {
		    al.trimToSize();
		    return new EquivalentSet(al);
		}
	    } else {
		if (al1.contains(elt2)) return elt2;
		else return NO_ELEMENT;
	    }
	} else if (elt2 instanceof EquivalentSet) {
	    ArrayList<Object> al2 = ((EquivalentSet)elt2).contents;
	    if (al2.contains(elt1)) return elt1;
	    else return NO_ELEMENT;
	} else if (elt1 == null ? elt2 == null : elt1.equals(elt2)) return elt1;
	else return NO_ELEMENT;
    }

    private static Object equivDiff(Object elt1, Object elt2) {
	if (elt1 == NO_ELEMENT) return NO_ELEMENT;
	else if (elt2 == NO_ELEMENT) return elt1;
	else if (elt1 instanceof EquivalentSet) {
	    ArrayList<Object> al1 = ((EquivalentSet)elt1).contents;
	    ArrayList<Object> al2 = null;
	    if (elt2 instanceof EquivalentSet)
		al2 = ((EquivalentSet)elt2).contents;
	    ArrayList al = new ArrayList();
	    int size1 = al1.size();
	    for (int i = 0; i < size1; ++i) {
		Object e1 = al1.get(i);
		if (al2 == null ?
		    (e1 == null ? elt2 != null : !e1.equals(elt2)) :
		    (!al2.contains(e1)))
		    al.add(e1);
	    }
	    if (al.size() == 0) return NO_ELEMENT;
	    else if (al.size() == 1) return al.get(0);
	    else {
		al.trimToSize();
		return new EquivalentSet(al);
	    }
	} else if (elt2 instanceof EquivalentSet) {
	    if (!((EquivalentSet)elt2).contents.contains(elt1))
		return elt1;
	    else return NO_ELEMENT;
	} else if (elt1 == null ? elt2 != null : !elt1.equals(elt2))
	    return elt1;
	else return NO_ELEMENT;
    }

    private static boolean equivEquals(Object elt1, Object elt2) {
	if (elt1 == elt2) return true;
	else if (elt1 == null || elt2 == null) return false;
	else if (elt1 instanceof EquivalentSet) {
	    if (elt2 instanceof EquivalentSet) {
		ArrayList<Object> al1 = ((EquivalentSet)elt1).contents;
		ArrayList<Object> al2 = ((EquivalentSet)elt2).contents;
		int size1 = al1.size();
		if (size1 != al2.size()) return false;
		else {
		    for (int i = 0; i < size1; ++i)
			if (!al2.contains(al1.get(i))) return false;
		    return true;
		}
	    } else return false;
	} else if (elt2 instanceof EquivalentSet) return false;
	else return elt1.equals(elt2);
    }

    private static boolean equivIsSubset(Object elt1, Object elt2) {
	if (elt1 == elt2) return true;
	else if (elt1 == NO_ELEMENT) return true;
	else if (elt2 == NO_ELEMENT) return false;
	else if (elt1 instanceof EquivalentSet) {
	    ArrayList<Object> al1 = ((EquivalentSet)elt1).contents;
	    if (elt2 instanceof EquivalentSet) {
		ArrayList<Object> al2 = ((EquivalentSet)elt2).contents;
		return al2.containsAll(al1);
	    } else return false;
	} else if (elt2 instanceof EquivalentSet) {
	    ArrayList<Object> al2 = ((EquivalentSet)elt2).contents;
	    return al2.contains(elt1);
	} else return elt1.equals(elt2);
    }

    /****************/
    /* Internal array manipulation routines.  These all assume their index
     * parameters are within bounds.  Of course, despite what the names of some
     * might suggest, they all make new arrays. */

    private static Object[] concat(Object elt, Object[] left, Object[] right) {
	int llen = (left == null ? 0 : left.length);
	int rlen = (right == null ? 0 : right.length);
	int len = llen + 1 + rlen;
	Object[] a = new Object[len];
	for (int i = 0; i < llen; ++i) a[i] = left[i];
	a[llen] = elt;
	for (int i = 0; i < rlen; ++i) a[i + llen + 1] = right[i];
	return a;
    }

    private static Object[] insert(Object[] ary, int idx, Object elt) {
	int len = ary.length + 1;
	Object[] a = new Object[len];
	for (int i = 0; i < idx; ++i) a[i] = ary[i];
	a[idx] = elt;
	for (int i = idx + 1; i < len; ++i) a[i] = ary[i - 1];
	return a;
    }

    private static Object[] remove(Object[] ary, int idx) {
	int len = ary.length - 1;
	if (len == 0) return null;
	else {
	    Object[] a = new Object[len];
	    for (int i = 0; i < idx; ++i) a[i] = ary[i];
	    for (int i = idx; i < len; ++i) a[i] = ary[i + 1];
	    return a;
	}
    }

    private static Object[] subseq(Object[] ary, int lo, int hi) {
	if (lo >= hi) return null;
	else {
	    int len = hi - lo;
	    Object[] a = new Object[len];
	    for (int i = 0; i < len; ++i)
		a[i] = ary[i + lo];
	    return a;
	}
    }

    // Does a merge-union on `ary1' and `ary2', omitting any elements not greater than
    // `lo' and less than `hi'.  If the result is too long to be a leaf, splits it and
    // makes a node.  Also, if any equivalent values are found, makes a node.
    private Object union(Object[] ary1, Object[] ary2, Object lo, Object hi) {
	int i1 = 0, i2 = 0, len1 = ary1.length, len2 = ary2.length;
	// We do these with linear rather than binary search because frequently,
	// the ends of the vectors will already be in range (the worst case for
	// binary search).
	if (lo != NEGATIVE_INFINITY) {
	    while (i1 < len1 && compare(lo, ary1[i1]) >= 0) ++i1;
	    while (i2 < len2 && compare(lo, ary2[i2]) >= 0) ++i2;
	}
	if (hi != POSITIVE_INFINITY) {
	    while (i1 < len1 && compare(hi, ary1[len1 - 1]) <= 0) --len1;
	    while (i2 < len2 && compare(hi, ary2[len2 - 1]) <= 0) --len2;
	}
	ArrayList<Object> res = new ArrayList<Object>();
	boolean any_equiv = false;
	while (true) {
	    if (i1 == len1) {
		while (i2 < len2) res.add(ary2[i2++]);
		break;
	    } else if (i2 == len2) {
		while (i1 < len1) res.add(ary1[i1++]);
		break;
	    } else {
		Object e1 = ary1[i1];
		Object e2 = ary2[i2];
		int comp_res = compare(e1, e2);
		if (comp_res < 0) {
		    res.add(e1);
		    ++i1;
		} else if (comp_res > 0) {
		    res.add(e2);
		    ++i2;
		} else {
		    if (e1 == null ? e2 == null : e1.equals(e2)) res.add(e1);
		    else {
			res.add(equivUnion(e1, e2));
			any_equiv = true;
		    }
		    ++i1;
		    ++i2;
		}
	    }
	}
	if (any_equiv) {
	    Object t = null;
	    // We could attempt a better algorithm, but this shouldn't happen often.
	    for (Object e : res)
		t = with(t, e);
	    return t;
	}
	int siz = res.size();
	Object[] res_ary = res.toArray();
	if (siz > MAX_LEAF_ARRAY_LENGTH) {
	    int idx = siz / 2;
	    return makeNode(res.get(idx), subseq(res_ary, 0, idx),
			    subseq(res_ary, idx + 1, siz));
	} else return res_ary;
    }

    private Object[] intersection(Object[] ary1, Object[] ary2, Object lo, Object hi) {
	int i1 = 0, i2 = 0, len1 = ary1.length, len2 = ary2.length;
	if (lo != NEGATIVE_INFINITY)
	    while (i1 < len1 && compare(lo, ary1[i1]) >= 0) ++i1;
	if (hi != POSITIVE_INFINITY)
	    while (i1 < len1 && compare(hi, ary1[len1 - 1]) <= 0) --len1;
	ArrayList res = new ArrayList();
	while (i1 < len1 && i2 < len2) {
	    Object e1 = ary1[i1];
	    Object e2 = ary2[i2];
	    int comp_res = compare(e1, e2);
	    if (comp_res < 0) ++i1;
	    else if (comp_res > 0) ++i2;
	    else {
		if (e1 == null ? e2 == null : e1.equals(e2)) res.add(e1);
		++i1;
		++i2;
	    }
	}
	if (res.isEmpty()) return null;
	else return res.toArray();
    }

    private Object[] difference(Object[] ary1, Object[] ary2, Object lo, Object hi) {
	int i1 = 0, i2 = 0, len1 = ary1.length, len2 = ary2.length;
	if (lo != NEGATIVE_INFINITY)
	    while (i1 < len1 && compare(lo, ary1[i1]) >= 0) ++i1;
	if (hi != POSITIVE_INFINITY)
	    while (i1 < len1 && compare(hi, ary1[len1 - 1]) <= 0) --len1;
	ArrayList res = new ArrayList();
	while (i1 < len1 && i2 < len2) {
	    Object e1 = ary1[i1];
	    Object e2 = ary2[i2];
	    int comp_res = compare(e1, e2);
	    if (comp_res < 0) {
		res.add(e1);
		++i1;
	    } else if (comp_res > 0) ++i2;
	    else {
		if (e1 == null ? e2 != null : !e1.equals(e2)) res.add(e1);
		++i1;
		++i2;
	    }
	}
	while (i1 < len1) res.add(ary1[i1++]);
	if (res.isEmpty()) return null;
	else return res.toArray();
    }

    // `ary2' may be null.
    private boolean isSubset(Object[] ary1, Object[] ary2, Object lo, Object hi) {
	int i1 = 0, i2 = 0, len1 = ary1.length, len2 = ary2 != null ? ary2.length : 0;
	if (lo != NEGATIVE_INFINITY)
	    while (i1 < len1 && compare(lo, ary1[i1]) >= 0) ++i1;
	if (hi != POSITIVE_INFINITY)
	    while (i1 < len1 && compare(hi, ary1[len1 - 1]) <= 0) --len1;
	while (i1 < len1 && i2 < len2) {
	    Object e1 = ary1[i1];
	    Object e2 = ary2[i2];
	    int comp_res = compare(e1, e2);
	    if (comp_res < 0) return false;
	    else if (comp_res > 0) ++i2;
	    else {
		if (e1 == null ? e2 != null : !e1.equals(e2)) return false;
		++i1;
		++i2;
	    }
	}
	return !(i1 < len1);
    }

    /* Binary search returns two pieces of information: (1) whether the element or
     * an equivalent one was found; and (2) the index at which the element (or an
     * equivalent one) either was found or should be inserted.  Since these are both
     * small integers, we pack them into a single `int', to save us from having to
     * allocate a result object.  [Editorial: Java needs multiple values very
     * badly!] */
    private static final int BIN_SEARCH_NOT_FOUND = 0;
    private static final int BIN_SEARCH_FOUND = 1;
    private static final int BIN_SEARCH_FOUND_MASK = 0x1;
    private static final int BIN_SEARCH_INDEX_SHIFT = 1;

    private int binarySearch(Object[] ary, Object elt) {
	int lo = 0, hi = ary.length - 1;
	while (lo <= hi) {
	    int mid = (lo + hi) / 2;
	    Object aelt = ary[mid];
	    int comp_res = compare(elt, aelt);
	    if (comp_res == 0)
		return (mid << BIN_SEARCH_INDEX_SHIFT) | BIN_SEARCH_FOUND;
	    else if (comp_res < 0) hi = mid - 1;
	    else lo = mid + 1;
	}
	return (lo << BIN_SEARCH_INDEX_SHIFT) | BIN_SEARCH_NOT_FOUND;
    }

    // Returns the index of the left edge of the first member of `ary' that is
    // above `lo'.
    private int binarySearchLo(Object[] ary, Object lo) {
	int bin_srch_res = binarySearch(ary, lo);
	int idx = bin_srch_res >> BIN_SEARCH_INDEX_SHIFT;
	if ((bin_srch_res & BIN_SEARCH_FOUND_MASK) == BIN_SEARCH_FOUND)
	    return idx + 1;
	else return idx;
    }

    // Returns the index of the right edge of the last member of `ary' that is
    // below `hi'.
    private int binarySearchHi(Object[] ary, Object hi) {
	int bin_srch_res = binarySearch(ary, hi);
	return bin_srch_res >> BIN_SEARCH_INDEX_SHIFT;
    }

    /****************/
    // Iterator class

    private static final class PTSIterator<Elt> implements Iterator<Elt> {

	private static final class IteratorNode {
	    IteratorNode (Object _subtree, int _index, IteratorNode _parent) {
		subtree = _subtree;
		index = _index;
		parent = _parent;
	    }
	    Object subtree;
	    int index;
	    IteratorNode parent;
	}

	private IteratorNode inode;

	private PTSIterator(Object subtree) {
	    inode = new IteratorNode(subtree, 0, null);
	    canonicalize();
	}

	private void canonicalize() {
	    while (true) {
		if (inode == null) break;
		else if (inode.subtree == null) {
		    inode = inode.parent;
		    if (inode == null) break;
		    else ++inode.index;
		} else if (!(inode.subtree instanceof Node)) {
		    if (inode.index < ((Object[])inode.subtree).length) break;
		    else {
			inode = inode.parent;
			if (inode == null) break;
			else ++inode.index;
		    }
		} else {
		    Node node = (Node)inode.subtree;
		    if (inode.index == 0) inode = new IteratorNode(node.left, 0, inode);
		    else if (inode.index == elementSize(node.element) + 1)
			inode = new IteratorNode(node.right, 0, inode.parent);
		    else break;
		}
	    }
	}

	public boolean hasNext() {
	    return inode != null;
	}

	public Elt next() {
	    Object elt;
	    if (inode == null) throw new NoSuchElementException();
	    else if (!(inode.subtree instanceof Node))
		elt = ((Object[])inode.subtree)[inode.index];
	    else {
		Node node = (Node)inode.subtree;
		if (node.element instanceof EquivalentSet) {
		    ArrayList<Object> al = ((EquivalentSet)node.element).contents;
		    elt = al.get(inode.index - 1);
		} else elt = node.element;
	    }
	    inode.index++;
	    canonicalize();
	    return (Elt)elt;
	}

	public void remove () {
	    throw new UnsupportedOperationException();
	}
    }

    /**
     * Saves the state of this <code>PureTreeSet</code> to a stream.
     *
     * @serialData Emits the internal data of the set, including the
     * comparator it uses; the size of the set [<code>int</code>]; and the
     * elements in order [<code>Object</code>s].
     */
    private void writeObject(java.io.ObjectOutputStream strm)
        throws java.io.IOException {
	strm.defaultWriteObject();	// writes `comp'
        strm.writeInt(size());
	for (Iterator it=iterator(); it.hasNext(); )
            strm.writeObject(it.next());
    }

    /**
     * Reconstitutes the <code>PureTreeSet</code> instance from a stream.
     */
    private void readObject(java.io.ObjectInputStream strm)
        throws java.io.IOException, ClassNotFoundException {
	strm.defaultReadObject();	// reads `comp'
        int size = strm.readInt();
	Elt[] ary = (Elt[])new Object[size];
	for (int i = 0; i < size; ++i)
	    ary[i] = (Elt)strm.readObject();
	tree = fromArray(ary, 0, size);
    }

}

