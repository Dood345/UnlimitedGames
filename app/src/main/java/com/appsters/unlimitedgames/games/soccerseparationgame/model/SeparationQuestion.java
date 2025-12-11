package com.appsters.unlimitedgames.games.soccerseparationgame.model;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class SeparationQuestion {

    public Player player0;
    public String club01;

    public List<Player> choices1;
    public Player correct1;

    public String club12;
    public Player player2;

    public static class Player {
        public String name;
        public String id;
    }

    public static List<SeparationQuestion> parse(String json) {
        List<SeparationQuestion> list = new ArrayList<>();

        try {
            JSONArray arr = new JSONArray(json);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                SeparationQuestion q = new SeparationQuestion();

                q.player0 = parsePlayer(o.getJSONObject("Player_0"));
                q.club01 = o.getString("Club_0_1");

                JSONArray choicesArr = o.getJSONArray("Choices_1");
                q.choices1 = new ArrayList<>();
                for (int j = 0; j < choicesArr.length(); j++) {
                    q.choices1.add(parsePlayer(choicesArr.getJSONObject(j)));
                }

                q.correct1 = parsePlayer(o.getJSONObject("Correct_1"));
                q.club12 = o.getString("Club_1_2");
                q.player2 = parsePlayer(o.getJSONObject("Player_2"));

                list.add(q);
            }
        } catch (Exception ignored) {}

        return list;
    }

    private static Player parsePlayer(JSONObject obj) throws Exception {
        Player p = new Player();
        p.name = obj.getString("name");
        p.id  = obj.getString("id");
        return p;
    }
}
