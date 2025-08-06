/* Builds a hierarchical tree and maps the tree into an indented string */
/* PrintHier calls can be streamlined to be based on StringHier */
/* PrintGenCol.Queue could be streamlined to be based on StringHier */

package com.ms4systems.devs.core.util;

import java.util.*;

public class hierTree {

	public static Pair topPair = new Pair();

	// Adds a child node to the given node; returns the child node
	public static Pair addChild(Pair node, String chd) {
		LinkedList<Object> chds = (LinkedList<Object>) node.getValue();
		Pair p = new Pair(chd, new LinkedList<Object>());
		chds.add(p);
		return p;
	}

	// Add multiple child nodes to the given node; returns an array of child
	// nodes
	public static Pair[] addChildren(Pair node, String[] children) {
		LinkedList<Object> chds = (LinkedList<Object>) node.getValue();
		Pair[] chps = new Pair[children.length];
		for (int i = 0; i < children.length; i++) {
			chps[i] = new Pair(children[i], new LinkedList<Object>());
			chds.add(chps[i]);
		}
		return chps;
	}

	// Visually indents strings based on leveling
	public static String indent(int level) {
		String s = "";
		for (int i = 0; i < level; i++) {
			s += "--";
		}
		return s;
	}

	public static void atomicPrint(String moduleName, int level) {
		System.out.println(indent(level) + moduleName);
	}

	public static void coupledPrint(String moduleName,
			LinkedList<Object> children, int level, int maxLevel) {
		System.out.println(indent(level) + moduleName);
		printComponents(children, level, maxLevel);
	}

