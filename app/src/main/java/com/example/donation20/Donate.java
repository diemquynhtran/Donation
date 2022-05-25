package com.example.donation20;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.api.DonationApi;
import com.example.main.DonationApp;
import com.example.models.Donation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.util.List;


public class Donate extends Base
{
    private Button donateButton;
    private RadioGroup paymentMethod;
    private ProgressBar progressBar;
    private NumberPicker amountPicker;
    private EditText amountText;
    private TextView amountTotal;
    private TextView total;

    //private Button buttonTest;

    private int amountOfDonating = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action",
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        donateButton = (Button) findViewById(R.id.donateButton);

        //buttonTest = (Button) findViewById(R.id.button);
        /*buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity (new Intent(Donate.this, TestActivity.class));
            }
        });*/


        paymentMethod = (RadioGroup) findViewById(R.id.paymentMethod);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        amountPicker = (NumberPicker) findViewById(R.id.amountPicker);
        amountText    = (EditText) findViewById(R.id.paymentAmount);
        amountTotal   = (TextView) findViewById(R.id.totalSoFar);
        total = (TextView) findViewById(R.id.textView);

        amountPicker.setMinValue(0);
        amountPicker.setMaxValue(1000);
        progressBar.setMax(10000);
        total.setText("Total so Far");

        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String method = paymentMethod.getCheckedRadioButtonId() == R.id.PayPal ? "PayPal" : "Direct";

                int donatedAmount =  amountPicker.getValue();
                if (donatedAmount == 0)
                {
                    String text = amountText.getText().toString();
                    if (!text.equals(""))
                        donatedAmount = Integer.parseInt(text);
                }

                if (donatedAmount > 0)
                {
                    Donation _newDonation = new Donation(donatedAmount, method, 0);
                    new InsertTask(Donate.this).execute("/donates", _newDonation);
                    //app.totalDonated += _newDonation.amount;
                    progressBar.setProgress(app.totalDonated);
                    String totalDonatedStr = "$" + app.totalDonated;
                    amountTotal.setText(totalDonatedStr);
                }
            }
        });

    }
    public void onDonationDeleteAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete ALL Donations?");
        builder.setIcon(android.R.drawable.ic_delete);
        builder.setMessage("Are you sure you want to Delete ALL the Donations");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new ResetTask(Donate.this).execute("/donates");
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void reset(MenuItem item) {
        super.reset(item);
        onDonationDeleteAll();
    }

    public void donateButtonPressed (View view)
    {
        String method = paymentMethod.getCheckedRadioButtonId() == R.id.PayPal ? "PayPal" : "Direct";

        int donatedAmount =  amountPicker.getValue();
        if (donatedAmount == 0)
        {
            String text = amountText.getText().toString();
            if (!text.equals(""))
                donatedAmount = Integer.parseInt(text);
        }

        if (donatedAmount > 0)
        {
            app.newDonation(new Donation(donatedAmount, method, 0));
            progressBar.setProgress(app.totalDonated);
            String totalDonatedStr = "$" + app.totalDonated;
            amountTotal.setText(totalDonatedStr);
        }
    }
    //INSERT DONATE
    private class InsertTask extends AsyncTask<Object, Void, String> {
        protected ProgressDialog dialog;
        protected Context context;

        public InsertTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Saving Donation....");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {
            String res = null;
            try {
                Log.v("Donate", "Donation App Inserting");
                res = DonationApi.insert((String) params[0], (Donation) params[1]);
            } catch (Exception e) {
                Log.v("Donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            this.dialog.setMessage("Success....");
            this.dialog.show();
            if (this.dialog.isShowing())
                this.dialog.dismiss();
            new GetAllTask(Donate.this).execute("/donates");
        }
    }

    private class ResetTask extends AsyncTask<Object, Void, String> {
        protected ProgressDialog dialog;
        protected Context context;

        public ResetTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Deleting Donations....");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {
            String res = null;
            try {
                for (int i = 0; i < app.donations.size(); i++) {
                    res = DonationApi.delete((String) params[0], (String) app.donations.get(i)._id);
                    //Log.v("Donate", "Log" + i);
                }

            } catch (Exception e) {
                Log.v("Donate", " RESET ERROR : " + e);
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //app.totalDonated = 0;
            //progressBar.setProgress(app.totalDonated);
            //amountTotal.setText("$" + app.totalDonated);
            new GetAllTask(Donate.this).execute("/donates");
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.v("Donate", "onResum");
        new GetAllTask(this).execute("/donates");
    }

    private class GetAllTask extends AsyncTask<String, Void, List<Donation>> {


        protected ProgressDialog dialog;
        protected Context context;
        public GetAllTask(Context context)
        {
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
                Log.v("Donate", "Donation App Getting All Donations");
                return (List<Donation>) DonationApi.getAll((String) params[0]);
            }
            catch (Exception e) {
                Log.v("Donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(List<Donation> result) {
            super.onPostExecute(result);
            //Log.v("s", result.toString());
            //use result to calculate the totalDonated amount here
            app.donations = result;
            app.totalDonated = 0;
            for (int i = 0; i < result.size(); i++) {
                app.totalDonated += result.get(i).amount;
            }
            progressBar.setProgress(app.totalDonated);
            amountTotal.setText("$" + app.totalDonated);
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }



}

