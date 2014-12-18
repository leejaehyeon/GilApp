package org.thegil.gilapp;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FirstCategoryUI extends ListActivity {

    private BackPressCloseHandler backPressCloseHandler;

    protected HttpClient httpClient;
    protected HttpContext httpContext;
    static final int REQUEST_CODE = 1;

    private List<HashMap<String, String>> arrayItems;

    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        List<HashMap<String, String>> arrayItems;

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
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_category, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }
            HashMap<String,String> item = new HashMap<String, String>();
            String title = null;
            item = (HashMap<String, String>)arrayItems.get(position);
            title = (String)item.get("title");
            // Bind the data efficiently with the holder.
            holder.title.setText(title);

            return convertView;
        }

        static class ViewHolder {
            TextView title;
        }
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fisrt_category);


        GilApplication app = (GilApplication)getApplication();
        httpClient = app.httpClient;
        httpContext = app.httpContext;

        arrayItems = new ArrayList<HashMap<String, String>>();
        displayData();
    }

    public void displayData() {

        HashMap<String, String> item;

        /*배움터길에 맞게 아이템리스트들을 변경*/
        item = new HashMap<String, String>();
        item.put("code",  "village");
        item.put("title",  "마을공동체");
        arrayItems.add( item );
        item = new HashMap<String, String>();
        item.put("code",  "school");
        item.put("title",  "배움터길");
        arrayItems.add( item );
        item = new HashMap<String, String>();
        item.put("code",  "center");
        item.put("title",  "길센터");
        arrayItems.add( item );
        item = new HashMap<String, String>();
        item.put("code",  "library");
        item.put("title",  "동네도서관");
        arrayItems.add( item );
        item = new HashMap<String, String>();
        item.put("code",  "community");
        item.put("title",  "커뮤니티");
        arrayItems.add( item );
        item = new HashMap<String, String>();
        item.put("code",  "appInfo");
        item.put("title",  "앱정보");
        arrayItems.add( item );


        setListAdapter(new EfficientAdapter(FirstCategoryUI.this, arrayItems));
    }


    public void onListItemClick(ListView parent, View v, int position, long id) {
        HashMap<String, String> item = new HashMap<String, String>();
        Intent intent;
        String title = null;
        String code = null;
        item = (HashMap<String, String>)arrayItems.get(position);
        title = (String)item.get("title");
        code = (String)item.get("code");

        if( code.equalsIgnoreCase("appInfo") ) {
            intent = new Intent(this, AboutUI.class);
        } else {
            intent = new Intent(this, BoardUI.class);
        }
        intent.putExtra("BOARD_TITLE", title);
        intent.putExtra("BOARD_CODE", code);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, 0, 0, "로그아웃");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            Intent intent = new Intent(this, Login.class);
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        }
        return false;
    }

}