	// Prints components in LinkedList<Object>
	// Called by coupledPrint
	public static void printComponents(LinkedList<Object> es, int level,
			int maxLevel) {
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Pair p = (Pair) it.next();
			printHier(p, level + 1, maxLevel);
		}
	}

	// Prints the tree starting with the max level infinity
	public static void printHier() {
		printHier(Integer.MAX_VALUE);
	}

	// Prints tree based on topPair
	public static void printHier(int maxLevel) {
		String moduleNm = (String) topPair.getKey();
		LinkedList<Object> chds = (LinkedList<Object>) topPair.getValue();
		if (chds.isEmpty()) {
			atomicPrint(moduleNm, 0);
		} else {
			coupledPrint(moduleNm, chds, 0, maxLevel);

		}
	}

	// UI to Print tree starting from specified node to maxLevel
	public static void printHier(Pair node, int maxLevel) {
		printHier(node, 0, maxLevel); // 0 indicates start from node level
	}

	// Prints tree starting from specified node and level to maxLevel
	public static void printHier(Pair node, int level, int maxLevel) {
		if (level > maxLevel) {
			return;
		}
		String modName = (String) node.getKey();
		System.out.println(indent(level) + modName);
		LinkedList<Object> chds = (LinkedList<Object>) node.getValue();
		Iterator it = chds.iterator();
		while (it.hasNext()) {
			Pair p = (Pair) it.next();
			String moduleNm = (String) p.getKey();
			LinkedList<Object> nchds = (LinkedList<Object>) p.getValue();
			if (nchds.isEmpty()) {
				atomicPrint(moduleNm, level + 1);
			} else {
				coupledPrint(moduleNm, nchds, level + 1, maxLevel);
			}
		}

	}

	// UI for printing tree from topPair to nodeName
	// Calls printHierFrom(Pair, node, maxLevel)
	public static void printHierFrom(String nodeName, int maxLevel) {
		printHierFrom(topPair, nodeName, maxLevel);
	}

	// Looks for nodeName below p and prints from that node if it exists
	public static void printHierFrom(Pair p, String nodeName, int maxLevel) {
		if (((String) p.getKey()).indexOf(nodeName) > -1) {
			printHier(p, 0, maxLevel);
		}
		LinkedList<Object> topChlds = (LinkedList<Object>) p.getValue();
		Iterator it = topChlds.iterator();
		;
		while (it.hasNext()) {
			Pair pr = (Pair) it.next();
			printHierFrom(pr, nodeName, maxLevel);
		}

	}

	// ////////////////////////////////////
	public static Pair atomicString(String moduleName, int level) {
		return new Pair(new Integer(level), moduleName);
	}

	public static LinkedList<Object> coupledString(String moduleName,
			LinkedList<Object> children, int level, int maxLevel) {
		LinkedList<Object> q = new LinkedList<Object>();
		q.add(new Pair(new Integer(level), moduleName));
		q.addAll(StringComponents(children, level, maxLevel));
		return q;
	}

	public static LinkedList<Object> StringComponents(LinkedList<Object> es,
			int level, int maxLevel) {
		LinkedList<Object> q = new LinkedList<Object>();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Pair p = (Pair) it.next();
			q.addAll(StringHier(p, level + 1, maxLevel));
		}
		return q;
	}

	// UI for StringHier; calls StringHier(maxLevel)
	public static LinkedList<Object> StringHier() {
		return StringHier(Integer.MAX_VALUE);
	}

	// Maps a tree starting from topPair into a LinkedList<Object> of strings
	// with indentation
	public static LinkedList<Object> StringHier(int maxLevel) {
		LinkedList<Object> q = new LinkedList<Object>();
		String moduleNm = (String) topPair.getKey();
		LinkedList<Object> chds = (LinkedList<Object>) topPair.getValue();
		if (chds.isEmpty()) {
			q.add(atomicString(moduleNm, 0));
			return q;
		} else {
			q.addAll(coupledString(moduleNm, chds, 0, maxLevel));
			return q;
		}
	}

	// Maps a tree into a LinkedList<Object> of strings with indentation
	public static LinkedList<Object> StringHier(Pair node, int maxLevel) {
		return StringHier(node, 0, maxLevel);
	}

	// Maps a tree into a LinkedList<Object> of strings starting from node at
	// level to maxLevel
	public static LinkedList<Object> StringHier(Pair node, int level,
			int maxLevel) {
		LinkedList<Object> q = new LinkedList<Object>();
		if (level > maxLevel) {
			return q;
		}

		String modName = (String) node.getKey();
		q.add(new Pair(new Integer(level), modName));
		LinkedList<Object> chds = (LinkedList<Object>) node.getValue();
		Iterator it = chds.iterator();
		while (it.hasNext()) {
			Pair p = (Pair) it.next();
			String moduleNm = (String) p.getKey();
			LinkedList<Object> nchds = (LinkedList<Object>) p.getValue();
			if (nchds.isEmpty()) {
				q.add(atomicString(moduleNm, level + 1));
			} else {
				q.addAll(coupledString(moduleNm, nchds, level + 1, maxLevel));
			}
		}
		return q;

	}

	public static LinkedList<Object> StringHierFrom(String nodeName,
			int maxLevel) {
		return StringHierFrom(topPair, nodeName, maxLevel);
	}

	// Pulls out all nodes at a given level
	public static LinkedList<Object> nodesAt(Pair top, int level) {
		LinkedList<Object> es = new LinkedList<Object>();
		LinkedList<Object> q = StringHierFrom(top, top.getKey().toString(),
				level);
		Iterator it = q.iterator();
		while (it.hasNext()) {
			Pair p = (Pair) it.next();
			if (((Integer) p.getKey()).intValue() == level)
				es.add(p.getValue());
		}
		return es;
	}

	// Gets all nodes starting from the top and returns depth of tree
	public static int maxLevel(Pair top) {
		LinkedList<Object> q = allNodes(top);
		Iterator it = q.iterator();
		int max = -Integer.MAX_VALUE;
		while (it.hasNext()) {
			int i = ((Integer) ((Pair) it.next()).getKey()).intValue();
			if (i > max)
				max = i;
		}
		return max;
	}

	// Returns LinkedList<Object> from top node
	public static LinkedList<Object> allNodes(Pair top) {
		return StringHierFrom(top, top.getKey().toString(), Integer.MAX_VALUE);
	}

	// Checks if nodeName is at the level starting from p
	public static boolean isAtLevel(Pair p, String nodeName, int level) {
		return nodesAt(p, level).contains(nodeName);
	}

	// Start with p and find nodeName below; print from nodeName
	public static LinkedList<Object> StringHierFrom(Pair p, String nodeName,
			int maxLevel) {
		if (((String) p.getKey()).indexOf(nodeName) > -1) {
			return StringHier(p, 0, maxLevel);
		}
		LinkedList<Object> q = new LinkedList<Object>();
		LinkedList<Object> topChlds = (LinkedList<Object>) p.getValue();
		Iterator<Object> it = topChlds.iterator();
		;
		while (it.hasNext()) {
			Pair pr = (Pair) it.next();
			q.addAll(StringHierFrom(pr, nodeName, maxLevel));
		}
		return q;
	}

	// Prints LinkedList<Object> produced by StringHier (includes indent)
	public static void printQueue(LinkedList<Object> q) {
		Iterator<Object> it = q.iterator();
		while (it.hasNext()) {
			Pair p = (Pair) it.next();
			System.out.println(indent(((Integer) p.getKey()).intValue())
					+ p.getValue());
		}
	}

	// Returns LinkedList<Object> as a string
	public static String QueueToString(LinkedList<Object> q) {
		Iterator<Object> it = q.iterator();
		String s = "";
		while (it.hasNext()) {
			Pair p = (Pair) it.next();
			s += indent(((Integer) p.getKey()).intValue()) + p.getValue()
					+ "\n";

		}
		return s;
	}

	// Map a tree into a bracket representation
	public static String bracketHier(Pair node) {
		String s = "";
		String operName = (String) node.getKey();
		s += operName + "(";
		LinkedList<Object> chds = (LinkedList<Object>) node.getValue();
		Iterator<Object> it = chds.iterator();
		while (it.hasNext()) {
			Pair p = (Pair) it.next();
			String argNm = (String) p.getKey();
			LinkedList<Object> nchds = (LinkedList<Object>) p.getValue();
			if (nchds.isEmpty()) {
				s += argNm + ",";
			} else {
				s += bracketHier(p) + ",";
			}
		}
		s = s.substring(0, s.length() - 1);
		return s + ")";

	}

	// ///////////////////////////////

	public static void traditionalCalculus() {

		topPair = new Pair("calculus", new LinkedList<Object>());
		Pair pc = addChild(topPair, "1. preCalculus");
		Pair ps[] = addChildren(pc, new String[] { "1. Numbers",
				"2. Equations", "3. Polynomials", "4. Plotting", "5. Graphs",
				"6. Functions", "7. Applications" });
		addChildren(ps[0], new String[] { "1. Algorithms", "2. Sequences",
				"3. Complex Numbers" });
		addChildren(ps[1], new String[] { "1. Linear Equations",
				"2. Inequalities" });
		addChildren(ps[2], new String[] { "1. Polynomial Algebra",
				"2. Factoring, Roots", "3. Algebraic Expressions" });
		addChildren(ps[5], new String[] { "1. Linear Functions",
				"2. Distance and Circles", "3. Conic Sections",
				"4. Trigonometry", "5. Logarithms and Exponentials",
				"6. Function Operations" });
		addChild(ps[6], "1. Finance");

		pc = addChild(topPair, "2. Limits and Continuity");
		ps = addChildren(pc, new String[] { "1. Ordinary Limits",
				"2. Continuity", "3. One-sided Limits and Asymptotes",
				"4. Special Limits"

		});
		addChildren(ps[0],
				new String[] { "1. Finding delta", "2. Basic Limits" });
		addChildren(ps[1], new String[] { "1. Epsilon and delta",
				"2. Missing values",
				"3.Discontinuities of simple piecewise defined functions" });
		addChildren(ps[2], new String[] { "1. Limits at infinity",
				"2. Asymptotes", "3. One-sided limits",
				"4. Asymptotes of oscillating functions" });
		addChildren(ps[3], new String[] { "1. Piecewise limits",
				"2. Trigonometric Limits" });

		pc = addChild(topPair, "3. The Derivative");
		ps = addChildren(pc, new String[] { "1. Slope and Tangents ",
				"2. Linearization", });
		addChildren(ps[0], new String[] { "1. Tangent line",
				"2. Differentiability" });
		addChildren(ps[1], new String[] { "1. Linearizations",
				"2. Linear approximation" });
		pc = addChild(topPair, "4. Techniques and Theory of Differentiation");
		ps = addChildren(pc, new String[] { "1. Powers, Products, Quotients",
				"2. Trigonometric Functions", "3. Chain Rule",
				"4. Implicit Differentiation", "5. Theory",
				"6. Exponential Functions"

		});
		addChildren(ps[4], new String[] { "1. Mean Value Theorem",
				"2. Inverse Function Derivative" });

		pc = addChild(topPair, "5. Applications of the Derivative");
		ps = addChildren(pc, new String[] { "1. Rate of Change",
				"2. Relative Rates", "3. Maxima, Minima", "4. Graphing",
				"5. Optimization", "6. L'Hopital's Rule", "7. Newton's Method",
				"8. Approximations" });
		addChildren(ps[0], new String[] { "1. Velocity", "2. Marginal cost",
				"3. Displacement" });
		addChildren(ps[2], new String[] { "1. Relative extrema",
				"2. Finding Extrema", "3. Tests for Extrema"

		});
		addChildren(ps[4], new String[] { "1. Maximal rectangles under curves",
				"2. Maximal Products", "3. Maximal Product"

		});
		addChildren(ps[7], new String[] { "1. Tangent line equation",
				"2. Trigonometric examples", "3. Linearization",
				"4. Differentials", "5. Linear approximation",
				"6. Taylor polynomials" });

		addChildren(ps[0], new String[] { "1. Velocity", "2. Marginal cost",
				"3. Displacement" });
		pc = addChild(topPair, "6. Integration");
		ps = addChildren(pc, new String[] { "1. Sums",
				"2. Indefinite Integrals", "3. Definite Integrals",
				"4. Fundamental Theorem", });
		addChildren(ps[0], new String[] { "1. Summation",
				"2. Geometric series", "3. Repeating decimals",
				"4. Riemann sums" });
		addChildren(ps[1], new String[] { "1. Indefinite integral",
				"2. Substitution - change of variables",
				"3. Integration by substitution", });
		addChildren(ps[2], new String[] { "1. Definite integrls",
				"2. Substitution methods", "3. Numerical Integration", });
		addChildren(
				ps[3],
				new String[] { "1. Differentiation and the Fundamental Theorem " });

		pc = addChild(topPair, "7. Applications of Integration");
		ps = addChildren(pc, new String[] { "1. Area", "2. Volume",
				"3. Assorted Applications", });
		pc = addChild(topPair, "8. Transcendental Functions");
		pc = addChild(topPair, "9. Methods of Integration");
		ps = addChildren(pc, new String[] { "1. Integration by Parts",
				"2. Trigonometric Integrals",
				"3. Trigonometric and Rationalizing Substitutions",
				"4. Partial Fractions", "5. Improper Integrals" });

		pc = addChild(topPair, "10. Geometry, Curves and Polar Coordinates");

	}

	// Example for tree construction
	// Running prints out a tree, for example table of contents for DEVSCalculus
	public static void DEVSCalculus() {

		topPair = new Pair("calculus", new LinkedList<Object>()); // (name, set
																	// of
																	// children's
																	// names)
		Pair pc = addChild(topPair, "1. preCalculus");
		Pair ps[] = addChildren(pc, new String[] { "1. Sets", "2. Relations",
				"3. Functions", "4. Examples" });
		addChildren(ps[0], new String[] { "1. Union", "2. Intersection",
				"3. Difference", "4. Cross-product" });
		addChildren(ps[1], new String[] { "1. Domain", "2. Range",
				"3. Multiplicity", "4. Composition" });
		addChildren(ps[2], new String[] { "1. Onto,one-one", "2. Inverse",
				"3. Composition" });

		addChild(ps[3], "1. ???");

		pc = addChild(topPair, "2. Event Sets");
		ps = addChildren(pc, new String[] { "1. Definitions",
				"2. Measures of Variation", "3. Extrema", "4. Refinement"

		});
		addChildren(ps[0], new String[] { "1. Sum", "2. Max", "3. Size" });
		addChildren(ps[1], new String[] { "1. Form Factor",
				"2. Non-increasing(non-decreasing) segments",
				"3. Locating Extrema", "4. Monoset Decomposition" });
		addChildren(ps[2], new String[] { "1. Within-the-Box",
				"2. Effect on Measures", "3. Termination",
				"4.  Form Factor Examples" });

		pc = addChild(topPair, "3. Basic Event Set Types");
		ps = addChildren(pc, new String[] { "1. Domain-based Event Sets",
				"2. Range-Based Event Sets", "3. Mapping between Types",
				"4. Ratio of Sizes" });
		pc = addChild(topPair, "4. Integrals and Derivatives of Event Sets");
		ps = addChildren(pc, new String[] { "1. Integral of an Event Set",
				"2. Derivative of an Event Set",
				"3. Integral of Within-the-box Refinement",
				"4. Fundamental Theorem Relating Integrals to Derivatives" });
		pc = addChild(topPair,
				"5. Event Set Representation of Continuous Functions");
		ps = addChildren(
				pc,
				new String[] {
						"1. Centrality of  Within-the-box Refinement",
						"2. Uncertainty Metric",
						"3. The Range-Based Representation Can Be Arbitrarily More Efficient Than Its Domain-Based Equivalent",
						"4. Convergence of Representations",
						"5. Multi-dimensional Representations" });
		addChildren(ps[0],
				new String[] { "1. Ratio Calculations Show Significant Gains" });

		pc = addChild(topPair, "6. Event Set Differential Equations");
		ps = addChildren(pc, new String[] { "1. Posing the Problem",
				"2. Solution in One-Dimension",
				"3. Comparison with Traditional  Differential Equations"

		});

	}

	public static void main(String argv[]) {

		Pair p = new Pair("add", new LinkedList<Object>());
		Pair pc1 = addChild(p, "a");
		Pair pc2 = addChild(p, "b");

		Pair pcc = addChild(pc1, "c");
		System.out.println(bracketHier(p));
		System.exit(0);

	} // main
}
