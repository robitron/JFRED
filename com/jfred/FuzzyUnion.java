// @(#)FuzzyUnion.java 97/12/16 1.17 Robby Glen Garner, Paco Xander Nathan
// Copyright (C) 1997-1999. Released under the Gnu Public License

package com.jfred;
import java.util.*;

public class FuzzyUnion extends Vector {

  // Include all the fuzzy sets in the given rule
  // into this set union, modifying probabilites

  public void addRule (FuzzyRule r) {
    for (int i = 0; i < r.size(); i++) {
      FuzzySet s = (FuzzySet) r.elementAt(i);

      // case 0: adding a rule to the list
      if (!contains(s.rule)) {
	addElement(s.rule);
	s.rule.sum = s.prob;
      }

      // case 1: accumulate fuzzy set probability 
      // for a rule that is already in the list
      else
	s.rule.sum += s.prob;
    }
  }


  // Include the given action rule into this
  // set union, with a high probability

  public void addRule (ActionRule r) {

    // case 0: adding a rule to the list
    if (!contains(r)) {
      addElement(r);
      r.sum = 100;
    }

    // case 1: accumulate fuzzy set probability 
    // for a rule that is already in the list
    else
      r.sum += 100;
  }


  // Include the given list of action rules
  // into this set union

  public void addRules (Vector v) {
    for (int i = 0; i < v.size(); i++) {
      ActionRule r = (ActionRule) v.elementAt(i);
      addRule(r);
    }
  }


  // Cull the list based on required properties

  public void cullProperties (Properties prop, boolean hasHTML) {
    Vector cull = new Vector();

    // first pass - decide which to cull

    for (Enumeration e = elements(); e.hasMoreElements(); ) {   
      ActionRule r = (ActionRule) e.nextElement();

      // check for "html:" fields

      if (!hasHTML && r.hasProperty(Rule.PROP_HTML)) {
	if (r.getProperty(Rule.PROP_HTML).equals("true"))
	  cull.addElement(r);
      }

      // check for "requires:" fields

      if (r.hasProperty(Rule.PROP_REQUIRES)) {
	String val = prop.getProperty(r.getProperty(Rule.PROP_REQUIRES));

        if ((val == null) || val.trim().equals(""))
	  cull.addElement(r);
      }

      // check for "equals:" fields

      if (r.hasProperty(Rule.PROP_EQUALS)) {
	String val = prop.getProperty(r.getProperty(Rule.PROP_EQUALS));

        if (r.testProperty(val, prop))
	  cull.addElement(r);
      }
    }

    // second pass - remove the culls

    for (Enumeration e = cull.elements(); e.hasMoreElements(); ) {   
      ActionRule r = (ActionRule) e.nextElement();

      removeElement(r);
    }
  }


  // Move all rules which have previously been used to the end of the
  // list and reduce their sums

  public void priorUse (RuleStack used) {
    Vector cull = new Vector();
    Vector kill = new Vector();

    // first pass - decide which to move

    for (Enumeration e = elements(); e.hasMoreElements(); ) {   
      ActionRule r = (ActionRule) e.nextElement();

      if (used.hasRule(r)) {
	if (!r.hasProperty(Rule.PROP_REPEAT)) {
	  cull.addElement(r);
	  used.removeRule(r);
	}

	else if (r.getProperty(Rule.PROP_REPEAT).equals("false")) {
	  cull.addElement(r);
	  kill.addElement(r);
	}
      }
    }

    // second pass - move the culls

    for (Enumeration e = cull.elements(); e.hasMoreElements(); ) {   
      ActionRule r = (ActionRule) e.nextElement();

      removeElement(r);

      if (!kill.contains(r))
	addElement(r);
    }
  }


  // Trace the list of candidate rules

  public void printTrace (RuleStack used) {
    for (int i = 0; i < size(); i++) {
      ActionRule r = (ActionRule) elementAt(i);
      String trace = r.name + " " + r.sum + " " + r.priority;

      if (used.hasRule(r))
	trace += " (" + used.search(r) + ")";

      System.out.println("\t[ " + trace + " ]");
    }
  }


  // Select one rule from the list, returning the
  // given default if none match

  public ActionRule selectOne (ActionRule def) {

    // first, find the cumulative probability 
    // distribution

    int totSum = 0;

    for (int i = 0; i < size(); i++) {
      ActionRule r = (ActionRule) elementAt(i);

      totSum += r.sum;
    }

    // translate the fuzzy rule distribution into
    // an exponential distribution - to create
    // a sortable rule in the list

    double totDist = 0.0;
    double dist[] = new double[size()];

    for (int i = 0; i < size(); i++) {
      ActionRule r = (ActionRule) elementAt(i);

      dist[i] = Math.exp((double) r.sum / (double) totSum) * -2.0f;
      totDist += dist[i];
    }

    // generate a random variable based on our 
    // exponential distribution

    if (size() > 0) {
      double estimator = (Fred.rand.nextDouble() * (totDist - dist[0])) + dist[0];

      for (int i = 0; i < size(); i++)
	if (estimator <= dist[i])
	  return (ActionRule) elementAt(i);
    }

    // otherwise, if not found then return default

    return def;
  }


  /** This is a generic version of C.A.R Hoare's Quick Sort 
   * algorithm.  This will handle arrays that are already
   * sorted, and arrays with duplicate keys.
   *
   * If you think of a one dimensional array as going from
   * the lowest index on the left to the highest index on the right
   * then the parameters to this function are lowest index or
   * left and highest index or right.  The first time you call
   * this function it will be with the parameters 0, a.length - 1.
   *
   * @param a       an Object array
   * @param lo0     left boundary of array partition
   * @param hi0     right boundary of array partition
   *
   * @author James Gosling
   * @author Kevin A. Smith
   * @version 	@(#)QSortAlgorithm.java	1.3, 29 Feb 1996
   */

  public void sort () {
    quickSort(elementData, 0, size() - 1);
  }

  private boolean compare (Object a[], int i, int j) {
    ActionRule r1 = (ActionRule) a[i];
    ActionRule r2 = (ActionRule) a[j];

    return (r1.priority > r2.priority) || (r1.sum > r2.sum);
  }

  private void swap (Object a[], int i, int j) {
    Object o = a[i]; 
    a[i] = a[j];
    a[j] = o;
  }

  private void quickSort (Object a[], int lo0, int hi0) {
    int lo = lo0;
    int hi = hi0;

    if (hi0 > lo0) {
      // arbitrarily establishing partition element 
      // as the midpoint of the array
      int mid = (lo0 + hi0) / 2;

      // loop through the array until indices cross
      while (lo <= hi) {

	// find the first element that is greater than or equal
	// to the partition element starting from the left index
	while ((lo < hi0) && compare(a, lo, mid))
	  ++lo;

	// find an element that is smaller than or equal to 
	// the partition element starting from the right index
	while ((hi > lo0) && compare(a, mid, hi))
	  --hi;

	// if the indexes have not crossed, swap
	if (lo <= hi) {
	  swap(a, lo, hi);
	  ++lo;
	  --hi;
	}
      }

      // if the right index has not reached the left side of array
      // must now sort the left partition
      if (lo0 < hi)
	quickSort(a, lo0, hi);

      // if the left index has not reached the right side of array
      // must now sort the right partition
      if (lo < hi0)
	quickSort(a, lo, hi0);
    }
  }
}
