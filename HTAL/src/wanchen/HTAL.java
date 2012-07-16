package wanchen;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Global {
	public static final int sampleSize = 300;
	public static final Double maxSteps = 10.0;
	public static final int default_al_size = 10;
	public static final int numClasses = 2;
}
class BipartiteGraph {
	private ExampleNodeSet example_node_set;
	private FeatureNodeSet feature_node_set;
	
	public BipartiteGraph (String f1, String f2, String f3) {
		example_node_set = new ExampleNodeSet ();
		example_node_set.InitializeSeeds(f1);
		example_node_set.InitializeTestNodes(f2);
		feature_node_set = new FeatureNodeSet (f3);		
	}
	public void Print () {
		example_node_set.Print();
	}
	
}

class ExampleNodeSet {
	private ArrayList<ExampleNode> example_nodes;
	private int seed_size;	// From 0 to (seed_size-1) elements are seeds
	private int test_size;	// From seed_size to (seed_size + test_size -1)
	private int active_learning_size; // how many additional labels are allowed
	private ArrayList<Integer> labeled_ids; 	// ids of seeds + active learned examples
	
	public ExampleNodeSet () {
		this(Global.default_al_size);
	}
	public ExampleNodeSet (int al_size) {
		seed_size = 0;
		test_size = 0;
		active_learning_size = al_size;
		example_nodes = new ArrayList<ExampleNode> ();
		labeled_ids = new ArrayList<Integer> ();
	}
	// must be called immediately after constructor
	public void InitializeSeeds (String filename) {
		BufferedReader File = null;
		try {
			File = new BufferedReader(new FileReader (filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
        }	
		try {
			int id = 0;
			while (true) {
				String exampleLine = File.readLine();
				if (exampleLine == null)
					break;
				example_nodes.add(new ExampleNode(id, exampleLine, true));
				labeled_ids.add(id);
				id ++;
				seed_size ++;
			}
			File.close();			
		} catch (IOException e) {
			e.printStackTrace();
            System.exit(1);
        }	
	}	
	// must be called  after InitializeSeeds
	public void InitializeTestNodes (String filename) {
		BufferedReader File = null;
		try {
			File = new BufferedReader(new FileReader (filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
        }	
		try {
			int id = this.seed_size;	// id of first test node is seed_size
			while (true) {
				String exampleLine = File.readLine();
				if (exampleLine == null)
					break;
				example_nodes.add(new ExampleNode(id, exampleLine));
				id ++;
				test_size ++;
			}
			File.close();			
		} catch (IOException e) {
			e.printStackTrace();
            System.exit(1);
        }	
	}
	public void Print () {
		for (ExampleNode n:example_nodes) {
			n.Print();
		}
	}
}
class ExampleNode {
	private int true_label;
	private int prediction = 0;
	private boolean is_labeled;	// if is seed, or active learned
	private ArrayList<Double> hitting_times; 	// hitting_times.get(i) is the hitting time for class i
	private List<Integer> feature_nodes;
	private String fstring;
	private Integer id;
	
	public ExampleNode (Integer ii, String str) {
		this(ii, str, false);
	}
	public ExampleNode (Integer ii, String str, boolean labeled) {
		id = ii;
		is_labeled = labeled;
		fstring = str;
		feature_nodes = new ArrayList<Integer> ();
		String[] tokens = str.split("\\s+");
		true_label = Integer.parseInt(tokens[0]);
		for (int i = 1; i < tokens.length; i++){
			feature_nodes.add(Integer.parseInt(tokens[i]));			
		}
		hitting_times = new ArrayList<Double> ();
		for (int i = 0 ; i < Global.numClasses; i++) {
			if (is_labeled)
				hitting_times.add(0.0);
			else
				hitting_times.add(Global.maxSteps);
		}
	}	
	public int Degree () {
		return feature_nodes.size();
	}
	public void SetIsLabeled () {
		is_labeled = true;
	}
	public void Print() {
		if (is_labeled) 
			System.out.println(id+"\t#"+fstring);
		else
			System.out.println(id+"\t "+fstring+"\tC0="+hitting_times.get(0)+"\tC1="+hitting_times.get(1));
	}
}
class FeatureNodeSet {
	private ArrayList<FeatureNode> feature_nodes;
	
	public FeatureNodeSet (String filename) {
		feature_nodes = new ArrayList<FeatureNode> ();
		BufferedReader File = null;
		try {
			File = new BufferedReader(new FileReader (filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
        }	
		try {
			while (true) {
				String Line = File.readLine();	// format: fid<tab>fname
				if (Line == null)
					break;
				feature_nodes.add(new FeatureNode(Line));
			}
			File.close();			
		} catch (IOException e) {
			e.printStackTrace();
            System.exit(1);
        }	
	}
	 
}
class FeatureNode {
	private ArrayList<Integer> example_nodes;
	private Integer id;
	
	public FeatureNode (Integer ii) {
		id = ii;
		example_nodes = new ArrayList<Integer> ();
	}
	public FeatureNode (String str) {
		String[] tokens = str.split("\\s+");
		id = Integer.parseInt(tokens[0]);
		example_nodes = new ArrayList<Integer> ();
	}
	public void AddExampleNode (Integer ii) {
		example_nodes.add(ii);
	}	
	public int Degree () {
		return example_nodes.size();
	}
}

public class HTAL {
	public static void main(String[] args) {
		BipartiteGraph graph = new BipartiteGraph (args[0], args[1], args[2]);
		graph.Print();
	}
}
