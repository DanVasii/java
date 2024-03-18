package crawler_srl;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;



public class main {
	public static WebDriverWait waiter;
	public static WebDriver driver;
	public static int i = 0;
	public static int j = 0;
	public static void main(String[] args) throws InterruptedException, ClassNotFoundException, SQLException
	{
		
		Class.forName("com.mysql.jdbc.Driver");
		
		Connection con = DriverManager.getConnection(	"jdbc:mysql://localhost:3306/farmvio","root","");

		String query = "SELECT * FROM dragos_srl";
		Statement stmt = con.createStatement();
		
		ResultSet rs = stmt.executeQuery(query);
		System.setProperty("webdriver.chrome.driver", ".\\driver\\chromedriver.exe");
		//set the caching folder 
		ChromeOptions opts = new ChromeOptions();
		opts.addArguments("user-data-dir=C:/users/Dan/Desktop/dan_selenium_profile");
		
		 driver = new ChromeDriver(opts);
		 waiter = new WebDriverWait(driver, 20000);
		
		driver.get("https://www.risco.ro/autentificare");
		
		WebElement button = waiter.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"auth\"]/input[2]")));
		
		Thread.sleep(2000);
		
		button.click();
		//press autentificare 
		while (rs.next())
		{
			j++;
			//we only need the name and the localitate cols
			String nume_firma_feadr = rs.getString("Denumire").trim();
			String localitate_firma = rs.getString("Localitate");
			search_firma(nume_firma_feadr,1,localitate_firma);
			
			//return;
		}
		
	}

	
	public static void search_firma(String nume,int index_inceput,String oras)
	{
		String nume_cautat = nume.substring(index_inceput);
		nume_cautat = nume_cautat.replace("S.R.L", "SRL");
		nume_cautat = nume_cautat.replace("S.R.L.", "SRL ");
		nume_cautat = nume_cautat.replace(".S.R.L.", " SRL ");
		nume_cautat = nume_cautat.replace("S.C.", "SC ");
		nume_cautat = nume_cautat.replace(".S.C.", " SC ");
		nume_cautat = nume_cautat.replaceAll("\\.", " ");
		if (nume_cautat.length()>3) {
		//search for this 
		WebElement input = driver.findElement(By.id("cautareDupaNumeVanzariInput"));
		input.clear();
		input.sendKeys(nume_cautat);
		
		WebElement results_list = waiter.until(ExpectedConditions.presenceOfElementLocated(By.id("as_ul")));
		
		//check if we have errors 
		if (driver.findElements(By.className("as_warning")).size()!=0) {
			//we have errors 
			//retry search 
			if (index_inceput == nume.length()-1) {
				//no need 
				//do nothing 
			}
			else
			{
				search_firma(nume, index_inceput+1,oras);
			}
		}
		else
		{
			//get all the results and get the best one 
			WebElement ul = driver.findElement(By.id("as_ul"));
			List<WebElement> lis = ul.findElements(By.tagName("li"));
		//foreach li show text 
			for (WebElement li : lis) {
				
				if (StringUtils.stripAccents(li.getText()).toLowerCase().contains(oras.toLowerCase()))
				{
					System.err.println(StringUtils.stripAccents(li.getText()));
				i++;
				System.err.println(i+" out of "+j);
				}
				
			}
		}
	}
	}
}


