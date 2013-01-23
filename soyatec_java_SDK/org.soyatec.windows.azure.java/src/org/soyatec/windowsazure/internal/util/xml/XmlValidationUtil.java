package org.soyatec.windowsazure.internal.util.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

public class XmlValidationUtil {

	/**
	 * Validate xml file using specific schema
	 * @param sourceXmlBytes
	 *        the byte array reading from xml file
	 * @param schemaUrl   
	 * 		  schema url used for validation
	 * @return  byte[]
	 */
	public static void validate(byte[] sourceXmlBytes, URL schemaUrl)
			throws IOException, SAXException {
		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			Schema schema = factory.newSchema(schemaUrl);
			Validator validator = schema.newValidator();
			Source source = new StreamSource(new ByteArrayInputStream(
					sourceXmlBytes));
			validator.validate(source);
		} catch (SAXException ex) {
			throw ex;
		} catch (IOException e) {
			throw e;
		}
	}

}
