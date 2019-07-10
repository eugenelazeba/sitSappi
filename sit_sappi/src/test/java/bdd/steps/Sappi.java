package bdd.steps;

import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

import static org.xmlunit.builder.Input.fromStream;

@Slf4j
public class Sappi {

    static final String PATH_RESOURCES = "src/test/resources/";

    @Then("compare (\\S+) response with (\\S+) request")
    public void compareXml(String atcomresNode, String sappiNode) throws Exception {

        Document responseAtcomres = getNodeXml(PATH_RESOURCES + "atcomres/responseData.xml", atcomresNode);
        prettyWrite(responseAtcomres, PATH_RESOURCES + "file/responseAtcomres.xml");

        Document requestSappi = getNodeXml(PATH_RESOURCES + "sappi/requestData.xml", sappiNode);
        prettyWrite(requestSappi, PATH_RESOURCES + "/file/requestSappi.xml");

        Diff myDiff = DiffBuilder.compare(fromStream(Sappi.class.getResourceAsStream("/file/responseAtcomres.xml")))
                .withTest(Input.fromStream(Sappi.class.getResourceAsStream("/file/requestSappi.xml")))
                .checkForSimilar()
                .ignoreComments()
                .normalizeWhitespace()
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText, ElementSelectors.byNameAndAllAttributes))
                .withNodeFilter(node -> !(node.getNodeName().equals("Ser_Mod_Dt_Tm") || node.getNodeName().equals("ns2:Ser_Mod_Dt_Tm")
                        || node.getNodeName().equals("Cost_Dt_Tm") || node.getNodeName().equals("ns2:Cost_Dt_Tm")
                        || node.getNodeName().equals("Com_Dt_Tm") || node.getNodeName().equals("ns2:Com_Dt_Tm")
                        || node.getNodeName().equals("Change_Ind")
                        || node.getNodeName().equals("Ptd_Tp")
                        || node.getNodeName().equals("Ptd_To_Tp")
                        || node.getNodeName().equals("Atol_Val")
                        || node.getNodeName().equals("Atol_Ind")
                        || node.getNodeName().equals("Rm_Gp_Cd")
                ))

                .build();

        Assert.assertFalse(myDiff.toString(), myDiff.hasDifferences());
    }

    private static Document getNodeXml(String pathFile, String tagName) throws ParserConfigurationException,
            SAXException, IOException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document newDoc = db.newDocument();
        Document parseDoc = db.parse(new FileInputStream(new File(pathFile)));

        NodeList list = parseDoc.getElementsByTagName(tagName);
        Element element = (Element) list.item(0);

        Node copiedNode = newDoc.importNode(element, true);

        newDoc.appendChild(copiedNode);
        return newDoc;
    }

    public static final void prettyWrite(Document xml, String pathDoc) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");

        try (Writer out = new StringWriter(); FileWriter file = new FileWriter(pathDoc)) {
            tf.transform(new DOMSource(xml), new StreamResult(out));

            file.write(out.toString());
            log.info("pretty XML :" + xml.getNodeName().toUpperCase() + out.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
