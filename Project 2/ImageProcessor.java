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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

public class ImageProcessor {

	class Color {
		int r;
		int g;
		int b;
		Color(int r, int g, int b) {
			this.r = r;
			this.g= g;
			this.b = b;
		}
	}

	class Edge{
		
		int dest;
		int weight;
		Edge(int dest, int weight) {
			this.dest = dest;
			this.weight = weight;
		}
	}
	
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
	ArrayList<ArrayList<Color>> color_info = new ArrayList<>();
	ArrayList<ArrayList<Color>> main_color_info = new ArrayList<>();
	ArrayList<Point> points = new ArrayList<>();
	ArrayList<ArrayList<Edge>> edges = new ArrayList<>();
	ArrayList<ArrayList<Edge>> pre_edges = new ArrayList<>();
	Map<Point, Integer> mp_point = new HashMap<>();
	int cnt_point = 0;
	int cnt_row = 0;
	int cnt_col = 0;
	int main_cnt_row = 0;
	int main_cnt_col = 0;
	
	ArrayList<ArrayList<Integer>> importances;
	
	private void addPoint(Point point) {
		mp_point.put(point,  cnt_point);
		ArrayList<Edge> edge = new ArrayList<>();
		edges.add(edge);
		points.add(point);
		cnt_point ++;
	}
	
	private void addEdge(int src, int dest, int weight) {
		Edge edge = new Edge(dest, weight);
		edges.get(src).add(edge);
	}
	
	private int getDistance(Color a, Color b) {
		int ans = (a.r - b.r) * (a.r - b.r) + (a.g - b.g) * (a.g - b.g) + (a.b - b.b) * (a.b - b.b);
		return ans;
	}
	
	private int yImportant(int i, int j) {
		int prev = (i - 1 + cnt_row) % cnt_row;
		int nex = (i + 1) % cnt_row;
		return getDistance(color_info.get(prev).get(j), color_info.get(nex).get(j));
	}
	private int yImportantForReduction(int i, int j) {
		int prev = (i - 1 + main_cnt_row) % main_cnt_row;
		int nex = (i + 1) % main_cnt_row;
		return getDistance(main_color_info.get(prev).get(j), main_color_info.get(nex).get(j));
	}
	private int xImportant(int i, int j) {
		int prev = (j - 1 + cnt_col) % cnt_col;
		int nex = (j + 1) % cnt_col;
		return getDistance(color_info.get(i).get(prev), color_info.get(i).get(nex));
	}
	private int xImportantForReduction(int i, int j) {
		int prev = (j - 1 + main_cnt_col) % main_cnt_col;
		int nex = (j + 1) % main_cnt_col;
		return getDistance(main_color_info.get(i).get(prev), main_color_info.get(i).get(nex));
	}
	
	private int getImportant(int i, int j) {
		return xImportant(i, j) + yImportant(i, j);
	}
	private int getImportantForReduction(int i, int j) {
		return xImportantForReduction(i, j) + yImportantForReduction(i, j);
	}
	
	public ArrayList<ArrayList<Integer>> getImportance() {
		ArrayList<ArrayList<Integer>> answer = new ArrayList<>();
		for(int i = 0; i < cnt_row; i ++) {
			ArrayList<Integer> rowInfo = new ArrayList<>();
			for(int j = 0; j < cnt_col; j ++) {
				rowInfo.add(getImportant(i, j));
			}
			answer.add(rowInfo);
		}
		return answer;
	}
	
	public ArrayList<ArrayList<Integer>> getImportanceForReduction() {
		ArrayList<ArrayList<Integer>> answer = new ArrayList<>();
		for(int i = 0; i < main_cnt_row; i ++) {
			ArrayList<Integer> rowInfo = new ArrayList<>();
			for(int j = 0; j < main_cnt_col; j ++) {
				rowInfo.add(getImportantForReduction(i, j));
			}
			answer.add(rowInfo);
		}
		return answer;
	}
	
	private void initialize() {
		importances = getImportanceForReduction();
	    cnt_point = 0;
	    mp_point = new HashMap<>();
	    edges = new ArrayList<>();
	    Point src = new Point(0, -1);
	    points = new ArrayList<>();
	    addPoint(src);
	    for(int i = 0; i < main_cnt_row; i ++) for(int j = 0; j < main_cnt_col; j ++){
	    	Point point = new Point(i, j);
	    	addPoint(point);
	    }
	    Point dest = new Point(main_cnt_row, main_cnt_col);
	    addPoint(dest);
	}
	
