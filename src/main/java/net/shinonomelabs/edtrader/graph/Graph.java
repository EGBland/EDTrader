package net.shinonomelabs.edtrader.graph;

import org.javatuples.Pair;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Graph<T, U extends Number & Comparable> {
  private final List<T> nodes = new ArrayList<>();
  private final List<Arc<T, U>> arcs = new ArrayList<>();

  public Graph() {}

  private Graph(List<T> nodes, List<Arc<T, U>> arcs) {
    this.nodes.addAll(nodes);
    this.arcs.addAll(arcs);
  }

  public void addNode(T node) {
    nodes.add(node);
  }

  public int nodesLength() {
    return nodes.size();
  }

  public void addArc(T nodeFrom, T nodeTo, U weight) {
    // TODO potentially slow?
    if (!nodes.contains(nodeFrom)) nodes.add(nodeFrom);
    if (!nodes.contains(nodeTo)) nodes.add(nodeTo);

    arcs.add(new Arc(nodeFrom, nodeTo, weight));
  }

  public Graph<T, U> truncate(U min, U max) {
    ArrayList<T> nodes = new ArrayList<>();
    nodes.addAll(this.nodes);
    Supplier<List<Arc<T, U>>> arrayListSupplier = () -> new ArrayList<Arc<T, U>>();
    List<Arc<T, U>> truncArcs =
        arcs.stream()
            .filter(arc -> min.compareTo(arc.weight) < 0 && max.compareTo(arc.weight) > 0)
            .collect(Collectors.toCollection(arrayListSupplier));

    return new Graph(nodes, truncArcs);
  }

  public Graph<T, U> routeTo(T nodeFrom, T nodeTo) {
    if (!nodes.contains(nodeFrom) || !nodes.contains(nodeTo)) {
      throw new IllegalArgumentException(
          "One or both of the provided nodes do not exist in the graph.");
    }
    return dijkstra(nodeFrom, nodeTo);
  }

  public List<Pair<T, U>> getNeighbours(T node) {
    return arcs.stream()
        .filter(arc -> arc.connects(node))
        .map(
            arc -> {
              if (arc.nodeFrom.equals(node)) {
                return new Pair<T, U>(arc.nodeTo, arc.weight);
              } else {
                return new Pair<T, U>(arc.nodeFrom, arc.weight);
              }
            })
        .collect(Collectors.toList());
  }

  private Graph<T, U> dijkstra(T nodeFrom, T nodeTo) {
    // TODO there is probably a better way to do this, than comparing Doubles
    List<T> unvisitedNodes = new ArrayList<>();
    unvisitedNodes.addAll(nodes);
    List<T> visitedNodes = new ArrayList<>();
    Map<T, Double> distances = new HashMap<>();
    distances.put(nodeFrom, 0.0);
    nodes.stream()
        .filter(n -> !n.equals(nodeFrom))
        .forEach(n -> distances.put(n, Double.POSITIVE_INFINITY));

    Map<T, Arc<T, U>> predecessors = new HashMap<>();
    while (unvisitedNodes.contains(nodeTo)) {
      T currentNode = unvisitedNodes.stream().min(Comparator.comparing(distances::get)).get();
      T finalCurrentNode = currentNode;
      double thisdist = distances.get(currentNode);
      List<Arc<T, U>> neighbours =
          arcs.stream().filter(arc -> arc.connects(finalCurrentNode)).collect(Collectors.toList());
      neighbours.forEach(
          arc -> {
            T neighbour = (arc.nodeFrom.equals(currentNode)) ? arc.nodeTo : arc.nodeFrom;
            if (visitedNodes.contains(neighbour)) return;
            double thatdist = distances.get(neighbour);
            double possdist = thisdist + arc.weight.doubleValue();
            if (possdist < thatdist) {
              distances.put(neighbour, possdist);
              predecessors.put(neighbour, arc);
            }
          });
      visitedNodes.add(currentNode);
      unvisitedNodes.remove(currentNode);
      distances.remove(currentNode);

      // System.out.println(distances.values().stream().mapToDouble(v -> v).min());
      if (distances.values().stream().mapToDouble(v -> v).min().getAsDouble()
          == Double.POSITIVE_INFINITY) {
        return null; // disconnected graph, no route
      }
    }

    List<T> nodes = new ArrayList<>();
    List<Arc<T, U>> arcs = new ArrayList<>();

    T currentNode = nodeTo;
    nodes.add(nodeTo);
    while (!currentNode.equals(nodeFrom)) {
      Arc<T, U> arc = predecessors.get(currentNode);
      arcs.add(arc);
      currentNode = (arc.nodeFrom.equals(currentNode)) ? arc.nodeTo : arc.nodeFrom;
      nodes.add(currentNode);
    }

    return new Graph<T, U>(nodes, arcs);
  }

  private static class Arc<T, U> {
    private final T nodeFrom, nodeTo;
    private final U weight;

    public Arc(T nodeFrom, T nodeTo, U weight) {
      this.nodeFrom = nodeFrom;
      this.nodeTo = nodeTo;
      this.weight = weight;
    }

    public boolean connects(T node) {
      return node.equals(nodeFrom) || node.equals(nodeTo);
    }

    public U getWeight() {
      return weight;
    }
  }
}
