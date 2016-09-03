/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim.routing;

//import java.util.ArrayList;
//import java.util.Arrays;

/*
 * Floyd-Warshall all-pairs shortest path algorithm
 * Modified implementation from http://algowiki.net/wiki/index.php/Floyd-Warshall%27s_algorithm
 */
public class ShortestPath {

   /*private int[][]    dist;
   private Vertex[][] P;
   
   public ShortestPath(Vertex[] vertices, Edge[] edges) {
	   calcShortestPaths(vertices, edges);
   }

   private void calcShortestPaths(Vertex[] vertices, Edge[] edges) {
       dist = initializeWeight(vertices.length, edges);
       P = new Vertex[vertices.length][vertices.length];

       for(int k=0; k<vertices.length; k++){
           for(int i=0; i<vertices.length; i++){
               for(int j=0; j<vertices.length; j++){
                   if(dist[i][k] != Integer.MAX_VALUE && dist[k][j] != Integer.MAX_VALUE 
                		   && dist[i][k]+dist[k][j] < dist[i][j]) {
                       dist[i][j] = dist[i][k]+dist[k][j];
                       P[i][j] = vertices[k];
                   }
               }
           }
       }
   }

   public int getOutputPort(Vertex source, Vertex target) {
	   ArrayList<Vertex> path = getShortestPath(source, target);
	   return source.getOutputPort(path.get(1).getId());
   }
   
   public int getShortestDistance(Vertex source, Vertex target){
       return dist[source.getId()][target.getId()];
   }

   public ArrayList<Vertex> getShortestPath(Vertex source, Vertex target){
       if(dist[source.getId()][target.getId()] == Integer.MAX_VALUE)
           return new ArrayList<Vertex>();
       ArrayList<Vertex> path = getIntermediatePath(source, target);
       path.add(0, source);
       path.add(target);
       return path;
   }

   private ArrayList<Vertex> getIntermediatePath(Vertex source, Vertex target){
       if(dist == null)
           throw new IllegalArgumentException("Must call calcShortestPaths(...) before attempting to obtain a path.");
       if(P[source.getId()][target.getId()] == null)
           return new ArrayList<Vertex>();
       ArrayList<Vertex> path = new ArrayList<Vertex>();
       path.addAll(getIntermediatePath(source, P[source.getId()][target.getId()]));
       path.add(P[source.getId()][target.getId()]);
       path.addAll(getIntermediatePath(P[source.getId()][target.getId()], target));
       return path;
   }

   private int[][] initializeWeight(int numNodes, Edge[] edges){
       int[][] Weight = new int[numNodes][numNodes];
       for(int i=0; i<numNodes; i++)
           Arrays.fill(Weight[i], Integer.MAX_VALUE);
       for(Edge e : edges)
           Weight[e.getFromId()][e.getToId()] = e.getWeight();
       return Weight;
   }*/
}