package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;


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

        
        JsonObject scheduleTypeMap = new JsonObject();
        JsonObject subjectMap = new JsonObject();
        JsonObject courseObject = new JsonObject();
        JsonArray sectionArray = new JsonArray();
        
        Iterator<String[]> csvIterator = csv.iterator();

        String[] column = csvIterator.next();
        
        // Step 3: Separate the columns 
        while (csvIterator.hasNext()){
            
            column = csvIterator.next();            
            
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
                
            HashMap<String, Object> courseDetails = new HashMap<>();
            
            courseDetails.put("subjectid", courseKey);
            courseDetails.put("num", courseNum);
            courseDetails.put("description", description);
            courseDetails.put("credits", Integer.parseInt(credits));


            // Merging the two together for the course section details
            courseObject.put(num, courseDetails);
          
            
            // Object for Section Entry
            JsonObject sectionDetails = new JsonObject();
            sectionDetails.put("crn", Integer.parseInt(crn));
            sectionDetails.put("subjectid", courseKey);
            sectionDetails.put("num", courseNum); 
            sectionDetails.put("section", section);
            sectionDetails.put("type", scheduleTypeAbbreviation); 
            sectionDetails.put("start", start);
            sectionDetails.put("end", end);
            sectionDetails.put("days", days);
            sectionDetails.put("where", where);
            
            // Instructor separation
            JsonArray instructorArray = new JsonArray();
            String[] instructors = instructor.split(", ");
            for (String instr : instructors){
                instructorArray.add(instr.trim());
            }
            sectionDetails.put("instructor", instructorArray);
            
            // Merging the two together
            sectionArray.add(sectionDetails);
          
            
        }

        
        // Adding the section headers with their details to main object
        
        mainObj.put("scheduletype", scheduleTypeMap);
        mainObj.put("subject", subjectMap);
        mainObj.put("course", courseObject);
        mainObj.put("section", sectionArray);


        
        return Jsoner.serialize(mainObj);

    }
    
    public String convertJsonToCsvString(JsonObject json) {
        
        /// Step 1: Create a list 
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");
        
        
        
        
        // Step 2: Add CSV headers
        String[] headers = {"crn", "subject", "num", "description", "section", "type", "credits", "start", "end", "days", "where", "schedule", "instructor"};
        csvWriter.writeNext(headers);
        
        
        // Step 3: Copy the data
        JsonObject courseMap = (JsonObject) json.get("course");
        JsonArray sectionArray = (JsonArray) json.get("section");
        
        // Iterating through the sections within each main section
        
            for (Object obj: sectionArray){
                JsonObject section = (JsonObject) obj;
                String[] column = new String[13];
                
                JsonObject scheduleTypeMap = (JsonObject) json.get("scheduletype");
                String scheduleTypeKey = section.get("type").toString();
                String fullScheduleType = scheduleTypeMap.get(scheduleTypeKey).toString();
                JsonObject subjectTypeMap = (JsonObject) json.get("subject");
                String subjectID = section.get("subjectid").toString();
                String courseKey = subjectID + " " + section.get("num").toString();
                JsonObject course = (JsonObject) courseMap.get(courseKey);
                String scheduleTypeAbbreviation = section.get("type").toString();
                
                
                
                column[0] = section.get("crn").toString();
                column[1] = subjectTypeMap.get(subjectID).toString();
                column[2] = courseKey;
                column[3] = course.get("description").toString();
                column[4] = section.get("section").toString();
                column[5] = scheduleTypeAbbreviation;
                column[6] = course.get("credits").toString();
                column[7] = section.get("start").toString();
                column[8] = section.get("end").toString();
                column[9] = section.get("days").toString();
                column[10] = section.get("where").toString();
                column[11] = fullScheduleType;
                column[12] = String.join(", ", (List<String>) section.get("instructor"));
                
                
                csvWriter.writeNext(column);
            }
            
        try {    
            csvWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(ClassSchedule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return writer.toString();
        
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