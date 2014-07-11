/*This class is to extract the basic information from the knowtator xml files and form an xml output for
 * feature selection use
 * */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import utils.FileFinder;
public class Extractor {

	public static String txtPath="";
	public static String xmlPath="";
	public static String outputPath="";
	public static String ReadFile(File f, boolean keep_newline) {
		try {
			StringBuilder sb = new StringBuilder();
			java.io.BufferedReader br = new BufferedReader(new FileReader(f));
			String line = null;
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				sb.append((sb.length() > 0 ? (keep_newline ? "\n" : " ") : "")
						+ line);
			}
			br.close();
			return sb.toString();
		} catch (Exception e) {
			System.out.println("ERROR: " + e);
			return null;
		}
	}

	public static void execute(char type) throws Exception{
		ArrayList<File> files = FileFinder.GetAllFiles(txtPath, ".txt", true);
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();

		Document outputdocument = builder.newDocument();
		outputdocument.setXmlVersion("1.0");

		Element root = outputdocument.createElement("PatientReports");
		outputdocument.appendChild(root);
		for (int i = 0; i < files.size(); i++) {
			File xmlFile=new File(xmlPath+"/"+files.get(i).getName() + ".knowtator.xml");
			if(!xmlFile.exists())
			{
				System.err.println("file "+xmlPath+"/"+files.get(i).getName() + ".knowtator.xml"+" not found, skipped");
				continue;
			}
			String paragraphtxt = ReadFile(files.get(i), true);
			int ID = Integer.valueOf(files.get(i).getName().substring(0,files.get(i).getName().lastIndexOf(".txt")));
			int reportID = ID+1;
			Element Report = outputdocument.createElement("Report");
			Report.setAttribute("ReportID",String.valueOf(reportID));
			Element field = outputdocument.createElement("field");
			field.setAttribute("name", "Original Text");
			Element paragraphs = outputdocument.createElement("paragraphs");
			Element paragraph = outputdocument.createElement("paragraph");
			paragraph.setAttribute("paragraphno", Integer.toString(i+1));
			paragraph.setAttribute("paragraphtxt", paragraphtxt);
			Element sentences = outputdocument.createElement("sentences");
			String[] sentencetxt = paragraphtxt.split("(?<=[a-z])\\.\\s+");
			for (int j = 0; j < sentencetxt.length; j++) {
				Element sentence = outputdocument.createElement("sentence");
				sentence.setAttribute("sentenceno", Integer.toString(j + 1));
				sentence.setAttribute("sentencetxt", sentencetxt[j]);
				String words[] = sentencetxt[j].replaceAll(",", "")
						.split(" |,");
				for (int k = 0; k < words.length; k++) {
					Element word = outputdocument.createElement("word");
					word.setAttribute("wordno", Integer.toString(k + 1));
					word.setAttribute("topic", "NA");
					Document doc=null;
					try {
						doc = builder.parse(xmlPath+"/"+files.get(i).getName() + ".knowtator.xml");
					} catch (SAXException e) {
						System.err.println("file "+xmlPath+"/"+files.get(i).getName() + ".knowtator.xml"+" not found, skipped");
						continue;
					} catch (IOException e) {
						System.err.println("file "+xmlPath+"/"+files.get(i).getName() + ".knowtator.xml"+" not found, skipped");
						continue;
					}
					XPathFactory factory = XPathFactory.newInstance();
					XPath xpath = factory.newXPath();
					String regx = "'" + words[k].replaceAll("\'", " i") + "'";
					// System.out.println("regx: "+regx);
					XPathExpression expr = xpath
							.compile("//spannedText[contains(text()," + regx
									+ ")]/preceding-sibling::mention/@id");
					Object result = expr.evaluate(doc, XPathConstants.NODESET);
					NodeList nodes = (NodeList) result;
					String topic = "";
					for (int m = 0; m < nodes.getLength(); m++) {
						String id = nodes.item(m).getNodeValue();
						XPathExpression expr2 = xpath
								.compile("//classMention[@id='" + id + "']"
										+ "/mentionClass");
						Object result2 = expr2.evaluate(doc,
								XPathConstants.NODESET);
						NodeList nodes2 = (NodeList) result2;
						if (nodes2 != null)
							for (int l = 0; l < nodes2.getLength(); l++)
								topic = nodes2.item(l).getTextContent();
					}
					if (topic != "")
						word.getAttributeNode("topic").setNodeValue(topic);
					word.setTextContent(words[k]);
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
				String filename = outputPath + "/" + ID + ".xml";
				SaveFile(outputdocument,filename);
			}
		}
		
		if(type=='o'){
			String filename = outputPath + "/output.xml";
			SaveFile(outputdocument,filename);
		}
	}

	public static void SaveFile(Document outputdocument, String filename) throws TransformerException {
		
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		// transformer.setOutputProperty(OutputKeys.ENCODING, "ISO8859_1");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "4");
		DOMSource source = new DOMSource(outputdocument);
		StreamResult result = new StreamResult(filename);
		transformer.transform(source, result);
		
		System.out.println("File saved to: " + filename);
	}

	public static void main(String[] args) {
		try {
			if(args.length<3)
			{
				System.out.println("parameters error!");
				showHelp();
				System.exit(0);
			}
			else{
				txtPath=args[1];
				xmlPath=args[2];
				outputPath=args[3];
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
			showHelp();
		}
	}

	private static void showHelp() {
		System.out.println("Extractor: generates xml file(s) contains origanal texts and topic labels");
		System.out.println("Usage:");
		System.out.println("Extractor -o [textFolder] [KnowtatorXmlsFolder] [outputFilePath]");
		System.out.println("\t\t generate one xml for the whole data set");
		System.out.println("Extractor -e [textFolder] [KnowtatorXmlsFolder] [outputFilesFolder]");
		System.out.println("\t\t generate one xml for each observation in the data set");
		System.out.println("Extractor -h, --help \t show this help and exit");
		
	}
}
