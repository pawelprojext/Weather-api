package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @noinspection deprecation*/
public class MainActivity extends AppCompatActivity {

    EditText city;
    ImageView search;
    TextView result;
    String icon;
    ImageView iconView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        String apiKey = "bbd6c42c8019e5a80263e26392691f22";

        city = findViewById(R.id.cityEditText);
        search = findViewById(R.id.searchButton);
        result = findViewById(R.id.resultTextView);
        iconView = findViewById(R.id.icon);

        search.setOnClickListener(view -> {
            try {
                String cityName = city.getText().toString();

                if (cityName.isEmpty()) {
                    throw new NullPointerException();
                }

                Pattern pattern1 = Pattern.compile("\\d");
                Matcher matcher1 = pattern1.matcher(cityName);

                if (matcher1.find()) {
                    throw new IllegalArgumentException();
                }

                Pattern pattern2 = Pattern.compile("[^a-zA-Z0-9]");
                Matcher matcher2 = pattern2.matcher(cityName);

                if (matcher2.find()) {
                    throw new IllegalArgumentException();
                }

                new GetWeatherTask().execute(cityName);
            } catch (NullPointerException npex) {
                result.setText("Nie podano nazwy miasta");
                iconView.setImageIcon(null);
            } catch (IllegalArgumentException iaex) {
                result.setText("W nazwie znalazły się niedozwolone znaki");
                iconView.setImageIcon(null);
            }
        });
    }

    /** @noinspection deprecation*/
    private class GetWeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                String cityName = strings[0];
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=bbd6c42c8019e5a80263e26392691f22&lang=pl");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                response.append(reader.readLine());
                JSONObject root = new JSONObject(new JSONTokener(response.toString()));
                JSONArray weather = root.getJSONArray("weather");
                icon = weather.getJSONObject(0).getString("icon");
                JSONObject main = root.getJSONObject("main");
                double temp = main.getDouble("temp");
                String description = weather.getJSONObject(0).getString("description");
                int visibility = root.getInt("visibility");
                double speed = root.getJSONObject("wind").getDouble("speed");
                int clouds = root.getJSONObject("clouds").getInt("all");
                String name = root.getString("name");

                temp = (int)(temp - 273.15);

                String out = "description: " + description + "\ntemp: " + temp + " C" + "\nvisibility: " + visibility + "%" + "\nwind: " + speed + " m/s" + "\nclouds: " + clouds + "%" + "\nname: " + name;

                reader.close();
                conn.disconnect();

                return out;

            } catch(Exception e) {
                e.printStackTrace();
                Log.d("ERR", e.getMessage());

                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {

                result.setText(response);

                String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png";
                Picasso.get().load(iconUrl).into(iconView);

            } else {
                result.setText("Błąd pobierania danych pogodowych.");
            }
        }
    }
}