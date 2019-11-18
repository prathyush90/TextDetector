package com.example.noob.textdetector.managers;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.noob.textdetector.ApiUtils;
import com.example.noob.textdetector.MainActivity;
import com.example.noob.textdetector.models.ResponseClass;
import com.example.noob.textdetector.models.Trie;
import com.example.noob.textdetector.retrofit.ApiService;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrieManager extends HandlerThread{

    private static final double THRESHOLD_SIGN = 0.20;
    private static final String LOG_TAG = "network";
    private Handler trieHandler;
    private Context mContext;
    private final int CREATE_TRIE = 0;
    private final int GET_BOARD   = 1;
    private Trie trieData;
    private HashMap<String,JSONObject> signMap;
    private HashMap<String,JSONObject> signWordMapping;
    private HashMap<String,Integer>    wordMapping;
    private boolean isReady = false;
    public TrieManager(String name, Context context) {
        super(name);
        this.mContext   = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void start(){
        super.start();

        trieData        = new Trie(false, StandardCharsets.UTF_8);
        signMap         = new HashMap<String, JSONObject>();
        signWordMapping = new HashMap<String, JSONObject>();
        wordMapping     = new HashMap<String,Integer>();
        isReady         = false;


    }

    public void getBoard(ArrayList<String> words){

        if(trieHandler != null && isReady) {
            Message msg = new Message();
            msg.what = GET_BOARD;
            msg.obj = words;
            trieHandler.sendMessage(msg);
        }
    }

    public void readSignBoards(){

        ApiService mAPIService    = ApiUtils.getAPIService();
        JsonObject params = new JsonObject();
        try {
            params.addProperty("appname","cochin");
            params.addProperty("token","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiNTk1Zjc1NmY1NGVkYWYyMzA4MjYzMTlhIiwiaWF0IjoxNDk5ODc2OTM4LCJleHAiOjE1NTE3MTY5Mzh9.9vFu_xjGMNb_w_1i-qIuxI9pUkdAMFkRqvmHmJAByHk");

        } catch (JsonIOException e) {
            e.printStackTrace();
        }
        mAPIService.sendPost(params).enqueue(new Callback<ResponseClass>() {
            @Override
            public void onResponse(Call<ResponseClass> call, Response<ResponseClass> response) {

                if(response.isSuccessful()) {
                    String stringified_data = response.body().getData().toString();
                    JSONArray signs = null;
                    try {
                        signs         = new JSONArray(stringified_data);
                        if(trieHandler != null){
                            Message msg         = new Message();
                            msg.what            = CREATE_TRIE;
                            msg.obj             = signs;
                            trieHandler.sendMessage(msg);
                        }
//                        ((MainActivity)mContext).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(mContext,"Data downloaded from cms",Toast.LENGTH_SHORT).show();
//                            }
//                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseClass> call, Throwable t) {
                Log.e("sss", "Unable to submit post to API.");
            }
        });

    }

    public void invalidate(){

        quitSafely();
    }

    @Override
    protected void onLooperPrepared() {

        trieHandler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case CREATE_TRIE:
                        createTrie((JSONArray)msg.obj);
                        break;

                    case GET_BOARD:
                        getSignBoard((ArrayList<String>) msg.obj);
                        break;
                }
            }
        };
        readSignBoards();
    }

    class MyKey {
        String str;
        Float i;

        public MyKey(String id, float probability_num) {
            this.i      = probability_num;
            this.str    = id;
        }

        public String toString(){
            return "Name: "+this.str+"-- Salary: "+this.i;
        }
    }

    private void  getSignBoard(ArrayList<String> obj) {







        ArrayList<String> filtered = new ArrayList<>();
        for(String word: obj){

            word = word.toLowerCase();
            if(!trieData.search(word)) {
                for (Map.Entry<String, Integer> entry : trieData.getSimilarityMap(word, 1).entrySet()) {
                    filtered.add(entry.getKey());
                }
            }else{
                filtered.add(word);
            }
        }
        if(filtered.size() > 0){
            Log.d("xx","check");
            String s= "";
            for(int k=0;k<filtered.size();k++){
                s += filtered.get(k)+",";
            }
            //((MainActivity)mContext).displayProbability(s);
        }else{
            ((MainActivity)mContext).removeAllViewsAndHide();
            return;
        }
        Set<String>keys = signMap.keySet();



        TreeMap<MyKey, Float> treemap = new TreeMap<MyKey, Float>(new Comparator<MyKey>() {


            @Override
            public int compare(MyKey myKey, MyKey t1) {
                return myKey.i.compareTo(t1.i);
            }
        });



        for(String id : keys){
            float probability_num   = 1;
            float probability_den   = 1;
            int foundCount = 0;
            int totalWords = 1;
            for(String word : filtered){
                try {
                    if(signWordMapping.get(id).has(word)){
                        float word_prob_occurence = ((float)signWordMapping.get(id).getInt(word) / signWordMapping.get(id).getInt("total"));
                        probability_num          *= word_prob_occurence;

                        foundCount++;

                    }else{
                        probability_num *= 0.0001;
                    }
                    if(wordMapping.containsKey(word)){
                        probability_den *= ((float)wordMapping.get(word) / wordMapping.get("total"));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            try {
                totalWords = signWordMapping.get(id).getInt("total");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            float probability = ((float) probability_num / probability_den) * ((float) 1/keys.size());
            float percentage  = ((float) foundCount/totalWords) * 100;
            if(percentage >= 25){
                treemap.put(new MyKey(id,probability),probability);
            }

        }
        if(!treemap.isEmpty()){

            String id           =  treemap.lastEntry().getKey().str;
            JSONObject board    =  signMap.get(id);
            if(treemap.lastEntry().getKey().i > THRESHOLD_SIGN){
                try {
                    JSONArray board_data = board.getJSONArray("board_data");
                    ((MainActivity)mContext).displaySignOnBoard(board_data,board.getString("_id"));
                    //((MainActivity)mContext).displayProbability(String.valueOf(treemap.lastEntry().getKey().i));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }


    }

    private void createTrie(JSONArray signsData) {
        if(wordMapping != null && signWordMapping != null && signMap!= null) {
            for (int i = 0; i < signsData.length(); i++) {
                JSONObject sign = null;
                try {
                    sign = signsData.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (sign != null) {
                    try {
//                        String id = sign.getString("_id");
                        signMap.put(sign.getString("_id"), sign);
                        JSONArray board_data = sign.getJSONArray("board_data");
                        JSONObject signwordmap = new JSONObject();

                        for (int k = 0; k < board_data.length(); k++) {
                            JSONArray row_data = board_data.getJSONArray(k);
                            for (int m = 0; m < row_data.length(); m++) {
                                String english_text = row_data.getJSONObject(m).getJSONObject("text_multilingual").getString("en");
                                String words[] = english_text.split(" ");
                                for (int wordInd = 0; wordInd < words.length; wordInd++) {
                                    String word = words[wordInd].toLowerCase();
                                    if (word.length() > 2) {
                                        if (signwordmap.has(word)) {
                                            int freq = signwordmap.getInt(word);
                                            signwordmap.put(word, freq + 1);
                                        } else {
                                            signwordmap.put(word, 1);
                                        }
                                        if (wordMapping.containsKey(word)) {
                                            int freq = wordMapping.get(word);
                                            wordMapping.put(word, freq + 1);
                                        } else {
                                            wordMapping.put(word, 1);
                                        }
                                        if (signwordmap.has("total")) {
                                            int freq = signwordmap.getInt("total");
                                            signwordmap.put("total", freq + 1);
                                        } else {
                                            signwordmap.put("total", 1);
                                        }

                                        if (wordMapping.containsKey("total")) {
                                            int freq = wordMapping.get("total");
                                            wordMapping.put("total", freq + 1);
                                        } else {
                                            wordMapping.put("total", 1);
                                        }
                                        if (trieData != null) {
                                            trieData.add(word);
                                        }
                                    }

                                }

                            }
                        }
                        signWordMapping.put(sign.getString("_id"), signwordmap);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.d("testtag", "nothing to print");
            isReady = true;
        }
    }



}