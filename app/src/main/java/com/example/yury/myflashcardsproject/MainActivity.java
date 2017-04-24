package com.example.yury.myflashcardsproject;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.yury.myflashcardsproject.database.DBCard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.yury.myflashcardsproject.RVAdapter.context;


public class MainActivity extends AppCompatActivity {

    public static String JSON_FILE = "questions.json";
    public List<Card> cards;
    public RVAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cards = new ArrayList<>();

        RecyclerView rvCardList = (RecyclerView) findViewById(R.id.rvCardList);
        rvCardList.setHasFixedSize(true);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        rvCardList.setLayoutManager(manager);
        adapter = new RVAdapter(cards, this);
        rvCardList.setAdapter(adapter);

        final DialogFragment addCardDialog = new AddCardDialog();

        FloatingActionButton fabAddCard = (FloatingActionButton) findViewById(R.id.fab_add_card);
        fabAddCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                addCardDialog.show(getSupportFragmentManager(), "TAG");
            }
        });

        CardReader cardReader = new CardReader();
        cardReader.execute();

    }

    public static class Card {
        String question;
        String answer;

        public Card(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }

    private class CardReader extends AsyncTask <Void, Void, List<Card>>{

        @Override
        protected List<Card> doInBackground(Void... voids) {

            String jsonString = "";
            List<Card> cardList   = new ArrayList<>();

//          LOAD CARDS FROM JSON FILE
            try {
                InputStream inputStream = getApplicationContext().getAssets().open(MainActivity.JSON_FILE);
                int size = inputStream.available();
                byte[] buffer = new byte[size];
                inputStream.read(buffer);
                inputStream.close();
                jsonString = new String(buffer, "UTF-8");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return  null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }


            try{
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("questions");
                for ( int i = 0; i < jsonArray.length(); i++) {
                    cardList.add(new Card(jsonArray.getJSONObject(i).getString("question"),
                            jsonArray.getJSONObject(i).getString("answer")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

//         LOAD CARDS FROM DATABASE

            DBCard db = new DBCard(getApplicationContext());
            db.open();
            cardList.addAll(db.getAllCards());
            db.close();

            return cardList;
        }

        @Override
        protected void onPostExecute(List<Card> cardList) {
            adapter.update(cardList);
        }
    }

}
