package com.sam_chordas.android.stockhawk.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.db.chart.model.LineSet;
import com.sam_chordas.android.stockhawk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;


public class StockDetailActivityFragment extends Fragment {
    private String symbol;
    private ArrayList<Double> chartable = new ArrayList<>();
    private LineSet mLineSet;

    public StockDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stock_detail, container, false);

        Intent intent = getActivity().getIntent();
        symbol = intent.getStringExtra("symbol");
        new FetchHistoricDataTask().execute(symbol);

        return rootView;
    }

    class FetchHistoricDataTask extends AsyncTask<String, Void, double[]> {

        private double[] getDataFromJson(String json) throws JSONException {
            JSONObject all = new JSONObject(json);
            JSONObject query = all.getJSONObject("query");
            JSONObject results = query.getJSONObject("results");
            JSONArray quote = results.getJSONArray("quote");
            double[] returnable = new double[quote.length()];
            Log.v("quote length", quote.length() + "");


            for (int i = 0; i < quote.length(); i++){
                JSONObject curr = quote.optJSONObject(i);
                double addMe = curr.getDouble("Close");
                Log.v("Adding these ", addMe + "");

                returnable[i] = addMe;
            }
            return returnable;
        }

        protected double[] doInBackground(String... params) {

            String baseUrl = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22";
            //add symbol between the two YHOO
            String beforeStart  = "%22%20and%20startDate%20%3D%20%22";
            //add start date in between (2009-09-11)
            String beforeEnd = "%22%20and%20endDate%20%3D%20%22";
            //add end date in between (2010-03-10)
            String theRest = "%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";

            String wholeUrl = baseUrl + params[0] + beforeStart;
            Calendar calEnd = Calendar.getInstance();
            Calendar calStart = Calendar.getInstance();
            calEnd.add(Calendar.DATE, -30);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getDefault());
            String prevDate = sdf.format(calStart.getTime());
            String currDate = sdf.format(calEnd.getTime());


            wholeUrl = wholeUrl + currDate + beforeEnd + prevDate + theRest;

            Log.v("url ", wholeUrl);
            Log.v("url ", wholeUrl);



            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String JsonString = null;

            try {
                URL url = new URL(wholeUrl);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null){
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                if (buffer.length() == 0) {
                    return null;
                }
                JsonString = buffer.toString();
            }
            catch (IOException e) {
                return null;
            }
            finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                return getDataFromJson(JsonString);
            } catch (JSONException e){
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(double[] doubles) {
            super.onPostExecute(doubles);

        }
    }

}



