/*
 * (C) Copyright 2015-2017, by Joris Kinable and Contributors.
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
package org.jgrapht.graph;

import java.util.Map;

import org.jgrapht.DirectedGraph;

/**
 * <p>
 * A weighted view of the backing graph specified in the constructor. This allows you to apply
 * algorithms designed for weighted graphs to an unweighted graph by providing an explicit edge
 * weight mapping. The implementation also allows for "masking" weights for a subset of the edges in
 * an existing weighted graph.
 * </p>
 *
 * <p>
 * Query operations on this graph "read through" to the backing graph. Vertex addition/removal and
 * edge addition/removal are all supported (and immediately reflected in the backing graph). Setting
 * an edge weight will pass the operation to the backing graph as well if the backing graph
 * implements the WeightedGraph interface. Setting an edge weight will modify the weight map in
 * order to maintain a consistent graph.
 * </p>
 *
 * <p>
 * Note that edges returned by this graph's accessors are really just the edges of the underlying
 * directed graph.
 * </p>
 *
 * <p>
 * This graph does <i>not</i> pass the hashCode and equals operations through to the backing graph,
 * but relies on <tt>Object</tt>'s <tt>equals</tt> and <tt>hashCode</tt> methods. This graph will be
 * serializable if the backing graph is serializable.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 * @since Aug 20, 2015
 */
public class AsWeightedDirectedGraph<V, E>
    extends AsWeightedGraph<V, E>
    implements DirectedGraph<V, E>
{
    private static final long serialVersionUID = -6264605187276749471L;

    /**
     * Constructor for AsWeightedGraph.
     *
     * @param g the backing graph over which a weighted view is to be created.
     * @param weightMap A mapping of edges to weights. If an edge is not present in the weight map,
     *        the edge weight for the underlying graph is returned. Note that a live reference to
     *        this map is retained, so if the caller changes the map after construction, the changes
     *        will affect the
     */
    public AsWeightedDirectedGraph(DirectedGraph<V, E> g, Map<E, Double> weightMap)
    {
        super(g, weightMap);
    }
}

// End AsWeightedDirectedGraph.java
