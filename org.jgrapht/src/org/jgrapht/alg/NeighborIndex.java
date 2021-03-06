/*
 * (C) Copyright 2005-2017, by Charles Fry and Contributors.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.event.VertexSetListener;
import org.jgrapht.util.ModifiableInteger;

/**
 * Maintains a cache of each vertex's neighbors. While lists of neighbors can be obtained from
 * {@link Graphs}, they are re-calculated at each invocation by walking a vertex's incident edges,
 * which becomes inordinately expensive when performed often.
 *
 * <p>
 * Edge direction is ignored when evaluating neighbors; to take edge direction into account when
 * indexing neighbors, use {@link DirectedNeighborIndex}.
 *
 * <p>
 * A vertex's neighbors are cached the first time they are asked for (i.e. the index is built on
 * demand). The index will only be updated automatically if it is added to the associated graph as a
 * listener. If it is added as a listener to a graph other than the one it indexes, results are
 * undefined.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Charles Fry
 * @since Dec 13, 2005
 */
public class NeighborIndex<V, E>
    implements GraphListener<V, E>
{
    Map<V, Neighbors<V>> neighborMap = new HashMap<>();
    private Graph<V, E> graph;

    /**
     * Creates a neighbor index for the specified undirected graph.
     *
     * @param g the graph for which a neighbor index is to be created.
     */
    public NeighborIndex(Graph<V, E> g)
    {
        // no need to distinguish directedgraphs as we don't do traversals
        graph = g;
    }

    /**
     * Returns the set of vertices which are adjacent to a specified vertex. The returned set is
     * backed by the index, and will be updated when the graph changes as long as the index has been
     * added as a listener to the graph.
     *
     * @param v the vertex whose neighbors are desired
     *
     * @return all unique neighbors of the specified vertex
     */
    public Set<V> neighborsOf(V v)
    {
        return getNeighbors(v).getNeighbors();
    }

    /**
     * Returns a list of vertices which are adjacent to a specified vertex. If the graph is a
     * multigraph, vertices may appear more than once in the returned list. Because a list of
     * neighbors can not be efficiently maintained, it is reconstructed on every invocation, by
     * duplicating entries in the neighbor set. It is thus more efficient to use
     * {@link #neighborsOf(Object)} unless duplicate neighbors are important.
     *
     * @param v the vertex whose neighbors are desired
     *
     * @return all neighbors of the specified vertex
     */
    public List<V> neighborListOf(V v)
    {
        return getNeighbors(v).getNeighborList();
    }

    /**
     * @see GraphListener#edgeAdded(GraphEdgeChangeEvent)
     */
    @Override
    public void edgeAdded(GraphEdgeChangeEvent<V, E> e)
    {
        E edge = e.getEdge();
        V source = graph.getEdgeSource(edge);
        V target = graph.getEdgeTarget(edge);

        // if a map does not already contain an entry,
        // then skip addNeighbor, since instantiating the map
        // will take care of processing the edge (which has already
        // been added)

        if (neighborMap.containsKey(source)) {
            getNeighbors(source).addNeighbor(target);
        } else {
            getNeighbors(source);
        }
        if (neighborMap.containsKey(target)) {
            getNeighbors(target).addNeighbor(source);
        } else {
            getNeighbors(target);
        }
    }

    /**
     * @see GraphListener#edgeRemoved(GraphEdgeChangeEvent)
     */
    @Override
    public void edgeRemoved(GraphEdgeChangeEvent<V, E> e)
    {
        V source = e.getEdgeSource();
        V target = e.getEdgeTarget();
        if (neighborMap.containsKey(source)) {
            neighborMap.get(source).removeNeighbor(target);
        }
        if (neighborMap.containsKey(target)) {
            neighborMap.get(target).removeNeighbor(source);
        }
    }

    /**
     * @see VertexSetListener#vertexAdded(GraphVertexChangeEvent)
     */
    @Override
    public void vertexAdded(GraphVertexChangeEvent<V> e)
    {
        // nothing to cache until there are edges
    }

    /**
     * @see VertexSetListener#vertexRemoved(GraphVertexChangeEvent)
     */
    @Override
    public void vertexRemoved(GraphVertexChangeEvent<V> e)
    {
        neighborMap.remove(e.getVertex());
    }

    private Neighbors<V> getNeighbors(V v)
    {
        Neighbors<V> neighbors = neighborMap.get(v);
        if (neighbors == null) {
            neighbors = new Neighbors<>(Graphs.neighborListOf(graph, v));
            neighborMap.put(v, neighbors);
        }
        return neighbors;
    }

    /**
     * Stores cached neighbors for a single vertex. Includes support for live neighbor sets and
     * duplicate neighbors.
     */
    static class Neighbors<V>
    {
        private Map<V, ModifiableInteger> neighborCounts = new LinkedHashMap<>();

        // TODO could eventually make neighborSet modifiable, resulting
        // in edge removals from the graph
        private Set<V> neighborSet = Collections.unmodifiableSet(neighborCounts.keySet());

        public Neighbors(Collection<V> neighbors)
        {
            // add all current neighbors
            for (V neighbor : neighbors) {
                addNeighbor(neighbor);
            }
        }

        public void addNeighbor(V v)
        {
            ModifiableInteger count = neighborCounts.get(v);
            if (count == null) {
                count = new ModifiableInteger(1);
                neighborCounts.put(v, count);
            } else {
                count.increment();
            }
        }

        public void removeNeighbor(V v)
        {
            ModifiableInteger count = neighborCounts.get(v);
            if (count == null) {
                throw new IllegalArgumentException(
                    "Attempting to remove a neighbor that wasn't present");
            }

            count.decrement();
            if (count.getValue() == 0) {
                neighborCounts.remove(v);
            }
        }

        public Set<V> getNeighbors()
        {
            return neighborSet;
        }

        public List<V> getNeighborList()
        {
            List<V> neighbors = new ArrayList<>();
            for (Map.Entry<V, ModifiableInteger> entry : neighborCounts.entrySet()) {
                V v = entry.getKey();
                int count = entry.getValue().intValue();
                for (int i = 0; i < count; i++) {
                    neighbors.add(v);
                }
            }
            return neighbors;
        }
    }
}

// End NeighborIndex.java
