package convert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Converter {

	public static void main(String[] args) throws JDOMException, IOException {
		File input = new File("input");
		File output = new File("output");
		output.delete();
		output.mkdir();
		
		for(File f : input.listFiles()) {
			  SAXBuilder sax = new SAXBuilder();
			  Document doc = sax.build(f);
			  
			  Element root = doc.getRootElement();
			  List<Element> descriptions = root.getChildren("description");
			  for(Element description : descriptions) {
				  List<Element> statements =  description.getChildren("statement");
				  for(Element statement : statements) {
					  List<Element> structures = statement.getChildren("structure");
					  for(Element structure : structures) {
						  structure.setName("biological_entity");
						  structure.setAttribute("type", "structure");
					  }
				  }
			  }
			  
			  XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
			  xout.output(doc, new FileOutputStream(new File("output", f.getName())));
		}
		
	}
	
}
