import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Httpc 
{
	private final String USER_AGENT = "Mozilla/5.0";
	private final String ACCEPT_LANG = "en-US,en;q=0.5";
	
	//Maybe easier if we group them into a class and just pass the class as reference
	@Argument (required = false, index = 0, usage = "Protocol Type - get|post")
	private String protocolType;
	@Option (name = "-v", usage = "Verbose Option - ON|OFF")
	private Boolean verboseReport;
	@Option(name = "-h")
	private Map <String, String> headerArguments = new <String, String> HashMap();
	@Option(name = "-d", forbids={"-f"})
	private Map <String, String> inlineData = new <String, String> HashMap();
	@Option(name = "-f", forbids={"-d"})
	private File fileName;
	@Option(name = "-o", usage = "Output server response to a txt file.")
	private static File outputFileName;
	@Argument (required = false, index = 1, usage = "URL - Http://...")
	private String testURL;
	@Option (name = "-help", help = true, usage = "Help message.")
	private boolean helpTerm;

	
	
	public static void main(String[] args) throws Exception
	{
		System.out.println("System starting ...");
		new Httpc().doMain(args); //Parse the arguments into variable
	}
	
	public void doMain(String[] args) throws Exception 
	{
		//Hacky hack so user can use key:value format in CMD instead of = assignment
		for(int i = 0; i < args.length; i++)
		{
			args[i] = args[i].replaceAll(":", "=");
		}

        CmdLineParser parser = new CmdLineParser(this);
        
        try 
        {
            parser.parseArgument(args);
                       
            if (helpTerm == true)
            {
            	System.out.println("httpc is a curl-like application but supports HTTP protocol only.");
            	System.out.println("Usage:");
            	System.out.println("       httpc command [arguments]");
            	System.out.println("The commands are:");
            	System.out.println("       get   executes a HTTP GET request and prints the response."
            			+ "\n       post   executes a HTTP POST request and prints the response."
            			+ "\n       help   prints this screen");
            	
            	System.out.println();
            	parser.printUsage(System.out);
            	return;          	
            }
            
            if (!(protocolType.toLowerCase().equals("get") || (protocolType.toLowerCase().equals("post"))))
            {
            	//parser.printUsage(System.out);
            	throw new CmdLineException(parser,"Missing or error HTTP protocol.");
            }
            
            if (testURL == null)
            {
            	//parser.printUsage(System.out);
            	throw new CmdLineException(parser,"Missing URL.");
            }     
        } 
        catch( CmdLineException e ) 
        {
            System.err.println(e.getMessage());
            System.err.println("httpc (get|post) [-v] (-h \"k:v\")* [-d inline-data] [-f file] URL");
            parser.printUsage(System.err);
            System.err.println();

            return;
        }
                    
        Httpc http = new Httpc();
              
        if(verboseReport == null)
        {
        	setVerboseReport(false);
        }
                     
        if (protocolType.equals("get"))
        {
        	String getParameter = "";
        	http.sendGet(testURL, verboseReport, headerArguments, getParameter);   	
        }
        else if (protocolType.equals("post")) 
        {   
        	System.out.println("Post Request received. Processing ...");
        	
        	if (inlineData.size() > 0)
        	{
        		System.out.println("Going inline ...");
        		http.sendPost(testURL, verboseReport, headerArguments, inlineData); 
        	}
        	else if (fileName != null)
        	{
        		System.out.println("Going file ..." + fileName);
        		http.sendPost(testURL, verboseReport, headerArguments, fileName);   
        	} 	  	
        }
    }

	/* 1. Set URL
	 * 2. Open connection
	 * 3. Set Header Type Details
	 * 4. Set Response Code variable (Optional)
	 * 5. Output response stream
	 */
	private void sendGet(String urlString, Boolean verbose, Map<String, String> header, String getParameter) throws Exception
	{	
		String url = urlString.concat(getParameter);
		url = urlString.replaceAll("=", ":");
		
		System.out.println("\nSending 'GET' request to URL : " + url);
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		//Set connection allow redirect
		con.setInstanceFollowRedirects(true); 
		HttpURLConnection.setFollowRedirects(true);
		
		//Add Request Header Details
		con.setRequestMethod("GET"); //Optional. Default GET.	
		if(header == null)
		{
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", ACCEPT_LANG);
		}
		else
		{			
			for( String key : header.keySet() )
	        {
	        	con.setRequestProperty(key, header.get(key));
	        }		
		}
		
		isRedirect(con, verbose);	//Check if the connection is redirect	
		isVerbose(con, verbose);    //Check if the verbose option is on
		
		//Display output
		outputResponse(con, url, con.getResponseCode());
	}
	
	/* 1. Set URL
	 * 2. Open connection
	 * 3. Set Header Details
	 * 4. Set POST Parameters
	 * 5. Send POST Request
	 * 6. Output response stream
	 */
	private void sendPost(String urlString, Boolean verbose, Map<String, String> header, Map<String, String> inlineData) throws Exception
	{
		String url = urlString.replaceAll("=", ":");
		URL obj = new URL(url);
		
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		//Set connection allow redirect
		con.setInstanceFollowRedirects(true); 
		HttpURLConnection.setFollowRedirects(true);
		
		//Add Header details
		con.setRequestMethod("POST");
		
		if(header == null)
		{
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", ACCEPT_LANG);
		}
		else
		{			
			for( String key : header.keySet() )
	        {
	        	con.setRequestProperty(key, header.get(key));
	        }		
		}
		
		String urlParameter = "";
		StringJoiner joiner = new StringJoiner("&");
		
		if(inlineData == null)
		{
			urlParameter = "";		
		}
		else
		{
			for( String key : inlineData.keySet() )
	        {
	        	con.setRequestProperty(key, inlineData.get(key));
	        	urlParameter = key + "=" + inlineData.get(key); //This create key1=value1
	        	joiner.add(urlParameter); //This will glue all the key,value pairs together with & as delimiter
	        }
			
			urlParameter = joiner.toString();
		}
			
		//Send POST request
		con.setDoOutput(true); //So we can use the URL connection for output data
			
		ByteArrayInputStream inputStream = new ByteArrayInputStream (urlParameter.getBytes("UTF-8")); //This only work for String, use FileInputStream for file.
		copy(inputStream, con.getOutputStream());
		inputStream.close();
		
		isVerbose(con, verbose);
	
		//Display output
		outputResponse(con, url, con.getResponseCode());
	}
	
	private void isVerbose(HttpURLConnection connection, Boolean verbose) 
	{
		//For verbose option
		Map<String, List<String>> headers = connection.getHeaderFields();
		Set<Map.Entry<String, List<String>>> entrySet = headers.entrySet();
		
		//Display full header details if verbose == true
		if(verbose == true)
		{
			System.out.println();
			System.out.println("Verbose option : ON --------------------");
					
			for (Map.Entry<String, List<String>> entry : entrySet) 
			{
				String headerName = entry.getKey();
				List<String> headerValues = entry.getValue();
				
		        for (String value : headerValues)
		        {
		        	System.out.println(headerName + " : " + value);
		        }
			}
			
			System.out.println("---------------------------------------");
			System.out.println();
		}	
	}

	/* This is the overload sendPost method - with File instead of inline data
	 * Read the file
	 * Translate it into a hashmap
	 * Use the generic sendPost()
	 */ 
	private void sendPost(String url, Boolean verbose, Map<String, String> header, File file) 
	{
		try 
		{
			Map<String, String> inlineData = new HashMap<String, String>();
			
	        BufferedReader in = new BufferedReader(new FileReader(file));
	        String line = "";
	        
	        while ((line = in.readLine()) != null) 
	        {
	            String parts[] = line.split(":");
	            inlineData.put(parts[0], parts[1]);
	        }
	        in.close();
			        
			sendPost(url, verbose, header, inlineData);
		} 
		catch (Exception e) 
		{
			System.err.println("File does not exit. Exiting program ...");
			e.printStackTrace();
		}	
	}
	
	//This method implements a redirect function for the GET protocol
	private void isRedirect(HttpURLConnection connection, Boolean verbose) throws Exception 
	{
		boolean redirect = false;
		String argument = "";

		//If the response code is in 300-ish (normal for redirect)
		int status = connection.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) 
		{
			if (status == HttpURLConnection.HTTP_MOVED_TEMP
				|| status == HttpURLConnection.HTTP_MOVED_PERM
					|| status == HttpURLConnection.HTTP_SEE_OTHER)
			{
				redirect = true;		
				System.out.println("The orginal requested URL has been redirected.");
			}
		}
		
		if (redirect == true) 
		{
			// get redirect url from "location" header field
			String newUrl = connection.getHeaderField("Location");
			
			System.out.println("The new URL is : " + newUrl);
			
			Map<String, List<String>> headers = connection.getHeaderFields();
			Set<Map.Entry<String, List<String>>> entrySet = headers.entrySet();
			
			Map<String, String> newHeader = new <String, String> HashMap();
			
			for (Map.Entry<String, List<String>> entry : entrySet) 
			{
				String headerName = entry.getKey();
				List<String> headerValues = entry.getValue();
				
		        for (String value : headerValues)
		        {
		        	newHeader.put(headerName, value);
		        }
			}	
				
			sendGet(newUrl, verbose, newHeader, argument);
		}
	}
	
	private void outputResponse(HttpURLConnection con, String url, int responseCode)
	{
					
		if (responseCode >= 400)
		{
			try 
			{
				if(outputFileName != null)
				{
					OutputStream outputStream = new FileOutputStream(outputFileName);
					copy(con.getErrorStream(), outputStream);
					
					System.out.println("Output to file ... " + outputFileName);
				}
				else
				{
					copy(con.getErrorStream(), System.out);
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			try 
			{
				if(outputFileName != null)
				{
					OutputStream outputStream = new FileOutputStream(outputFileName);
					copy(con.getInputStream(), outputStream);
					
					System.out.println("Output to file ... " + outputFileName);
				}
				else
				{
					copy(con.getInputStream(), System.out);
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}	
	}
	
	// copy method from From E.R. Harold's book "Java I/O"
	public static void copy(InputStream in, OutputStream out) throws IOException 
	{
		// do not allow other threads to read from the
	    // input or write to the output while copying is
	    // taking place
	    synchronized (in) 
	    {
	    	synchronized (out) 
	        {
	    		byte[] buffer = new byte[256];
	    		int bytesRead = 0;
	            
	    		while (true) 
	            {
	            	bytesRead = in.read(buffer);
	            	if (bytesRead == -1)
	            		break;
	            	out.write(buffer, 0, bytesRead);     	
	            }
	    		out.close();
	        }
	    }
	 } 	  
	
	public void setVerboseReport(Boolean verboseReport) 
	{
		this.verboseReport = verboseReport;
	}
}