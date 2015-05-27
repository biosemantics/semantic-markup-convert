package convert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import extract.HttpRequester;

public class MainClass {
	String volume="";

	public static void main(String[] args) {
		// convert();
		
			
		String[] sequenceFilesVolumes = {"V8","V27"};
			//{ "V26","V4", "V5", "V7", "V8", "V19", "V20", "V21", "V23", "V26", "V27"};
		
		for( String eachVolum: sequenceFilesVolumes)
		{
		String fileName = eachVolum; 
		String newFile = fileName+"_Final";
		String genusFile = newFile + "/Genus_Family/genus_names.txt";
		String genusfamilyFile = newFile + "/Genus_Family/genus_family.txt";
			
		getFamilyNamesFromFile(fileName,newFile,genusFile,genusfamilyFile);
		convert(newFile,fileName);
		arrangeFiles(newFile+"/Converted_Files",eachVolum);
		}
		
		/*	String[] volumes = { "V2", "V3", "V22"};
		
		for( String eachVolum: volumes)
		{
			
		String fileName = eachVolum;
		String newFile = fileName+"_Final";
		String genusFile = newFile + "/Genus_Family/genus_names.txt";
		String genusfamilyFile = newFile + "/Genus_Family/genus_family.txt";
			
		correctFiles(fileName,newFile,genusFile);
		getGenusNames(fileName,newFile,genusFile);
		HttpRequester.getFamilyName(fileName, genusFile, genusfamilyFile);
		addFamilyTag(fileName, genusFile, genusfamilyFile,newFile);
		convert(newFile,fileName);
		arrangeFiles(newFile+"/Converted_Files",eachVolum);
		}*/
		
		// HttpRequester.createFolders(fileName);
		// copyFiles(newFile);
		// Validator();
		
	}

