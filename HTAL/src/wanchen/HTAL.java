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
import java.util.Random;

class Global {
	public static final int iterations = 1000;	// for random walk
	public static final Double maxSteps = 30.0;
	public static final int numClasses = 2;
	public static final boolean verbal = false;
}
class BipartiteGraph {
	private ExampleNodeSet example_node_set;
	private FeatureNodeSet feature_node_set;
	
	public BipartiteGraph (String f1, String f2, String f3) {
		example_node_set = new ExampleNodeSet ();
		example_node_set.InitializeSeeds(f1);
		example_node_set.InitializeTestNodes(f2);
		feature_node_set = new FeatureNodeSet (f3);	
		feature_node_set.InitializeFeatureEdges(f1, 0);
		feature_node_set.InitializeFeatureEdges(f2, example_node_set.GetSeedSize());
	}
	public int GetTestSize () {
		return example_node_set.GetTestSize();
	}
	
	public Integer ComputeAllHittingTime () {	// compute two classes hitting time for all example nodes, and return the unlabeled node who has minimum difference
		Integer most_uncertain_id = example_node_set.GetSeedSize();
		Double min_diff = Global.maxSteps;
		for (int i = example_node_set.GetSeedSize(); i < example_node_set.GetTotalSize(); i++) {
			if (example_node_set.GetExampleNode(i).IsLabeled() == false) {
				Double t0 = RandomWalkHittingTime (i, 0);
				Double t1 = RandomWalkHittingTime (i, 1);
				Double diff = Math.abs(t0 - t1);
				if (diff < min_diff) {
					min_diff = diff;
					most_uncertain_id = i;
				}
			}
		}
		return most_uncertain_id;		
	}
	
	public void LabelOneMoreExample (Integer id) {
		example_node_set.LabelOneMoreExample(id);
	}
	
	private Double RandomWalkHittingTime (Integer start_id, int target_class) {
		Double total_time = 0.0;
		for (int it = 0; it < Global.iterations; it++) {
			total_time += OneSampleHittingTime (start_id, target_class);
		}
		Double avg = total_time/Global.iterations;
		example_node_set.GetExampleNode(start_id).SetHittingTime(avg, target_class);
		return avg;
	}
	
	private Double OneSampleHittingTime (Integer start_id, int target_class) {
		Double steps = 0.0;
		boolean hit = example_node_set.GetExampleNode(start_id).IsLabeled(target_class);
		Integer current_example_id = start_id;
		Integer current_feature_id;
		while (hit == false && steps < Global.maxSteps) {
			steps ++;
			current_feature_id = example_node_set.RandomStep(current_example_id);
			current_example_id = feature_node_set.RandomStep(current_feature_id);
			hit = example_node_set.GetExampleNode(current_example_id).IsLabeled(target_class);
		}
		return steps;
	}
	
	public double Accuracy () {
		int total = example_node_set.GetTotalSize() - example_node_set.GetLabeledSize();
		int correct = example_node_set.CountCorrect();
		return (double) correct / (double) total;
	}

 	public void Print () {
		System.out.println("\nTotally "+example_node_set.GetTotalSize()+ " examples, "+example_node_set.GetLabeledSize()+" are labeled.");
		example_node_set.Print();
	}
	
 	public void PrintClassNumbers () {
 		for (int i = 0; i < Global.numClasses; i++) {
 			System.out.print("Class "+i+" has "+example_node_set.GetLabeledSize(i)+" labeled exmaples, ");
 		}
 		System.out.println();
 	}
}

class ExampleNodeSet {
	private ArrayList<ExampleNode> example_nodes;
	private int seed_size;	// From 0 to (seed_size-1) elements are seeds
	private int test_size;	// From seed_size to (seed_size + test_size -1)
	private ArrayList<Integer> labeled_ids; 	// ids of seeds + active learned examples
	
	public ExampleNodeSet () {
		seed_size = 0;
		test_size = 0;
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
		System.out.println();
	}
	public int GetLabeledSize () {
		return labeled_ids.size();
	}
	public int GetLabeledSize (int c) {
		int cnt = 0;
		for (Integer i:labeled_ids) {
			if (example_nodes.get(i).IsLabeled(c)) {
				cnt ++;
			}
		}
		return cnt;
	}
	public int GetTotalSize () {
		return example_nodes.size();
	}
 	public int GetSeedSize () {
 		return seed_size;
 	}
	public int GetTestSize () {
		return test_size;
	}

 	public ExampleNode GetExampleNode (int id) {
		return example_nodes.get(id);
	}
	
