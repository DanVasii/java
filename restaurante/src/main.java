

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.*;

import com.mysql.cj.jdbc.Driver;
import com.mysql.cj.xdevapi.JsonArray;

public class main {
	private static String key = "AIzaSyCIDTZwCsxUDF2XHUYFLTwrOG8TIxJXo38";
	private static CloseableHttpClient client;
	private static int count = 0;
	public static PreparedStatement prep;
	public static int max_requests;
	public static void main(String[] args) throws SQLException, ClientProtocolException, IOException, ParseException, InterruptedException
	{
		String[] cats = {"grocery_or_supermarket","meal_delivery","meal_takeaway","restaurant","supermarket"};
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/farmvio","root","");
		prep = con.prepareStatement("INSERT INTO places_to_search (place_id,place_name,types,rating,user_ratings,loc) values (?,?,?,?,?,'Bucuresti') ");
	 client = HttpClients.createDefault();
		
	 FileWriter writer = new FileWriter("raport_request_19dec.txt");
	 
		Statement stmt = con.createStatement();
		
		String query = "SELECT * FROm circles_buc WHere id > 498";
		ResultSet rs = stmt.executeQuery(query);
		int circle=0;
		 max_requests = 0;
		while (rs.next())
		{
			writer.write("La id "+rs.getInt("id")+ " avem "+max_requests);
			writer.write('\n');
			System.err.println("La id "+rs.getInt("id")+ " avem "+max_requests);
			if (max_requests>=2500)
			{
				System.err.println(rs.getInt("id"));
				
				writer.write(rs.getInt("id"));
				writer.write('\n');
				writer.close();
				break;
			}
			else {
				for (String cat : cats)
			search_places(cat,rs.getInt("radius"),rs.getString("lat"),rs.getString("lng"),null);
			System.err.println(circle);
			circle++;
			}
		}
	}
	
	public static void search_places(String type,int radius,String lat,String lng,String token) throws ClientProtocolException, IOException, ParseException, InterruptedException, SQLException
	{
		max_requests++;
		String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+lat+"%2C"+lng+"&radius="+radius+"&type="+type+"&key="+key;
			
		System.err.println(url);
		if (token!=null && token.trim()!="")
		{
			url+="&pagetoken="+token.trim();
		}
		HttpPost main_post = new HttpPost(url);
		
	    CloseableHttpResponse response = client.execute(main_post);
	     
	    String json2 = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
	    
	    JSONParser parser = new JSONParser();
	    
	    JSONObject main_obj = (JSONObject) parser.parse(json2);
	    
	    JSONArray results =  (JSONArray) main_obj.get("results");
	    String place_id,place_name,types,rating;
	    int user_ratings;
	    String token_got;
	    if (main_obj.get("next_page_token")!=null)
	     token_got = main_obj.get("next_page_token").toString();
	    else
	     token_got = "";
	    count+=results.size();
	    for (int i = 0;i<results.size();i++)
	    {
	    	JSONObject place_obj = (JSONObject) results.get(i);
	    	if (place_obj.get("name") != null)
	    		place_name =  place_obj.get("name").toString();
	    	else
	    	{
	    		place_name = "";
	    	}
	    	
	    	if (place_obj.get("place_id") != null)
	    		place_id =  place_obj.get("place_id").toString();
	    	else
	    	{
	    		place_id = "";
	    	}
	    	
	    	if (place_obj.get("rating") != null)
	    		rating =  place_obj.get("rating").toString();
	    	else
	    	{
	    		rating = "";
	    	}
	    	
	    	if (place_obj.get("types") != null)
	    		types =  place_obj.get("types").toString();
	    	else
	    	{
	    		types = "";
	    	}
	    	
	    	if (place_obj.get("user_ratings_total") != null)
	    		user_ratings =  Integer.parseInt(place_obj.get("user_ratings_total").toString());
	    	else
	    	{
	    		user_ratings = 0;
	    	}
	    	
	    	prep.setString(1, place_id);
	    	prep.setString(2, place_name);
	    	prep.setString(3, types);
	    	prep.setString(4,rating);
	    	prep.setInt(5, user_ratings);
	    	prep.execute();
	    	
	    }
	    System.err.println(count);
	    System.err.println(token_got);
	    Thread.sleep(500);
	    if ( token_got!=null && token_got.trim()!="")
	    search_places(type,radius,lat,lng,token_got);
	    
	    
	}
}
