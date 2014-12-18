package org.thegil.gilapp;

import android.content.Context;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by LEE on 2014-12-07.
 */
public class Login {
    private String userID;
    private String userPW;

    Login( ) {
        userID = null;
        userPW = null;
    }

    public boolean getXmlData( Context context ) {
        boolean returnType = false;
        InputStream in = null;
        String fileName = "LoginInfo.xml";

        try {
            FileInputStream input = context.openFileInput( fileName );
            in = new BufferedInputStream( input );
            StringBuffer out = new StringBuffer( );
            byte[] buffer = new byte[4094];

            int readSize;
            while ( (readSize = in.read(buffer)) != -1) {
                out.append(new String(buffer, 0, readSize));
            }
            String data = out.toString( );
            /*여기까지가 파일 스트링 가져오는 작업*/
            //이제 xml파싱

            /*인스턴스 생성*/
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance( );
            factory.setNamespaceAware( true );
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput( new StringReader( data ) );
            int eventType = xpp.getEventType();
            int type = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch( eventType )
                {
                    case XmlPullParser.START_DOCUMENT:
                        System.out.println("Start document");
                        break;
                    case XmlPullParser.START_TAG:
                        System.out.println("Start tag "+xpp.getName());
                        String strTag = xpp.getName( );
                        if( strTag.equalsIgnoreCase("ID")) {
                            type = 1;
                        } else if( strTag.equalsIgnoreCase("Password")) {
                            type = 2;
                        } else {
                            type = 0;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        System.out.println( "End tag "+xpp.getName( ) );
                        type = 0;
                        break;
                    case XmlPullParser.TEXT:
                        if( type == 1 ) {
                            userID = xpp.getText( );
                        } else if( type == 2 ) {
                            userPW = xpp.getText( );
                        }
                }
                eventType = xpp.next();
                returnType = true;
            }
        } catch( FileNotFoundException fnfe ) {
            Log.d("loadData", fnfe.getMessage());
            returnType = false;
        } catch( Exception e ) {
            returnType = false;
        } finally {
            if( in != null ) {
                try {
                    in.close( );
                } catch( IOException ioe ) {
                    Log.d( "", ioe.getMessage() );
                }
            }

        }
        return returnType;
    }

    public int LoginTo(HttpClient httpClient, HttpContext httpContext, Context context) {
        String result = "";
        String url = "http://thegil.org/2014/bbs/login_check.php";
        String logoutURL = "http://thegil.org/2014/bbs/logout.php";

        HttpRequest httpRequest = new HttpRequest();

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("mb_id", userID));
        nameValuePairs.add(new BasicNameValuePair("mb_password", userPW));

        httpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        httpRequest.requestGet(httpClient, httpContext, logoutURL, "utf-8"); //로그아웃
        result = httpRequest.requestPost(httpClient, httpContext, url, nameValuePairs, "utf-8");//로그인

        if( result.indexOf( "<div id=\"hd_login_msg\">" ) > 0 ) {
            System.out.println( "Login Success" );

            return 1;
        } else {
            String errMsg = "Login Fail";
            Log.i("tag", "Login Fail");
            System.out.println(errMsg);
            return 0;
        }
    }
}
