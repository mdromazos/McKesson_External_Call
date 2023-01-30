package com.informatica.mdm.bes.test_utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
	
	
	public static String loadMockFile(String fileName) throws UnsupportedEncodingException, IOException, URISyntaxException {
		Path resourceDirectory = Paths.get("source","test","resources", "mock", fileName);
		String absolutePath = resourceDirectory.toFile().getAbsolutePath();

		System.out.println(absolutePath);
        java.net.URL url = FileUtil.class.getResource(absolutePath);
       
        java.nio.file.Path resPath = java.nio.file.Paths.get(absolutePath);
        return new String(java.nio.file.Files.readAllBytes(resPath), "UTF8"); 
	}
	
	public static File loadMockFileReturnFile(String fileName) throws UnsupportedEncodingException, IOException, URISyntaxException {
		Path resourceDirectory = Paths.get("source","test","resources", "mock", fileName);
		String absolutePath = resourceDirectory.toFile().getAbsolutePath();

		System.out.println(absolutePath);
        java.net.URL url = FileUtil.class.getResource(absolutePath);
       
        java.nio.file.Path resPath = java.nio.file.Paths.get(absolutePath);
        return new File(absolutePath);
	}
	
	public static InputStream loadMockFileIS(String fileName) throws UnsupportedEncodingException, IOException, URISyntaxException {
		Path resourceDirectory = Paths.get("source","test","resources", "mock", fileName);
		String absolutePath = resourceDirectory.toFile().getAbsolutePath();

		System.out.println(absolutePath);
        java.net.URL url = FileUtil.class.getResource(absolutePath);
       
        java.nio.file.Path resPath = java.nio.file.Paths.get(absolutePath);
		return new FileInputStream(absolutePath);

	}
	
	public static InputStream loadFileIS(String fileName) throws UnsupportedEncodingException, IOException, URISyntaxException {
		Path resourceDirectory = Paths.get("source","test","resources", fileName);
		String absolutePath = resourceDirectory.toFile().getAbsolutePath();

		System.out.println(absolutePath);
        java.net.URL url = FileUtil.class.getResource(absolutePath);
       
        java.nio.file.Path resPath = java.nio.file.Paths.get(absolutePath);
        
		return new FileInputStream(absolutePath);

	}
}
