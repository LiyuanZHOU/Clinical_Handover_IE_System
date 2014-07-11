/*This class is to include the information from Standford NLP and form an xml output for
 * feature selection use
 * */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import utils.FileFinder;
import utils.ParseTree;

public class StanfordXML {
	public static String txtPath = "";
	public static String xmlPath = "";
	public static String outputPath = "";
	public static String StanfordInputpath = "";

	public static void execute(char type) throws Exception{
		ArrayList<File> xmlfiles = FileFinder
				.GetAllFiles(StanfordInputpath, ".xml", true);
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();

		Document outputdocument = builder.newDocument();
		outputdocument.setXmlVersion("1.0");

		Element root = outputdocument.createElement("PatientReports");
		outputdocument.appendChild(root);

		// read Stanford xml files
		for (File xmlfile : xmlfiles) {
			
			if(type == 'e'){
				outputdocument = builder.newDocument();
				outputdocument.setXmlVersion("1.0");

				root = outputdocument.createElement("PatientReports");
				outputdocument.appendChild(root);
			}
			
			String txtName = xmlfile.getName().substring(0,
					xmlfile.getName().lastIndexOf(".xml"));
			System.out.println(txtName);
			//TODO
			String id = txtName.substring(5, txtName.lastIndexOf(".txt"));
			//int idt = Integer.valueOf(id)+1;
			@SuppressWarnings("unused")
			int idt = Integer.valueOf(id);
			
			// find the original txt of the knowtator xml file
			File txtfile = new File(txtPath + "/" + txtName);
			String paragraphtxt = Extractor.ReadFile(txtfile, true);
			Element Report = outputdocument.createElement("Report");
			Report.setAttribute("ReportID", String.valueOf(id));
			Element field = outputdocument.createElement("field");
			field.setAttribute("name", "Original Text");
			Element paragraphs = outputdocument.createElement("paragraphs");
			Element paragraph = outputdocument.createElement("paragraph");
			paragraph.setAttribute("paragraphno", "1");
			paragraph.setAttribute("paragraphtxt", paragraphtxt);
			Element sentences = outputdocument.createElement("sentences");
			// String[] sentencetxt = paragraphtxt.split("(?<=[a-z])\\.\\s*");
			paragraphtxt = paragraphtxt.replaceAll("\\.\\s*", ".|").replaceAll(
					" +", " ");
			String[] sentencetxt = paragraphtxt.split("\\|");

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			// parse the standford xml file
			Document StanfordDoc = builder.parse(xmlfile);
			Document doc2 = null;
			try{
			doc2 = builder.parse(xmlPath+"/"+txtName + ".knowtator.xml");
			
			for (int j = 0; j < sentencetxt.length; j++) {

				int sentenceno = j + 1;
				Element sentence = outputdocument.createElement("sentence");
				sentence.setAttribute("sentenceno",
						Integer.toString(sentenceno));

				sentencetxt[j] = sentencetxt[j].replaceAll("'s", " is");
				sentencetxt[j] = sentencetxt[j].replaceAll("hasn\\'t",
						"has not");
				sentencetxt[j] = sentencetxt[j].replaceAll("doesn\\'t",
						"does not");

				sentence.setAttribute("sentencetxt", sentencetxt[j]);
				
				XPathExpression expr = xpath.compile("//sentence[@id="
						+ sentenceno + "]/parse");
				Object result = expr.evaluate(StanfordDoc,
						XPathConstants.NODESET);
				NodeList nodes = (NodeList) result;
				String parse = "";
				for (int l = 0; l < nodes.getLength(); l++) {
					Node node = nodes.item(l);
					parse = node.getTextContent().replaceAll("\n",
							"");
					parse = parse.replaceAll(" +", " ");
					sentence.setAttribute("standfordparse", parse);
				}
				
				expr = xpath.compile("//sentence[@id="
						+ sentenceno + "]//token");
				result = expr.evaluate(StanfordDoc,
						XPathConstants.NODESET);
				nodes = (NodeList) result;
				for (int w = 0; w < nodes.getLength(); w++){
					
					String wordID = Integer.toString(w + 1);
					expr = xpath.compile("//sentence[@id="
							+ sentenceno + "]//token[@id="
							+ wordID + "]/word");
					result = expr.evaluate(StanfordDoc,
							XPathConstants.NODESET);
					NodeList contentnodes = (NodeList) result;	
					String content = contentnodes.item(0).getTextContent();
					
					Element word = outputdocument.createElement("word");
					word.setTextContent(content);
					
					word.setAttribute("wordno", wordID);
					word.setAttribute("topic", "NA");
	//				System.out.println(content);

					expr = xpath.compile("//sentence[@id=" + sentenceno
							+ "]/tokens/token[@id=" + wordID + "]"
							+ "/CharacterOffsetBegin");
					result = expr.evaluate(StanfordDoc, XPathConstants.NODESET);
					NodeList beginnode = (NodeList) result;
					int begin = Integer.valueOf(beginnode.item(0)
							.getTextContent());

					expr = xpath.compile("//sentence[@id=" + sentenceno
							+ "]/tokens/token[@id=" + wordID + "]"
							+ "/CharacterOffsetEnd");
					result = expr.evaluate(StanfordDoc, XPathConstants.NODESET);
					NodeList endnode = (NodeList) result;
					int end = Integer.valueOf(endnode.item(0).getTextContent());
					
					String regx = "//span[@start <= " + begin
							+ " and @end >= " + end
							+ "]";
//					String regx = "'" + content.replaceAll("\'", " i") + "'";
					// System.out.println("regx: "+regx);
					expr = xpath
							.compile(regx + "/preceding-sibling::mention/@id");
					result = expr.evaluate(doc2,
							XPathConstants.NODESET);
					NodeList topicnodes = (NodeList) result;
					String topic = "";
					for (int m = 0; m < topicnodes.getLength(); m++) {
						Node topicnode = topicnodes.item(m);
						String id2 = topicnode.getNodeValue();
						expr = xpath
								.compile("//classMention[@id='" + id2 + "']"
										+ "/mentionClass");
						result = expr.evaluate(doc2,
								XPathConstants.NODESET);
						NodeList node2 = (NodeList) result;
						if (node2 != null)
							for (int n = 0; n < node2.getLength(); n++) {
								topic = node2.item(n).getTextContent();
							}
						if (topic != "")
							word.getAttributeNode("topic").setNodeValue(topic);
					}
					
					expr = xpath.compile("//sentence[@id="
							+ sentenceno + "]/tokens/token[@id=" + wordID
							+ "]" + "/lemma");
					result = expr.evaluate(StanfordDoc,
							XPathConstants.NODESET);
					NodeList lemmanode = (NodeList) result;
					String lemma = lemmanode.item(0).getTextContent();
					word.setAttribute("standfordlemma", lemma);
					
					expr = xpath.compile("//sentence[@id="
							+ sentenceno + "]/tokens/token[@id=" + wordID
							+ "]" + "/POS");
					result = expr.evaluate(StanfordDoc,
							XPathConstants.NODESET);
					NodeList POSnode = (NodeList) result;
					word.setAttribute("standfordpos", POSnode.item(0).getTextContent());
					
					expr = xpath.compile("//sentence[@id="
							+ sentenceno + "]/tokens/token[@id=" + wordID
							+ "]" + "/NER");
					result = expr.evaluate(StanfordDoc,
							XPathConstants.NODESET);
					NodeList NERnode = (NodeList) result;
					String	NER = NERnode.item(0).getTextContent();
					word.setAttribute("standfordNER", NER);
						
					if (NER.contains("NUMBER") || NER.contains("DATE")) {
						expr = xpath
								.compile("//sentence[@id=" + sentenceno
										+ "]/tokens/token[@id=" + wordID + "]"
										+ "/NormalizedNER");
						result = expr.evaluate(StanfordDoc,
								XPathConstants.NODESET);
						NodeList NormNERnode = (NodeList) result;
						for (int n = 0; n < NormNERnode.getLength(); n++) {
						word.setAttribute("standfordNormalizedNER",
									NormNERnode.item(0).getTextContent());
						}
					}
					

					Document penntree = builder.newDocument();
					ParseTree tree = new ParseTree();
					penntree = tree.braceMatch(parse);
					String re = "//*[@content=\"" + content.toLowerCase()+"\"]";
					expr =  xpath.compile(re);
					result = expr.evaluate(penntree, XPathConstants.NODESET);
					NodeList parsetreNodes = (NodeList) result;
					for (int n = 0; n < parsetreNodes.getLength(); n++) {
						Node node = parsetreNodes.item(n);
						String pt = node.getAttributes().getNamedItem("nodeName").getNodeValue();
//						System.out.println(pt);
						while (node.getParentNode() != null){
							node = node.getParentNode();
							if(node.hasAttributes()){
								
								pt = node.getAttributes().getNamedItem("nodeName").getNodeValue() + "-" + pt;
							}
						}
						word.setAttribute("ParseTree", pt);
					}
				
					
					String dep = "NONE";
					String ex = "//sentence[@id=" + sentenceno
							+ "]//basic-dependencies//governor[@idx=" + wordID+"]/following-sibling::dependent";
					expr =  xpath.compile(ex);
					result = expr.evaluate(StanfordDoc, XPathConstants.NODESET);
					NodeList dependentsNodes = (NodeList) result;
					
					for (int n = 0; n < dependentsNodes.getLength(); n++) {
						Node node = dependentsNodes.item(n);
						String de = node.getTextContent();
						if(dep!="NONE")
							dep = dep + "-" + de;
						else
							dep = de;
					}
					word.setAttribute("basic-dependents", dep);
										
					String gov = "NONE";
					ex = "//sentence[@id=" + sentenceno
							+ "]//basic-dependencies//dependent[@idx=" + wordID+"]/preceding-sibling::governor";
					expr =  xpath.compile(ex);
					result = expr.evaluate(StanfordDoc, XPathConstants.NODESET);
					NodeList govNodes = (NodeList) result;
					
					for (int n = 0; n < govNodes.getLength(); n++) {
						Node node = govNodes.item(n);
						String de = node.getTextContent();
						if(gov!="NONE")
							gov = gov + "-" + de;
						else
							gov = de;
					}
					word.setAttribute("basic-governors", gov);
					
					sentence.appendChild(word);
				}		
				sentences.appendChild(sentence);
			}
			
			paragraph.appendChild(sentences);		
			paragraphs.appendChild(paragraph);
			field.appendChild(paragraphs);
			Report.appendChild(field);
			root.appendChild(Report);
			
			if(type == 'e'){
				String filename = outputPath + "/" + id + ".xml";
				Extractor.SaveFile(outputdocument,filename);
			}
			
			}catch(FileNotFoundException e){
				System.out.println(e.toString());
			}
		}
		
		if(type=='o'){
			String filename = outputPath + "/Stanford.xml";
			Extractor.SaveFile(outputdocument,filename);
		}

	}
	public static void main(String[] args) {
		try {
			if (args.length < 5) {
				System.out.println("parameters error!");
				showHelp();
				System.exit(0);
			} else {
				txtPath = args[1];
				xmlPath = args[2];
				StanfordInputpath = args[3];
				outputPath = args[4];
			}

			if(args[0].equals("-o")){
				System.out.println("creating xml file...");
				execute('o');
				}
				
				else if(args[0].equals("-e")){
					System.out.println("creating xml files...");
					execute('e');
				}
				
				else {
					showHelp();
					System.exit(0);
				}
			System.out.println("done!");
			
		} catch (Exception e) {
			e.printStackTrace();
			//showHelp();
		}
	}

	private static void showHelp() {
		System.out.println("StanfordXML: generates xml file(s) contains origanal texts, Stanford NLP information and topic labels");
		System.out.println("Usage:");
		System.out.println("StanfordXML -o [textFolder] [KnowtatorXmlsFolder] [StanfordXmlsFolder] [outputFilePath]");
		System.out.println("\t\t generate one xml for the whole data set");
		System.out.println("StanfordXML -e [textFolder] [KnowtatorXmlsFolder] [StanfordXmlsFolder] [outputFilesFolder]");
		System.out.println("\t\t generate one xml for each observation in the data set");
		System.out.println("StanfordXML -h, --help \t show this help and exit");
		
	}
}
