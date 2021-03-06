/*
 * (C) Copyright 2012-2017, by Alejandro Ramon Lopez del Huerto and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.matching;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.util.ArrayUnenforcedSet;
import org.jgrapht.util.TypeUtil;

/**
 * An implementation of Edmonds Blossom Shrinking algorithm for constructing maximum matchings on
 * graphs. The algorithm runs in time O(V^4).
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alejandro R. Lopez del Huerto
 * @since Jan 24, 2012
 */
public class EdmondsBlossomShrinking<V, E>
    implements MatchingAlgorithm<V, E>
{
    private final UndirectedGraph<V, E> graph;
    private Map<V, V> match;
    private Map<V, V> path;
    private Map<V, V> contracted;

    /**
     * Construct an instance of the Edmonds blossom shrinking algorithm.
     * 
     * @param graph the input graph
     * @throws IllegalArgumentException if the graph is not undirected
     */
    public EdmondsBlossomShrinking(Graph<V, E> graph)
    {
        if (graph == null) {
            throw new IllegalArgumentException("Input graph cannot be null");
        }
        if (!(graph instanceof UndirectedGraph)) {
            throw new IllegalArgumentException("Only undirected graphs supported");
        }
        this.graph = TypeUtil.uncheckedCast(graph, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Matching<E> computeMatching()
    {
        Set<E> edges = findMatch();
        return new MatchingImpl<>(edges, edges.size());
    }

    /**
     * Runs the algorithm on the input graph and returns the match edge set.
     *
     * @return set of edges
     */
    private Set<E> findMatch()
    {
        Set<E> result = new ArrayUnenforcedSet<>();
        match = new HashMap<>();
        path = new HashMap<>();
        contracted = new HashMap<>();

        for (V i : graph.vertexSet()) {
            // Any augmenting path should start with _exposed_ vertex
            // (vertex may not escape match-set being added once)
            if (!match.containsKey(i)) {
                // Match is maximal iff graph G contains no more augmenting paths
                V v = findPath(i);
                while (v != null) {
                    V pv = path.get(v);
                    V ppv = match.get(pv);
                    match.put(v, pv);
                    match.put(pv, v);
                    v = ppv;
                }
            }
        }

        Set<V> seen = new HashSet<>();
        graph.vertexSet().stream().filter(v -> !seen.contains(v) && match.containsKey(v)).forEach(
            v -> {
                seen.add(v);
                seen.add(match.get(v));
                result.add(graph.getEdge(v, match.get(v)));
            });

        return result;
    }

    private V findPath(V root)
    {
        Set<V> used = new HashSet<>();
        Queue<V> q = new ArrayDeque<>();

        // Expand graph back from its contracted state
        path.clear();
        contracted.clear();

        graph.vertexSet().forEach(vertex -> contracted.put(vertex, vertex));

        used.add(root);
        q.add(root);

        while (!q.isEmpty()) {
            V v = q.remove();

            for (E e : graph.edgesOf(v)) {
                V to = graph.getEdgeSource(e);

                if (to.equals(v)) {
                    to = graph.getEdgeTarget(e);
                }

                if ((contracted.get(v).equals(contracted.get(to))) || to.equals(match.get(v))) {
                    continue;
                }

                // Check whether we've hit a 'blossom'
                if ((to.equals(root))
                    || ((match.containsKey(to)) && (path.containsKey(match.get(to)))))
                {
                    V stem = lowestCommonAncestor(v, to);

                    Set<V> blossom = new HashSet<>();

                    markPath(v, to, stem, blossom);
                    markPath(to, v, stem, blossom);

                    graph
                        .vertexSet().stream()
                        .filter(
                            i -> contracted.containsKey(i) && blossom.contains(contracted.get(i)))
                        .forEach(i -> {
                            contracted.put(i, stem);
                            if (!used.contains(i)) {
                                used.add(i);
                                q.add(i);
                            }
                        });

                    // Check whether we've had hit a loop (of even length (!) presumably)
                } else if (!path.containsKey(to)) {
                    path.put(to, v);

                    if (!match.containsKey(to)) {
                        return to;
                    }

                    to = match.get(to);

                    used.add(to);
                    q.add(to);
                }
            }
        }
        return null;
    }

    private void markPath(V v, V child, V stem, Set<V> blossom)
    {
        while (!contracted.get(v).equals(stem)) {
            blossom.add(contracted.get(v));
            blossom.add(contracted.get(match.get(v)));
            path.put(v, child);
            child = match.get(v);
            v = path.get(match.get(v));
        }
    }

    private V lowestCommonAncestor(V a, V b)
    {
        Set<V> seen = new HashSet<>();
        for (;;) {
            a = contracted.get(a);
            seen.add(a);
            if (!match.containsKey(a)) {
                break;
            }
            a = path.get(match.get(a));
        }
        for (;;) {
            b = contracted.get(b);
            if (seen.contains(b)) {
                return b;
            }
            b = path.get(match.get(b));
        }
    }
}

// End EdmondsBlossomShrinking.java
