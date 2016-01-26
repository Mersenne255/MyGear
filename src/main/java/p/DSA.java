package p;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.swing.text.Document;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DSA {
	public static void main(String[] args) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(true);

			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();

			builder.setErrorHandler(new DSA().new SimpleErrorHandler());
			InputSource inputSource = new InputSource( new StringReader( "<?xml version=\"1.0\" encoding=\"UTF-8\"?><note></note>" ) );
			// the "parse" method also validates XML, will throw an exception if
			// misformatted
			builder.parse(inputSource);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public class SimpleErrorHandler implements ErrorHandler {
		public void warning(SAXParseException e) throws SAXException {
			System.out.println(e.getMessage());
		}

		public void error(SAXParseException e) throws SAXException {
			System.out.println(e.getMessage());
		}

		public void fatalError(SAXParseException e) throws SAXException {
			System.out.println(e.getMessage());
		}
	}
}
