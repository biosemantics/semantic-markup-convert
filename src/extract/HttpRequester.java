package extract;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class HttpRequester {

	public HttpRequester() {
		
	}

	public static Document httpRequestURL(String targetUrl){
		URL obj;
		Document doc = null;
		try {
			obj = new URL(targetUrl);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			 
			// optional default is GET
			con.setRequestMethod("GET");
	 
			//add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
	 		
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
						
			doc =  db.parse(con.getInputStream());		

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return doc;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FileReader reader;
		HashMap<String,String> genusFamily = new HashMap<String, String>();
		try {
			PrintWriter writer = new PrintWriter(new File("genus-family.txt"));
			reader = new FileReader(new File("genus_names.txt"));
			BufferedReader bf = new BufferedReader(reader);
			String genus;
			while((genus = bf.readLine()) != null){
				genus = genus.trim();
				genus = genus.toLowerCase();
				String targetUrl = "http://efloras.org/browse.aspx?floral_id=1&name_str="+genus+"&btnSearch=Search";
				org.jsoup.nodes.Document doc = Jsoup.connect(targetUrl).get();
				Elements aElement = doc.select("#ucFloraTaxonList_panelTaxonList span table tbody tr.underline td.small");
				try{
					String pageUrl = "http://efloras.org/florataxon.aspx?flora_id=1&taxon_id="+aElement.get(0).text();
					doc = Jsoup.connect(pageUrl).get();
					aElement = doc.select("span#lblTaxonChain a");
					String familyname;
					familyname = aElement.get(aElement.size()-2).text();
					System.out.println(familyname);
					genusFamily.put(genus, familyname);
				}catch(IndexOutOfBoundsException e){
					System.out.println("not found: "+ genus);
					continue;
				}
				/*Document response = httpRequestURL(targetUrl);
				String pageUrl = getPageUrl(response, genus);
				response = httpRequestURL(pageUrl);
				String familyname = getFamilyName(response);*/
			}
			for(String genusname: genusFamily.keySet()){
				writer.println(genusname+","+genusFamily.get(genusname));
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private static String getFamilyName(Document response) {
		String familyname = "";
		Element elem = response.getDocumentElement();
		NodeList list = elem.getElementsByTagName("span");
		for(int i=0; i < list.getLength(); i++){
			Node n = list.item(i);
			if(n.getAttributes().getNamedItem("id").getTextContent() == "lblTaxonChain"){
				NodeList alist = n.getChildNodes();
				familyname = alist.item(3).getTextContent();
			}
		}
		return familyname;
	}

	private static String getPageUrl(Document response, String genus) {
		String pageUrl = "http://efloras.org/";
		Element elem = response.getDocumentElement();
		NodeList list = elem.getElementsByTagName("a");
		for(int i=0; i < list.getLength(); i++){
			Node n = list.item(i);
			if(n.getTextContent().toLowerCase() == genus){
				pageUrl+=n.getAttributes().getNamedItem("href").getTextContent();
				System.out.println(pageUrl);
				break;
			}
		}
		return pageUrl;
	}

}
