/*
 * (C) Copyright 2003-2017, by Barak Naveh and Contributors.
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
package org.jgrapht.alg;

import java.util.LinkedList;
import java.util.List;

import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * This class will deal with finding the optimal or approximately optimal minimum tour (hamiltonian
 * cycle) or commonly known as the
 * <a href="http://mathworld.wolfram.com/TravelingSalesmanProblem.html">Traveling Salesman
 * Problem</a>.
 *
 * @author Andrew Newell
 */
public class HamiltonianCycle
{
    /**
     * This method will return an approximate minimal traveling salesman tour (hamiltonian cycle).
     * This algorithm requires that the graph be complete and the triangle inequality exists (if
     * x,y,z are vertices then d(x,y)+d(y,z) &lt; d(x,z) for all x,y,z) then this algorithm will
     * guarantee a hamiltonian cycle such that the total weight of the cycle is less than or equal
     * to double the total weight of the optimal hamiltonian cycle. The optimal solution is
     * NP-complete, so this is a decent approximation that runs in polynomial time.
     *
     * @param g is the graph to find the optimal tour for.
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     *
     * @return The optimal tour as a list of vertices.
     */
    public static <V, E> List<V> getApproximateOptimalForCompleteGraph(SimpleWeightedGraph<V, E> g)
    {
        List<V> vertices = new LinkedList<>(g.vertexSet());

        // If the graph is not complete then return null since this algorithm
        // requires the graph be complete
        if ((vertices.size() * (vertices.size() - 1) / 2) != g.edgeSet().size()) {
            return null;
        }

        List<V> tour = new LinkedList<>();

        // Each iteration a new vertex will be added to the tour until all
        // vertices have been added
        while (tour.size() != g.vertexSet().size()) {
            boolean firstEdge = true;
            double minEdgeValue = 0;
            int minVertexFound = 0;
            int vertexConnectedTo = 0;

            // A check will be made for the shortest edge to a vertex not within
            // the tour and that new vertex will be added to the vertex
            for (int i = 0; i < tour.size(); i++) {
                V v = tour.get(i);
                for (int j = 0; j < vertices.size(); j++) {
                    double weight = g.getEdgeWeight(g.getEdge(v, vertices.get(j)));
                    if (firstEdge || (weight < minEdgeValue)) {
                        firstEdge = false;
                        minEdgeValue = weight;
                        minVertexFound = j;
                        vertexConnectedTo = i;
                    }
                }
            }
            tour.add(vertexConnectedTo, vertices.get(minVertexFound));
            vertices.remove(minVertexFound);
        }
        return tour;
    }
}

// End HamiltonianCycle.java
