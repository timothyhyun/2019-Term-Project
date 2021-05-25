/*
 * Timothy Hyun
 * Commander Schenk
 * AP Computer Science A
 * Master Project
 */
package application;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Utility {
public static Long decodedLong;
public static String decodedString;	
public static List<String> decodedArrayList = new ArrayList<String>();
public static List<JSONObject> objectsz = new ArrayList<JSONObject>();
public static List<String> objectString = new ArrayList<String>();
    public static String get(String url) throws MalformedURLException,
            ProtocolException, IOException {
    	HttpURLConnection con = null;
        //String url = "http://www.something.com";

        try {

            URL myurl = new URL(url);
            con = (HttpURLConnection) myurl.openConnection();

            con.setRequestMethod("GET");

            StringBuilder content;

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {

                String line;
                content = new StringBuilder();

                while ((line = in.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }

            return content.toString();

        } finally {
            
            con.disconnect();
        }
        
    }
    
    public static String post(String url, String urlParameters) throws MalformedURLException,
    ProtocolException, IOException {
    	HttpURLConnection con = null;
	   
    	byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

    	try {

    		URL myurl = new URL(url);
    		con = (HttpURLConnection) myurl.openConnection();

    		con.setDoOutput(true);
    		con.setRequestMethod("POST");
    		con.setRequestProperty("User-Agent", "Java client");
    		con.setRequestProperty("Content-Type", "application/json");

    		try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
    			wr.write(postData);
    		}

    		StringBuilder content;

    		try (BufferedReader in = new BufferedReader(
    				new InputStreamReader(con.getInputStream()))) {

    			String line;
    			content = new StringBuilder();

    			while ((line = in.readLine()) != null) {
    				content.append(line);
    				content.append(System.lineSeparator());
    			}
    		}

    		return content.toString();

    	} finally {
    
    		con.disconnect();
    	}
    }
    public static void decodeJSON(String json, String parameters, String returnType) throws ParseException {
    	
    		Object objects = new JSONParser().parse(json);
    		JSONObject jo = (JSONObject) objects;
    		if (returnType == "Long") {
    			Long id = (Long)jo.get(parameters);
    			decodedLong = id;
    		} else if (returnType == "String"){
    			
    			String ids = jo.get(parameters).toString();
    			decodedString = ids;
    		}
    }
    
    public static void decodeJSONArray(String json, String parameters) throws ParseException {
    	Utility.objectsz.clear();
    	Object objects = new JSONParser().parse(json);
    	JSONObject jo = (JSONObject) objects;
    	 JSONArray arrays = (JSONArray)jo.get(parameters);
    	 if (arrays.size()==0) {
    		 return;
    	 }
    	 for (int i = 0; i < arrays.size(); i++) {
    		 JSONObject temp = (JSONObject)arrays.get(i);
    		 objectsz.add(temp);
    	 }
    	
    	
    }
    public static void decodeJSONArrayStrings(String json, String parameters) throws ParseException{
    	Utility.objectString.clear();
    	Object objects = new JSONParser().parse(json);
    	JSONObject jo = (JSONObject) objects;
    	 JSONArray arrays = (JSONArray)jo.get(parameters);
    	 if (arrays.size()==0) {
    		 return;
    	 }
    	 for (int i = 0; i < arrays.size(); i++) {
    		String temp = arrays.get(i).toString();
    		objectString.add(temp);
    		 
    	 }

}
}