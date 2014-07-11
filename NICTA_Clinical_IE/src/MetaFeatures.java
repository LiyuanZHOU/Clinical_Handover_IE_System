/*This class is to include the information from Standford NLP and form an xml output for * feature selection use * */import java.io.BufferedReader;import java.io.File;import java.io.FileReader;import java.util.ArrayList;import javax.xml.parsers.DocumentBuilder;import javax.xml.parsers.DocumentBuilderFactory;import javax.xml.xpath.XPath;import javax.xml.xpath.XPathConstants;import javax.xml.xpath.XPathExpression;import javax.xml.xpath.XPathFactory;import org.w3c.dom.Document;import org.w3c.dom.Element;import org.w3c.dom.Node;import org.w3c.dom.NodeList;import utils.FileFinder;import utils.ParseTree;public class MetaFeatures {	public static String txtPath = "";	public static String xmlPath = "";	public static String StanfordInputpath = "";	public static String metamapPath = "";	public static String outputPath = "";	public static void execute(char type) throws Exception {		ArrayList<File> xmlfiles = FileFinder.GetAllFiles(StanfordInputpath,				".xml", true);		DocumentBuilderFactory domFactory = DocumentBuilderFactory				.newInstance();		domFactory.setNamespaceAware(true); // never forget this!		DocumentBuilder builder = domFactory.newDocumentBuilder();		Document outputdocument = builder.newDocument();		outputdocument.setXmlVersion("1.0");		Element root = outputdocument.createElement("PatientReports");		outputdocument.appendChild(root);		// read Stanford xml files		for (File xmlfile : xmlfiles) {			if(type == 'e'){				outputdocument = builder.newDocument();				outputdocument.setXmlVersion("1.0");				root = outputdocument.createElement("PatientReports");				outputdocument.appendChild(root);			}						String txtName = xmlfile.getName().substring(0,					xmlfile.getName().lastIndexOf(".xml"));			System.out.println(txtName);						String id = "";			char[] charA = txtName.toCharArray();			for (int a = 0; a < charA.length; a++){				if (Character.isDigit(charA[a])) id +=charA[a];			}			int idt = Integer.valueOf(id);			// find the original txt of the knowtator xml file			File txtfile = new File(txtPath + "/" + txtName);			String paragraphtxt = Extractor.ReadFile(txtfile, true);			Element Report = outputdocument.createElement("Report");			Report.setAttribute("ReportID", String.valueOf(idt));			Element field = outputdocument.createElement("field");			field.setAttribute("name", "Original Text");			Element paragraphs = outputdocument.createElement("paragraphs");			Element paragraph = outputdocument.createElement("paragraph");			paragraph.setAttribute("paragraphno", "1");			paragraph.setAttribute("paragraphtxt", paragraphtxt);			Element sentences = outputdocument.createElement("sentences");			paragraphtxt = paragraphtxt.replaceAll("\\.\\s*", ".|").replaceAll(					" +", " ");			XPathFactory factory = XPathFactory.newInstance();			XPath xpath = factory.newXPath();			// parse the standford xml file			Document StanfordDoc = builder.parse(xmlfile);			Document doc2 = builder.parse(xmlPath + "/" + txtName					+ ".knowtator.xml");			Document metadoc = builder.parse(metamapPath + "/" + id					+ ".metamapped.xml");						ArrayList<String[]> mappings = new ArrayList<String[]>();			File mappings_file = new File("iccco_new/mappings_newdata/" + txtName);			try {								BufferedReader br = new BufferedReader(new FileReader(mappings_file));		        String line = br.readLine();		        while (line != null) {		        	String[] map_features = line.split(" ");		        	mappings.add(map_features);		            line = br.readLine();		        }		        br.close();		    } catch (Exception e){		    	System.out.println("no mapping features for file: " + txtName);		    }		    			ArrayList<String[]> snomed = new ArrayList<String[]>();	        File snomed_file = new File("iccco_new/out_new/" + id + ".metamapped.txt");					    try {		    	BufferedReader br = new BufferedReader(new FileReader(snomed_file));		        String line = br.readLine();		        while (line != null) {		        	String[] snomed_features = line.split(" ");		        	snomed.add(snomed_features);		            line = br.readLine();		        }		        br.close();		    } catch (Exception e){		    	System.out.println("no snomed features for file: " + txtName);		    }		    ArrayList<String[]> temporal_expressions = new ArrayList<String[]>();			File te_file = new File("iccco_new/temporal_expression_mappings/" + txtName);			try {								BufferedReader br = new BufferedReader(new FileReader(te_file));		        String line = br.readLine();		        while (line != null) {		        	String[] tes = line.split(" ");		        	temporal_expressions.add(tes);		            line = br.readLine();		        }		        br.close();		    } catch (Exception e){		    	System.out.println("no temporal_expression_mappings for file: " + txtName);		    }									XPathExpression expr = xpath.compile("//sentences/sentence");			Object result = expr.evaluate(StanfordDoc,					XPathConstants.NODESET);			NodeList sents = (NodeList) result;			for (int l = 0; l < sents.getLength(); l++) {				Node sent = sents.item(l);				String sentenceno = sent.getAttributes().getNamedItem("id").getNodeValue();				Element sentence = outputdocument.createElement("sentence");				sentence.setAttribute("sentenceno",sentenceno);								System.out.println("Sentence: " + sentenceno);								String sentencetxt = "";								expr = xpath.compile("./parse");				result = expr.evaluate(sent,XPathConstants.NODESET);				NodeList nodes = (NodeList) result;				String parse = "";				for (int p = 0; p < nodes.getLength(); p++) {					Node node = nodes.item(p);					parse = node.getTextContent().replaceAll("\n", "");					parse = parse.replaceAll(" +", " ");					sentence.setAttribute("standfordparse", parse);				}								expr = xpath.compile("./tokens/token");				result = expr.evaluate(sent, XPathConstants.NODESET);				nodes = (NodeList) result;				for (int w = 0; w < nodes.getLength(); w++) {										Node node = nodes.item(w);					String wordID = node.getAttributes().getNamedItem("id").getNodeValue();										System.out.println("word: " + wordID);										NodeList contentnodes = node.getChildNodes();					Element word = outputdocument.createElement("word");										String content = contentnodes.item(1).getTextContent();										word.setTextContent(content);					sentencetxt = sentencetxt + content + " ";					word.setAttribute("wordno", wordID);					word.setAttribute("topic", "NA");//					for (int s = 0; s < contentnodes.getLength(); s ++){//						System.out.println("item " + s +": "+ contentnodes.item(s).getTextContent());//					}//										int begin = Integer.valueOf(contentnodes.item(5).getTextContent());					int end = Integer.valueOf(contentnodes.item(7).getTextContent());										word.setAttribute("begin", String.valueOf(begin));					word.setAttribute("end", String.valueOf(end));										String regx = "//span[@start <= " + begin							+ " and @end >= " + end							+ "]";					expr = xpath							.compile(regx + "/preceding-sibling::mention/@id");					result = expr.evaluate(doc2, XPathConstants.NODESET);					NodeList topicnodes = (NodeList) result;					String topic = "";					for (int m = 0; m < topicnodes.getLength(); m++) {						Node topicnode = topicnodes.item(m);						String id2 = topicnode.getNodeValue();						expr = xpath.compile("//classMention[@id='" + id2								+ "']" + "/mentionClass");						result = expr.evaluate(doc2, XPathConstants.NODESET);						NodeList node2 = (NodeList) result;						if (node2 != null)							for (int n = 0; n < node2.getLength(); n++) {								topic = node2.item(n).getTextContent();							}						if (topic != "")							word.getAttributeNode("topic").setNodeValue(topic);					}										word.setAttribute("AMT_ID", "0");					word.setAttribute("SNOMED_ID", "0");					word.setAttribute("SNOMED_ABSTRACTION_ID", "0");			        word.setAttribute("CORPUS_CHECK", "0");			        			        for(int m = 0; m < mappings.size();m++){			        	if (begin == Integer.valueOf(mappings.get(m)[1])){			        		word.setAttribute("AMT_ID", mappings.get(m)[3]);							word.setAttribute("SNOMED_ID", mappings.get(m)[4]);							word.setAttribute("SNOMED_ABSTRACTION_ID", mappings.get(m)[5]);					        word.setAttribute("CORPUS_CHECK", mappings.get(m)[6]);					        break;			        	}			        }				    			        word.setAttribute("snomed", "0");			        for(int s = 0; s < snomed.size(); s++){			        	if (begin == Integer.valueOf(snomed.get(s)[0])){			        		word.setAttribute("snomed", snomed.get(s)[2]);					        break;			        	}			        }			        			        word.setAttribute("EXPRESSION_TYPE", "NONE");			        word.setAttribute("EXPRESSION_LINK", "NONE");			        for(int m = 0; m < temporal_expressions.size(); m++){			        	if (begin == Integer.valueOf(temporal_expressions.get(m)[0])){			        		word.setAttribute("EXPRESSION_TYPE", temporal_expressions.get(m)[2]);			        		word.setAttribute("EXPRESSION_LINK", temporal_expressions.get(m)[3]);					        break;			        	}			        }					word.setAttribute("standfordlemma", contentnodes.item(3).getTextContent());					word.setAttribute("standfordpos", contentnodes.item(9).getTextContent());					word.setAttribute("standfordNER", contentnodes.item(11).getTextContent());					//					if (NER.contains("NUMBER") || NER.contains("DATE")) {//						expr = xpath.compile("//sentence[@id=" + sentenceno//								+ "]/tokens/token[@id=" + wordID + "]"//								+ "/NormalizedNER");//						result = expr.evaluate(StanfordDoc,//								XPathConstants.NODESET);//						NodeList NormNERnode = (NodeList) result;//						for (int n = 0; n < NormNERnode.getLength(); n++) {//							word.setAttribute("standfordNormalizedNER",//									NormNERnode.item(0).getTextContent());//						}//					}					Document penntree = builder.newDocument();					ParseTree tree = new ParseTree();					penntree = tree.braceMatch(parse);					String re = "//*[@content=\"" + content.toLowerCase()							+ "\"]";					expr = xpath.compile(re);					result = expr.evaluate(penntree, XPathConstants.NODESET);					NodeList parsetreNodes = (NodeList) result;					for (int n = 0; n < parsetreNodes.getLength(); n++) {						Node parsetreNode = parsetreNodes.item(n);						String pt = parsetreNode.getAttributes()								.getNamedItem("nodeName").getNodeValue();						while (parsetreNode.getParentNode() != null) {							parsetreNode = parsetreNode.getParentNode();							if (parsetreNode.hasAttributes()) {								pt = parsetreNode.getAttributes()										.getNamedItem("nodeName")										.getNodeValue()										+ "-" + pt;							}						}						word.setAttribute("ParseTree", pt);					}					String dep = "NONE";					String ex = "./dependencies[@type = \"basic-dependencies\"]//governor[@idx=" + wordID							+ "]/following-sibling::dependent";					expr = xpath.compile(ex);					result = expr.evaluate(sent, XPathConstants.NODESET);					NodeList dependentsNodes = (NodeList) result;					for (int n = 0; n < dependentsNodes.getLength(); n++) {						Node dependentsNode = dependentsNodes.item(n);						String de = dependentsNode.getTextContent();						if (dep != "NONE")							dep = dep + "-" + de;						else							dep = de;					}					word.setAttribute("basic-dependents", dep);					String gov = "NONE";					ex = "./dependencies[@type = \"basic-dependencies\"]//dependent[@idx=" + wordID							+ "]/preceding-sibling::governor";					expr = xpath.compile(ex);					result = expr.evaluate(sent, XPathConstants.NODESET);					NodeList govNodes = (NodeList) result;					for (int n = 0; n < govNodes.getLength(); n++) {						Node govNode = govNodes.item(n);						String de = govNode.getTextContent();						if (gov != "NONE")							gov = gov + "-" + de;						else							gov = de;					}					word.setAttribute("basic-governors", gov);					// add meta features										String s = "//Phrase[PhraseStartPos <= " + begin							+ " and PhraseLength+PhraseStartPos >= " + end							+ "]";					xpath.reset();					expr = xpath.compile(s);					metadoc = builder.parse(metamapPath + "/" + id							+ ".metamapped.xml");					Node phrasenode = (Node) xpath.evaluate(s, metadoc,							XPathConstants.NODE);					if (phrasenode != null){					Document phraseDoc = builder.newDocument();					Element e = (Element) phraseDoc.adoptNode(phrasenode);					phraseDoc.appendChild(e);					expr = xpath.compile("//PhraseText");					result = expr.evaluate(phraseDoc, XPathConstants.NODESET);					NodeList Pnode = (NodeList) result;					String phrase = Pnode.item(0).getTextContent()							.replaceAll(" ", "_");					word.setAttribute("phrase", phrase);					expr = xpath							.compile("/Phrase/Candidates/Candidate/CandidatePreferred");					result = expr.evaluate(phraseDoc, XPathConstants.NODESET);					NodeList CandidatesNodes = (NodeList) result;					for (int c = 0; c < 5; c++) {						String attriname = "candidate" + c;						String value;						if (CandidatesNodes.item(c) != null)							value = CandidatesNodes.item(c).getTextContent()									.replaceAll(" ", "_");						else							value = "NONE";						word.setAttribute(attriname, value);					}					expr = xpath							.compile("Phrase/Mappings//Candidate/CandidatePreferred");					result = expr.evaluate(phraseDoc, XPathConstants.NODESET);					NodeList MappingNodes = (NodeList) result;					if (MappingNodes.item(0) != null)						word.setAttribute("Top_mapping", MappingNodes.item(0)								.getTextContent().replaceAll(" ", "_"));					else						word.setAttribute("Top_mapping", "NONE");					}										sentence.setAttribute("sentencetxt", sentencetxt);					sentence.appendChild(word);						}										sentences.appendChild(sentence);			}			paragraph.appendChild(sentences);			paragraphs.appendChild(paragraph);			field.appendChild(paragraphs);			Report.appendChild(field);			root.appendChild(Report);						if(type == 'e'){				String filename =  outputPath + "/" + id + ".xml";				Extractor.SaveFile(outputdocument,filename);			}					}		if(type=='o'){			String filename = "metamap.xml";			Extractor.SaveFile(outputdocument,filename);		}	}	public static void main(String[] args) {		try {			if (args.length < 6) {				System.out.println("parameters error!");				showHelp();				System.exit(0);			} else {				txtPath = args[1];				xmlPath = args[2];				StanfordInputpath = args[3];				metamapPath = args[4];				outputPath = args[5];			}						if(args[0].equals("-o")){			System.out.println("creating xml file...");			execute('o');			}						else if(args[0].equals("-e")){				System.out.println("creating xml files...");				execute('e');			}						else {				showHelp();				System.exit(0);			}						System.out.println("done!");					} catch (Exception e) {			e.printStackTrace();			showHelp();			System.exit(0);		}	}	private static void showHelp() {		System.out.println("MetaFeatures: generates xml file(s) contains origanal texts, Stanford NLP information, topic labels and metamap information ");		System.out.println("Usage:");		System.out.println("MetaFeatures -o [textFolder] [KnowtatorXmlsFolder] [StanfordXmlsFolder] [MetamapXmlsFolder] [outputFilePath]");		System.out.println("\t\t generate one xml for the whole data set");		System.out.println("MetaFeatures -e [textFolder] [KnowtatorXmlsFolder] [StanfordXmlsFolder] [MetamapXmlsFolder] [outputFilesFolder]");		System.out.println("\t\t generate one xml for each observation in the data set");		System.out.println("MetaFeatures -h, --help \t show this help and exit");	}}