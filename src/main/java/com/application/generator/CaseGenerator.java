package com.application.generator;

import java.io.File;

public class CaseGenerator{

    public static void generate(String fileName){
        String separator = System.getProperty("file.separator");
        String inputDirLocation = new File(System.getProperty("user.dir")).getParent()+separator+"webapps"+separator+"TestEnumerator"+separator+"conf"+separator+"inputJSON"+separator;
        String outputDirLocation = new File(System.getProperty("user.dir")).getParent()+separator+"webapps"+separator+"TestEnumerator"+separator+"out"+separator+"outputJSON"+separator;
        //String outputDirLocation = new File(System.getProperty("user.dir")).getParent()+separator+"webapps"+separator+"TestEnumerator"+separator+"conf"+separator+"outputZip"+separator;
        String inputJsonPath = inputDirLocation+fileName;
        new File(outputDirLocation).mkdirs();
        JsonHandler.getJSON(inputJsonPath,outputDirLocation);
    }
}
