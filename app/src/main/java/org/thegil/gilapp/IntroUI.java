package org.thegil.gilapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;


public class IntroUI extends Activity implements Runnable {

    protected HttpClient httpClient;
    protected HttpContext httpContext;

    private int LoginStatus;

    static final int REQUEST_CODE = 1;

    private BackPressCloseHandler backPressCloseHandler;
    private int backPressCheck = 0;

    public Toast loginToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backPressCloseHandler = new BackPressCloseHandler(this) {
            public void onBackPressed() {
                if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                    backKeyPressedTime = System.currentTimeMillis();
                    showGuide();
                    return;
                }
                if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                    backPressCheck = 1;
                    activity.finish();
                    toast.cancel();
                }
            }
        };
        setContentView(R.layout.intro);

        GilApplication app = ( GilApplication )getApplication();
        httpClient = app.httpClient;
        httpContext= app.httpContext;

        loginToast = Toast.makeText( IntroUI.this, "로그인 중......", Toast.LENGTH_SHORT );
        loginToast.show( );
        Thread thread = new Thread( this );
        thread.start( );

    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    public void run( ) {
        loadData(IntroUI.this);
        handler.sendEmptyMessage( 0 );

    }

    private Handler handler = new Handler( ) {
        @Override
        public void handleMessage( Message msg ) {
            Intent intent = null;
            loginToast.cancel();

            if( backPressCheck == 1 ) {
                return;
            }
            switch( LoginStatus ) {
                case -1:
                    System.out.println(-1);
                    intent = new Intent( IntroUI.this, LoginUI.class );
                    Toast.makeText( IntroUI.this, "로그인 정보를 설정해 주세요.", Toast.LENGTH_SHORT ).show();
                    startActivityForResult( intent, REQUEST_CODE );
                    break;
                case 0:
                    System.out.println(0);
                    intent = new Intent( IntroUI.this, LoginUI.class );
                    Toast.makeText( IntroUI.this, "아이디 혹은 비밀번호가 잘못 입력되었습니다.\n 다시 입력해주세요.", Toast.LENGTH_SHORT ).show();
                    startActivityForResult( intent, REQUEST_CODE );
                    break;
                case 1:
                    Toast.makeText( IntroUI.this, "로그인 성공", Toast.LENGTH_SHORT ).show();
                    intent = new Intent( IntroUI.this, FirstCategoryUI.class );
                    startActivity( intent );
                    finish( );
                    break;

            }
        }
    };

    private void loadData( Context context ) {
        boolean getSucess = false;
        Login login = new Login( );
        getSucess = login.getXmlData( context );
        if( !getSucess ) {
            LoginStatus = -1; //파일을 읽을 수 없음
        }
        else {
            LoginStatus = login.LoginTo( httpClient, httpContext, context );
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        System.out.println( " REsult!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        switch(requestCode) {
            case REQUEST_CODE:
                if (resultCode == RESULT_OK) {	// resultCode 가 항상 0 으로 넘어옴. 해결책 못 찾음. 일단 SetView 가 실행되면 다시 로딩하자.
                    loginToast = Toast.makeText( IntroUI.this, "로그인 중......", Toast.LENGTH_SHORT );
                    loginToast.show( );
                    Thread thread = new Thread(this);
                    thread.start();
                }
                if (resultCode == 444) {
                    finish( );
                }
        }
    }

}