	static void convert(String newFile, String volume) {
		// Give input folder here

		File input = new File(newFile); 
		// Output folder name here
				
		File output = new File(newFile+"/"+"Converted_Files");
		output.delete();
		output.mkdir();

		SAXBuilder jdomBuilder = new SAXBuilder();
		HashMap<String, String> tagNamesMap=  new HashMap<String, String>();
		Document jDomDocument;
		
		try {
			
			PrintWriter logFileWriter=new PrintWriter(newFile+"/Genus_Family/ConversionLog.txt");
			
			File[] listOfFiles = input.listFiles();
			Arrays.sort(listOfFiles);
			
			for (File f : listOfFiles) {
				try {
					jDomDocument = jdomBuilder.build(f);
					String fileName=f.getName();
					// Different methods in Converter can be called
					// The parameters to the methods need to be constructed here
					// Can also be done as:
					//addTaxonHierarchy(jDomDocument);
					
					removeTags(jDomDocument,logFileWriter, fileName);
					removeAttributes(jDomDocument,logFileWriter, fileName);
					addHierarchy(jDomDocument);
					renameTags(jDomDocument,logFileWriter, fileName); // check the renameTags function
					// below
					 renameAttributes(jDomDocument); // check the
					// renameAttributes function below
					Converter.addMetaTag(jDomDocument,volume,logFileWriter, fileName);
					
					Converter.addTypeAttribute(jDomDocument);
					
					renameTagsWithAttributes(jDomDocument,logFileWriter, fileName);
					
					//addParentTags(jDomDocument);
					
					XMLOutputter xout = new XMLOutputter(
							Format.getPrettyFormat());
					xout.output(jDomDocument, new FileOutputStream(new File(
							output, f.getName())));
					f.delete();
				} catch (JDOMException e) {
					System.out.println(f.getName() + "has jdom exception: "
							+ e.getMessage());
					continue;
				}
			}
			logFileWriter.close();
			logFileWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static Document renameTagsWithAttributes(Document jDomDocument, PrintWriter logFileWriter, String docFileName) {
		
		Element rootElement = jDomDocument.getRootElement();
//		rootElement.get
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//*", Filters.element());
		List<Element> elements = expression.evaluate(jDomDocument);
		for(Element elem : elements){
//			HashMap<String, String> namesMap = new HashMap<String, String>();
			if(elem.getName().endsWith("_distribution"))
			{
				logFileWriter.println(docFileName+", "+ elem.getName()+" converted to description type = " + elem.getName().substring(elem.getName().indexOf("_")+1) );
				elem.setName("description");
				elem.setAttribute("type","distribution");
				
			}
			else if(elem.getName().equals("statement")) {
				if(elem.getParentElement().getName().equals("key_statement")) {
					logFileWriter.println(docFileName+", "+ elem.getName()+" converted to description type = morphology" );
					elem.setName("description");
					elem.setAttribute("type","morphology");
				}	
			}
			else if(elem.getName().equals("habitat") ||elem.getName().equals("elevation")
					||elem.getName().equals("phenology")||elem.getName().equals("ecology")
					) {
				
				logFileWriter.println(docFileName+", "+ elem.getName()+" converted to description type = " + elem.getName() );
					elem.setAttribute("type",elem.getName());
					elem.setName("description");
					
			}
				
		}
		return jDomDocument;
	}

	
	static Document addParentTags(Document jDomDocument) {
		HashMap<String, String> childParentMap = new HashMap<String, String>();
		childParentMap.put("//conservation", "discussion");
		childParentMap.put("//introduced", "discussion");
		Converter.addParentTag(jDomDocument,childParentMap);
		return jDomDocument;
	}
	
	static Document removeAttributes(Document jDomDocument,PrintWriter logFileWriter, String docFileName) {
		HashMap<String, String> tagAttributeMap = new HashMap<String, String>();
		tagAttributeMap.put("//references", "heading");		
		Converter.removeAttributes(jDomDocument, tagAttributeMap, logFileWriter, docFileName);
		return jDomDocument;
	}
	
	static Document removeTags(Document jDomDocument,PrintWriter logFileWriter, String docFileName) {
		ArrayList tagNames= new ArrayList<String>();
		tagNames.add("//number_of_infrataxa");
		tagNames.add("//etymology");
	//	tagNames.add("//references");
		Converter.removeTags(jDomDocument,tagNames,logFileWriter,docFileName);

		/*
		tagNames.clear();
		tagNames.add("//references/*");
		Converter.removeParentTag(jDomDocument,tagNames,logFileWriter,docFileName);*/
		
		return jDomDocument;
	}
	
	static Document renameTags(Document jDomDocument, PrintWriter logFileWriter, String docFileName) {
		HashMap<String, String> namesMap = new HashMap<String, String>();

		namesMap.put("//TaxonIdentification", "taxon_identification");
		namesMap.put("//TaxonHierarchy", "taxon_hierarchy");
		namesMap.put("//common_name", "other_name");
		namesMap.put("//past_name", "other_name");
		namesMap.put("//other_info","other_info_on_pub");
		namesMap.put("//introduced", "discussion");
		namesMap.put("//conservation", "discussion");
//		namesMap.put("//family_name", "taxon_name");
//		namesMap.put("//family_authority", "authority");
//		namesMap.put("//genus_authority", "authority");
//		namesMap.put("//species_authority", "authority");
		
		Converter.renameTags(jDomDocument, namesMap,logFileWriter, docFileName);
		Converter.convertToRankAttribute(jDomDocument, logFileWriter, docFileName);
		
		namesMap.clear();
/*	
		namesMap.put("//family_name", "taxon_name"); 
		Converter.renameAddRankAttribute(jDomDocument,namesMap,"family",logFileWriter, docFileName);
		
		namesMap.clear();
		namesMap.put("//tribe_name", "taxon_name");
		//jDomDocument= Converter.renameTags(jDomDocument, namesMap);
		Converter.renameAddRankAttribute(jDomDocument,namesMap,"tribe",logFileWriter, docFileName);
		
		namesMap.clear();
		namesMap.put("//genus_name", "taxon_name");
		Converter.renameAddRankAttribute(jDomDocument,namesMap,"genus",logFileWriter, docFileName);
		
		namesMap.clear();
		namesMap.put("//species_name", "taxon_name");
	//	jDomDocument= Converter.renameTags(jDomDocument, namesMap);
		Converter.renameAddRankAttribute(jDomDocument,namesMap,"species",logFileWriter, docFileName);
		
		namesMap.clear();
		namesMap.put("//subgenus_name", "taxon_name");
		Converter.renameAddRankAttribute(jDomDocument,namesMap,"subgenus",logFileWriter, docFileName);
		
		
		namesMap.clear();
		namesMap.put("//variety_name", "taxon_name");
		Converter.renameAddRankAttribute(jDomDocument,namesMap,"variety",logFileWriter, docFileName);
		
		namesMap.clear();
		namesMap.put("//section_name", "taxon_name");
		Converter.renameAddRankAttribute(jDomDocument,namesMap,"section",logFileWriter, docFileName);
*/
		return jDomDocument;
	}

	static Document renameAttributes(Document jDomDocument) {
		HashMap<String, String> namesMap = new HashMap<String, String>();

		namesMap.put("//taxon_identification/@Status", "status");

		return Converter.renameAttributes(jDomDocument, namesMap);
	}

	static Document reArrangeTags(Document jDomDocument) {
		HashMap<String, String> arrangementMap = new HashMap<String, String>();

		arrangementMap.put("//taxon_hierarchy", "//taxon_identification");

		return Converter.rearrangeTags(jDomDocument, arrangementMap);
	}

	static Document convert_nonEmptyTag(Document jDomDocument) {
		ArrayList<String> paths = new ArrayList<String>();
		paths.add("//number");
		paths.add("//meta/source/author");
		paths.add("//meta/source/date");
		paths.add("//meta/source/title");
		paths.add("//meta/source/pages");
		paths.add("//meta/other_info_on_meta/@type");
		paths.add("//processed_by/processor/date");
		paths.add("//processed_by/processor/operator");
		paths.add("//processed_by/processor/software/@*");
		paths.add("//processed_by/processor/resource/@*");
		paths.add("//taxon_identification/taxon_heirarchy");
		paths.add("//taxon_identification/place_of_publication/publication_title");
		paths.add("//taxon_identification/place_of_publication/place_in_publication");
		paths.add("//taxon_identification/place_of_publication/other_info_on_pub/@*");
		paths.add("//taxon_identification/other_info_on_name/@*");
		paths.add("//taxon_identification/taxon_name/@*");
		paths.add("//taxon_identification/strain_number/@*");
		paths.add("//description/@type");
		paths.add("//type/@*");
		paths.add("//other_name/@*");
		paths.add("//material/@*");
		paths.add("//discussion/@*");
		paths.add("//taxon_relation_articulation/@*");
		paths.add("//key/key_heading");
		paths.add("//key/key_author");
		paths.add("//key/discussion");
		paths.add("//key/key_head");
		paths.add("//key/key_statement/statement_id");
		paths.add("//key/key_statement/next_statement_id");
		paths.add("//key/key_statement/description/@*");

		return Converter.correctNonEmptyTags(jDomDocument, paths);

	}
	
	private static Document addTaxonHierarchy(Document jDomDocument) {
		String hierarchy="";
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//TaxonIdentification[@Status='ACCEPTED']/family_name", Filters.element());
		List<org.jdom2.Element> elements = expression
				.evaluate(jDomDocument);
		for (org.jdom2.Element elem : elements) {
			hierarchy="family "+elem.getText() ;
		}
		XPathExpression<Element> tribeExpression = xFactory.compile("//TaxonIdentification[@Status='ACCEPTED']/tribe_name", Filters.element());
		List<org.jdom2.Element> tribeElements = tribeExpression.evaluate(jDomDocument);
		for (org.jdom2.Element elem : tribeElements) {
			hierarchy= hierarchy+"; tribe "+elem.getText();
		}
		XPathExpression<Element> genusExpression = xFactory.compile("//TaxonIdentification[@Status='ACCEPTED']/genus_name", Filters.element());
		List<org.jdom2.Element> genusElements = genusExpression.evaluate(jDomDocument);
		for (org.jdom2.Element elem : genusElements) {
			hierarchy=hierarchy+"; genus "+elem.getText();
		}
		XPathExpression<Element> speciesExpression = xFactory.compile("//TaxonIdentification[@Status='ACCEPTED']/species_name", Filters.element());
		List<org.jdom2.Element> speciesElements = speciesExpression.evaluate(jDomDocument);
		for (org.jdom2.Element elem : speciesElements) {
			hierarchy=hierarchy+"; species "+elem.getText();
		}
		XPathExpression<Element> taxonoExpression = xFactory
				.compile("//TaxonIdentification[@Status='ACCEPTED']", Filters.element());
		List<Element> taxonelements = taxonoExpression
				.evaluate(jDomDocument);
		for (Element elem : taxonelements) {
			Element hierarchyTag = new Element("TaxonHierarchy");
			hierarchyTag.setText(hierarchy);
			elem.addContent(hierarchyTag);
		}
		XMLOutputter xout = new XMLOutputter(
				Format.getPrettyFormat());
		xout.outputString(jDomDocument);
		
		
		return jDomDocument;
	}

	private static Document addHierarchy(Document jDomDocument) {
		String hierarchy=" ";
		XPathFactory xFactory = XPathFactory.instance();
		XPathExpression<Element> expression = xFactory.compile("//TaxonIdentification[@Status='ACCEPTED']/*", Filters.element());
		List<org.jdom2.Element> elements = expression
				.evaluate(jDomDocument);
		for (org.jdom2.Element elem : elements) {
			if(elem.getName().endsWith("_name"))
			{
				
//			if(hierarchy!=" ")
//			{
//				hierarchy=hierarchy+"; ";
//			}
				
			hierarchy=  hierarchy+ elem.getName().substring(0,elem.getName().indexOf("_"))+" " + elem.getText() +"; ";
			
			}
		}

		XPathExpression<Element> taxonoExpression = xFactory
				.compile("//TaxonIdentification[@Status='ACCEPTED']", Filters.element());
		List<Element> taxonelements = taxonoExpression
				.evaluate(jDomDocument);
		for (Element elem : taxonelements) {
			Element hierarchyTag = new Element("TaxonHierarchy");
			hierarchyTag.setText(hierarchy);
			elem.addContent(hierarchyTag);
		}
		XMLOutputter xout = new XMLOutputter(
				Format.getPrettyFormat());
		xout.outputString(jDomDocument);
		
		
		return jDomDocument;
	}
	
	private static void correctFiles(String fileName,String newFile, String genusFile) {
		// TODO Auto-generated method stub
		HashMap<String, Integer> genus_names = new HashMap<String, Integer>();
		File input = new File(fileName);
		
		File output = new File(newFile);
		output.delete();
		output.mkdir();
		File familyInfo= new File(newFile+"/Corrected_Files");
		familyInfo.delete();
		familyInfo.mkdir();
		
		PrintWriter writer;
		try {
			
			writer = new PrintWriter(newFile+"/correctedFileList.txt", "UTF-8");
			SAXBuilder jdomBuilder = new SAXBuilder();
			Document jDomDocument;
			try {
				File[] listOfFiles = input.listFiles();
				Arrays.sort(listOfFiles);
				for (File f : listOfFiles) {
					try {
						
						String taxonFileName=f.getName();
						
						if(taxonFileName.endsWith("ceae.xml"))
						{
							jDomDocument = jdomBuilder.build(f);
							XPathFactory xFactory = XPathFactory.instance();
						
							 XPathExpression<Element> expression = xFactory.compile("//TaxonIdentification[@Status='ACCEPTED']/genus_name",
									 Filters.element());
									List<Element> elements = expression
											.evaluate(jDomDocument);
									for (Element elem : elements) {
										String genusName=elem.getText();
										if(genusName.toLowerCase().endsWith("ceae")) {
											elem.setName("family_name");
											Element authElem =  elem.getParentElement().getChild("genus_authority");
											if(authElem != null)
											{
												authElem.setName("family_authority");
											}
											writer.println(taxonFileName);
											XMLOutputter xout = new XMLOutputter(
													Format.getPrettyFormat());
											xout.output(jDomDocument, new FileOutputStream(new File(
													familyInfo, f.getName())));
											xout.output(jDomDocument, new FileOutputStream(new File(
													input, f.getName())));
										}
									}
									
						}
					}catch (JDOMException e) {
						System.out.println(f.getName() + "has jdom exception: "
								+ e.getMessage());
						continue;
					}
					
						
	}
				writer.close();
				writer.flush();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}catch (FileNotFoundException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	} catch (UnsupportedEncodingException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
			}

	/*
	 * Get list of genus names from all files in the folder and write it to a
	 * file called "genus_names.txt"
	 */
	private static void getGenusNames(String fileName,String newFile, String genusFile) {
		// TODO Auto-generated method stub
		HashMap<String, Integer> genus_names = new HashMap<String, Integer>();
		File input = new File(fileName);
		
		File output = new File(newFile);
		output.delete();
		output.mkdir();
		File familyInfo= new File(newFile+"/Genus_Family");
		familyInfo.delete();
		familyInfo.mkdir();
		
		PrintWriter writer;
		try {
						
			writer = new PrintWriter(genusFile, "UTF-8");
			SAXBuilder jdomBuilder = new SAXBuilder();
			Document jDomDocument;
			try {
				File[] listOfFiles = input.listFiles();
				Arrays.sort(listOfFiles);
				for (File f : listOfFiles) {
					try {
						jDomDocument = jdomBuilder.build(f);
						XPathFactory xFactory = XPathFactory.instance();
						 XPathExpression<Element> expression =
						 xFactory.compile("//TaxonIdentification[@Status='ACCEPTED']/genus_name",
						 Filters.element());
						List<Element> elements = expression
								.evaluate(jDomDocument);
						for (Element elem : elements) {
							genus_names.put(elem.getText()+","+f.getName(), 1);
						}
					} catch (JDOMException e) {
						System.out.println(f.getName() + "has jdom exception: "
								+ e.getMessage());
						continue;
					}
				}
				for (String keys : genus_names.keySet()) {
					writer.println(keys);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writer.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private static void getFamilyNamesFromFile(String fileName, String newFile, String genusFile, String genusfamilyFile) {
		// TODO Auto-generated method stub
		HashMap<String, Integer> family_names = new HashMap<String, Integer>();
		
		HashMap<String, Integer> genus_names = new HashMap<String, Integer>();
		
		File input = new File(fileName);
		
		File output = new File(newFile);
		output.delete();
		output.mkdir();
		File familyInfo= new File(newFile+"/Genus_Family");
		familyInfo.delete();
		familyInfo.mkdir();
		
		PrintWriter writer;
		PrintWriter genusWriter;
		try {
				
			writer = new PrintWriter(genusfamilyFile, "UTF-8");
			genusWriter = new PrintWriter(genusFile, "UTF-8");
			SAXBuilder jdomBuilder = new SAXBuilder();
			Document jDomDocument;
			
			int fileCount=input.listFiles().length;
			String path=input.getAbsolutePath();
			
			ArrayList <String> authors =  new ArrayList <String>();
			String familyName = null;
			try {
								
				for(int i=1; i<=fileCount; i++)
				{
					File f = new File(path+"/"+i+".xml");
					try {
						jDomDocument = jdomBuilder.build(f);
						XPathFactory xFactory = XPathFactory.instance();
						
						XPathExpression<Element> expression = xFactory.compile(
								"//TaxonIdentification[@Status='ACCEPTED']/family_name", Filters.element());
						List<Element> elements = expression
								.evaluate(jDomDocument);
						for (Element elem : elements) {
							
							familyName = elem.getText();
							family_names.put(elem.getText()+","+f.getName(), 1);
							writer.println(elem.getText()+","+f.getName());
							
							authors.clear();
							XPathExpression<Element> authorExpression = xFactory.compile(
									"//author", Filters.element());
							List<Element> authorElements = authorExpression
									.evaluate(jDomDocument);
							for (Element authElem : authorElements) {
							
								authors.add(authElem.getText());
							
							}
							XMLOutputter xout = new XMLOutputter(
									Format.getPrettyFormat());
							xout.output(jDomDocument, new FileOutputStream(new File(
									output, f.getName())));
							
						}
						if (elements.isEmpty()) {
							
							XPathExpression<Element> genusExpression =
									 xFactory.compile("//TaxonIdentification[@Status='ACCEPTED']/genus_name",
									 Filters.element());
									List<Element> genusElements = genusExpression
											.evaluate(jDomDocument);
									for (Element elem : genusElements) {
										genus_names.put(elem.getText()+","+familyName+","+f.getName(), 1);
										genusWriter.println(elem.getText()+","+familyName+","+f.getName());
									}
							
							XPathExpression<Element> taxonoExpression = xFactory
									.compile("//TaxonIdentification", Filters.element());
							List<Element> taxonelements = taxonoExpression
									.evaluate(jDomDocument);
							for (Element elem : taxonelements) {
								Element familyTag = new Element("family_name");
								familyTag.setText(familyName);
								elem.addContent(0,familyTag);
								
								for(String auths: authors)
								{
									Element authorTag = new Element("author");
									authorTag.setText(auths);
									elem.addContent(authorTag);
								}
							}
							XMLOutputter xout = new XMLOutputter(
									Format.getPrettyFormat());
							xout.output(jDomDocument, new FileOutputStream(new File(
									output, f.getName())));
							
						}
					} catch (JDOMException e) {
						System.out.println(f.getName() + "has jdom exception: "
								+ e.getMessage());
						continue;
					}
				}
//				for (String keys : family_names.keySet()) {
//					writer.println(keys);
//				}
//				for( String keys: genus_names.keySet()) {
//					genusWriter.println(keys);
//				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writer.close();
			genusWriter.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	private static void addFamilyTag(String fileName, String genusFile,
			String genusfamilyFile, String newFile) {
		// TODO Auto-generated method stub
		HashMap<String, Integer> genus_names = new HashMap<String, Integer>();
		File input = new File(fileName);
		PrintWriter writer;
		
	
		File output = new File(newFile);

		if(!output.exists())
		{
		output.mkdir();
		}
		
		try {
			
			SAXBuilder jdomBuilder = new SAXBuilder();
			Document jDomDocument;
			try {
				for (File f : input.listFiles()) {
					try {
						jDomDocument = jdomBuilder.build(f);
						XPathFactory xFactory = XPathFactory.instance();
						
						XPathExpression<Element> familyTagExpression = xFactory.compile(
								"//family_name", Filters.element());
						List<org.jdom2.Element> familyTagelements = familyTagExpression.evaluate(jDomDocument);
						
						if(familyTagelements.isEmpty() )
						{

						XPathExpression<Element> expression = xFactory.compile("//TaxonIdentification[@Status='ACCEPTED']/genus_name", Filters.element());
						List<org.jdom2.Element> elements = expression
								.evaluate(jDomDocument);
						for (org.jdom2.Element elem : elements) {
							// genus_names.put(elem.getText(), 1);
							String genus = elem.getText();
							
							FileReader reader;
							reader = new FileReader(genusfamilyFile);
							String genusName;
							BufferedReader bufferedReader = new BufferedReader(
									reader);
							while ((genusName = bufferedReader.readLine()) != null) {
								String familyName = genusName.substring(genusName.indexOf(",") + 1,genusName.indexOf(",",genusName.indexOf(",")+1) );
								genusName = genusName.substring(0,
										genusName.indexOf(","));
								if (genusName.toLowerCase().equals(
										genus.toLowerCase())) {
									XPathExpression<Element> taxonoExpression = xFactory
											.compile("//TaxonIdentification[@Status='ACCEPTED']", Filters.element());
									
									List<Element> taxonelements = taxonoExpression
											.evaluate(jDomDocument);
									for (Element taxonelem : taxonelements) {
										
										XPathExpression<Element> familyExpression = xFactory
												.compile("//TaxonIdentification[@Status='ACCEPTED']//family_name", Filters.element());
										List<Element> familyelements = familyExpression
												.evaluate(jDomDocument);
										if( familyelements.isEmpty() )
										{
										Element familyTag = new Element("family_name");
										familyTag.setText(familyName);
										taxonelem.addContent(0,familyTag);
										}
										XMLOutputter xout = new XMLOutputter(
												Format.getPrettyFormat());
										xout.output(jDomDocument, new FileOutputStream(new File(
												output, f.getName())));
									//	f.delete();
										
									//break;

								}

							}
							}
							break;
						}
					}
					else
						{
							XMLOutputter xout = new XMLOutputter(
									Format.getPrettyFormat());
							xout.output(jDomDocument, new FileOutputStream(new File(
									output, f.getName())));
							//f.delete();
						}
						
						
						
					} catch (JDOMException e) {
						System.out.println(f.getName() + "has jdom exception: "
								+ e.getMessage());
						continue;
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}

	private static void arrangeFiles(String folderName, String volume) {
		// TODO Auto-generated method stub
		HashMap<String, Integer> genus_names = new HashMap<String, Integer>();
		File input = new File(folderName);
		File output = new File("Families");
		if(!output.exists()) {
			output.mkdir();
		}
		
		PrintWriter writer;
		try {
			writer = new PrintWriter(folderName + "/files_names.txt", "UTF-8");
			SAXBuilder jdomBuilder = new SAXBuilder();
			Document jDomDocument;
			try {
				for (File f : input.listFiles()) {
					try {
						jDomDocument = jdomBuilder.build(f);
						XPathFactory xFactory = XPathFactory.instance();
						 XPathExpression<Element> expression =
						 xFactory.compile("//taxon_identification[@status='ACCEPTED']/taxon_name[@rank='family']",
						Filters.element());
						
						List<org.jdom2.Element> elements = expression
								.evaluate(jDomDocument);
						for (org.jdom2.Element elem : elements) {
							// genus_names.put(elem.getText(), 1);
							String family = elem.getText();
							String dirPath = "Families/"+ family ;
							File dir = new File(dirPath);
							
							if(!dir.exists()) {
				                // Create directory
				                if(dir.mkdirs()) {
				                    System.out.println("Directory Created");
				                }
							}	
				             Files.copy(f.toPath(),	(new File(dirPath+"/"+ volume+"_" + f.getName())).toPath()
				            		 ,StandardCopyOption.REPLACE_EXISTING);
									writer.println(volume+"/" + f.getName()+" copied to "+ family);

								

							}

						
					} catch (JDOMException e) {
						System.out.println(f.getName() + "has jdom exception: "
								+ e.getMessage());
						continue;
					}
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writer.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	
	
	private static void copyFiles(String fileName) {
		// TODO Auto-generated method stub
		HashMap<String, Integer> genus_names = new HashMap<String, Integer>();
		File input = new File(fileName);
		PrintWriter writer;
		try {
			writer = new PrintWriter(fileName + "/genus_names.txt", "UTF-8");
			SAXBuilder jdomBuilder = new SAXBuilder();
			Document jDomDocument;
			try {
				for (File f : input.listFiles()) {
					try {
						jDomDocument = jdomBuilder.build(f);
						XPathFactory xFactory = XPathFactory.instance();
						 XPathExpression<Element> expression =
						 xFactory.compile("//taxon_identification[@status='ACCEPTED']/taxon_name[@rank='family_name']",
						Filters.element());
						
						List<org.jdom2.Element> elements = expression
								.evaluate(jDomDocument);
						for (org.jdom2.Element elem : elements) {
							// genus_names.put(elem.getText(), 1);
							String genus = elem.getText();
							//
							FileReader reader;
							reader = new FileReader(new File(fileName
									+ "/genus-family.txt"));
							String genusName;
							BufferedReader bufferedReader = new BufferedReader(
									reader);
							while ((genusName = bufferedReader.readLine()) != null) {
								String familyName = genusName
										.substring(genusName.indexOf(",") + 1);
								genusName = genusName.substring(0,
										genusName.indexOf(","));
								if (genusName.toLowerCase().equals(
										genus.toLowerCase())) {
									String path = "/home/biosemantics/TaxonFamily/Families/"
											+ familyName + "/";
									Files.copy(f.toPath(),
											(new File(path + f.getName()))
													.toPath(),
											StandardCopyOption.REPLACE_EXISTING);
									break;

								}

							}

						}
					} catch (JDOMException e) {
						System.out.println(f.getName() + "has jdom exception: "
								+ e.getMessage());
						continue;
					}
				}
				for (String keys : genus_names.keySet()) {
					writer.println(keys);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writer.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}
