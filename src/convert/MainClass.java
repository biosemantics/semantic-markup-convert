import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import convert.Converter;


public class MainClass {

	public static void main(String[] args) {
		convert();
		//Validator();
	}
	
	
	static void convert(){
		//Give input folder here
		File input = new File("ETC-FNA-v2_output3_familycopied_authoritycopied");
		//Output folder name here
		File output = new File("ETC-FNA-v2_output_final");
		output.delete();
		output.mkdir();
		
		SAXBuilder jdomBuilder = new SAXBuilder();
        Document jdomDocument;
		try {
			for(File f : input.listFiles()) {
				try {
					jdomDocument = jdomBuilder.build(f);
					//Different methods in Converter can be called
					// The parameters to the methods need to be constructed here
					// Can also be done as:
					// renameTags(jdomDocument); - check the renameTags function below
					// renameAttributes(jdomDocument); - check the renameAttributes function below 
					jdomDocument = Converter.addMetaTag(jdomDocument);
					jdomDocument = Converter.correctTaxonNames(jdomDocument, authorityList);
					jdomDocument = Converter.correctDescriptions(jdomDocument);
					XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
					xout.output(jdomDocument, new FileOutputStream(new File(output, f.getName())));
				} catch (JDOMException e) {
					System.out.println(f.getName()+"has jdom exception: "+e.getMessage());
					continue;
				}
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static Document renameTags(Document jdomDocument){
		HashMap<String, String> namesMap = new HashMap<String, String>();
		
		namesMap.put("//TaxonIdentification", "taxon_identification");
		namesMap.put("//TaxonHierarchy", "taxon_hierarchy");
		
		return Converter.renameTags(jdomDocument, namesMap);
	}
	
	static Document renameAttributes(Document jdomDocument){
		HashMap<String, String> namesMap = new HashMap<String, String>();
		
		namesMap.put("//taxon_identification/@Status", "status");
		
		return Converter.renameAttributes(jdomDocument, namesMap);
	}
	
	static Document reArrangeTags(Document jDomDocument){
		HashMap<String, String> arrangementMap = new HashMap<String, String>();
		
		arrangementMap.put("//taxon_hierarchy", "//taxon_identification");
		
		return Converter.rearrangeTags(jDomDocument, arrangementMap);
	}

	static Document convert_nonEmptyTag(Document jdomDocument){
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
		
		return Converter.correctNonEmptyTags(jdomDocument, paths);
		
	}
	
	/*
	Get list of genus names from all files in the folder and write it to a file called "genus_names.txt" 
	*/
	private static void getGenusNames() {
		// TODO Auto-generated method stub
		HashMap<String, Integer> genus_names= new HashMap<String, Integer>();
		File input = new File("ETC-FNA-v2_output3_1");
		PrintWriter writer;
		try {
			writer = new PrintWriter("genus_names.txt", "UTF-8");
			SAXBuilder jdomBuilder = new SAXBuilder();
	        Document jdomDocument;
			try {
				for(File f : input.listFiles()) {
					try {
						jdomDocument = jdomBuilder.build(f);
						XPathFactory xFactory = XPathFactory.instance();
						XPathExpression<Element> expression = xFactory.compile("//taxon_identification[@status='ACCEPTED']/taxon_name[@rank='genus']", Filters.element());
						List<Element> elements = expression.evaluate(jdomDocument);
						for(Element elem: elements){
							genus_names.put(elem.getText(), 1);
						}
					} catch (JDOMException e) {
						System.out.println(f.getName()+"has jdom exception: "+e.getMessage());
						continue;
					}
				}
				for(String keys: genus_names.keySet()){
					writer.println(keys);
				}
			}catch (IOException e) {
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
	
	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

}
