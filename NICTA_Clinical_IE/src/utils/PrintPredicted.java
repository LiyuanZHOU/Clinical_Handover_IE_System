package utils;import java.io.BufferedReader;import java.io.File;import java.io.FileReader;import java.util.ArrayList;import utils.FileFinder;public class PrintPredicted {	public static String splitmark = "	";	public static String txtPath = "";//	public static String txtPath = "experiments log files/22 Apr 2013 10:30:28 PM/predicted";	public static String label = "";	public static void main(String[] args) throws Exception{				txtPath = args[0];		label = args[1];				txtPath = "text/CRF/predicted";		ArrayList<File> files = FileFinder.GetAllFiles(txtPath, ".txt", true);		int count = 0;		for(File f : files){			@SuppressWarnings("resource")			BufferedReader br = new BufferedReader(new FileReader(f));			String line = null;						while ((line = br.readLine()) != null) {				String[] split = line.split(splitmark);				if (split.length < 2) continue;				String word = split[0];								String annotated = split[split.length-2];				String predicted = split[split.length-1];								if(annotated.equals(label))				{					System.out.println(f.getName());					count++;					if(!annotated.equals(predicted)) {//						System.out.println(f.getName());												System.out.println(count + ": " + word + splitmark + annotated +splitmark + predicted);						System.out.println("==================================================================");					}					else System.out.println(count + ": " + word + splitmark + annotated +splitmark + predicted);				}		}		}				}	@SuppressWarnings("unused")	private static void showHelp() {		System.out.println("PrintPredicted: this is a simple script to print out the correct lable and predicted lable for each word");		System.out.println("Usage:");		System.out.println("PrintPredicted [inputFilesFolder] [printLabel]");			}}	