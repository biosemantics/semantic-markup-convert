package convert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
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

	public static Document addMetaTag(Document jdomDocument,String volume, PrintWriter logFileWriter, String docFileName) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jdomDocument);
		if(elements.isEmpty()){
			Element metaTag = new Element("meta");
			Element sourceTag = new Element("source");
		//	sourceTag.addContent(new Element("title").setText("FNA Volume"+volume.replaceAll("[^0-9]", "")));
			
			
			
			
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
			sourceTag.addContent(new Element("title").setText("FNA "+volume));
			
			metaTag.addContent(sourceTag);
			
			jdomDocument.getRootElement().addContent(0, metaTag);
			logFileWriter.println(docFileName+", "+ "meta element added");
		}
		return jdomDocument;
	}
	/**
	 * This method handles reading the titles in from a directory and rearranges their xml content so that
	 * Author precedes title, which precedes date, which ultimately precedes the pages count.
	 * Currently set to return the meta element, though this is rather trivial I suppose.
	 */
	public static Element moveTitle(Document jdomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jdomDocument);
		Element meta = null, source = null;
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					break;
				}
			}
		}
		if (meta != null) {
			source = meta.getChild("source");
			source.detach();
		}
		if (source != null) {
		List<Element> children = source.getChildren();
		Element author = null, date = null, title = null, pages = null;
		for(Element e: children) {
			if (e.getName().equals("author")) {
				author = e;
			} else if (e.getName().equals("date")) {
				date = e;
			} else if (e.getName().equals("title")) {
				title = e;
			} else if (e.getName().equals("pages")) {
				pages = e;
			} 
		}
		if (author!= null) {
		author.detach();
		} if (date !=null) {
		date.detach();
		} if (title != null) {
		title.detach();
		} if (pages != null) {
		pages.detach();
		}
		
		if (author != null) {
			source.addContent(author);
		} if (date != null) {
			source.addContent(date);
		} if (title != null) {
			source.addContent(title);
		} if (pages != null) {
			source.addContent(pages);
		}
		
		}
		meta.addContent(source);
		return meta;
	}
	/**
	 * This method is designed to remove and rewrite our XML files without the number element present.
	 * @param jdomDocument This is our document we're reading from (XML file)
	 * @return This method currently returns our meta element, though this is also rather trivial.
	 */
	public static Element removeNumber(Document jdomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jdomDocument);
		Element meta = null, parent = null;
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.toString().equals("[Element: <meta/>]")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
		}
		if (meta != null) {
			parent = (Element) meta.getParent();
			List<Element> children = parent.getChildren();
			for (Element e: children) {
				if (e.getName().equals("number")) {
					Element number = e;
					e.detach();
					break;
				}
			}
		}
		
		return meta;
	}
	public static Element removeIllustrated(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		Element source = null;
		Element meta = null, parent = null;
		List<Element> sourceChildren = new ArrayList<Element>();
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					source = e.getChild("source");
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> content = parent.getChildren();	
			List<Element> deleted = new ArrayList<Element>();
			sourceChildren = source.getChildren();
			for (Element e: content) {
				if (e.toString().equals("[Element: <illustrated/>]")) {
					deleted.add(e);
				}
			}
			for (Element e: deleted) {
				e.detach();
			}
		}
		return parent;
	}
	public static Element removeNullDescription(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		Element meta = null, parent = null;
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> content = parent.getChildren();	
			for (Element e: content) {
				System.out.println(e);
				if (e.toString().equals("[Element: <discussion/>]")) {
					String contains = e.getValue();
					System.out.println(contains);
				}
			}
		}
		return parent;
	}
	public static Element changeOtherInfo(Document jDomDocument) {
			XPathFactory xFactory = XPathFactory.instance();
			XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
			List<Element> elements = expression.evaluate(jDomDocument);
			Element meta = null, parent = null;
			if (!elements.isEmpty()) {
				for (Element e: elements) {
					if (e.getName().equals("meta")) {
						meta = e;
						parent = meta.getParentElement();
						break;
					}
				}
				List<Element> content = parent.getChildren();
				for (Element e: content) {
					List<Element> content2 = e.getChildren();
					for (Element e2: content2) {
						if (e2.toString().equals("[Element: <other_info/>]")) {
							e2.setName("other_info_on_pub");
						}
						List<Element> content3 = e2.getChildren();
						for (Element e3: content3) {
							if (e3.toString().equals("[Element: <other_info/>]")) {
								e3.setName("other_info_on_pub");
							}
						}
					}
					if (e.toString().equals("[Element: <other_info/>]")) {
						e.setName("other_info_on_pub");
					}
				}
				
			}
			return parent;
	}
	public static Element changeEtymology(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		Element meta = null, parent = null;
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> content = parent.getChildren();
			List<Element> detached = new ArrayList<Element>();
			for (Element e: content) {
				if (e.toString().equals("[Element: <etymology/>]")) {
					e.setName("discussion");
					detached.add(e);
					
				}
			}
			for (Element e: detached) {
				e.detach();
				parent.addContent(e);
			}
			
			
		}
		return parent;
		
	}
	public static Element modifyKeyHeading(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		Element meta = null, parent = null;
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> content = parent.getChildren();	
			for (Element e: content) {
				if (e.toString().equals("[Element: <key/>]")) {
					List<Element> newContent = e.getChildren();
					for (Element e2: newContent) {
						if (e2.toString().equals("[Element: <key_heading/>]")) {
							e2.setName("key_head");
						}
					}
				}
			}
		}
		return parent;
	}
	public static Element moveReferencesHeading(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		Element meta = null, parent = null;
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> content = parent.getChildren();	
			for (Element e: content) {
				if (e.toString().equals("[Element: <references/>]")) {
					//att.detach();
					System.out.println(e.getAttributes());
					e.removeAttribute("heading");
				System.out.println(e.getAttributes());
				}
			}
		}
		return parent;
	}
	public static Element addAuthorityAndDate(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		Element meta = null, parent = null;
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> content = parent.getChildren();
			for (Element e: content) {
				if (e.toString().equals("[Element: <taxon_identification/>]")) {
					List<Element> newContent = e.getChildren();
					for (Element e2: newContent) {
						if (e2.toString().equals("[Element: <taxon_name/>]")) {
							Attribute auth = e2.getAttribute("authority");
							if (auth==null) {
								auth = new Attribute("authority", "unknown");
								e2.setAttribute(auth);
							}
							Attribute date = e2.getAttribute("date");
							if (date == null) {
								date = new Attribute("date", "unknown");
								e2.setAttribute(date);
							}
						}
					}
				}
			}
			
		}
		return meta;
		
		
	}
	/**
	 * This method's responsibility is to move the <other_name> tag in the XML document towards the bottom of the 
	 * XML file to properly fit the new Schema. This was accomplished by saving the elements we wish to detach 
	 * into a list, then detaching/reattaching that element to the parent at the end of the parent's content.
	 * @param jdomDocument
	 * @return
	 */
	public static Element moveOtherName(Document jdomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jdomDocument);
		Element meta = null, parent = null;
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Content> content = parent.getContent();
			List<Content> copiedContent = new ArrayList<Content>();
			List<Content> movedContent = new ArrayList<Content>();
			for (Content e: content) {
				if (e.toString().equals("[Element: <other_name/>]")) {
					movedContent.add(e);
				} else {
					continue;
				}
			}
			for (Content e2: movedContent) {
				e2.detach();
				if (!e2.getValue().equals("")) {
				meta.getParent().addContent(e2);
				}
			}
		}
		return meta;
	}
	public static Element addPoaceaeFamily(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		Element meta = null, parent = null; 
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> contents = parent.getChildren();
			for (Element e: contents) {
				System.out.println(e);
				if (e.toString().equals("[Element: <taxon_identification/>]")) {
					if (e.getAttribute("status").getValue().equals("ACCEPTED")) {
						Element element = new Element("taxon_name");
						element.setText("Poaceae");
						element.setAttribute("rank","family");
						e.addContent(element);
					}
				}
			}
			
		}
		return meta;
	}
	public static Element RenameTaxonIdentification(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		Element meta = null, parent = null; 
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> contents = parent.getChildren();
			for (Element e: contents) {
				System.out.println(e);
				if (e.toString().equals("[Element: <TaxonIdentification/>]")) {
					String name = e.getName();
					String newName = "taxon_identification";
					e.setName(newName);
					Attribute data = e.getAttribute("Status");
					if (data != null) {
					data.setName("status");
					}
				}
			}
			
		}
		return meta;
	}
	public static Element changeKeyFromAlphaToNumeric(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		Element meta = null, parent = null; 
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> contents = parent.getChildren();
			for (Element e: contents) {
				if (e.toString().equals("[Element: <key/>]")) {
					List<Element> newContents = e.getChildren();
					for (Element e2: newContents) {
						if (e2.toString().equals("[Element: <key_statement/>]")) {
							List<Element> lastContents = e2.getChildren();
							for (Element e3: lastContents) {
								if (e3.toString().equals("[Element: <statement_id/>]")) {
									char val = e3.getText().charAt(0);
									String newVal = val+".";
									e3.setText(newVal);
								} else if (e3.toString().equals("[Element: <next_statement_id/>]")) {
									char val = e3.getText().charAt(0);
									String newVal = val+".";
									e3.setText(newVal);
								}
							}
						}
					}
				}
			}
			
		}
		return meta;
	}
	public static Element renameAuthority(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		Element meta = null, parent = null; 
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> contents = parent.getChildren();
			List<Element> detached = new ArrayList<Element>();
			for (Element e: contents) {
				
				if (e.toString().equals("[Element: <taxon_identification/>]")) {
					List<Element> data = e.getChildren();
					if (data.size() == 1) {
						if (data.get(0).toString().equals("[Element: <family_name/>]")) {
							data.get(0).setName("taxon_name");
							data.get(0).setAttribute("rank", "family");
						}
						if (data.get(0).toString().equals("[Element: <genus_name/>]")) {
							data.get(0).setName("taxon_name");
							data.get(0).setAttribute("rank", "genus");
							continue;
						}
						if (data.get(0).toString().equals("[Element: <species_name/>]")) {
							data.get(0).setName("taxon_name");
							data.get(0).setAttribute("rank", "species");
							continue;
						}
						if (data.get(0).toString().equals("[Element: <subspecies_name/>]")) {
							data.get(0).setName("taxon_name");
							data.get(0).setAttribute("rank", "subspecies");
							continue;
						}
						if (data.get(0).toString().equals("[Element: <variety_name/>]")) {
							data.get(0).setName("taxon_name");
							data.get(0).setAttribute("rank", "variety");
							continue;
						}
						if (data.get(0).toString().equals("[Element: <tribe_name/>]")) {
							data.get(0).setName("taxon_name");
							data.get(0).setAttribute("rank", "tribe");
							continue;
						}
						if (data.get(0).toString().equals("[Element: <section_name/>]")) {
							data.get(0).setName("taxon_name");
							data.get(0).setAttribute("rank", "section");
							continue;
						}
						
					}
					for (int i=0; i < data.size(); i++) {
						if (data.get(i).toString().equals("[Element: <family_name/>]")) {
							if (i != data.size()-1) {
							if (data.get(i+1).toString().equals("[Element: <family_authority/>]")) {
								String authority = data.get(i+1).getText();
								data.get(i).setAttribute("rank", "family");
								data.get(i).setAttribute("authority", authority);
								data.get(i).setName("taxon_name");
								detached.add(data.get(i+1));
							} 
							}else {
								data.get(i).setName("taxon_name");
								data.get(i).setAttribute("rank", "family");
							}
							
						}
						if (data.get(i).toString().equals("[Element: <genus_name/>]")) {
							if (i != data.size()-1) {
							if (data.get(i+1).toString().equals("[Element: <genus_authority/>]")) {
								String authority = data.get(i+1).getText();
								data.get(i).setAttribute("rank", "genus");
								data.get(i).setAttribute("authority", authority);
								data.get(i).setName("taxon_name");
								detached.add(data.get(i+1));
							}
							} else {
								data.get(i).setName("taxon_name");
								data.get(i).setAttribute("rank", "genus");
							}
						}
						if (data.get(i).toString().equals("[Element: <species_name/>]")) {
							if (i != data.size()-1) {
							if (data.get(i+1).toString().equals("[Element: <species_authority/>]")) {
								String authority = data.get(i+1).getText();
								data.get(i).setAttribute("rank", "species");
								data.get(i).setAttribute("authority", authority);
								data.get(i).setName("taxon_name");
								detached.add(data.get(i+1));
							}
							} else {
								data.get(i).setName("taxon_name");
								data.get(i).setAttribute("rank", "species");
							}
						}
						if (data.get(i).toString().equals("[Element: <subspecies_name/>]")) {
							if (i != data.size()-1) {
							if (data.get(i+1).toString().equals("[Element: <subspecies_authority/>]")) {
								String authority = data.get(i+1).getText();
								data.get(i).setAttribute("rank", "subspecies");
								data.get(i).setAttribute("authority", authority);
								data.get(i).setName("taxon_name");
								detached.add(data.get(i+1));
							}
							} else {
								data.get(i).setName("taxon_name");
								data.get(i).setAttribute("rank", "subspecies");
							}
						}
						if (data.get(i).toString().equals("[Element: <variety_name/>]")) {
							if (i != data.size()-1) {
							if (data.get(i+1).toString().equals("[Element: <variety_authority/>]")) {
								String authority = data.get(i+1).getText();
								data.get(i).setAttribute("rank", "variety");
								data.get(i).setAttribute("authority", authority);
								data.get(i).setName("taxon_name");
								detached.add(data.get(i+1));
							}
							} else {
								data.get(i).setName("taxon_name");
								data.get(i).setAttribute("rank", "variety");
							}
						}
						if (data.get(i).toString().equals("[Element: <tribe_name/>]")) {
							if (i != data.size()-1) {
							if (data.get(i+1).toString().equals("[Element: <tribe_authority/>]")) {
								String authority = data.get(i+1).getText();
								data.get(i).setAttribute("rank", "tribe");
								data.get(i).setAttribute("authority", authority);
								data.get(i).setName("taxon_name");
								detached.add(data.get(i+1));
							}
							} else {
								data.get(i).setName("taxon_name");
								data.get(i).setAttribute("rank", "tribe");
							}
						}
						if (data.get(i).toString().equals("[Element: <section_name/>]")) {
							if (i != data.size()-1) {
							if (data.get(i+1).toString().equals("[Element: <section_authority/>]")) {
								String authority = data.get(i+1).getText();
								data.get(i).setAttribute("rank", "section");
								data.get(i).setAttribute("authority", authority);
								data.get(i).setName("taxon_name");
								detached.add(data.get(i+1));
							}
							} else {
								data.get(i).setName("taxon_name");
								data.get(i).setAttribute("rank", "section");
							}
						}
						if (data.get(i).toString().equals("[Element: <subgenus_name/>]")) {
							if (i != data.size()-1) {
							if (data.get(i+1).toString().equals("[Element: <subgenus_authority/>]")) {
								String authority = data.get(i+1).getText();
								data.get(i).setAttribute("rank", "subgenus");
								data.get(i).setAttribute("authority", authority);
								data.get(i).setName("taxon_name");
								detached.add(data.get(i+1));
							}
							} else {
								data.get(i).setName("taxon_name");
								data.get(i).setAttribute("rank", "subgenus");
							}
						}
						if (data.get(i).toString().equals("[Element: <subsection_name/>]")) {
							if (i != data.size()-1) {
							if (data.get(i+1).toString().equals("[Element: <subsection_authority/>]")) {
								String authority = data.get(i+1).getText();
								data.get(i).setAttribute("rank", "subsection");
								data.get(i).setAttribute("authority", authority);
								data.get(i).setName("taxon_name");
								detached.add(data.get(i+1));
							}
							} else {
								data.get(i).setName("taxon_name");
								data.get(i).setAttribute("rank", "subsection");
							}
						}
						if (data.get(i).toString().equals("[Element: <series_name/>]")) {
							if (i != data.size()-1) {
							if (data.get(i+1).toString().equals("[Element: <series_authority/>]")) {
								String authority = data.get(i+1).getText();
								data.get(i).setAttribute("rank", "series");
								data.get(i).setAttribute("authority", authority);
								data.get(i).setName("taxon_name");
								detached.add(data.get(i+1));
							}
							} else {
								data.get(i).setName("taxon_name");
								data.get(i).setAttribute("rank", "series");
							}
						}
						if (data.get(i).toString().equals("[Element: <subfamily_name/>]")) {
							if (i != data.size()-1) {
							if (data.get(i+1).toString().equals("[Element: <subfamily_authority/>]")) {
								String authority = data.get(i+1).getText();
								data.get(i).setAttribute("rank", "subfamily");
								data.get(i).setAttribute("authority", authority);
								data.get(i).setName("taxon_name");
								detached.add(data.get(i+1));
							}
							} else {
								data.get(i).setName("taxon_name");
								data.get(i).setAttribute("rank", "subfamily");
							}
						}
					}
				}
			}
			if (detached.size() > 0) {
				for (Element e: detached) {
					e.detach();
				}
			}
			
			
		}
		return parent;
		
	}
	public static Element switchBackVolumes(String newVolume, Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		Element meta = null, parent = null; 
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			Element source = meta.getChild("source");
			List<Element> contents = source.getChildren();
			for (Element e: contents) {
				if (e.toString().equals("[Element: <title/>]")) {
					e.setText(newVolume);
				}
			}
			
		}
		return meta;
	}
	public static Element changeKeyStatements(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		ArrayList<Element> detached = new ArrayList<Element>();
		Element meta = null, parent = null; 
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> contents = parent.getChildren();
			for (Element e2: contents)  {
				if (e2.toString().equals("[Element: <key/>]")) {
					List<Element> newContents = e2.getChildren();
					for (Element e3: newContents) {
						if (e3.toString().equals("[Element: <key_statement/>]")) {
							List<Element> moreContents = e3.getChildren();
							for (Element e4: moreContents) {
								if (e4.toString().equals("[Element: <statement/>]")) {
									e4.setAttribute("type", "morphology");
									e4.setName("description");
								}
							}
						}
					}
				}
				
			}
			
		}
		return parent;
	}
	public static Element convertCommonName(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		ArrayList<Element> detached = new ArrayList<Element>();
		Element meta = null, parent = null; 
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> contents = parent.getChildren();
			for (Element e2: contents)  {
				if (e2.toString().equals("[Element: <common_name/>]")) {
					e2.setAttribute("type", "common");
					e2.setName("other_name");
				}
			}
			
		}
		return parent;
	}
	public static Element convertDeprecatedTags(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		ArrayList<Element> detached = new ArrayList<Element>();
		Element meta = null, parent = null; 
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> contents = parent.getChildren();
			for (Element e2: contents)  {
				if (e2.toString().equals("[Element: <habitat/>]") || e2.toString().equals("[Element: <elevation/>]")
						|| e2.toString().equals("[Element: <phenology/>]") || e2.toString().equals("[Element: <ecology/>]")) {
					System.out.println(true);
				}
				
			}
			
		}
		return parent;
	}
	/**
	 * This method is designed to remove the deprecated [weedy\], [illustrated\], and [endemic\] tags
	 * to fit the updated version of our XML schema.
	 * @param jDomDocument
	 * @return
	 */
	public static Element removeWeedyEndemicIllustratedElements(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		ArrayList<Element> detached = new ArrayList<Element>();
		Element meta = null, parent = null; 
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> contents = parent.getChildren();
			for (Element e2: contents)  {
				if (e2.toString().equals("[Element: <endemic/>]")) {
					detached.add(e2);
					continue;
				}
				if (e2.toString().equals("[Element: <illustrated/>]")) {
					detached.add(e2);
					continue;
				} if (e2.toString().equals("[Element: <weedy/>]")) {
					detached.add(e2);
					continue;
				}
				
			}
			for (Element e3: detached) {
				e3.detach();
			}
		}
		return parent;
	}
	public static Element modifyDiscussion(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		ArrayList<Element> detached = new ArrayList<Element>();
		Element meta = null, parent = null; 
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> content = parent.getChildren();
			for (Element e: content) {
				if (e.toString().equals("[Element: <description/>]")) {
					e.setAttribute("type", "morphology");
				}
			}
			
			
			
		}
		return parent;
	}
	public static Element modifyDistribution(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		ArrayList<Element> detached = new ArrayList<Element>();
		Element meta = null, parent = null; 
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> content = parent.getChildren();
			for (Element e: content) {
				if (e.toString().equals("[Element: <distribution/>]")) {
					e.setName("description");
					e.setAttribute("type", "distribution");
				}
			}
			
			
			
		}
		return parent;
	}
	/**
	 * This method is designed to carefully extract the <other_name> element which has been deprecated from our
	 * XML Schema doc.
	 * @param JdomDocument
	 * @return
	 */
	public static Element removeConservedName(Document JdomDocument) {
		
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(JdomDocument);
		Element meta = null, parent = null;
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			
		}
		List<Content> content = parent.getContent();
		List<Content> savedContent  = new ArrayList<Content>();
		for (Content c: content) {
			if (c.toString().equals("[Element: <conserved_name/>]")) {
				savedContent.add(c);
			}
		}
		for (Content c2: savedContent) {
			c2.detach();
		}
		
		return parent;
		
	}
	/**
	 * This method is designed to change our <phenology_fruiting> element into a <description> element of type
	 * phenology. Also sets the scope of the phenology description to be of scope 'fruiting'.
	 * @param jDomDocument
	 * @return
	 */
	public static Element modifyPhenology(Document jDomDocument) {
		
		ArrayList<Element> phenologies = new ArrayList<Element>();
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		Element meta = null, parent = null;
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
				}
				
			}
			List<Element> contents = parent.getChildren();
			for (Element e2: contents) {
				if (e2.toString().equals("[Element: <phenology_fruiting/>]")) {
					phenologies.add(e2);
				}
			}
			for (Element c: phenologies) {
				c.setName("description");
				c.setAttribute("type", "phenology");
				c.setAttribute("scope","fruiting");
			}
			
		}
		
		
		return meta;
		
	}
	public static Element removeHashSymbols(Document jDomDocument) {
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//meta", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		Element meta = null, parent = null;
		if (!elements.isEmpty()) {
			for (Element e: elements) {
				if (e.getName().equals("meta")) {
					meta = e;
					parent = meta.getParentElement();
					break;
				}
			}
			List<Element> content = parent.getChildren();
			for (Element e: content) {
				if (e.toString().equals("[Element: <key/>]")) {
					List<Element> newContent = e.getChildren();
					for (Element e2: newContent) {
						if (e2.toString().equals("[Element: <key_statement/>]")) {
							List<Element> finalContent = e2.getChildren();
							for (Element e3: finalContent) {
								
								if (e3.getText().contains(" ###")) {
									String text = e3.getText().replace(" ###", "");
									e3.setText(text);
								}
								
							}
						}
					}
				}
			}
		}
		return parent;
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
		rootElement.setAttribute("schemaLocation", "http://www.github.com/biosemantics   http://raw.githubusercontent.com/biosemantics/schemas/master/semanticMarkupInput.xsd", xsiNamespace);
		return jdomDocument;
	}
	
}

