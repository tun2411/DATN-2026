package com.example.bedatn.utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;


public class ReadFileUtils extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String imageUrl = request.getRequestURI();
        int repIndex = imageUrl.indexOf("/repository");
        String relativeImagePath = null;
        if(repIndex != -1) {
            repIndex += "/repository".length();
            relativeImagePath = imageUrl.substring(repIndex);
        }
        ServletOutputStream outStream;
        outStream = response.getOutputStream();
        FileInputStream fin = new FileInputStream("C://home/office" + relativeImagePath);
        BufferedInputStream bin = new BufferedInputStream(fin);
        BufferedOutputStream bout = new BufferedOutputStream(outStream);
        int ch =0;
        while((ch=bin.read())!=-1)
            bout.write(ch);
        bin.close();
        fin.close();
        bout.close();
        outStream.close();
    }
}
