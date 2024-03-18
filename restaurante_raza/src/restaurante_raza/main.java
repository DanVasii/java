package restaurante_raza;

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
	private static String key = "AIzaSyAsulFOxSCM7O7uc72Sxff6xSuztTkUXhA";
	private static CloseableHttpClient client;
	private static int count = 0;
	public static PreparedStatement prep;
	public static void main(String[] args) throws SQLException, ClientProtocolException, IOException, ParseException, InterruptedException
	{
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/farmvio_aux","root","");
		prep = con.prepareStatement("INSERT INTO places_to_search (place_id,place_name,types,rating,user_ratings) values (?,?,?,?,?) ");
	 client = HttpClients.createDefault();
		
		Statement stmt = con.createStatement();
		
		String query = "SELECT * FROm circles";
		ResultSet rs = stmt.executeQuery(query);
		
		while (rs.next())
		{
			search_places(rs.getInt("radius"),rs.getString("lat"),rs.getString("lng"),null);
		}
	}
	
	public static void search_places(int radius,String lat,String lng,String token) throws ClientProtocolException, IOException, ParseException, InterruptedException, SQLException
	{
		String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+lat+"%2C"+lng+"&radius="+radius+"&type=grocery_or_supermarket&key="+key;
			
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
	    Thread.sleep(2000);
	    if (count/20<=7000 && token_got!=null && token_got.trim()!="")
	    search_places(radius,lat,lng,token_got);
	    
	    
	}
}
