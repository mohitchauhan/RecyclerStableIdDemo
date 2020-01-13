package com.recyclerviewdemo;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "RecyclerViewExample";

    private List<FeedItem> feedsList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private StableIdsRecyclerViewAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private boolean isScrolling;
    private int start = 140;
    private final RecyclerView.OnScrollListener chatThreadScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem <= 10) {
                if (!isScrolling) {
                    isScrolling = true;
                    generateMoreItems();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addOnScrollListener(chatThreadScrollListener);

        new MessageGenerationTask().execute(start);



    }

    private void generateMoreItems() {
        Toast.makeText(MainActivity.this, "Fetching old feeds !", Toast.LENGTH_SHORT).show();
        new MessageGenerationTask().execute(start -= 10);
    }

    public static class StableIdsRecyclerViewAdapter extends RecyclerView.Adapter<StableIdsRecyclerViewAdapter.CustomViewHolder> {
        private List<FeedItem> feedItemList;

        public StableIdsRecyclerViewAdapter(List<FeedItem> feedItemList) {
            this.feedItemList = feedItemList;
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, viewGroup, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
            final FeedItem feedItem = feedItemList.get(i);
            //Setting text view title
            customViewHolder.textView.setText(Html.fromHtml(feedItem.getTitle()));
        }

        @Override
        public long getItemId(int position) {
            FeedItem feedItem = feedItemList.get(position);
            // Lets return in real stable id from here
            return feedItem.getId();
        }

        @Override
        public int getItemCount() {
            return (null != feedItemList ? feedItemList.size() : 0);
        }

        void setData(ArrayList<FeedItem> newList) {
            this.feedItemList = newList;
        }


        class CustomViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            CustomViewHolder(View view) {
                super(view);
                this.textView = (TextView) view.findViewById(R.id.title);
            }
        }


    }

    @SuppressLint("StaticFieldLeak")
    public class MessageGenerationTask extends AsyncTask<Integer, Void, ArrayList<FeedItem>> {

        private static final int MIN = 0;
        private static final int MAX = 150;


        @Override
        protected void onPreExecute() {

        }

        @Override
        protected ArrayList<FeedItem> doInBackground(Integer... params) {
            ArrayList<FeedItem> newList = new ArrayList<>();
            int start = params[0];
            if (start < MIN || start + 10 > MAX) return newList;

            for (int i = start; i < start + 10; i++) {
                FeedItem item = new FeedItem();
                item.setTitle("Title " + i);
                item.setId(i);
                newList.add(item);
            }
            return newList;
        }

        @Override
        protected void onPostExecute(ArrayList<FeedItem> result) {

            if (result.size() > 0) {
                if (adapter == null) {
                    adapter = new StableIdsRecyclerViewAdapter(result);
                    adapter.setHasStableIds(true);
                    mRecyclerView.setItemAnimator(null);
                    mRecyclerView.setAdapter(adapter);
                    mRecyclerView.scrollToPosition(result.size() - 1);
                    feedsList = result;
                } else {
                    final ArrayList<FeedItem> newList = new ArrayList<>(feedsList);
                    newList.addAll(0, result);
                    adapter.setData(newList);
                    DiffUtil.DiffResult newResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                        @Override
                        public int getOldListSize() {
                            return feedsList.size();
                        }

                        @Override
                        public int getNewListSize() {
                            return newList.size();
                        }

                        @Override
                        public boolean areItemsTheSame(int i, int i1) {
                            return feedsList.get(i).equals(newList.get(i1));
                        }

                        @Override
                        public boolean areContentsTheSame(int i, int i1) {
                            return true;
                        }
                    });
                    Log.d(TAG, " load more applied " + feedsList.size());
                    newResult.dispatchUpdatesTo(adapter);
                    feedsList = newList;
                    isScrolling = false;
                }


            } else {
                Toast.makeText(MainActivity.this, "No more data !", Toast.LENGTH_SHORT).show();
            }
        }
    }
}