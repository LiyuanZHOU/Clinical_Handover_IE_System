import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

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

public class Nicta_stats {
	
	String inputFolder;
	int totalNumber;
	
	static HashMap<String,ArrayList<Double>> hm = new HashMap<>();
	
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
	
	public Nicta_stats(String string) {
		inputFolder = string;
	}

	public static void main(String args[]) throws IOException, XPathExpressionException, Exception {
		
		Nicta_stats stats = new Nicta_stats(args[0]);
		
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
			
			for (int m = 0; m < doc_class[i]; m++) {
				Node annotation = annotations.item(m);
				
//				for(int t = 0; t < annotation.getChildNodes().getLength(); t++){
//					System.out.println(t + ": " + annotation.getChildNodes().item(t).getTextContent());
//					System.out.println(f.getName());
//				}
				
				//indicate this span is not an empty one
				if(annotation.getChildNodes().getLength() > 5){
					String spannedText = annotation.getChildNodes().item(5).getTextContent();
//					System.out.println("spannedText: " + spannedText);
					
					String id = annotation.getChildNodes().item(1).getAttributes().getNamedItem("id").getNodeValue();
//					System.out.println("id: " + id);
					
					expr = xpath.compile("//classMention[@id='" + id
							+ "']" + "/mentionClass");
					result = expr.evaluate(doc, XPathConstants.NODESET);
					NodeList node = (NodeList) result;
					String topic = node.item(0).getTextContent();
					
					double words = spannedText.split(" ").length;
//					System.out.println("number of words: " + words);
					
					if(hm.containsKey(topic))
					{
						double tp = hm.get(topic).get(0)+1;
						hm.get(topic).set(0, tp);
						
						tp = hm.get(topic).get(1) + words;
						hm.get(topic).set(1, tp);
						
						hm.get(topic).add(words);
						
						if(topic.equals("Appointment/Procedure_Ward")){
							System.out.println("test");
						}
						
						if(words > hm.get(topic).get(2)) hm.get(topic).set(2, words);
						if (words < hm.get(topic).get(3)) hm.get(topic).set(3, words);
					}
					else {
						ArrayList<Double> list = new ArrayList<>();
						//no. of instances
						list.add(1.0);
						//no. of words
						list.add(words);
						//max length
						list.add(words);
						//min length
						list.add(words);
						
						hm.put(topic, list);
					}			
				}
					
//				System.out.println("topic: " + topic);
			}			
		}
		
		spans_mean = spans_total * 1.0 / stats.totalNumber;

		
		//loop for documents to calculate the min 
		double sum = 0;		
		
		for (int i = 0; i < stats.totalNumber; i++) {
			sum += Math.pow(doc_class[i] - spans_mean, 2);
		}
		
		spans_sd = Math.sqrt(sum / (stats.totalNumber - 1.0));
		
		System.out.println("min number of spans in a report: " + spans_min);
		System.out.println("max number of spans in a report: " + spans_max);
		System.out.println("mean for number of spans in a report: " + spans_mean);
		System.out.println("standard deviation for number of spans in a report: " + spans_sd);
		System.out.println();
		
		Iterator<Entry<String, ArrayList<Double>>> iter = hm.entrySet().iterator(); 
		while (iter.hasNext()) { 
		    Entry<String, ArrayList<Double>> entry = iter.next(); 
		    String key = entry.getKey(); 
		    ArrayList<Double> val = entry.getValue(); 
		    
		    double I = val.get(0);
		    double It = val.get(1);
		    double I_length_mean = It * 1.0 / I;
		    
		    double sumI = 0;
		    
		    for (int i = 4; i < val.size(); i++){
				sumI += Math.pow(val.get(i) - I_length_mean, 2);
			}
		    
		    double I_length_sd = Math.sqrt(sumI / (I - 1.0));
		    
		    System.out.printf("total %s: %d\n",key, (int)I);
//		    System.out.printf("total annotated words in %s: %d\n",key, (int)It);
		    
		    System.out.printf("max number of words in %s: %d\n", key, (int)(double)val.get(2));
		    System.out.printf("min number of words in %s: %d\n", key, (int)(double)val.get(3));
		    System.out.printf("mean for number of words in %s: %.2f \n", key, I_length_mean);
		    System.out.printf("standard deviation for for number of words in %s: %.2f \n", key, I_length_sd);
		    System.out.println();
		    
		} 
	}
}
