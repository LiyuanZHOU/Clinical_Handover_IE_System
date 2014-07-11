import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.FileFinder;

public class icccoStats {
	
	String inputFolder;
	int totalNumber;
	
	//to count the total number of instances
	static int I = 0;
	static int C1 = 0;
	static int C2 = 0;
	static int C3 = 0;
	static int O = 0;
	
	//to count the total number of words of each class
	static int It = 0;
	static int C1t = 0;
	static int C2t = 0;
	static int C3t = 0;
	static int Ot = 0;
	
	public icccoStats(String string) {
		inputFolder = string;
	}

	public static void main(String args[]) throws IOException, XPathExpressionException, Exception {
		
		icccoStats stats = new icccoStats(args[0]);
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		ArrayList<File> files = FileFinder.GetAllFiles(stats.inputFolder,
				".xml", true);
		stats.totalNumber = files.size();
		
		int spans_min = 10000;
		int spans_max = 0;
		int spans_total = 0;
		double spans_mean = 0;
		double spans_sd = 0;
		
		//store the total number of spans in each document
		int[] doc_class = new int[stats.totalNumber];
		
		//store word length of each span in different classes
		ArrayList<Integer> Il = new ArrayList<Integer>();
		ArrayList<Integer> C1l = new ArrayList<Integer>();
		ArrayList<Integer> C2l = new ArrayList<Integer>();
		ArrayList<Integer> C3l = new ArrayList<Integer>();
		ArrayList<Integer> Ol = new ArrayList<Integer>();
		
		int I_length_min = 1000;
		int I_length_max = 0;
		int C1_length_min = 1000;
		int C1_length_max = 0;
		int C2_length_min = 1000;
		int C2_length_max = 0;
		int C3_length_min = 1000;
		int C3_length_max = 0;
		int O_length_min = 1000;
		int O_length_max = 0;
		
		//to store how many reports address all 5 class - 0
		int[] Address = new int[6];
		
		for (int i = 0; i < stats.totalNumber; i++) {
			File f = files.get(i);
//			System.out.println("fileName: " + f.getName());
		
			Document doc = builder.parse(f);
		
			XPathExpression expr = xpath.compile("//annotation");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList annotations = (NodeList) result;
			
			doc_class[i] = annotations.getLength();
			spans_total += doc_class[i];
			if(doc_class[i] > spans_max) spans_max = doc_class[i];
			if(doc_class[i] < spans_min) spans_min = doc_class[i];
			
			int If = 0;
			int C1f = 0;
			int C2f = 0;
			int C3f = 0;
			int Of = 0;
		
			for (int m = 0; m < doc_class[i]; m++) {
				Node annotation = annotations.item(m);
				
				String spannedText = annotation.getChildNodes().item(7).getTextContent();
//				System.out.println("spannedText: " + spannedText);
				
				String id = annotation.getChildNodes().item(1).getAttributes().getNamedItem("id").getNodeValue();
//				System.out.println("id: " + id);
				
				expr = xpath.compile("//classMention[@id='" + id
						+ "']" + "/mentionClass");
				result = expr.evaluate(doc, XPathConstants.NODESET);
				NodeList node = (NodeList) result;
				String topic = node.item(0).getTextContent();
				
				int words = spannedText.split(" ").length;
//				System.out.println("number of words: " + words);
				
				if(topic.equals("Iccco: Identification of the patient")) {
					I++;
					It += words;
					Il.add(words);
					if (words > I_length_max) I_length_max = words;
					if (words < I_length_min) I_length_min = words;
					If = 1;
				}
				else if (topic.equals("iCcco: Clinical presentation/history")) {
					C1++;
					C1t += words;
					C1l.add(words);
					if (words > C1_length_max) C1_length_max = words;
					if (words < C1_length_min) C1_length_min = words;
					C1f = 1;
					
				}
				else if (topic.equals("icCco: Clinical status")) {
					C2++;
					C2t += words;
					C2l.add(words);
					if (words > C2_length_max) C2_length_max = words;
					if (words < C2_length_min) C2_length_min = words;
					C2f = 1;
				}
				else if (topic.equals("iccCo: Care plan")) {
					C3++;
					C3t += words;
					C3l.add(words);
					if (words > C3_length_max) C3_length_max = words;
					if (words < C3_length_min) C3_length_min = words;
					C3f = 1;
				}
				else if (topic.equals("icccO: Outcomes and goals of care")) {
					O++;
					Ot += words;
					Ol.add(words);
					if (words > O_length_max) O_length_max = words;
					if (words < O_length_min) O_length_min = words;
					Of = 1;
				}
				else System.out.println("妈的你玩我");
				
//				System.out.println("topic: " + topic);
			}
			
			int num = If + C1f + C2f + C3f + Of;
			Address[num] ++;
			
		}
		
		spans_mean = spans_total * 1.0 / stats.totalNumber;
		
		System.out.println("total I: " + I);
		System.out.println("total C1: " + C1);
		System.out.println("total C2: " + C2);
		System.out.println("total C3: " + C3);
		System.out.println("total O: " + O);
		System.out.println();
		System.out.println("min number of spans in a report: " + spans_min);
		System.out.println("max number of spans in a report: " + spans_max);
		System.out.println("mean for number of spans in a report: " + spans_mean);
		
		//loop for documents to calculate the min 
		double sum = 0;		
		
		for (int i = 0; i < stats.totalNumber; i++) {
			sum += Math.pow(doc_class[i] - spans_mean, 2);
		}
		
		spans_sd = Math.sqrt(sum / (stats.totalNumber - 1.0));
		
		System.out.println("standard deviation for number of spans in a report: " + spans_sd);
		
		double I_length_mean = It * 1.0 / I;
		double C1_length_mean = C1t * 1.0 / C1;
		double C2_length_mean = C2t * 1.0 / C2;
		double C3_length_mean = C3t * 1.0 / C3;
		double O_length_mean = Ot * 1.0 / O;
		
		double sumI = 0;	
		for (int i = 1; i < Il.size(); i++){
			sumI += Math.pow(Il.get(i) - I_length_mean, 2);
		}
		double I_length_sd = Math.sqrt(sumI / (I - 1.0));
		
		double sumC1 = 0;	
		for (int i = 1; i < C1l.size(); i++){
			sumC1 += Math.pow(C1l.get(i) - C1_length_mean, 2);
		}
		double C1_length_sd = Math.sqrt(sumC1 / (C1 - 1.0));
		
		double sumC2 = 0;	
		for (int i = 1; i < C2l.size(); i++){
			sumC2 += Math.pow(C2l.get(i) - C2_length_mean, 2);
		}
		double C2_length_sd = Math.sqrt(sumC2 / (C2 - 1.0));
		
		double sumC3 = 0;	
		for (int i = 1; i < C3l.size(); i++){
			sumC3 += Math.pow(C3l.get(i) - C3_length_mean, 2);
		}
		double C3_length_sd = Math.sqrt(sumC3 / (C3 - 1.0));
		
		double sumO = 0;	
		for (int i = 1; i < Ol.size(); i++){
			sumO += Math.pow(Ol.get(i) - O_length_mean, 2);
		}
		double O_length_sd = Math.sqrt(sumO / (O - 1.0));
		
		System.out.println();
		System.out.println("min number of words in I: " + I_length_min);
		System.out.println("max number of words in I: " + I_length_max);
		System.out.println("mean for number of words in I: " + I_length_mean);
		System.out.println("standard deviation for number of words in I: " + I_length_sd);
		
		System.out.println();
		System.out.println("min number of words in C1: " + C1_length_min);
		System.out.println("max number of words in C1: " + C1_length_max);
		System.out.println("mean for number of words in C1: " + C1_length_mean);
		System.out.println("standard deviation for number of words in C1: " + C1_length_sd);
		
		System.out.println();
		System.out.println("min number of words in C2: " + C2_length_min);
		System.out.println("max number of words in C2: " + C2_length_max);
		System.out.println("mean for number of words in C2: " + C2_length_mean);
		System.out.println("standard deviation for number of words in C2: " + C2_length_sd);
		
		System.out.println();
		System.out.println("min number of words in C3: " + C3_length_min);
		System.out.println("max number of words in C3: " + C3_length_max);
		System.out.println("mean for number of words in C3: " + C3_length_mean);
		System.out.println("standard deviation for number of words in C3: " + C3_length_sd);
		
		System.out.println();
		System.out.println("min number of words in O: " + O_length_min);
		System.out.println("max number of words in O: " + O_length_max);
		System.out.println("mean for number of words in O: " + O_length_mean);
		System.out.println("standard deviation for number of words in O: " + O_length_sd);
		
		System.out.println();
		for (int i = 0; i < Address.length; i++){
			System.out.println("Number of reports address " + i + " classes: " + Address[i]);
		}
	}
}
