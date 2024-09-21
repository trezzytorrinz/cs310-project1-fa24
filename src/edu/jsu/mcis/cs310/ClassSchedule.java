package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
        JsonObject scheduleTypeObj = new JsonObject();
        JsonObject subjectObj = new JsonObject();
        JsonObject courseObj = new JsonObject();
        JsonObject sectionObj = new JsonObject();
        JsonArray sectionArray = new JsonArray();
        
        
        // Step 2: Create HashMaps for special headers
        HashMap<String, String> scheduleTypeMap = new HashMap<>();
        HashMap<String, String> subjectMap = new HashMap<>();
       
        
        
        // Step 3: Separate the columns 
        for (int i = 1; i < csv.size(); i++){
            String[] column = csv.get(i);
            
            String crn = column[0];
            String subject = column[1];
            String num = column[2];
            String description = column[3];
            String section = column[4];
            String scheduleTypeAbbreviation = column[5];
            String[] parts = num.split(" ");
            String courseKey = parts[0];
            String courseNum = parts[1]; 
            String credits = column[6];
            String start = column[7];
            String end = column[8];
            String days = column[9];
            String where = column[10];
            String scheduleTypeFull = column[11];
            String instructor = column[12];
            
                    
            
            // Object for Course Entry
            JsonObject courseDetails = new JsonObject();
            courseDetails.put("subjectid", courseKey);
            courseDetails.put("num", courseNum);
            courseDetails.put("description", description);
            courseDetails.put("credits", credits);
             
            // Merging the two together for the course section details
            courseObj.put(num, courseDetails);
            
            // Object for Section Entry
            JsonObject sectionDetails = new JsonObject();
            sectionDetails.put("crn", crn);
            System.out.println(sectionDetails);
            sectionDetails.put("subjectid", courseKey);
            System.out.println(sectionDetails);
            sectionDetails.put("num", courseNum);
            sectionDetails.put("section", section);
            sectionDetails.put("type", scheduleTypeAbbreviation);
            sectionDetails.put("start", start);
            sectionDetails.put("end", end);
            sectionDetails.put("days", days);
            sectionDetails.put("where", where);
            
            // Instructor separation
            JsonArray instructorArray = new JsonArray();
            instructorArray.add(instructor);
            sectionObj.put("instructor", instructorArray);
            // Merging the two together
            sectionObj.put(num, sectionDetails);
            
            sectionArray.add(sectionObj);
            
            // Checks for scheduletype and subject
            if (!scheduleTypeMap.containsKey(scheduleTypeAbbreviation)){
                scheduleTypeMap.put(scheduleTypeAbbreviation, scheduleTypeFull);
            }
            
            if(!subjectMap.containsKey(subject)){
                subjectMap.put(courseKey, subject);
            }
            
            
            
        }
        
        // Map the Schedule Type Entry 
        for (Map.Entry<String, String> entry : scheduleTypeMap.entrySet()){
            scheduleTypeObj.put(entry.getKey(), entry.getValue());
        }
        
        // Map the Subject Entry
        for(Map.Entry<String, String> entry : subjectMap.entrySet()){
            subjectObj.put(entry.getKey(), entry.getValue());
        }
            
        // Adding the section headers with their details to main object
        
        System.out.println(sectionArray);
        mainObj.put("scheduletype", scheduleTypeObj);
        mainObj.put("subject", subjectObj);
        mainObj.put("course", courseObj);
        mainObj.put("section", sectionArray);
        
        
        
        return mainObj.toString();

    }
    
    public String convertJsonToCsvString(JsonObject json) {
        // Step 1:
        
        // Step 2:
        
        // Step 3:
        
        // Step 4:
        
        // Step 5:
        
        // Step 6:
        
        return "";
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