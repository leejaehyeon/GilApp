package org.thegil.gilapp;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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


public class BoardUI extends ListActivity {

    protected String boardTitle;
    protected String boardCode;
    protected HttpClient httpClient;
    protected HttpContext httpContext;
    List<HashMap<String, String>> arrayItems;
    private EfficientAdapter adapter;
    private ProgressDialog pd;

    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<HashMap<String,String>> arrayItems;

        public EfficientAdapter(Context context, List<HashMap<String, String>> data) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);

            arrayItems = data;
        }

        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         *
         * @see android.widget.ListAdapter#getCount()
         */

        public int getCount() {
            return arrayItems.size() ;
        }

        /**
         * Since the data comes from an array, just returning the index is
         * sufficent to get at the data. If we were using a more complex data
         * structure, we would return whatever object represents one row in the
         * list.
         *
         * @see android.widget.ListAdapter#getItem(int)
         */
        public Object getItem(int position) {
            return position;
        }

        /**
         * Use the array index as a unique id.
         *
         * @see android.widget.ListAdapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a view to hold each row.
         *
         * @see android.widget.ListAdapter#getView(int, android.view.View,
         *      android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            HashMap<String, String> item = new HashMap<String, String>();
            item = (HashMap<String, String>)arrayItems.get(position);
            String title = (String)item.get("title");

            convertView = mInflater.inflate(R.layout.list_item_board, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            ViewHolder holder;
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.title);

            convertView.setTag(holder);
            // Bind the data efficiently with the holder.
            holder.title.setText(title);

            return convertView;
        }

        static class ViewHolder {
            TextView title;
            ImageView icon;
        }

        static class GroupHolder {
            TextView title;
        }
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board);

        intenter();

        arrayItems = new ArrayList<HashMap<String, String>>();

        getData();
        displayData();
    }
    protected boolean getData( ) {

        String[][] village = {
                { "B01&sca=소개", "단체소개" }, { "B02", "공지사항" }, { "B03", "회의자료" },
                { "B04", "자유게시판" }, { "B07", "소모임방" }, { "B05", "사진자료" },
                { "B06", "길장터" }
        };

        String[][] school = {
                { "B11&sca=소개", "단체소개" }, { "B12", "공지사항" }, { "B13", "회의자료" },
                { "B14", "자유게시판" }, { "B15", "재학생톡" }, { "B16", "졸업생톡" },
                { "B17", "문서자료" }, { "B18", "사진자료" }
        };

        String[][] center = {
                { "B21&sca=소개", "단체소개" }, { "B22", "공지사항" }, { "B23", "회의자료" },
                { "B24", "자유게시판" }, { "B25", "청년진로" }, { "B26", "사진자료" }
        };

        String[][] library = {
                { "B41&sca=소개", "단체소개" }, { "B42", "공지사항" }, { "B43", "회의자료" },
                { "B44", "자유게시판" }, { "B45", "책이야기" }, { "B46", "도서검색" }
        };

        String[][] community = {
                { "B51", "행사안내" }, { "B52", "연대단체" }, { "B53", "광고홍보" }
        };


        // 각 항목 찾기
        HashMap<String, String> item;

        if (boardCode.equalsIgnoreCase("village")) {
            for (int i = 0; i < village.length; i++) {
                item = new HashMap<String, String>();
                item.put("link",  village[i][0]);
                item.put("title",  village[i][1]);
                arrayItems.add( item );
            }
        } else if (boardCode.equalsIgnoreCase("school")) {
            for (int i = 0; i < school.length; i++) {
                item = new HashMap<String, String>();
                item.put("link",  school[i][0]);
                item.put("title",  school[i][1]);
                arrayItems.add( item );
            }
        } else if (boardCode.equalsIgnoreCase("center")) {
            for (int i = 0; i <center.length; i++) {
                item = new HashMap<String, String>();
                item.put("link", center[i][0]);
                item.put("title", center[i][1]);
                arrayItems.add( item );
            }
        } else if (boardCode.equalsIgnoreCase("library")) {
            for (int i = 0; i <library.length; i++) {
                item = new HashMap<String, String>();
                item.put("link", library[i][0]);
                item.put("title", library[i][1]);
                arrayItems.add( item );
            }
        } else if (boardCode.equalsIgnoreCase("community")) {
            for (int i = 0; i <community.length; i++) {
                item = new HashMap<String, String>();
                item.put("link", community[i][0]);
                item.put("title", community[i][1]);
                arrayItems.add( item );
            }
        }
        return true;
    }
    public void displayData() {
        setListAdapter(new EfficientAdapter(BoardUI.this, arrayItems));
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
        Bundle extras = getIntent().getExtras();
        // 가져온 값을 set해주는 부분

        boardTitle = extras.getString("BOARD_TITLE").toString();
        boardCode = extras.getString("BOARD_CODE").toString();
    }



    public void onListItemClick(ListView parent, View v, int position, long id) {
        HashMap<String, String> item = new HashMap<String, String>();
        String title = null;
        String link = null;
        item = (HashMap<String, String>)arrayItems.get(position);
        title = (String)item.get("title");
        link = (String)item.get("link");

        Intent intent = new Intent(this, ArticleListUI.class);
        intent.putExtra("ITEMS_TITLE", title);
        intent.putExtra("ITEMS_LINK", link);
        startActivity(intent);

    }
}
