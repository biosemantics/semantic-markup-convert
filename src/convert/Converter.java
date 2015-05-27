package convert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
public class Converter {

	
	
	/*
	 * Used to rename tags 
	 * tagNamesMap - pairs of (complete_path_of_old_name, newname)
	 */
	public static Document renameTags(Document doc, HashMap<String, String> tagNamesMap, PrintWriter logFileWriter, String docFileName){
		XPathFactory xFactory = XPathFactory.instance();
		try
		{
		
		for(String key: tagNamesMap.keySet()){
			XPathExpression<Element> expression = xFactory.compile(key, Filters.element());
			List<Element> elements = expression.evaluate(doc);
			for(Element elem : elements){
				elem.setName(tagNamesMap.get(key));
				logFileWriter.println(docFileName+", "+key.replace("//", "")+" renamed to "+tagNamesMap.get(key)+" ");
			}
		}
		}
		catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		return doc;
	}
	
	public static Document renameTagsAddAttributes(Document doc, HashMap<String, String> tagNamesMap, String attributeName, String attributeValue){
		XPathFactory xFactory = XPathFactory.instance();
		for(String key: tagNamesMap.keySet()){
			XPathExpression<Element> expression = xFactory.compile(key, Filters.element());
			List<Element> elements = expression.evaluate(doc);
			for(Element elem : elements){
				elem.setName(tagNamesMap.get(key));
				elem.setAttribute(attributeName,attributeValue);
				
			}
		}
		return doc;
	}
	
	public static Document removeTags(Document doc, ArrayList<String> tagNames,PrintWriter logFileWriter, String docFileName){
		XPathFactory xFactory = XPathFactory.instance();
		for (String tagName:tagNames)
		{
			XPathExpression<Element> expression = xFactory.compile(tagName, Filters.element());
			List<Element> elements = expression.evaluate(doc);
			for(Element elem : elements){
				
				elem.getParentElement().removeChild(elem.getName());
				logFileWriter.println(docFileName+", element "+elem.getName()+" removed");
			}
		}
		return doc;
	}
	
	public static Document removeAttributes(Document doc, HashMap<String, String> tagAttributeMap,PrintWriter logFileWriter, String docFileName){
		XPathFactory xFactory = XPathFactory.instance();
		for (String tagName:tagAttributeMap.keySet())
		{
			XPathExpression<Element> expression = xFactory.compile(tagName, Filters.element());
			List<Element> elements = expression.evaluate(doc);
			for(Element elem : elements){
				elem.removeAttribute(tagAttributeMap.get(tagName));
				//elem.getAttribute(tagAttributeMap.get(tagName)).detach();
				logFileWriter.println(docFileName+", "+tagAttributeMap.get(tagName)+" removed from "+tagName );
			}
		}
		return doc;
	}
	
