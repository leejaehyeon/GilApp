package org.thegil.gilapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArticleListUI extends ListActivity implements Runnable {

    private ProgressDialog pd;

    protected String itemsTitle;
    protected String itemsLink;
    protected HttpClient httpClient;
    protected HttpContext httpContext;
    private List<HashMap<String, String>> arrayItems;
    private int nPage;
    private EfficientAdapter adapter;
    static final int REQUEST_WRITE = 1;
    static final int REQUEST_VIEW = 2;
    protected int mLoginStatus;
    protected String mErrorMsg;

    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<HashMap<String, String>> arrayItems;

        public EfficientAdapter(Context context, List<HashMap<String, String>> data) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);

            arrayItems = data;
        }
        public int getCount() {
            return arrayItems.size() + 1;
        }
        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == arrayItems.size()) {
                MoreHolder holder;
                convertView = mInflater.inflate(R.layout.list_item_moreitem, null);

                holder = new MoreHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
                holder.title.setText("더 보 기");
            } else {
                // A ViewHolder keeps references to children views to avoid unneccessary calls
                // to findViewById() on each row.
                ViewHolder holder;

                if (convertView != null) {
                    Object a = convertView.getTag();
                    if (!(a instanceof ViewHolder)) {
                        convertView = null;
                    }
                }

                // When convertView is not null, we can reuse it directly, there is no need
                // to reinflate it. We only inflate a new View when the convertView supplied
                // by ListView is null.
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_item_itemsview, null);

                    // Creates a ViewHolder and store references to the two children views
                    // we want to bind data to.
                    holder = new ViewHolder();
                    holder.date = (TextView) convertView.findViewById(R.id.date);
                    holder.name = (TextView) convertView.findViewById(R.id.name);
                    holder.subject = (TextView) convertView.findViewById(R.id.subject);
                    holder.comment = (TextView) convertView.findViewById(R.id.comment);

                    convertView.setTag(holder);
                } else {
                    // Get the ViewHolder back to get fast access to the TextView
                    // and the ImageView.
                    holder = (ViewHolder) convertView.getTag();
                }
                HashMap<String, String> item = new HashMap<String, String>();
                item = (HashMap<String, String>)arrayItems.get(position);
                String date = (String)item.get("date");
                String name = (String)item.get("name");
                String subject = (String)item.get("subject");
                String comment = (String)item.get("comment");
                // Bind the data efficiently with the holder.
                holder.date.setText(date);
                holder.name.setText(name);
                holder.subject.setText(subject);
                holder.comment.setText(comment);

                if (comment.length() > 0) {
                    holder.comment.setBackgroundResource(R.drawable.circle);
                } else {
                    holder.comment.setBackgroundResource(R.drawable.icon_none);
                }
            }
            return convertView;
        }

        static class ViewHolder {
            TextView date;
            TextView name;
            TextView subject;
            TextView comment;
        }

        static class MoreHolder {
            TextView title;
        }
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_list);

        GilApplication app = (GilApplication)getApplication();
        httpClient = app.httpClient;
        httpContext = app.httpContext;

        intenter();

        arrayItems = new ArrayList<HashMap<String, String>>();
        nPage = 1;

        LoadingData();
    }

    public void LoadingData() {
        pd = ProgressDialog.show(this, "", "로딩중", true,
                false);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        if (!getData(httpClient, httpContext)) {
            if (mErrorMsg.length() > 0) {
                mLoginStatus = -2;
            } else {
                // Login
                Login login = new Login();

                mLoginStatus = login.LoginTo(httpClient, httpContext, ArticleListUI.this);

                if (mLoginStatus > 0) {
                    if (getData(httpClient, httpContext)) {
                        mLoginStatus = 1;
                    } else {
                        mLoginStatus = -2;
                    }
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
            if(pd != null){
                if(pd.isShowing()){
                    pd.dismiss();
                }
            }
            displayData();
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
            ab.setMessage( "게시판을 볼 권한이 없습니다. " + mErrorMsg);
            ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            });
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
            if (nPage == 1) {
                adapter = new EfficientAdapter(ArticleListUI.this, arrayItems);
                setListAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
        Bundle extras = getIntent().getExtras();
        // 가져온 값을 set해주는 부분

        itemsTitle = extras.getString("ITEMS_TITLE").toString();
        itemsLink = extras.getString("ITEMS_LINK").toString();
    }

    protected boolean getData(HttpClient httpClient, HttpContext httpContext) {

        String Page = Integer.toString(nPage);
        String url = "http://thegil.org/2014/bbs/board.php?bo_table=" + itemsLink + "&page=" + Page; //변경
        HttpRequest httpRequest = new HttpRequest();

        String result = httpRequest.requestPost(httpClient, httpContext, url, null, "utf-8");

        // 각 항목 찾기
        HashMap<String, String> item;

        //Pattern p = Pattern.compile("(<tr height=22 align=center class=fAContent>)(.|\\n)*?(<td colspan=8 height=1 background=./img/skin/default/footer_line.gif></td>)", Pattern.CASE_INSENSITIVE);
        Pattern p = Pattern.compile( "(<tr class)(.|\\n)*?(</tr>)", Pattern.CASE_INSENSITIVE ); //<tr class에서 </tr>까지가 글 하나
        Matcher m = p.matcher(result);
        while (m.find()) { // Find each match in turn; String can't do this.
            item = new HashMap<String, String>();
            String matchstr = m.group(0);

            // isNew
            if (matchstr.indexOf("<img src=./img/skin/default/i_new.gif") >= 0) {
                item.put("isNew", "1");
            } else {
                item.put("isNew", "0");
            }

            // isReply  <img src=./img/skin/default/i_re.gif
            if (matchstr.indexOf("<img src=./img/skin/default/i_re.gif") >= 0) {
                item.put("isReply", "1");
            } else {
                item.put("isReply", "0");
            }

            // link
            Pattern p2 = Pattern.compile("wr_id=[0-9]+&amp;page=[0-9]+", Pattern.CASE_INSENSITIVE);
            Matcher m2 = p2.matcher(matchstr);
            if (m2.find()) { // Find each match in turn; String can't do this.
                //        	items.add(m.group(0)); // Access a submatch group; String can't do this. }
                String subject = m2.group(0);
                subject = subject.replaceAll("&amp;", "&");
                item.put("link", subject);
            } else {
                item.put("link", "");
            }

            // subject
            p2 = Pattern.compile( "(page=[0-9]+\\\">)(.|\\n)*?(?=<)", Pattern.CASE_INSENSITIVE);
            //p2 = Pattern.compile( "(?<=page=[0-9]+\\\">)(.|\\n)*?(?=<)", Pattern.CASE_INSENSITIVE);
            m2 = p2.matcher(matchstr);
            String subject;
            if (m2.find()) { // Find each match in turn; String can't do this.
                //        	items.add(m.group(0)); // Access a submatch group; String can't do this. }
                subject = m2.group(0);
            } else {
                subject = "";
            }
            // <[a-zA-Z0-9\\s\\\"/&_=\\.\\?;\\-:]+>
//	        subject = subject.replaceAll("<((.|\\n)*?)+>", "");
            subject = subject.replaceAll("page=[0-9]+\\\">", ""); //후방탐색자를 삽입하면 오류가 떠서 따로 처리
            subject = subject.replaceAll("&nbsp;", "");
            subject = subject.replaceAll("&lt;", "<");
            subject = subject.replaceAll("&gt;", ">");
            subject = subject.replaceAll("&amp;", "&");
            subject = subject.replaceAll("&quot;", "\"");
            subject = subject.replaceAll("&apos;", "'");
            subject = subject.replaceAll("&#039;", "'");
            subject = subject.trim();
            item.put("subject", subject);

            // writer
            p2 = Pattern.compile("(?<=<span class=\"sv_member\">)(.|\\n)*?(?=</td>)", Pattern.CASE_INSENSITIVE);
            m2 = p2.matcher(matchstr);
            String writer;
            if (m2.find()) { // Find each match in turn; String can't do this.
                //        	items.add(m.group(0)); // Access a submatch group; String can't do this. }
                writer = m2.group(0);
            } else {
                writer = "";
            }
            writer = writer.replaceAll("</span>", "");
            writer = writer.trim();
            writer = item.put("name", writer);

            // comment
            p2 = Pattern.compile("(?<=<span class=\"cnt_cmt\">)[0-9]+?(?=</span>)", Pattern.CASE_INSENSITIVE);
            m2 = p2.matcher(matchstr);
            if (m2.find()) { // Find each match in turn; String can't do this.
                //        	items.add(m.group(0)); // Access a submatch group; String can't do this. }
                item.put("comment", m2.group(0));
            } else {
                item.put("comment", "");
            }

            // date
            p2 = Pattern.compile("(?<=<td class=\"td_date\">)[0-9]{2}(:|-)[0-9]{2}(?=</td>)", Pattern.CASE_INSENSITIVE);
            m2 = p2.matcher(matchstr);
            String date;
            if (m2.find()) { // Find each match in turn; String can't do this.
                //        	items.add(m.group(0)); // Access a submatch group; String can't do this. }
                date = m2.group(0);
            } else {
                date = "";
            }
            //date = date.replaceAll("<((.|\\n)*?)+>", "");
            //date = date.trim();

            item.put("date", date);

            arrayItems.add( item );
        }

        return true;
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        if (position == arrayItems.size()) {
            nPage++;
            pd = ProgressDialog.show(this, "", "로딩중입니다. 잠시만 기다리십시오...", true,
                    false);

            Thread thread = new Thread(this);
            thread.start();
        } else {
            HashMap<String, String> item = new HashMap<String, String>();
            item = (HashMap<String, String>)arrayItems.get(position);
            String subject = (String)item.get("subject");
            String date = (String)item.get("date");
            String username = (String)item.get("name");
            String link = (String)item.get("link");

            Intent intent = new Intent(this, ArticleUI.class);
            intent.putExtra("SUBJECT", subject);
            intent.putExtra("DATE", date);
            intent.putExtra("USERNAME", username);
            intent.putExtra("LINK", link);
            intent.putExtra("BOARDID", itemsLink);
            startActivityForResult(intent, REQUEST_VIEW);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, 0, 0, "새로고침");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }



}
