import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import utils.FileFinder;

//this script generate the confusion matrix for the given data for further error analysis 
public class ErrorAnalysis {
	public static String splitmark = "	";
	public static HashMap<String, Integer> class_number = new HashMap<String,Integer>();
	public static int classes = 0;
	public static int[][] cm = new int[36][36];  
	

	public static void main(String args[]) throws Exception{
		String txtPath = args[0];

		ArrayList<File> files = FileFinder.GetAllFiles(txtPath, ".txt", true);
		for (int i = 0; i < files.size(); i++){
			File f = files.get(i);
			execute(f,i);
		}
		
		printResult();
	}
	
	private static void printResult() {
		Iterator<Entry<String, Integer>> iter = class_number.entrySet().iterator(); 
		while (iter.hasNext()) { 
		    Entry<String, Integer> entry = iter.next(); 
		    String key = entry.getKey(); 
		    int val = entry.getValue(); 
		 
		    System.out.println("\t"+key + "\t: " + val);
		}
		
		for (int j = 0; j < classes; j++){
			System.out.print("\t" + j);
		}
		System.out.println();
		
		
		for(int i = 0; i < classes; i++){
			for (int j = 0; j < classes; j++){
				System.out.print("\t" + cm[i][j]);
			}
			System.out.println();
		}
		
	}

	private static void execute(File f, int i) throws Exception {
		int column = 0; 
		int row = 0;
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = null;
		System.out.println("***********" + f.getName());
		while ((line = br.readLine()) != null) {
			String[] split = line.split(splitmark);
			if (split.length < 2) continue;
			
			String annotated = split[split.length-2];
			String predicted = split[split.length-1];
			
			if(class_number.containsKey(annotated)){row = class_number.get(annotated);}
			else {
				class_number.put(annotated, classes);
				row = classes;
				classes += 1;
			}
			
			if(class_number.containsKey(predicted)){column = class_number.get(predicted);}
			else {
				class_number.put(predicted, classes);
				column = classes;
				classes += 1;
			}
			
			cm[row][column] += 1;
		}
		br.close();		
		
	}
}
