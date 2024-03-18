package date_restaurante;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class main {
	private static String key = "AIzaSyCIDTZwCsxUDF2XHUYFLTwrOG8TIxJXo38";

	public static void main(String[] args) throws InvalidFormatException, IOException, ParseException
	{
		XSSFWorkbook details_wb = new XSSFWorkbook();
		XSSFSheet details_sheet = details_wb.createSheet();
		
		XSSFWorkbook wb = new XSSFWorkbook(new File("places_3.xlsx"));
		XSSFSheet sheet = wb.getSheetAt(0);
		
		Iterator<Row> iterator = sheet.rowIterator();
		iterator.next();
		
		CloseableHttpClient client = HttpClients.createDefault();
		int row_count,cell;
		row_count = 0;
		while (iterator.hasNext())
		{
			
			Row row = iterator.next();
			String place_id = row.getCell(1).getStringCellValue();
			HttpPost post = new HttpPost("https://maps.googleapis.com/maps/api/place/details/json?place_id="+place_id+"&key="+key);
			System.err.println(place_id);
			CloseableHttpResponse response = client.execute(post);
			String json2 = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
			JSONParser parser = new JSONParser();    
			JSONObject main_obj = (JSONObject) ((JSONObject) parser.parse(json2)).get("result");
			//System.err.println(json2);
			Row cr_row = details_sheet.createRow(row_count);
			System.err.println(row_count);
			cell =0;
			Iterator<Cell> cell_iterator = row.cellIterator();
			
			while (cell_iterator.hasNext())
			{
				String cell_type = cell_iterator.next().getCellType().toString();
				//System.err.println(cell_type);
				try {
				cr_row.createCell(cell).setCellValue(cell_iterator.next().getStringCellValue());
				}
				catch(Exception e)
				{
					cr_row.createCell(cell).setCellValue(cell_iterator.next().getRichStringCellValue());

				}
				cell++;
			}

			cr_row.createCell(cell).setCellValue(main_obj.get("name").toString());
			cell++;
			System.err.println(main_obj);
			if (main_obj.get("geometry")!=null)
			{
				
				cr_row.createCell(cell).setCellValue(main_obj.get("geometry").toString());
				cell++;
			}
			if (main_obj.get("formatted_phone_number")!=null)
			{
				cr_row.createCell(cell).setCellValue(main_obj.get("formatted_phone_number").toString());
				cell++;				
			}
			
			if (main_obj.get("formatted_address")!=null)
			{
				cr_row.createCell(cell).setCellValue(main_obj.get("formatted_address").toString());
				cell++;				
			}
			
			if (main_obj.get("opening_hours")!=null)
			{
				cr_row.createCell(cell).setCellValue(main_obj.get("opening_hours").toString());
				cell++;				
			}
			
			if (main_obj.get("reviews")!=null)
			{
				cr_row.createCell(cell).setCellValue(main_obj.get("reviews").toString());
				cell++;				
			}
			
			if (main_obj.get("website")!=null)
			{
				cr_row.createCell(cell).setCellValue(main_obj.get("website").toString());
				cell++;				
			}

			row_count++;
			//break;
		}
		FileOutputStream out = new FileOutputStream("details_3_19dec.xlsx");
		details_wb.write(out);
	}
}
