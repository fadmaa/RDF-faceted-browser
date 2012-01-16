package org.deri.rdf.browser.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

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
}