	private ArrayList<Integer> findRoute(int src_id, int dest_id) {
		ArrayList<Integer> answer = new ArrayList<>();
		
		int[] weights = new int[cnt_point];
		int[] parent = new int[cnt_point];
		boolean[] isVisit = new boolean[cnt_point];
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
		
		q.add(new Edge(src_id, 0));
		while(!q.isEmpty()) {
			Edge edge = q.poll();
			int node = edge.dest;
			int wt = edge.weight;
			if(weights[node] < wt) continue;
			
			ArrayList<Edge> edgeArrayList = edges.get(node);
			for(int i = 0; i < edgeArrayList.size(); i ++) {
				edge = edgeArrayList.get(i);
				
				int new_weight = wt + edge.weight;
				
				int dest = edge.dest;
				if(isVisit[dest] == true && weights[dest] <= new_weight) continue;
				isVisit[dest] = true;
				weights[dest] = new_weight;
				parent[dest] = node;
				q.add(new Edge(dest, new_weight));
			}
		}
		
		if(isVisit[dest_id] == false) {
			System.out.println("Not connected");
			return answer;
		}
		
		while(true) {
			answer.add(dest_id);
			if(dest_id == src_id) {
				break;
			}
			dest_id = parent[dest_id];
		}
		return answer;
	}
	
	
	
	
	private void verticalCut() {
	    int src_id = 0;
	    int dest_id = cnt_point - 1;
	    for(int i = 0; i < main_cnt_col; i ++) {
	    	Point point = new Point(0, i);
	    	int id = mp_point.get(point);
	    	addEdge(src_id, id, importances.get(0).get(i));
	    	Point last_point = new Point(main_cnt_row - 1, i);
	    	int last_id = mp_point.get(last_point);
	    	addEdge(last_id, dest_id, 0);
	    }
	    for(int i = 0; i < main_cnt_row - 1; i ++) {
	    	for(int j = 0; j < main_cnt_col; j ++) {
	    		Point point = new Point(i, j);
	    		int id = mp_point.get(point);
	    		Point downPoint = new Point(i + 1, j);
	    		int down_id = mp_point.get(downPoint);
	    		addEdge(id, down_id, importances.get(i + 1).get(j));
	    		
	    		if(j > 0) {
	    			Point prevPoint = new Point(i + 1, j - 1);
		    		int prev_id = mp_point.get(prevPoint);
		    		addEdge(id, prev_id, importances.get(i + 1).get(j - 1));
	    		}
	    		if(j < main_cnt_col - 1) {
	    			Point nexPoint = new Point(i + 1, j + 1);
		    		int next_id = mp_point.get(nexPoint);
		    		addEdge(id, next_id, importances.get(i + 1).get(j + 1));
	    		}
	    	}
	    }
	    
	    ArrayList<Integer> answer = findRoute(src_id, dest_id);
	    
	    for(int i = 1; i < answer.size() - 1; i ++) {
	    	Point point = points.get(answer.get(i));
	    	int x = point.x;
	    	int y = point.y;
	    	main_color_info.get(x).remove(y);
	    }
	    main_cnt_col --;
	}
	
	
	
	
	
	public void writeReduced(int k, String outName) {
		main_cnt_row = cnt_row;
		main_cnt_col = cnt_col;
		main_color_info = new  ArrayList<>();
		for(ArrayList<Color> colorList: color_info) {
			ArrayList<Color> main_row = new ArrayList<>();
			for(Color color:colorList) {
				main_row.add(color);
			}
			main_color_info.add(main_row);
	    	
	    }
		for(int i = 0; i < k; i ++) {
			initialize();
			verticalCut();
		}
		try {
			FileWriter fileWriter = new FileWriter(outName);
		    PrintWriter printWriter = new PrintWriter(fileWriter);
		    printWriter.println("" + main_cnt_row);
		    printWriter.println("" + main_cnt_col);
		    
		    for(int i = 0; i < main_cnt_row; i ++) {
		    	String answer = "";
		    	for(int j = 0; j < main_cnt_col; j ++) {
		    		if(j > 0) answer = answer + " ";
		    		Color color = main_color_info.get(i).get(j);
		    		answer = answer + color.r + " " + color.g + " " + color.b;
		    	}
		    	
		    	printWriter.println(answer);
		    }
		    printWriter.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	ImageProcessor(String name) {
		try {
			File file = new File(name); 
		    Scanner sc = new Scanner(file);
		    cnt_row = sc.nextInt();
		    cnt_col = sc.nextInt();
		    main_cnt_col = cnt_col;
		    main_cnt_row = cnt_row;
		    color_info = new ArrayList<>();
		    main_color_info = new ArrayList<>();
		    for(int i = 0; i < cnt_row; i ++) {
		    	ArrayList<Color> row = new ArrayList<>();
		    	ArrayList<Color> main_row = new ArrayList<>();
		    	for(int j = 0; j < cnt_col; j ++) {
		    		Color color = new Color(sc.nextInt(), sc.nextInt(), sc.nextInt());
		    		row.add(color);
		    		main_row.add(color);
		    	}
		    	color_info.add(row);
		    	main_color_info.add(main_row);
		    }
		    sc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
