package org.thegil.gilapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;


public class LoginUI extends Activity {
    private String userID;
    private String userPW;
    private String Status;
    private BackPressCloseHandler backPressCloseHandler;

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
                    if (getParent() == null) {
                        setResult( 444, new Intent()); // 종료 리절트 코드는 444
                    } else {
                        getParent().setResult( 444, new Intent());
                    }
                    activity.finish();
                    toast.cancel();
                }
            }
        };

        setContentView(R.layout.login);

        if ( getXmlData(this)) {
            EditText tID = (EditText)findViewById(R.id.editID);
            tID.setText(userID);
            EditText tPW = (EditText)findViewById(R.id.editPW);
            tPW.setText(userPW);
        }

        findViewById( R.id.btnLogin).setOnClickListener( ButtonClick );
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    Button.OnClickListener ButtonClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            switch( v.getId( ) ) {
                case R.id.btnLogin:
                    if( !saveData() ) { //폼에 맞지않는 값이 들어오면 클라이언트에서 사전에 처리
                        return;
                    }

                    if (getParent() == null) {
                        setResult(Activity.RESULT_OK, new Intent());
                    } else {
                        getParent().setResult(Activity.RESULT_OK, new Intent());
                    }
                    break;
            }
            finish( );
        }
    };


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
            System.out.println("@@@@@@@@@@@@@@@###################@@@@@@@@@@@@");

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
            System.out.println("FNFE1@@@@@@@@@@@@@@@###################@@@@@@@@@@@@");

            returnType = false;
        } catch( Exception e ) {
            returnType = false;
            e.printStackTrace();
            System.out.println("FNFE2@@@@@@@@@@@@@@@###################@@@@@@@@@@@@");

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
    private boolean formCheck( String userID, String userPW ) {

        if( userID.equalsIgnoreCase("") ) {
            Toast.makeText( LoginUI.this, "아이디를 입력해주세요", Toast.LENGTH_SHORT ).show();
            return false;
        }

        if( userPW.equalsIgnoreCase("") ) {
            Toast.makeText( LoginUI.this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT ).show();
            return false;
        }

        return true;
    }
    public boolean saveData() {

        EditText textID = (EditText)findViewById(R.id.editID);
        EditText textPW = (EditText)findViewById(R.id.editPW);

        String userID = textID.getText().toString();
        String userPW = textPW.getText().toString();

        if( !formCheck( userID, userPW ) ) { //아이디 비밀번호 양식 체크
            return false;
        }
        String fileName = "LoginInfo.xml";
        FileOutputStream fileos = null;
        try{
            fileos = openFileOutput(fileName, Context.MODE_PRIVATE);
        }catch(FileNotFoundException e){
            Log.e("FileNotFoundException", "can't create FileOutputStream");
            System.out.println(e.getMessage());
        }
        //we create a XmlSerializer in order to write xml data
        XmlSerializer serializer = Xml.newSerializer();
        try
        {
            // we set the FileOutputStream as output for the serializer, using UTF-8 encoding
            serializer.setOutput(fileos, "UTF-8");
            //Write <?xml declaration with encoding (if encoding not null) and standalone flag (if standalone not null)
            serializer.startDocument(null, Boolean.valueOf(true));
            //set indentation option
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            //start a tag called "root"
            serializer.startTag(null, "LoginInfo");
            //i indent code just to have a view similar to xml-tree

            serializer.startTag(null, "ID");
            //set an attribute called "attribute" with a "value" for
//            serializer.attribute(null, "data", userID);
            serializer.text(userID);
            serializer.endTag(null, "ID");

            serializer.startTag(null, "Password");
            //set an attribute called "attribute" with a "value" for
//            serializer.attribute(null, "data", userPW);
            serializer.text(userPW);
            serializer.endTag(null, "Password");

            serializer.endTag(null, "LoginInfo");
            serializer.endDocument();
            //write xml data into the FileOutputStream
            serializer.flush();
            //finally we close the file stream

        } catch (Exception e) {
            Log.e("Exception", "error occurred while creating xml file");
        }
        finally {
            try {
                fileos.close( );
            }
            catch(Exception e ) {
                e.getMessage();
            }
        }
        return true;
    }
}