	public static Document removeParentTag(Document doc, ArrayList<String> tagNames,PrintWriter logFileWriter, String docFileName){
		XPathFactory xFactory = XPathFactory.instance();
		for (String tagName:tagNames)
		{
			
			XPathExpression<Element> expression = xFactory.compile(tagName, Filters.element());
			List<Element> childElements = expression.evaluate(doc);
			int contentIndex=0;
			Element parenttoRemove = null;
			for(Element elem : childElements){
				
				
				Element parent  = elem.getParentElement();
				parenttoRemove=parent;
				Element mainParent  = parent.getParentElement();
				if(contentIndex==0)	
				{
					contentIndex=mainParent.indexOf(parent);
				}
				else
				{
					contentIndex++;
				}
				
				parent.removeChild(elem.getName());		
				mainParent.addContent(contentIndex,elem);
								
			}
			if(parenttoRemove!=null)
			{
			parenttoRemove.getParentElement().removeChild(parenttoRemove.getName());
			logFileWriter.println(docFileName+", "+parenttoRemove.getName()+" removed");
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
	 * Adds meta tag to document that does not have one
	 */
	
	public static Document addMetaTag(Document jdomDocument,String volume,PrintWriter logFileWriter, String docFileName) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jdomDocument);
		if(elements.isEmpty()){
			Element metaTag = new Element("meta");
			Element sourceTag = new Element("source");
		//	sourceTag.addContent(new Element("title").setText("FNA Volume"+volume.replaceAll("[^0-9]", "")));
			
			sourceTag.addContent(new Element("title").setText("FNA "+volume));
			
			XPathExpression<Element> authorityExpression = xFactory.compile("//author", Filters.element());
			List<Element> authorElements = authorityExpression.evaluate(jdomDocument);
			String author="";
			for (Element authorElem : authorElements) {
				if(author=="")
				{
					author=authorElem.getText();
				}
				else
					author=author+" & "+authorElem.getText();
				
				authorElem.getParentElement().removeContent(authorElem);
				
			}
			if(author=="")
			{
				author="unknown";
			}
				
			sourceTag.addContent(new Element("author").setText(author));	
			sourceTag.addContent(new Element("date").setText("unknown"));
			metaTag.addContent(sourceTag);
			jdomDocument.getRootElement().addContent(0, metaTag);
			logFileWriter.println(docFileName+", "+ "meta element added");
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

	
		
	public static Document convertToRankAttribute(Document jDomDocument,PrintWriter logFileWriter, String docFileName) {
		String hierarchy=" ";
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//taxon_identification/*", Filters.element());
		List<org.jdom2.Element> elements = expression
				.evaluate(jDomDocument);
		for (org.jdom2.Element elem : elements) {
			if(elem.getName().endsWith("_name"))
			{
				String rank= elem.getName().substring(0,elem.getName().indexOf("_"));
				Element authElem=elem.getParentElement().getChild(rank + "_authority");
				
				if(rank.equals("unranked") && authElem== null )
				{
					String childAuthTag=elem.getName().replace("_name", "_authority");
					authElem=elem.getParentElement().getChild(childAuthTag);
				}
				
				logFileWriter.println(docFileName+", "+elem.getName()+" renamed to taxon_name ");
				elem.setName("taxon_name");
				
				elem.setAttribute("rank", rank);
				logFileWriter.println(docFileName+", "+rank+" set as a rank ");
				
				if(authElem != null)
				{
				elem.setAttribute("authority", authElem.getText());
				authElem.getParentElement().removeContent(authElem);
				logFileWriter.println(docFileName+", "+ "authority added to "+ rank );
				}
			}
			
			
			}
		return jDomDocument;
		}
	
	public static Document addParentTag(Document jDomDocument,
			HashMap<String, String> parentChildMap) {
		XPathFactory xFactory = XPathFactory.instance();
		for(String key: parentChildMap.keySet()){
			XPathExpression<Element> parentExpression = xFactory.compile(key, Filters.element());
			
			List<Element> parentElements = parentExpression.evaluate(jDomDocument);
			
			for(Element elem : parentElements){
				
				Element newParent  = new Element(parentChildMap.get(key));
				Element parent  = elem.getParentElement();
				int childIndex=parent.indexOf(elem);
				parent.addContent(childIndex,newParent);
				parent.removeChild(elem.getName());		
				newParent.addContent(elem);
				
			}
		}
		return jDomDocument;
	}

	public static Document renameAddRankAttribute(Document jdomDocument, HashMap<String, String> tagNamesMap,String rank , 
			PrintWriter logFileWriter, String docFileName) {
		
		XPathFactory xFactory = XPathFactory.instance();
		try
		{
		
		for(String key: tagNamesMap.keySet()){
			
			XPathExpression<Element> expression = xFactory.compile(key,
					Filters.element());
			List<Element> elements = expression.evaluate(jdomDocument);
			for (Element elem : elements) {
				
				elem.setName(tagNamesMap.get(key));
				logFileWriter.println(docFileName+", "+ key.replace("//", "")+" renamed to "+tagNamesMap.get(key)+" ");
				
				if (elem.getAttribute("rank") == null) {
					elem.setAttribute("rank", rank);
					
					logFileWriter.println(docFileName+", "+rank+" set as a rank ");

					Element authElem=elem.getParentElement().getChild(rank + "_authority");
					if(authElem != null)
					{
					elem.setAttribute("authority", authElem.getText());
					authElem.getParentElement().removeContent(authElem);
					logFileWriter.println(docFileName+", "+ "authority added to "+ rank );
					}
			
				}
			}

			
		}
		}
		catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		
				return jdomDocument;
	}

	
	public static Document addRankAttribute(Document jdomDocument, String rank) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//taxon_name",
				Filters.element());
		List<Element> elements = expression.evaluate(jdomDocument);
		for (Element elem : elements) {

			if (elem.getAttribute("rank") == null) {
				elem.setAttribute("rank", rank);

				Element authElem=elem.getParentElement().getChild(rank + "_authority");
				if(authElem != null)
				{
				elem.setAttribute("authority", authElem.getText());
				authElem.getParentElement().removeContent(authElem);
				}
		
			}
		}
		return jdomDocument;
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
	public static Document correctTaxon(Document jdomDocument, HashMap<String, String> taxonAuthorityMap){
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
