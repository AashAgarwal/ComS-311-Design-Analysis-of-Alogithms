/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author agarwal
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Vector;

public class WGraph {
	class Point {
		int x;
		int y;
		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Point other = (Point) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
	}
	
	class Edge{
		@Override
		public String toString() {
			return "Edge [src=" + src + ", dest=" + dest + ", weight=" + weight + "]";
		}
		int src;
		int dest;
		int weight;
		Edge(int src, int dest, int weight) {
			this.src = src;
			this.dest = dest;
			this.weight = weight;
		}
	}
	
	ArrayList<Point> points = new ArrayList<>();
	ArrayList<ArrayList<Edge>> edges = new ArrayList<>();
	ArrayList<ArrayList<Edge>> pre_edges = new ArrayList<>();
	Map<Point, Integer> mp_point = new HashMap<>();
	int cnt_point = 0;
	int src_point = 0;
	int dest_point = 1;
	
	private void addEdge(int src, int dest, int weight) {
		Edge edge = new Edge(src, dest, weight);
		if(src == src_point || dest == dest_point) {
			pre_edges.get(src).add(edge);
		}
		else {
			edges.get(src).add(edge);
		}
	}
	
	private void initialize() {
		pre_edges = new ArrayList<>();
		for(int i = 0; i < cnt_point; i ++) {
			ArrayList<Edge> pre_edgeArrayList = new ArrayList<>();
			ArrayList<Edge> edgeArrayList = edges.get(i);
			for(int j = 0; j < edgeArrayList.size(); j ++) {
				pre_edgeArrayList.add(edgeArrayList.get(j));
			}
			pre_edges.add(pre_edgeArrayList);
		}
	}
	
	private ArrayList<Integer> findRoute() {
		ArrayList<Integer> answer = new ArrayList<>();
		int src_id = src_point;
		int dest_id = dest_point;
		
		int[] weights = new int[cnt_point];
		int[] parent = new int[cnt_point];
		boolean[] isVisit = new boolean[cnt_point];
		int inf = -1;
		for(int i = 0; i < cnt_point; i ++)  {
			isVisit[i] = false;
			weights[i] = 0;
		}
		weights[src_id] = 0;
		isVisit[src_id] = true;
		parent[src_id] = src_id;
		
		PriorityQueue<Edge> q = new PriorityQueue<Edge>(new Comparator<Edge>(){
			public int compare(Edge a, Edge b) {
				if(a.weight != b.weight) {
					if(a.weight < b.weight) {
						return -1;
					}
					return 1;
				}
				return 0;
			}
        });
		
		q.add(new Edge(src_id, src_id, 0));
		while(!q.isEmpty()) {
			Edge edge = q.poll();
			int node = edge.dest;
			int wt = edge.weight;
			if(weights[node] < wt) continue;
			
			ArrayList<Edge> edgeArrayList = pre_edges.get(node);
			for(int i = 0; i < edgeArrayList.size(); i ++) {
				edge = edgeArrayList.get(i);
				
				int new_weight = wt + edge.weight;
				
				int dest = edge.dest;
				if(isVisit[dest] == true && weights[dest] <= new_weight) continue;
				isVisit[dest] = true;
				weights[dest] = new_weight;
				parent[dest] = node;
				q.add(new Edge(src_id, dest, new_weight));
			}
		}
		for(int i = 0; i < cnt_point; i ++) {
			System.out.print(weights[i] + " ");
			System.out.println(isVisit[i]);
		}
		
		if(isVisit[dest_id] == false) {
			System.out.println("Not connected");
			return answer;
		}
		
		ArrayList<Integer> pre_answer = new ArrayList<Integer>();
		while(true) {
			pre_answer.add(dest_id);
			if(dest_id == src_id) {
				break;
			}
			dest_id = parent[dest_id];
		}
		for(int i = pre_answer.size() - 2; i > 0; i --) {
			int id = pre_answer.get(i);
			answer.add(points.get(id).x);
			answer.add(points.get(id).y);
		}
		
		return answer;
	}
	private void addPoint(Point point) {
		mp_point.put(point,  cnt_point);
		ArrayList<Edge> edge = new ArrayList<>();
		edges.add(edge);
		points.add(point);
		cnt_point ++;
	}
	
	public ArrayList<Integer> V2V(int ux, int uy, int vx, int vy) {
		initialize();

		ArrayList<Integer> answer = new ArrayList<>();
		Point src = new Point(ux, uy);
		Point dest = new Point(vx, vy);
		Integer src_id = mp_point.get(src);
		Integer dest_id = mp_point.get(dest);
		if(src_id == null || dest_id == null) {
			return answer;
		}
		addEdge(src_point, src_id, 0);
		addEdge(dest_id, dest_point, 0);
		for(int i = 0; i < pre_edges.size(); i ++) {
			for(int j = 0; j < pre_edges.get(i).size(); j ++) {
				System.out.print(pre_edges.get(i).get(j) + " ");
			}
			System.out.println();
		}
		return findRoute();
	}
	
	public ArrayList<Integer> V2S(int ux, int uy, ArrayList<Integer> S) {
		initialize();
		ArrayList<Integer> answer = new ArrayList<>();
		Point src = new Point(ux, uy);
		Integer src_id = mp_point.get(src);
		if(src_id == null) {
			return answer;
		}
		addEdge(src_point, src_id, 0);
		for(int i = 0; i < S.size(); i += 2) {
			Point dest = new Point(S.get(i), S.get(i + 1));
			Integer dest_id = mp_point.get(dest);
			if(dest_id == null) {
				continue;
			}
			addEdge(dest_id, dest_point, 0);
		}
		return findRoute();
		
	}
	
	
	
	public ArrayList<Integer> S2S(ArrayList<Integer> S1, ArrayList<Integer> S2) {
		initialize();
		for(int i = 0; i < S1.size(); i += 2) {
			Point src = new Point(S1.get(i), S1.get(i + 1));
			Integer src_id = mp_point.get(src);
			if(src_id == null) {
				continue;
			}
			addEdge(src_point, src_id, 0);
		}
		for(int i = 0; i < S2.size(); i += 2) {
			Point dest = new Point(S2.get(i), S2.get(i + 1));
			Integer dest_id = mp_point.get(dest);
			if(dest_id == null) {
				continue;
			}
			addEdge(dest_id, dest_point, 0);
		}
		return findRoute();
	}
	private void input(String name) {
		try {
			File file = new File(name); 
		    Scanner sc = new Scanner(file);
		    int count = sc.nextInt();
		    int count_edge = sc.nextInt();
		    int[] edgeInfo = new int[5];
		    for(int i = 0; i < count_edge; i ++) {
		    	for(int j = 0; j < 5; j ++) {
		    		edgeInfo[j] = sc.nextInt();
		    	}
		    	Point point = new Point(edgeInfo[0], edgeInfo[1]);
		    	Integer src = mp_point.get(point);
		    	if(src == null) {
		    		src = cnt_point;
		    		addPoint(point);
		    	}
		    	Point dest_point = new Point(edgeInfo[2], edgeInfo[3]);
		    	Integer dest = mp_point.get(dest_point);
		    	if(dest == null) {
		    		dest = cnt_point;
		    		addPoint(dest_point);
		    	}
		    	addEdge(src.intValue(), dest.intValue(), edgeInfo[4]);
		    }
		    sc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	WGraph(String name) {
		int inf = 1000000;
		addPoint(new Point(inf, inf));
		addPoint(new Point(-inf, -inf));
		input(name);
	}
	
}
