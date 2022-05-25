package com.example.donation20;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.api.DonationApi;
import com.example.models.Donation;

import java.util.List;

public class Report extends Base implements AdapterView.OnItemClickListener
{
    ListView listView;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        listView = (ListView) findViewById(R.id.reportList);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.report_swipe_refresh_layout);
        new GetAllTask(this).execute("/donates");
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetAllTask(Report.this).execute("/donates");
            }
        });
        }

    @Override
    public void reset(MenuItem item)
    {
        app.totalDonated =0;
        //app.donations = [];
        DonationAdapter adapter = new DonationAdapter(this, app.donations);
        listView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        new GetTask(this).execute("/donates", view.getTag().toString());
    }

    private class GetAllTask extends AsyncTask<String, Void, List<Donation>> {
        protected ProgressDialog dialog;
        protected Context context;
        public GetAllTask(Context context) {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Retrieving Donations List");
            this.dialog.show();
        }
        @Override
        protected List<Donation> doInBackground(String... params) {
            try {
                return (List<Donation>) DonationApi.getAll((String) params[0]);
            } catch (Exception e) {
                Log.v("ASYNC", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(List<Donation> result) {
            super.onPostExecute(result);
            app.donations = result;
            DonationAdapter adapter = new DonationAdapter(context, app.donations);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(Report.this);
            mSwipeRefreshLayout.setRefreshing(false);
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    private class GetTask extends AsyncTask<String, Void, Donation> {
        protected ProgressDialog dialog;
        protected Context context;

        public GetTask(Context context) {
            this.context = context;
        }


        @Override
        protected Donation doInBackground(String... params) {
            try {
                return (Donation) DonationApi.get((String) params[0], (String)
                        params[1]);
            } catch (Exception e) {
                Log.v("donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Retrieving Donation Details");
            this.dialog.show();
        }
        @Override
        protected void onPostExecute(Donation result) {
            super.onPostExecute(result);
            Donation donation = result;
            Toast.makeText(Report.this, "Donation Data [ " + donation.upvotes +
                    "]\n " +
                    "With ID of [" + donation._id + "]", Toast.LENGTH_LONG).show();
            if (dialog.isShowing())
                dialog.dismiss();
        }

    }

    private class DeleteTask extends AsyncTask<String, Void, String> {

        protected ProgressDialog dialog;
        protected Context context;
        public DeleteTask(Context context) {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Deleting Donation");
            this.dialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
            try {
                return (String) DonationApi.delete((String) params[0], (String)
                        params[1]);
            } catch (Exception e) {
                Log.v("donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String s = result;
            Log.v("donate", "DELETE REQUEST : " + s);
            new GetAllTask(Report.this).execute("/donates");
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    public void onDonationDelete (final Donation donation) {
        String stringId = donation._id;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Donation?");
        builder.setIcon(android.R.drawable.ic_delete);
        builder.setMessage("Are you sure you want to Delete the 'Donation with ID' \n [ "
                + stringId + " ] ?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new DeleteTask(Report.this).execute("/donates", donation._id);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

class DonationAdapter extends ArrayAdapter<Donation> {
    private Context context;
    public List<Donation> donations;

    public DonationAdapter(Context context, List<Donation> donations) {
        super(context, R.layout.row_donate, donations);
        this.context = context;
        this.donations = donations;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row_donate, parent, false);
        Donation donation = donations.get(position);
        TextView amountView = (TextView) view.findViewById(R.id.row_amount);
        TextView methodView = (TextView) view.findViewById(R.id.row_method);
        TextView upvotesView = (TextView) view.findViewById(R.id.row_upvotes);
        amountView.setText("$" + donation.amount);
        methodView.setText(donation.method);
        upvotesView.setText("" + donation.upvotes);
        view.setTag(donation._id);

        ImageView imgDelete = (ImageView) view.findViewById((R.id.imgDelete));
        imgDelete.setTag(donation);
        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("tag", String.valueOf(view.getTag()));
                if (view.getTag() instanceof Donation) {
                    onDonationDelete((Donation) view.getTag());
                }
            }
        });
        return view;
    }

    @Override
    public int getCount() {
        return donations.size();
    }
}}