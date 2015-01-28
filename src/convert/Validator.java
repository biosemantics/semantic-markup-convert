package convert;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

public class Validator {

	public static void main(String[] args) {
		File input = new File("output");
		for(File f : input.listFiles()) {
			System.out.println(validate(f.getAbsolutePath(), new File("in.xsd"), null));
		}
	}
	
	public static boolean validate(String input, File schemaFile, URL url) {
		Source schemaSource = null;
		if(schemaFile != null) {
			schemaSource = new StreamSource(schemaFile);
			return validate(input, schemaSource);
		} else if(url != null) {
			try(InputStream inputStream = url.openStream()) {
			    schemaSource = new StreamSource(inputStream);
			    return validate(input, schemaSource);
			} catch (IOException e) {
				System.out.println("Couldn't open or close input stream from url");
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean validate(String input, Source schemaSource) {
		if(schemaSource != null) {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = null;
			try {
				schema = factory.newSchema(schemaSource);
			} catch (SAXException e) {
				System.out.println("Couldn't create schema");
				e.printStackTrace();
			}
			if(schema != null) {
				javax.xml.validation.Validator validator = schema.newValidator();	
				try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input.getBytes("UTF-8"))) {
					try {
						validator.validate(new StreamSource(byteArrayInputStream));
					} catch (IOException e) {
						System.out.println("Couldn't validate xml document");
						e.printStackTrace();
					} catch(SAXException e) {
						System.out.println("Validation of an input failed");
						e.printStackTrace();
						return false;
					}
					return true;
				} catch (UnsupportedEncodingException e) {
					System.out.println("Encoding not supported");
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Couldn't open or close byte array strema");
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
