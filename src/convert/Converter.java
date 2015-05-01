package convert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HastaxonAuthorityMap;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class Converter {

	
	/*
	 * Used to set "unknown" to empty tags
	 *  paths - takes list of tags that are non-empty and that need to updated
	 */
	public static Document correctNonEmptyTags(Document doc, ArrayList<String> paths){
		XPathFactory xFactory = XPathFactory.instance();
		for(String path: paths){
			XPathExpression<Element> expression = xFactory.compile(path, Filters.element());
			List<Element> elements = expression.evaluate(doc);
			for(Element elem : elements){
				if(elem.getText().length() == 0){
					elem.setText("unknown");
				}
			}
		}
		
		return doc;
	}
	
	/*
	 * Used to set authority for taxons
	 * taxonAuthorityMap - pairs of (taxonnames,authority)
	 * Can be modified to take taxonDateMap (date is set to unknown now)
	 */
	public static Document correctTaxon(Document jdomDocument, HastaxonAuthorityMap<String, String> taxonAuthorityMap){
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//taxon_identification/taxon_name", Filters.element());
		List<Element> elements = expression.evaluate(jdomDocument);
		for(Element elem : elements){
				String authority_value;
				if(taxonAuthorityMap.get(elem.getText())!=null){
					authority_value = taxonAuthorityMap.get(elem.getText());
				}else{
					authority_value = "unknown";
				}
				elem.setAttribute("authority", authority_value);
				elem.setAttribute("date", "unknown");
		}
		return jdomDocument;
	}
	
	/*
	 * Used to rename tags 
	 * tagNamesMap - pairs of (complete_path_of_old_name, newname)
	 */
	public static Document renameTags(Document doc, HashMap<String, String> tagNamesMap){
		XPathFactory xFactory = XPathFactory.instance();
		for(String key: tagNamesMap.keySet()){
			XPathExpression<Element> expression = xFactory.compile(key, Filters.element());
			List<Element> elements = expression.evaluate(doc);
			for(Element elem : elements){
				elem.setName(tagNamesMap.get(key));
			}
		}
		return doc;
	}
	
	/*
	 * Used to rename attributes
	 * attributeNamesMap - pairs of  attribute's (complete_path_of_old_name, newname)
	 */
	
	public static Document renameAttributes(Document doc, HashMap<String, String> attributeNamesMap){
		XPathFactory xFactory = XPathFactory.instance();
		for(String key: attributeNamesMap.keySet()){
			XPathExpression<Attribute> expression = xFactory.compile(key, Filters.attribute());
			List<Attribute> elements = expression.evaluate(doc);
			for(Attribute elem : elements){
				elem.setName(attributeNamesMap.get(key));
			}
		}
		return doc;
	}
	
	/*
	 * Used to move an element to a destination
	 * arrangementMap - pairs of (complete_path_of_element_to_be_moved, complete_path_of_destination_parent)
	 */
	
	public static Document rearrangeTags(Document jDomDocument,
			HashMap<String, String> arrangementMap) {
		XPathFactory xFactory = XPathFactory.instance();
		for(String key: arrangementMap.keySet()){
			XPathExpression<Element> expression1 = xFactory.compile(key, Filters.element());
			XPathExpression<Element> expression2 = xFactory.compile(arrangementMap.get(key), Filters.element());
			List<Element> source = expression1.evaluate(jDomDocument);
			List<Element> destination = expression2.evaluate(jDomDocument);
			int count = 0;
			for(Element elem : source){
				Element parent  = elem.getParentElement();
				parent.removeChild(elem.getName());
				Element dest = destination.get(count); 
				dest.addContent(dest.getContentSize(), elem);
				count++;
			}
		}
		return jDomDocument;
	}

	/*
	 * Adds meta tag to document that does not have one
	 */
	
	public static Document addMetaTag(Document jdomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jdomDocument);
		if(elements.isEmpty()){
			Element metaTag = new Element("meta");
			Element sourceTag = new Element("source");
			sourceTag.addContent(new Element("author").setText("unknown"));
			sourceTag.addContent(new Element("date").setText("unknown"));
			metaTag.addContent(sourceTag);
			jdomDocument.getRootElement().addContent(0, metaTag);
		}
		return jdomDocument;
	}
	
	/*
	 * Adds type attribute to description elements
	 */
	public static Document addTypeAttribute(Document jdomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//description", Filters.element());
		List<Element> elements = expression.evaluate(jdomDocument);
		for(Element elem : elements){
			if(elem.getAttribute("type") == null){
				elem.setAttribute("type", "morphology");
			}
		}
		return jdomDocument;
	}
	
	/*
	 * sets the namespace to the xml file
	 */

	public static Document setNameSpace(Document jdomDocument){
		Namespace bioNamespace = Namespace.getNamespace("bio", "http://www.github.com/biosemantics");
		Namespace xsiNamespace = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		Element rootElement = jdomDocument.getRootElement();
		rootElement.setNamespace(bioNamespace);
		rootElement.addNamespaceDeclaration(bioNamespace);
		rootElement.addNamespaceDeclaration(xsiNamespace);
		rootElement.setAttribute("schemaLocation", "http://www.github.com/biosemantics http://www.w3.org/2001/XMLSchema-instance", xsiNamespace);
		return jdomDocument;
	}
	
}
