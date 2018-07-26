import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.hssf.extractor.OldExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;



public class Main {
	
	static int processLogContainsInstance(ArrayList<ProcessInstance> list, int id) {
	    for (ProcessInstance item : list) {
	        if (item.getInstanceId() == id ) {
	            return list.indexOf(item);
	        }
	    }
	    return -1;
	}
	
	
	// funkcja zmieniaj�ca timestamp (dni od 1 stycznia 1900 - windows) na date w formacie odpowiednim dla mXMLa
	private static String transformDate (String inputTimeString) throws ParseException{
		
		System.out.println("\ntime str: "+inputTimeString);
		double timestampDouble = Double.parseDouble(inputTimeString);
		timestampDouble = timestampDouble - 25569;  // odejmowanie dni (roznica miedzy windows a unix - dni od 1 stycznia 1970)
		timestampDouble = timestampDouble * 86400000;  // milisekundy (24 * 60 * 60 * 1000)
		timestampDouble = timestampDouble - 7200000;   // odejmowanie dwoch godzin (strefa czasowa)
		java.util.Date timestampUtildate = new java.util.Date((long)timestampDouble);
		
		//DateTimeFormatter formatterDate1 = DateTimeFormatter.ofPattern("d/M/YY");
		//DateTimeFormatter formatterDateTime1 = DateTimeFormatter.ofPattern("d/M/yy h:mm");		
		//LocalDate date1 = LocalDate.parse(inputDateString, formatterDate1);
		//LocalDateTime time1 = LocalDateTime.parse(inputTimeString, formatterDateTime1);		
		ZonedDateTime zonedDateTime1 = ZonedDateTime.ofInstant(timestampUtildate.toInstant() , ZoneId.of ( "Europe/Warsaw" ) );
		
		DateTimeFormatter outputFormatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		//System.out.println(date); // Sat Jan 02 00:00:00 GMT 2010
		
		String outputDateString1 = outputFormatter1.format(zonedDateTime1);
		
		return outputDateString1;
		
	}
	
	
	// funkcja czytaj�ca plik .xls
	private static ProcessLog readXlsFile (String xlsFilePath){
		
		ProcessLog processLog1 = new ProcessLog();
		ProcessInstance processInstance1;
		AuditTrialEntry auditTrialEntry1;
		
		// xlsFilePath to scie�ka do pliku excel wygenerowanego z simul8
		File xlsFile = new File(xlsFilePath);
		try {			
		    POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(xlsFile));
		    HSSFWorkbook wb = new HSSFWorkbook(fs);
		    HSSFSheet sheet = wb.getSheetAt(0);
		    HSSFRow row;
		    HSSFCell cell;
		    HSSFCell cellTime;	
		    HSSFCell cellDate;
		    
		    DataFormatter dataFormatter1 = new DataFormatter();
		    

		    int rows; // No of rows
		    rows = sheet.getPhysicalNumberOfRows();

		    int cols = 0; // No of columns
		    int tmp = 0;

		    // This trick ensures that we get the data properly even if it doesn't start from first few rows
		    for(int i = 0; i < 10 || i < rows; i++) {
		        row = sheet.getRow(i);
		        if(row != null) {
		            tmp = sheet.getRow(i).getPhysicalNumberOfCells();
		            if(tmp > cols) cols = tmp;
		        }
		    }

		    // czytanie skoroszytu po rekordzie
		    for(int r = 3; r < rows; r++) {
		        row = sheet.getRow(r);
		        if(row != null) {		            
		            cell = row.getCell((short)5);
		            
		            if(cell != null) {
		            	System.out.print("cell 6 contains: "+cell.toString());
		            	int tempInstanceId = (int)Double.parseDouble(cell.toString());
		            	// pobieranie indeksu pod kt�rym znajduje si� dana instancja, je�li takiej nie ma funkcja zwr�ci -1
		            	int processInstancesListElIndex = processLogContainsInstance(processLog1.instances, tempInstanceId);
		            	
		            	if(processInstancesListElIndex >= 0){
		            		// dodawanie wpisu do istniej�cej instancji procesu w logu
		            		// poni�ej s� poszczeg�lne pola wpisu (Audit Trial Entry)
		            		String workflowModelEl1 = row.getCell((short)2).toString();
		            		System.out.print(", cell 3: "+workflowModelEl1);
		            		String eventTypeString1 = row.getCell((short)3).toString();
		            		System.out.print(", cell 4: "+eventTypeString1);
		            		eventTypeString1.replaceAll("\\s+","");
		            		// timestamp
		            		cellTime = row.getCell((short)4);
		            		String timestamptTime1String = cellTime.toString();			            	
		            		String validTimestamp1 = transformDate(timestamptTime1String);
		            		System.out.print(", cell 5: "+validTimestamp1);
		            		// tworzenie obiektu AuditTrialEntry
		            		auditTrialEntry1 = new AuditTrialEntry(workflowModelEl1, eventTypeString1, validTimestamp1, "");
		            		System.out.println("");
		            		
		            		processInstance1 = processLog1.instances.get(processInstancesListElIndex);		         
		            		processInstance1.auditTrialEntries.add(auditTrialEntry1);
		            		processLog1.instances.set(processInstancesListElIndex, processInstance1);
		            		
		            	}else{
		            		// jeszcze nie ma takiej instacji procesu w logu, dodawanie nowej instancji do logu
		            		// poni�ej s� poszczeg�lne pola wpisu (Audit Trial Entry)
		            		String workflowModelEl1 = row.getCell((short)2).toString();
		            		System.out.print(", cell 3: "+workflowModelEl1);
		            		String eventTypeString1 = row.getCell((short)3).toString();
		            		System.out.print(", cell 4: "+eventTypeString1);
		            		eventTypeString1.replaceAll("\\s+","");
		            		// timestamp		            		
		            		cellTime = row.getCell((short)4);
		            		String timestamptTime1String = cellTime.toString();			            	
		            		String validTimestamp1 = transformDate(timestamptTime1String);
		            		System.out.print(", cell 5: "+validTimestamp1);
		            		// tworzenie obiektu AuditTrialEntry
		            		auditTrialEntry1 = new AuditTrialEntry(workflowModelEl1, eventTypeString1, validTimestamp1, "");
		            		System.out.println("");
		            		
		            		processInstance1 = new ProcessInstance(tempInstanceId);
		            		processInstance1.auditTrialEntries.add(auditTrialEntry1);
		            		processLog1.instances.add(processInstance1);
		            	}
		            }
		        }
		    }
		} catch(Exception ioe) {
		    ioe.printStackTrace();
		}
		return processLog1;
	}



	
	
	// funkcja tworz�ca mXMLa
	private static void buildXmlDocument(ProcessLog inputProcessLog1,  String sourceFilePath){
		
		try {

	        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	        
	        DOMImplementationLS ls = (DOMImplementationLS)DOMImplementationRegistry.newInstance().getDOMImplementation("LS");

	        // root elements
	        Document doc = docBuilder.newDocument();
	        Element rootElement = doc.createElement("Process");
	        ((Element)rootElement).setAttribute("id","Poprawa_egzaminu");
	        ((Element)rootElement).setAttribute("description","Simulated process");
	        doc.appendChild(rootElement);
	        
	        // sortowanie instancji po id
	        Collections.sort(inputProcessLog1.instances);
	        
	        Element instanceElement1;
	        Element auditTrialEntrElement1;
	        
	        // Elementy wpisu AuditTrialEntry 
	        Element audTrialWorkFlMdlElemet;
	        Element audTrialEventTypeElement;
	        Element audTrialTimestampElement;
	        Element audTrialOriginatorElement;
	        
	        // dodawanie instancji 
			for(ProcessInstance procInst1 : inputProcessLog1.instances){
				instanceElement1 = doc.createElement("ProcessInstance");
				String procInstanceIdString = Integer.toString(procInst1.getInstanceId());
				((Element)instanceElement1).setAttribute("id",procInstanceIdString);
		        ((Element)instanceElement1).setAttribute("description","Simulated process instance");
		        
		        // dodawanie wpis�w do instancji 
		        for(AuditTrialEntry audTrialEntry1 : procInst1.auditTrialEntries){
		        	
		        	// poszczeg�lne pola wpisu 
		        	auditTrialEntrElement1 = doc.createElement("AuditTrailEntry");
		        	audTrialWorkFlMdlElemet = doc.createElement("WorkflowModelElement");
		        	audTrialWorkFlMdlElemet.appendChild(doc.createTextNode(audTrialEntry1.getWorkFlModlElement()));
		        	audTrialEventTypeElement = doc.createElement("EventType");
		        	audTrialEventTypeElement.appendChild(doc.createTextNode(audTrialEntry1.getEventType()));
		        	audTrialTimestampElement = doc.createElement("Timestamp");
		        	audTrialTimestampElement.appendChild(doc.createTextNode(audTrialEntry1.getTimestamp()));
		        	audTrialOriginatorElement = doc.createElement("Originator");
		        	audTrialOriginatorElement.appendChild(doc.createTextNode(audTrialEntry1.getOriginator()));
		        	
		        	auditTrialEntrElement1.appendChild(audTrialWorkFlMdlElemet);
		        	auditTrialEntrElement1.appendChild(audTrialEventTypeElement);
		        	auditTrialEntrElement1.appendChild(audTrialTimestampElement);
		        	auditTrialEntrElement1.appendChild(audTrialOriginatorElement);
		        	
		        	instanceElement1.appendChild(auditTrialEntrElement1);
		        }
		        
		        rootElement.appendChild(instanceElement1);
	        }
			
	        // Creates a LSSerializer object and saves to file.
	        LSSerializer serializer = ls.createLSSerializer();
	        serializer.getDomConfig().setParameter("format-pretty-print", true);
	        LSOutput output = ls.createLSOutput();
	        OutputStream ostream = new FileOutputStream(sourceFilePath);
	        output.setByteStream(ostream);        
	        serializer.write(doc, output);

	        System.out.println("File saved!");
		
		} catch (ParserConfigurationException pce) {
	        pce.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
        // czytanie pliku .xls
        ProcessLog procLog1 = readXlsFile("./sample_log/log1112.xls");
        
        // tworzenie i zapisywanie mXMLa
        String sourceFilePath1 = "./sample_log/xml_file1.xml";
        buildXmlDocument(procLog1, sourceFilePath1);     
        
		

	}

}
