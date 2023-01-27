package com.sapiens.ssi.logging;

import com.sapiens.ssi.constants.SSIConstant;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
@Log4j2
public class SSILogConfig {

	public static String jobName = null;;
	public static String packageName = null;

	public static DocumentBuilder documentBuilder = null;
	public static Document document = null;
	public static Transformer transformer = null;
	public static TransformerFactory transformerFactory = null;
	public static DOMSource domSource = null;
	public static String xmlFilePath = null;

	static {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		transformerFactory = TransformerFactory.newInstance();
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			transformer = transformerFactory.newTransformer();
		} catch (ParserConfigurationException | TransformerConfigurationException e) {
			log.error("<" + SSILogConfig.class.getSimpleName() + ">" + "ERROR OCCURED");
			e.printStackTrace();
		}

	}

	public static void ChangeLevel(String level) {
		if (level.toLowerCase().equals(SSIConstant.debug))
			Configurator.setRootLevel(Level.DEBUG);
		else if (level.toLowerCase().equals(SSIConstant.trace))
			Configurator.setRootLevel(Level.TRACE);
		else if (level.toLowerCase().equals(SSIConstant.fatal))
			Configurator.setRootLevel(Level.FATAL);
		else if (level.toLowerCase().equals(SSIConstant.info))
			Configurator.setRootLevel(Level.INFO);
		else if (level.toLowerCase().equals(SSIConstant.warn))
			Configurator.setRootLevel(Level.WARN);
		else if (level.toLowerCase().equals(SSIConstant.error))
			Configurator.setRootLevel(Level.ERROR);
	}

	public static void logConfig(String logFileLocation, String LogFileName, String LogFileSize, String rollOverSize) {

		xmlFilePath = SSIConstant.log4j2;

		try {
			document = documentBuilder.parse(xmlFilePath);

			Node Property = document.getElementsByTagName("Property").item(0);
			NodeList list = Property.getChildNodes();
			Node location = list.item(0);
			location.setTextContent(logFileLocation);

			Node Property1 = document.getElementsByTagName("Property").item(1);
			NodeList list1 = Property1.getChildNodes();
			Node filename = list1.item(0);
			filename.setTextContent(LogFileName);

			Node policies = document.getElementsByTagName("SizeBasedTriggeringPolicy").item(0);
			NamedNodeMap attr1 = policies.getAttributes();
			Node Size = attr1.getNamedItem("size");
			Size.setTextContent(LogFileSize);


			Node rollOver = document.getElementsByTagName("DefaultRolloverStrategy").item(0);
			NamedNodeMap strategy = rollOver.getAttributes();
			Node def = strategy.getNamedItem("max");
			def.setTextContent(rollOverSize);

			// write the object to the file
			domSource = new DOMSource(document);
			StreamResult streamResult = new StreamResult(new File(xmlFilePath));
			transformer.transform(domSource, streamResult);

			File file = new File(xmlFilePath);
			SSILogger.context.setConfigLocation(file.toURI());

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

}
