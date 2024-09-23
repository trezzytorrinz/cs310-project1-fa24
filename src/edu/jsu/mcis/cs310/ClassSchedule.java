package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class ClassSchedule {
    
    private final String CSV_FILENAME = "jsu_sp24_v1.csv";
    private final String JSON_FILENAME = "jsu_sp24_v1.json";
    
    private final String CRN_COL_HEADER = "crn";
    private final String SUBJECT_COL_HEADER = "subject";
    private final String NUM_COL_HEADER = "num";
    private final String DESCRIPTION_COL_HEADER = "description";
    private final String SECTION_COL_HEADER = "section";
    private final String TYPE_COL_HEADER = "type";
    private final String CREDITS_COL_HEADER = "credits";
    private final String START_COL_HEADER = "start";
    private final String END_COL_HEADER = "end";
    private final String DAYS_COL_HEADER = "days";
    private final String WHERE_COL_HEADER = "where";
    private final String SCHEDULE_COL_HEADER = "schedule";
    private final String INSTRUCTOR_COL_HEADER = "instructor";
    private final String SUBJECTID_COL_HEADER = "subjectid";
    
    public String convertCsvToJsonString(List<String[]> csv) {
        
        // Step 1: Create objects
        JsonObject mainObj = new JsonObject();

        // Step 2: Create HashMaps for special headers
        HashMap<String, String> scheduleTypeMap = new HashMap<>();
        HashMap<String, String> subjectMap = new HashMap<>();
        HashMap<String, HashMap<String, Object>> courseMap = new HashMap<>();
        ArrayList<HashMap<String, Object>> sectionList = new ArrayList<>();
        List<String> courseOrder = new ArrayList<>();
        JsonObject courseObject = new JsonObject();

        
        
        // Step 3: Separate the columns 
        for (int i = 0; i < csv.size(); i++){
            String[] column = csv.get(i);
            
            // Checks to see if the field is empty
            
            
            String crn = column[0];
            String subject = column[1];
            String num = column[2];
            String description = column[3];
            String section = column[4];
            String scheduleTypeAbbreviation = column[5];
            String credits = column[6];
            String start = column[7];
            String end = column[8];
            String days = column[9];
            String where = column[10];
            String scheduleTypeFull = column[11];
            String instructor = column[12];
            
            
            
            String[] parts = num.split(" ");
            String courseKey = parts[0];
            String courseNum = parts[1]; 
            // Populating subject and scheduletype
            scheduleTypeMap.put(scheduleTypeAbbreviation, scheduleTypeFull);
            subjectMap.put(parts[0], subject);
            
            
            // Object for Course Entry
            if (!courseMap.containsKey(courseKey)){
                HashMap<String, Object> courseDetails = new HashMap<>();
                courseDetails.put("subjectid", courseKey);
                courseDetails.put("num", courseNum);
                courseDetails.put("description", description);
                courseDetails.put("credits", credits);
                

                // Merging the two together for the course section details
                courseOrder.add(courseKey);
                

            }
            // Object for Section Entry
            HashMap<String, Object> sectionDetails = new HashMap<>();
            sectionDetails.put("crn", crn);
            sectionDetails.put("subject", courseKey);
            sectionDetails.put("num", courseNum); // Causing issues
            sectionDetails.put("section", section);
            sectionDetails.put("type", scheduleTypeAbbreviation); // Causing issues
            sectionDetails.put("start", start);
            sectionDetails.put("end", end);
            sectionDetails.put("days", days);
            sectionDetails.put("where", where);
            
            // Instructor separation
            List<String> instructorArray = new ArrayList<>();
            instructorArray.add(instructor);
            sectionDetails.put("instructor", instructorArray);
            
            // Merging the two together
            sectionList.add(sectionDetails);
            
          
            
        }
        
        // Map the Course Entry 
        JsonObject courseJson = new JsonObject();
        for (String courseKey : courseOrder){
            JsonObject courseEntry = new JsonObject();
            HashMap<String, Object> courseDetails = courseMap.get(courseKey);
            if (courseDetails == null){
                System.out.println("courseDetails is null for: " + courseKey);
            } else {
                System.out.println("Able to fetch details for: " + courseKey);
            }
            courseObject.put(courseKey, courseMap.get(courseKey));
            for (Iterator<String> it = courseDetails.keySet().iterator(); it.hasNext();) {
                String key = it.next();
                courseEntry.put(key, courseDetails.get(key));
            }
            
            courseJson.put(courseKey, courseEntry);
        }
        
        // Adding the section headers with their details to main object
        
        mainObj.put("scheduletype", scheduleTypeMap);
        mainObj.put("subject", subjectMap);
        mainObj.put("course", courseJson);
        mainObj.put("section", sectionList);

        
        return mainObj.toString();

    }
    
    public String convertJsonToCsvString(JsonObject json) {
        
        /// Step 1: Create a list 
        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter);
        
        // Step 2: Add CSV headers
        String[] headers = {"crn", "subject", "num", "description", "section", "type", "credits", "start", "end", "days", "where", "schedule", "instructor"};
        csvWriter.writeNext(headers);
        
        //Json Array
        JsonArray jsonArray = (JsonArray) json.get("section");
        
        for (Object obj : jsonArray) {
            
            JsonObject section = (JsonObject) obj;
            
            String[] column = new String[headers.length];
            // Populate the row based on JSON structure
            
            column[0] = section.get("crn").toString();
            JsonObject subjectMap = (JsonObject) json.get("subject");
            column[1] = subjectMap.get(section.get("subjectid").toString()).toString();
            column[2] = section.get("num").toString();
            JsonObject courseMap = (JsonObject) json.get("course");
            column[3] = courseMap.get("description").toString();
            column[4] = section.get("section").toString();
            JsonObject scheduleTypeMap = (JsonObject) json.get("scheduletype");
            column[5] = scheduleTypeMap.get(section.get("type").toString()).toString();            
            column[6] = courseMap.get("credits").toString();
            column[7] = section.get("start").toString(); 
            column[8] = section.get("end").toString(); 
            column[9] = section.get("days").toString();
            column[10] = section.get("where").toString(); 
            column[11] = String.join(", ", (List<String>) section.get("instructor")); 

            // Write the populated row to the CSV
            csvWriter.writeNext(column);
        } 
        
        return stringWriter.toString(); // Return the CSV string    
    }
    
        
   
    
    public JsonObject getJson() {
        
        JsonObject json = getJson(getInputFileData(JSON_FILENAME));
        return json;
        
    }
    
    public JsonObject getJson(String input) {
        
        JsonObject json = null;
        
        try {
            json = (JsonObject)Jsoner.deserialize(input);
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return json;
        
    }
    
    public List<String[]> getCsv() {
        
        List<String[]> csv = getCsv(getInputFileData(CSV_FILENAME));
        return csv;
        
    }
    
    public List<String[]> getCsv(String input) {
        
        List<String[]> csv = null;
        
        try {
            
            CSVReader reader = new CSVReaderBuilder(new StringReader(input)).withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
            csv = reader.readAll();
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return csv;
        
    }
    
    public String getCsvString(List<String[]> csv) {
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");
        
        csvWriter.writeAll(csv);
        
        return writer.toString();
        
    }
    
    private String getInputFileData(String filename) {
        
        StringBuilder buffer = new StringBuilder();
        String line;
        
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        
        try {
        
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream("resources" + File.separator + filename)));

            while((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return buffer.toString();
        
    }
    
}