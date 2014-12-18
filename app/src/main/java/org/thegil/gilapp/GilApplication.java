package org.thegil.gilapp;

/**
 * Created by LEE on 2014-12-07.
 */
import android.app.Application;
import android.util.Log;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

public class GilApplication extends Application {

    public HttpClient httpClient = null;
    public HttpContext httpContext = null;
    public CookieStore cookieStore = null;
    public String mUserID;



    public GilApplication( ) {
        super( );
    }


    @Override
    public void onCreate( ) {
        httpClient = new DefaultHttpClient( );
        httpContext = new BasicHttpContext( );
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute( ClientContext.COOKIE_STORE, cookieStore );

        Log.d("GilApplication", "onCreate( )");
    }



    @Override
    public void onTerminate( ) {
    }

}
