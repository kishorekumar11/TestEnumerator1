package com.application.form;

import java.io.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.application.generator.CaseGenerator;
import com.application.generator.JsonHandler;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.zeroturnaround.zip.ZipUtil;

import static java.io.File.separator;

public class ProcessServlet extends HttpServlet {

    private String filePath;
    private String outputDir;
    private int maxFileSize = 500* 1024;
    private int maxMemSize = 500* 1024;
    private File file ;


    private void zipFolder(String zipDir,String outputDir)
    {
        ZipUtil.pack(new File(zipDir), new File(outputDir+"out.zip"));
    }

    private boolean deleteDirectory(File dir)
    {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(children[i]);
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {

        filePath = new File(System.getProperty("user.dir")).getParent()+separator+"webapps"+separator+"TestEnumerator"+separator+"conf"+separator+"inputJSON"+separator;
        outputDir = new File(System.getProperty("user.dir")).getParent()+separator+"webapps"+separator+"TestEnumerator"+separator+"conf"+separator+"outputZip"+separator;

        // Clearing the inputJSON and outputZip directory before execution
        JsonHandler.clearDirectory(filePath);
        JsonHandler.clearDirectory(outputDir);

        response.setContentType("text/html");

        DiskFileItemFactory factory = new DiskFileItemFactory();

//         maximum size that will be stored in memory
        factory.setSizeThreshold(maxMemSize);

        // Location to save data that is larger than maxMemSize.
        //factory.setRepository(new File(new File(System.getProperty("user.dir")).getParent()+separator+"webapps"+separator+"TestEnumerator"+separator+"conf"+separator+"excess"));

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax( maxFileSize );

        try {
            // Parse the request to get file items.
            List fileItems = upload.parseRequest(request);

            // Process the uploaded file items
            Iterator i = fileItems.iterator();
            String fileName = "";
            while ( i.hasNext () ) {
                FileItem fi = (FileItem)i.next();
                if ( !fi.isFormField () ) {
                    fileName = fi.getName();

                    // Write the file
                    if( fileName.lastIndexOf("\\") >= 0 ) {
                        file = new File( filePath + fileName.substring( fileName.lastIndexOf("\\"))) ;
                    } else {
                        file = new File( filePath + fileName.substring(fileName.lastIndexOf("\\")+1)) ;
                    }
                    fi.write( file ) ;
                }
            }

            // Calling the main function
            CaseGenerator.generate(fileName);

            // Zipping the output directory
            String zipDir = new File(System.getProperty("user.dir")).getParent()+separator+"webapps"+separator+"TestEnumerator"+separator+"out"+separator;
            zipFolder(zipDir,outputDir);

            // Deleting the folder of outputJSON's
            deleteDirectory(new File(zipDir));

        } catch(Exception ex) {
            System.out.println(ex);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, java.io.IOException {
    }
}

