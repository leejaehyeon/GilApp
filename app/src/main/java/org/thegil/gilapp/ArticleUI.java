package org.thegil.gilapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArticleUI extends Activity implements Runnable {

    /** Called when the activity is first created. */
//	protected String itemsTitle;
//	protected String itemsLink;
    protected String mBoardID; //bo_table 의 값( B11, B12등의 값);
    protected String mBoardNo;
    protected HttpClient httpClient;
    protected HttpContext httpContext;
    private ProgressDialog pd;
    String htmlDoc;
    String mContent;
    String mErrorMsg;
    String mContentOrig; //페이지 html 값
    int nThreadMode = 0;
    boolean bDeleteStatus;
    static final int REQUEST_WRITE = 1;
    static final int REQUEST_MODIFY = 2;
    static final int REQUEST_COMMENT_WRITE = 3;
    static final int REQUEST_COMMENT_REPLY_VIEW = 4;
    static final int REQUEST_COMMENT_DELETE_VIEW = 5;
    String mCommentNo;
    String mUserID; //로그인 한 아이디
    protected int mLoginStatus;
    private WebView webView;

    String g_isPNotice;
    String g_isNotice;
    String g_Subject; //글 제목
    String g_UserName; //글쓴이
    String g_UserID;
    String g_Date; //글 쓴 날짜혹은 시간( 년도 빼고)
    String g_Link; //wr_id=***값

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article);
        webView = (WebView) findViewById(R.id.webView); //웹뷰를 webView변수에 할당

        GilApplication app = (GilApplication)getApplication();
        httpClient = app.httpClient;
        httpContext = app.httpContext;
        mUserID = app.mUserID; //moojiageActivy참조

        intenter();

        /*Pattern p = Pattern.compile("(?<=boardNo=)(.|\\n)*?(?=&)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(g_Link);

        if (m.find()) { // Find each match in turn; String can't do this.
        	mBoardNo = m.group(0);
        } else {
        	mBoardNo = "";
        }
        */
        LoadData();
    }

    public void LoadData() {
        pd = ProgressDialog.show(this, "", "로딩중", true, false);

        Thread thread = new Thread(this);
        thread.start();

        nThreadMode = 1;

        return;
    }

    public void run() {
      // Load Data
        if (!getData(httpClient, httpContext)) { //리턴값이 트루면 게시물 가져오기 성공
            // Login
            Login login = new Login();

            mLoginStatus = login.LoginTo(httpClient, httpContext, ArticleUI.this);

            if (mLoginStatus > 0) {
                if (getData(httpClient, httpContext)) {
                    mLoginStatus = 1;
                } else {
                    mLoginStatus = -2;
                }
            }
        } else {
            mLoginStatus = 1;
        }

        handler.sendEmptyMessage(0);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (pd != null) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
            }
            if (nThreadMode == 1) {
                displayData();
            } else {
                if (!bDeleteStatus) {
                    AlertDialog.Builder ab = null;
                    ab = new AlertDialog.Builder( ArticleUI.this );
                    ab.setMessage(mErrorMsg);
                    ab.setPositiveButton(android.R.string.ok, null);
                    ab.setTitle( "확인" );
                    ab.show();
                    return;
                } else {
                    if (nThreadMode == 2) {
                        if (getParent() == null) {
                            setResult(Activity.RESULT_OK, new Intent());
                        } else {
                            getParent().setResult(Activity.RESULT_OK, new Intent());
                        }
                        finish();
                    } else {
                        LoadData();
                    }
                }
            }
        }
    };

    public void displayData() {
        if (mLoginStatus == -1) {
            AlertDialog.Builder ab = null;
            ab = new AlertDialog.Builder( this );
            ab.setMessage( "로그인 정보가 설정되지 않았습니다. 설정 메뉴를 통해 로그인 정보를 설정하십시오.");
            ab.setPositiveButton(android.R.string.ok, null);
            ab.setTitle( "로그인 오류" );
            ab.show();
        } else if (mLoginStatus == -2){
            AlertDialog.Builder ab = null;
            ab = new AlertDialog.Builder( this );
            ab.setMessage( "게시판을 볼 권한이 없습니다.");
            ab.setPositiveButton(android.R.string.ok, null);
            ab.setTitle( "권한 오류" );
            ab.show();
        } else if (mLoginStatus == 0){
            AlertDialog.Builder ab = null;
            ab = new AlertDialog.Builder( this );
            ab.setMessage( "로그인을 실패했습니다 설정 메뉴를 통해 로그인 정보를 변경하십시오.");
            ab.setPositiveButton(android.R.string.ok, null);
            ab.setTitle( "로그인 오류" );
            ab.show();
        } else {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadDataWithBaseURL("http://thegil.org/", htmlDoc, "text/html", "utf-8", "");
        }
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
        Bundle extras = getIntent().getExtras();
        // 가져온 값을 set해주는 부분
        g_Subject = extras.getString("SUBJECT").toString(); //게시글 제목 반환
        g_UserName = extras.getString("USERNAME").toString(); //글쓴이 반환
        g_Date = extras.getString("DATE").toString(); //날짜 혹은 시간 반환
        g_Link = extras.getString("LINK").toString(); //링크 중 wr_id와 값 반환
        mBoardID = extras.getString("BOARDID").toString(); //bo_table의 값만 반환
        g_Link = mBoardID + "&" + g_Link; //합치기
    }

    protected boolean getData(HttpClient httpClient, HttpContext httpContext) {
        String url = "http://thegil.org/2014/bbs/board.php?bo_table=" + g_Link;
        HttpRequest httpRequest = new HttpRequest();

        String result = httpRequest.requestGet(httpClient, httpContext, url, "utf-8");
        if( result.indexOf( "<div id=\"hd_login_msg\">" ) == 0 ) {

            return false;
        }
        mContentOrig = result;

        Pattern p = null;
        Matcher m = null;

        String strSubject = null; //글제목
        String strUser = null; //글쓴이
        String strUserDate = null; //글 쓴 날짜
        String strHit = null; //조회수
        String strCommentNum = null; //댓글수
        String strAttach = null; //첨부파일

//글제목
        strSubject = g_Subject;

//글쓴이
        strUser = g_UserName;

        int match1, match2;
        String strTitle;
        match1 = result.indexOf("<section id=\"bo_v_info\">");
        if (match1 < 0) return false;
        match2 = result.indexOf("</section>", match1);
        if (match2 < 0) return false;
        strTitle = result.substring(match1, match2);
//글 쓴 날짜
        p = Pattern.compile("(\\d\\d-){2}\\d\\d \\d\\d:\\d\\d", Pattern.CASE_INSENSITIVE);
        m = p.matcher(strTitle);
        if (m.find()) { // Find each match in turn; String can't do this.
            strUserDate = m.group(0);
        } else {
            strUserDate = "";
        }
//조회수
        p = Pattern.compile("(?<=조회<strong>)\\d+(?=회</strong>)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(strTitle);

        if (m.find()) { // Find each match in turn; String can't do this.
            strHit = m.group(0);
        } else {
            strHit = "";
        }
//댓글수
        p = Pattern.compile("(?<=댓글<strong>)\\d+(?=회</strong>)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(strTitle);

        if (m.find()) { // Find each match in turn; String can't do this.
            strCommentNum = m.group(0);
        } else {
            strCommentNum = "";
        }

        strTitle = "<div class='title'>" + strSubject + "</div><div class='name'><span>" + strUser + "</span>&nbsp;&nbsp;<span>" + strUserDate + "</span>&nbsp;&nbsp;<span>" + strHit + "</span>명이 읽음</div>";

//본문 체크
        match1 = result.indexOf("<div id=\"bo_v_img\">");
        if (match1 < 0) return false;
        match2 = result.indexOf("<!-- } 본문 내용 끝 -->", match1);
        if (match2 < 0) return false;
        mContent = result.substring(match1, match2);
//        mContent = mContent.replaceAll("<meta http-equiv=\\\"Content-Type\\\" content=\\\"text/html; charset=euc-kr\\\">", "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=euc-kr\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, target-densitydpi=medium-dpi\">");
       /* mContent = mContent.replaceAll("<td width=200 align=right class=fMemoSmallGray>", "<!--");
        mContent = mContent.replaceAll("<td width=10></td>", "-->");
        mContent = mContent.replaceAll("<!-- 메모에 대한 답변 -->", "<!--");
        mContent = mContent.replaceAll("<!-- <font class=fMemoSmallGray>", "--><!--");
        mContent = mContent.replaceAll("<nobr class=bbscut id=subjectTtl name=subjectTtl>", "");
        mContent = mContent.replaceAll("</nobr>", "");*/
        mContent = "<div class='content'>" + mContent + "</div>";

        p = Pattern.compile("(<img src=)(.|\\n)*?(>)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(mContent);
        while (m.find()) { // Find each match in turn; String can't do this.
            String matchstr = m.group(0);

            Pattern p2 = Pattern.compile("(?<=src=\\\")(.|\\n)*?(?=\\\")", Pattern.CASE_INSENSITIVE);
            Matcher m2 = p2.matcher(matchstr);

            if (m2.find()) { // Find each match in turn; String can't do this.
            	String imgSrc = m2.group(0);

            	String img = "<img onload=\"resizeImage2(this)\" style=\"CURSOR:hand;\" src=\"" + imgSrc + "\" >";
            	mContent = mContent.replaceFirst("(<img src=)(.|\\n)*?(>)", img);
            }
        }
         //이미지 관련 기능 나중에 구현
        p = Pattern.compile("(?<=<!-- 첨부파일 시작 \\{ -->)(.|\\n)*?(?=<!-- \\} 첨부파일 끝 -->)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(result);
        if (m.find()) { // Find each match in turn; String can't do this.
            strAttach = m.group(0);
        } else {
            strAttach = "";
        }
        /*match1 = result.indexOf("<!-- 첨부파일 시작 { -->");
        match2 = result.indexOf("<!-- } 첨부파일 끝 -->", match1);
        System.out.println("###############");
        strAttach = result.substring(match1, match2);*/
        strAttach = "<div class='attach'>" + strAttach + "</div>";

        /*match1 = result.indexOf("<!-- 별점수 -->");
        if (match1 < 0) return false;
        match2 = result.indexOf("<!-- 관련글 -->", match1);
        if (match2 < 0) return false;
        String strProfile_str = result.substring(match1, match2);

        p = Pattern.compile("(?<=<td class=cContent>)(.|\\n)*?(?=</td>)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(strProfile_str);
        String strProfile;
        if (m.find()) { // Find each match in turn; String can't do this.
            strProfile = m.group(0);
        } else {
            strProfile = "None";
        }
        strProfile = "<div class='profile'>" + strProfile + "</div>";*/

        match1 = result.indexOf("<!-- 댓글 시작 { -->");
        if (match1 < 0) return false;
        match2 = result.indexOf("<!-- } 댓글 끝 -->", match1);
        if (match2 < 0) return false;
        String mComment_str = result.substring(match1, match2);

        String mComment = "";

        String[] items = mComment_str.split("<article id=\\\"c_\\d+\\\" (style=\"margin-left:\\d+px;border-top-color:#e0e0e0\\\")?>");
        int i = 0;
        for (i = 1; i < items.length; i++) { // Find each match in turn; String can't do this.
            String matchstr = items[i];

            // is Re
            if (matchstr.indexOf("class=\\\"icon_reply\\\" alt=\\\"댓글의 댓글\\\"") > 0) {
                mComment = mComment + "<div class='re_reply'>";
            } else {
                mComment = mComment + "<div class='reply'>";
            }

            // Name
            p = Pattern.compile("(?<=<span class=\\\"member\\\">)(.|\\n)*?(?=</span>)", Pattern.CASE_INSENSITIVE);
            m = p.matcher(matchstr);

            String strName;
            if (m.find()) { // Find each match in turn; String can't do this.
                strName = m.group(0);
            } else {
                strName = "";
            }
            // strName = strName.replaceAll("<((.|\\n)*?)+>", "");
            mComment = mComment + "<div class='reply_header'>" + strName + " (";

            // Date
            p = Pattern.compile("<time datetime=.+?>(.|\\n)*?</time>", Pattern.CASE_INSENSITIVE);
            m = p.matcher(matchstr);

            String strDate;
            if (m.find()) { // Find each match in turn; String can't do this.
                strDate =  m.group(0);
            } else {
                strDate = "";
            }
            strDate = strDate.replaceAll("<((.|\\n)*?)+>", "");
            /*strDate = strDate.replaceAll("\n", "");
            strDate = strDate.replaceAll("\r", "");
            strDate = strDate.trim();*/
            mComment = mComment + strDate + ")</div>";

            // comment
            p = Pattern.compile("(?<=<!-- 댓글 출력 -->)(.|\\n)*?(?=<!-- 수정 -->)", Pattern.CASE_INSENSITIVE);
            m = p.matcher(matchstr);

            String strComment;
            if (m.find()) { // Find each match in turn; String can't do this.
                strComment = m.group(0);
                System.out.println("###############################");
                System.out.println(strComment);
                System.out.println("###############################");
            } else {
                strComment = "";
            }
            strComment = strComment.replaceAll("\n", "");
            strComment = strComment.replaceAll("\r", "");
            strComment = strComment.replaceAll("<br>", "\n");
            strComment = strComment.replaceAll("&nbsp;", " ");
//            strComment = strComment.replaceAll("(<)(.|\\n)*?(>)", "");
            mComment = mComment + "<div class='reply_content'>" + strComment + "</div></div>";
        }

        //String strHeader = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">";
        String strHeader = "<!DOCTYPE html>";
        strHeader += "<html><head>";
        strHeader += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=euc-kr\">";
        strHeader += "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, target-densitydpi=medium-dpi\">";
        strHeader += "<style>body {font-family:\"고딕\";font-size:medium;}.title{text-margin:10px 0px;font-size:large}.name{color:gray;margin:10px 0px;font-size:small}.content{border-top:1px solid gray}.profile {text-align:center;color:white;background: lightgray; margin:10px0px;border-radius:5px;font-size:small}.reply{border-bottom:1px solid gray;margin:10px 0px}.reply_header {color:gray;;font-size:small}.reply_content {margin:10px 0px}.re_reply{border-bottom:1px solid gray;margin:10px 0px 0px 20px;background:lightgray}</style>";
        strHeader += "</head>";
        String strBottom = "</body></html>";
        String strResize = "<script>function resizeImage2(mm){var width = eval(mm.width);var height = eval(mm.height);if( width > 300 ){var p_height = 300 / width;var new_height = height * p_height;eval(mm.width = 300);eval(mm.height = new_height);}} function image_open(src, mm) { var width = eval(mm.width); window.open(src,'image');}</script>";
//        String cssStr = "<link href=\"./css/default.css\" rel=\"stylesheet\">";
        String strBody = "<body>";

        htmlDoc = strHeader + strTitle + strResize + strBody + mContent + strAttach + mComment + strBottom;

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }
}
