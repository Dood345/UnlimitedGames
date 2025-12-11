package com.appsters.unlimitedgames.games.soccerseparationgame.repository;

import android.util.Log;

import com.appsters.unlimitedgames.games.soccerseparationgame.model.SeparationQuestion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.function.BiConsumer;

public class SoccerSeparationGameRepository {

    private static final String TAG = "SeparationRepo";

    private final android.content.SharedPreferences sharedPreferences;

    public SoccerSeparationGameRepository(android.content.Context context) {
        this.sharedPreferences = context.getSharedPreferences("SoccerGamePrefs", android.content.Context.MODE_PRIVATE);
    }

    public void loadQuestions(BiConsumer<List<SeparationQuestion>, Exception> callback) {

        Log.d(TAG, "Loading questions…");

        fetchTeammateQuestions(
                2,
                10,
                3,
                (json, err) -> {
                    if (err != null) {
                        Log.e(TAG, "Error while fetching questions", err);
                        callback.accept(null, err);
                        return;
                    }

                    Log.d(TAG, "Raw JSON received: " + json);

                    List<SeparationQuestion> parsed = SeparationQuestion.parse(json);
                    Log.d(TAG, "Parsed " + parsed.size() + " questions");

                    callback.accept(parsed, null);
                });
    }

    public void fetchTeammateQuestions(
            int steps,
            int numQuestions,
            int numOptions,
            BiConsumer<String, Exception> callback) {

        String urlString = "http://sdmay26-37.ece.iastate.edu:8080/soccer/teammates/question"
                + "?steps=" + steps
                + "&num_questions=" + numQuestions
                + "&num_options=" + numOptions;

        Log.d(TAG, "Fetching from URL: " + urlString);

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestProperty("Accept", "application/json");

                int code = connection.getResponseCode();
                Log.d(TAG, "HTTP Response Code: " + code);

                if (code != HttpURLConnection.HTTP_OK) {
                    callback.accept(null, new Exception("HTTP " + code));
                    return;
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();

                Log.d(TAG, "JSON response length: " + sb.length());
                callback.accept(sb.toString(), null);

            } catch (Exception e) {
                Log.e(TAG, "Network error", e);
                callback.accept(null, e);

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    public void saveLocalHighScore(int score) {
        int currentHigh = getLocalHighScore();
        if (score > currentHigh) {
            sharedPreferences.edit().putInt("high_score", score).apply();
        }
    }

    public int getLocalHighScore() {
        return sharedPreferences.getInt("high_score", 0);
    }

    public void clearUserData() {
        sharedPreferences.edit().clear().apply();
    }
}
