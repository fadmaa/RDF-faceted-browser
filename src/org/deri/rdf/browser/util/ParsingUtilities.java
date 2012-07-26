package org.deri.rdf.browser.util;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ParsingUtilities {

	 static public JSONObject evaluateJsonStringToObject(String s) throws JSONException {
	        if( s == null ) {
	            throw new IllegalArgumentException("parameter 's' should not be null");
	        }
	        JSONTokener t = new JSONTokener(s);
	        Object o = t.nextValue();
	        if (o instanceof JSONObject) {
	            return (JSONObject) o;
	        } else {
	            throw new JSONException(s + " couldn't be parsed as JSON object");
	        }
	    }

	    static public JSONArray evaluateJsonStringToArray(String s) throws JSONException {
	        JSONTokener t = new JSONTokener(s);
	        Object o = t.nextValue();
	        if (o instanceof JSONArray) {
	            return (JSONArray) o;
	        } else {
	            throw new JSONException(s + " couldn't be parsed as JSON array");
	        }
	    }
	    
	    static public String varname(String s){
	    	return s.toLowerCase().replaceAll("\\s+", "_").replaceAll("[^_a-zA-Z0-9]", "");
	    }
	    
	    static public Double replaceCommas(String s){
	    	//remove commas e.g. 2,799,300 becomes 2799300
	    	//exception 0,444 becomes 0.444
	    	Matcher m = p.matcher(s);
		    if (m.matches()){
		    	return Double.parseDouble(s.replace(",", "."));
		    }else{
		    	return Double.parseDouble(s.replaceAll(",", ""));
		    }
	    }
	    
	    static public String putCommasBack(Double d){
	    	return String.valueOf(d).replace(".", ",");
	    }
	    
	    static public Set<String> getProperties(String template) throws XPathExpressionException{
			Set<String> properties = new HashSet<String>();
			if(template.isEmpty()){
				return properties;
			}
	        XPath xpath = XPathFactory.newInstance().newXPath();
	        String expression = "//*/@sparql_content";
	        InputSource inputSource = new InputSource(new StringReader(template));
	        NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
	        for(int i=0;i<nodes.getLength();i++){
	        	Node n = nodes.item(i);
	        	properties.addAll(Arrays.asList(n.getNodeValue().split(" ")));
	        }
	        
	        String attrExpression = "//*/@sparql_attribute";
	        inputSource = new InputSource(new StringReader(template));
	        nodes = (NodeList) xpath.evaluate(attrExpression, inputSource, XPathConstants.NODESET);
	        for(int i=0;i<nodes.getLength();i++){
	        	Node n = nodes.item(i);
	        	String s = n.getNodeValue();
	        	properties.addAll(Arrays.asList(s.substring(s.indexOf(":")+1).split(" ")));
	        }
	        
	        return properties;
		}
	    
	    private static Pattern p = Pattern.compile("^0,\\d+");
}