	public void LabelOneMoreExample (Integer selected_id) {
		if (Global.verbal) {
			System.out.println("Label exmaple "+selected_id);
//--			example_nodes.get(selected_id).Print();
		}
		example_nodes.get(selected_id).SetIsLabeled();
		labeled_ids.add(selected_id);
	}

	public int CountCorrect () {
		int cnt = 0;
		for (int i = seed_size; i < this.GetTotalSize(); i++) {
	//--		if (Global.verbal)	System.out.println();
			if (example_nodes.get(i).IsLabeled() == false) {
				if (example_nodes.get(i).PredictionIsCorrect()) {
					cnt ++;
			//--		if (Global.verbal)	System.out.print(i+ " ");
				}
			}
		//--	if (Global.verbal)	System.out.println();
		}
		return cnt;
	}
	
	public Integer RandomStep (Integer id) {
		return example_nodes.get(id).RandomStep();
	}
}
class ExampleNode {
	private int true_label;
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
	public Integer RandomStep () {
		Random rand = new Random ();
		return feature_nodes.get(rand.nextInt(feature_nodes.size()));
	}
	public boolean IsLabeled () {
		return is_labeled;
	}
	public boolean IsLabeled (int c) {
		if (is_labeled && true_label == c)
			return true;
		else
			return false;
	}
	public int Degree () {
		return feature_nodes.size();
	}
	public void SetIsLabeled () {
		is_labeled = true;
	}
	public void SetHittingTime (Double t, int c) {
		hitting_times.set(c, t);
	}
	public boolean PredictionIsCorrect () {
		int pred = 0;
		Double min_hitting_time = hitting_times.get(0);
		for (int i = 0; i < Global.numClasses; i++) {
			if (hitting_times.get(i) < min_hitting_time) {
				pred = i;
				min_hitting_time = hitting_times.get(i);
			}
		}
		return (pred == true_label);			
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
			int id = 0;
			while (true) {
				String Line = File.readLine();	// format: fid<tab>fname
				if (Line == null)
					break;
				feature_nodes.add(new FeatureNode(id, Line));
				id ++;
			}
			File.close();			
		} catch (IOException e) {
			e.printStackTrace();
            System.exit(1);
        }	
	}
	public void InitializeFeatureEdges (String filename, int base_id) {
		BufferedReader File = null;
		try {
			File = new BufferedReader(new FileReader (filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
        }	
		try {
			int id = base_id;
			while (true) {
				String exampleLine = File.readLine();
				if (exampleLine == null)
					break;
				String[] tokens = exampleLine.split("\\s+");
				for (int i = 1; i < tokens.length; i++) {
					feature_nodes.get(Integer.parseInt(tokens[i])).AddExampleNode(id);
				}
				id ++;
			}
			File.close();			
		} catch (IOException e) {
			e.printStackTrace();
            System.exit(1);
        }	
	}	
	public Integer RandomStep (Integer id) {
		return feature_nodes.get(id).RandomStep();
	}
	public FeatureNode GetFeatureNode (int id) {
		return feature_nodes.get(id);
	}
}
class FeatureNode {
	private ArrayList<Integer> example_nodes;
	private Integer id;
	public FeatureNode (Integer ii) {
		id = ii;
		example_nodes = new ArrayList<Integer> ();		
	}
	public FeatureNode (Integer ii, String str) {
		this(ii);
	}

	public void AddExampleNode (Integer ii) {
		example_nodes.add(ii);
	}	
	public Integer RandomStep () {
		Random rand = new Random ();
		return example_nodes.get(rand.nextInt(example_nodes.size()));
	}

	public int Degree () {
		return example_nodes.size();
	}
}

public class HTAL {
	public static void main(String[] args) {
	// arguments: 
	// seed file, test file, feature file, number of addition examples to label	
	
		BipartiteGraph graph = new BipartiteGraph (args[0], args[1], args[2]);
		
		int al_size = graph.GetTestSize() / 5;
		if (args.length >=4	) {
			if (Integer.parseInt(args[3]) < graph.GetTestSize() )
				al_size = Integer.parseInt(args[3]);
		}
		
		if (Global.verbal)	graph.Print();
		
		for (int i = 0; i < al_size; i++) {
			Integer next_to_label = graph.ComputeAllHittingTime();
			graph.LabelOneMoreExample(next_to_label);
			System.out.println(" Accuracy: "+graph.Accuracy());
			graph.PrintClassNumbers();
			if (Global.verbal)	graph.Print();
		}		
		graph.Print();
	}
	
}
