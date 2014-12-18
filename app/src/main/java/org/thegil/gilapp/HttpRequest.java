package org.thegil.gilapp;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by LEE on 2014-12-08.
 */
public class HttpRequest {
    public String requestPost( HttpClient httpClient, HttpContext httpContext, String url, ArrayList<NameValuePair> postData, String encode ) {
        InputStream is = null;
        String result = "";

        try {
            /** 연결 타입아웃내에 연결되는지 테스트, 30초 이내에 되지 않는다면 에러 */
            /** 네트웍 연결해서 데이타 받아오기 */
            HttpParams params = httpClient.getParams( );
            HttpConnectionParams.setConnectionTimeout( params, 30000 );
            HttpConnectionParams.setSoTimeout(params, 30000);

            HttpPost httppost = new HttpPost( url );

            if( postData != null ) {
                UrlEncodedFormEntity entityRequest = new UrlEncodedFormEntity( postData, encode );
                httppost.setEntity( entityRequest );
            }

            HttpResponse response = httpClient.execute( httppost, httpContext );

            HttpEntity entityResponse = response.getEntity( );

            is = entityResponse.getContent( );

            /** convert response to string */
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, encode), 8);

            result = org.apache.commons.io.IOUtils.toString(reader);
            result = result.replaceAll( "\r\n", "\n" );
            is.close( );
        } catch( IOException ioe ) {
            ioe.getMessage();
        }
        return result;
    }

    public String requestGet( HttpClient httpClient, HttpContext httpContext, String url, String encode ) {
        InputStream is = null;
        String result = "";

        try {
            /** 연결 타입아웃내에 연결되는지 테스트, 30초 이내에 되지 않는다면 에러 */
            /** 네트웍 연결해서 데이타 받아오기 */
            HttpParams params = httpClient.getParams( );
            HttpConnectionParams.setConnectionTimeout( params, 30000 );
            HttpConnectionParams.setSoTimeout(params, 30000);

            HttpGet httpget = new HttpGet( url );

            HttpResponse response = httpClient.execute( httpget, httpContext );

            HttpEntity entityResponse = response.getEntity( );

            is = entityResponse.getContent( );

            /** convert response to string */
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, encode), 8);

            result = org.apache.commons.io.IOUtils.toString( reader );
            result = result.replaceAll( "\r\n", "\n" );
            is.close( );
        } catch( IOException ioe ) {
            ioe.getMessage();
        }
        return result;
    }


}
