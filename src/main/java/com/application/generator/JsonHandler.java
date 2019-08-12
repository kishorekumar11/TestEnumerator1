package com.application.generator;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang.SerializationUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;


public class JsonHandler implements Serializable{

    private static boolean newObject = false;

    public static void clearDirectory(String outputDir){
        File file = new File(outputDir);
        String[] myFiles;
        if(file.isDirectory()){
            myFiles = file.list();
            for (int i=0; i<myFiles.length; i++) {
                File myFile = new File(file, myFiles[i]);
                myFile.delete();
            }
        }
    }

    public static void getJSON(String path,String outputDir) {
        Map<String, Map> schema = getSchema(path);
        String separator = System.getProperty("file.separator");
        String testCasePath = new File(System.getProperty("user.dir")).getParent()+separator+"webapps"+separator+"TestEnumerator"+separator+"conf"+separator+"testJSON"+separator+"testCase2.json";
        JSONObject testCase = getObject(testCasePath);
        for (Map.Entry<String, Map> entry : schema.entrySet()) {
            LinkedHashMap jo = new LinkedHashMap();
            Map<String, String> template = new LinkedHashMap();
            String link = entry.getKey();

            Iterator<Map.Entry> itr = entry.getValue().entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry pair = itr.next();
                String value = (String) ((Map) pair.getValue()).get("type");
                template.put((String) pair.getKey(), value);
            }
            jo.put("Link", link);
            jo.put("Template", template);
            jo.put("testCases", new JSONArray());
            newObject = true;
            createTestCases(jo,link,outputDir,testCase);
        }
        new File("testCasePath").delete();
    }

    private static Map<String, Map> getSchema(String path) {
        Map<String, String> links = getLinks(path);
        JSONObject jo = getObject(path);
        Map<String, Map> schema = new HashMap();
        for (Map.Entry<String, String> entry : links.entrySet()) {
            String link = entry.getKey();
            String[] ref = entry.getValue().substring(2).split("/");
            Map properties = (Map) jo.get(ref[0]);
            for (int i = 1; i < ref.length; i++) {
                properties = (Map) properties.get(ref[i]);
            }
            properties = (Map) properties.get("properties");
            schema.put(link, properties);
        }
        return schema;
    }

    private static JSONObject getObject(String path) {
        try {
            Object obj = new JSONParser().parse(new FileReader(path));
            return (JSONObject) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void createTestCases(LinkedHashMap jo, String link,String outputDir,JSONObject testCase){
        LinkedHashMap properties = (LinkedHashMap)jo.get("Template");
        Set<String> keySet = properties.keySet();
        ArrayList<String> propKeys = new ArrayList(keySet);

        Collection<String> values = properties.values();
        ArrayList<String>  propValues= new ArrayList(values);

        ArrayList<ArrayList> validDataTypes = new ArrayList();
        ArrayList<ArrayList> invalidDataTypes = new ArrayList();
        int validElementCombination = 1;
        int invalidElementCombination = 0;
        for(String val : propValues){
            JSONObject obj = (JSONObject)testCase.get(val);
            validElementCombination *= ((JSONArray) obj.get("validInput")).size();
            validDataTypes.add( (JSONArray) ((JSONArray) obj.get("validInput")).clone());
            invalidElementCombination += ((JSONArray) obj.get("invalidInput")).size();
            invalidDataTypes.add( (JSONArray) ((JSONArray) obj.get("invalidInput")).clone());
        }
        if(validElementCombination>10000){
            validElementCombination=10000;
        }
        if(invalidElementCombination>10000){
            invalidElementCombination = 10000;
        }
        LinkedHashMap validCombination = getValidCombinations(validDataTypes,validElementCombination,propKeys,propValues,testCase,jo,link,outputDir);
        getInValidCombinations(invalidDataTypes,invalidElementCombination,propKeys,validCombination,jo,link,outputDir);
        propKeys.clear();
        propValues.clear();

    }

    private static LinkedHashMap getValidCombinations(ArrayList validDataTypes, int validElementCombination, ArrayList propKeys, ArrayList propValues, JSONObject testCase,LinkedHashMap jo,String link,String outputDir){

        int n = validDataTypes.size();
        int[] indices = new int[n];

        for (int i = 0; i < n; i++) {
            indices[i] = 0;
        }

        long noOfElements = 0;
        JSONArray allCombination = new JSONArray();
        LinkedHashMap validCombination = new LinkedHashMap();
        while (true) {
            LinkedHashMap combination = new LinkedHashMap();
            combination.put("testDataMeta",new JSONObject());
            combination.put("testData",new JSONObject());
            ( (JSONObject)combination.get("testDataMeta") ).put("isValidInput",true);
            for (int i = 0; i < n; i++) {
                //combination.put(propKeys.get(i), ((JSONArray) validDataTypes.get(i)).get(indices[i]));
                ( (JSONObject)combination.get("testData") ).put(propKeys.get(i), ((JSONArray) validDataTypes.get(i)).get(indices[i]));
            }

            allCombination.add(combination);
            validCombination = combination;
            noOfElements++;
            if(noOfElements == validElementCombination){
                writeJSON(jo,link,allCombination,outputDir);
                allCombination.clear();
                noOfElements = 0;
            }

            int next = n - 1;
            while (next >= 0 && (indices[next] + 1 >= ((JSONArray) validDataTypes.get(next)).size())) {
                next--;
            }
            if (next < 0) {
                break;
            }
            indices[next]++;
            for (int i = next + 1; i < n; i++) {
                indices[i] = 0;
            }
        }
        if(noOfElements!=0){
            writeJSON(jo,link,allCombination,outputDir);
        }
        return validCombination;
    }

    private static void getInValidCombinations(ArrayList invalidDataTypes, int invalidElementCombination, ArrayList propKeys, LinkedHashMap validCombination,LinkedHashMap jo,String link,String outputDir){
        JSONArray allCombination = new JSONArray();
        ((JSONObject) validCombination.get("testDataMeta")).put("isValidInput",false);
        long noOfElements = 0;
        for(int i =0;i<invalidDataTypes.size();i++){
            LinkedHashMap validTestCase = (LinkedHashMap)SerializationUtils.clone(validCombination);
            ((JSONObject) validTestCase.get("testDataMeta")).put("invalidParameter",propKeys.get(i));
            for(int j=0;j<((JSONArray)invalidDataTypes.get(i)).size();j++) {
                ((JSONObject) validTestCase.get("testData")).put(propKeys.get(i), ((JSONArray)invalidDataTypes.get(i)).get(j));
                allCombination.add(validTestCase);
                noOfElements++;
                if(noOfElements == invalidElementCombination){
                    writeJSON(jo,link,allCombination,outputDir);
                    allCombination.clear();
                    noOfElements = 0;
                }
            }
        }
        if(noOfElements!=0){
            writeJSON(jo,link,allCombination,outputDir);
        }

    }

    private static Map<String, String> getLinks(String path) {
        JSONObject jo = getObject(path);
        Map data = (Map) jo.get("paths");
        Iterator<Map.Entry> itr1 = data.entrySet().iterator();
        Map<String, String> links = new HashMap();

        while (itr1.hasNext()) {

            Map.Entry pair = itr1.next();
            String link = (String) pair.getKey();
            Map link_data = (Map) pair.getValue();

            if (link_data.get("post") != null) {
                JSONArray arr = (JSONArray) ((Map) link_data.get("post")).get("parameters");
                Map schema = (Map) ((Map) arr.get(0)).get("schema");
                String reference = (String) schema.get("$ref");
                links.put(link, reference);
            }
        }
        return links;
    }

    private static void writeJSON(LinkedHashMap obj,String link,JSONArray combination,String outputDir){
        String separator = System.getProperty("file.separator");
        ObjectMapper mapperMain=new ObjectMapper();
        ObjectWriter writer = mapperMain.writer(new DefaultPrettyPrinter());
        link = link.replaceAll("/","_");
        File fileName = new File(outputDir+link+".json");
        String element =  combination.toJSONString();
        element = element.substring(1,element.length()-1);

        try {
            if(!fileName.exists()){
                writer.writeValue(new FileWriter(fileName,false),obj);
            }
            else if(newObject){
                writer.writeValue(new FileWriter(fileName,true),obj);
            }
            RandomAccessFile randomAccessFile = new RandomAccessFile(fileName, "rw");
            long pos = randomAccessFile.length();
            while (randomAccessFile.length() > 0) {
                pos--;
                randomAccessFile.seek(pos);
                if (randomAccessFile.readByte() == ']') {
                    randomAccessFile.seek(pos);
                    break;
                }
            }
            if(newObject){
                newObject = false;
                randomAccessFile.writeBytes(element+"]\n}");
            }
            else {
                randomAccessFile.writeBytes(",\n\t\t" + element + "]\n}");
            }
            randomAccessFile.close();

        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
