import java.io.File;import java.io.IOException;import java.util.ArrayList;import javax.xml.parsers.DocumentBuilder;import javax.xml.parsers.DocumentBuilderFactory;import javax.xml.xpath.XPath;import javax.xml.xpath.XPathConstants;import javax.xml.xpath.XPathExpression;import javax.xml.xpath.XPathFactory;import org.w3c.dom.Document;import org.w3c.dom.NodeList;import utils.FileFinder;public class test {		public static void main(String[] args) throws Exception{				DocumentBuilderFactory domFactory = DocumentBuilderFactory				.newInstance();		domFactory.setNamespaceAware(true); // never forget this!		DocumentBuilder builder = domFactory.newDocumentBuilder();		Document doc=null;		try {			doc = builder.parse("C:\\Users\\annjouno\\Desktop\\EXPORT\\ID Case 2.txt.knowtator.xml");		}catch (IOException e) {			System.err.println("not found, skipped");		}				XPathFactory factory = XPathFactory.newInstance();		XPath xpath = factory.newXPath();		XPathExpression expr = xpath				.compile("//spannedText");		Object result = expr.evaluate(doc, XPathConstants.NODESET);		NodeList nodes = (NodeList) result;				for(int i = 0; i < nodes.getLength(); i++){			String s = nodes.item(i).getTextContent();			System.out.println(s);						ArrayList<File> files = FileFinder.GetAllFiles("C:\\Users\\annjouno\\Desktop\\RE__annotated_files_\\Cases 51-200", ".txt", true);						for(File file : files){				String txt = Extractor.ReadFile(file, true);				if(txt.contains(s)){					System.out.println(file.getName());				}			}		}					}			}