package com.example.noob.textdetector;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Field;

public class ShowSignActivity extends AppCompatActivity {
    LinearLayout signageLayout;
    String locale = "en";
    private ScaleGestureDetector scaleGestureDetector = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sign);
        signageLayout = findViewById(R.id.signMain);
        Intent intent = getIntent();

        if(intent.hasExtra("data") && intent.hasExtra("locale")){

            try {
                JSONArray data = new JSONArray(intent.getStringExtra("data"));
                String locale  = intent.getStringExtra("locale");

                displaySignOnBoard(data);
            } catch (JSONException e) {
                Toast.makeText(ShowSignActivity.this,"Error occured",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }else{
            finish();
        }
    }



    public void displaySignOnBoard(JSONArray board_data){

                signageLayout.removeAllViews();
                signageLayout.setVisibility(View.GONE);
                try {
                    for (int k = 0; k < board_data.length(); k++) {
                        JSONArray row_data = board_data.getJSONArray(k);
                        LinearLayout row_layout = new LinearLayout(this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        //params.setMargins(0,(int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,1),0,0);
                        row_layout.setLayoutParams(params);
                        row_layout.setOrientation(LinearLayout.HORIZONTAL);
                        for (int m = 0; m < row_data.length(); m++) {
                            String sign_text = "";
                            LinearLayout item_layout = new LinearLayout(this);
                            LinearLayout.LayoutParams params_item = new LinearLayout.LayoutParams((int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,200), (int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,40));
                            //params_item.leftMargin                 = (int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,1);

                            item_layout.setLayoutParams(params_item);
                            item_layout.setOrientation(LinearLayout.HORIZONTAL);
                            if(row_data.getJSONObject(m).getJSONObject("text_multilingual").has(locale)) {
                                sign_text = row_data.getJSONObject(m).getJSONObject("text_multilingual").getString(locale);
                            }else{
                                sign_text = row_data.getJSONObject(m).getJSONObject("text_multilingual").getString("en");
                            }

                            if(row_data.getJSONObject(m).getString("color").equals("blue")){
                                item_layout.setBackgroundResource(R.drawable.blue_rounded);
                            }else if(row_data.getJSONObject(m).getString("color").equals("red")){
                                item_layout.setBackgroundResource(R.drawable.red_rounded);
                            }else{
                                item_layout.setBackgroundResource(R.drawable.blue_rounded);
                            }
                            TextView txt            = new TextView(this);
                            ImageView direction     = new ImageView(this);
                            ImageView icon          = new ImageView(this);
                            String direction_name   = row_data.getJSONObject(m).getString("direction_name");
                            int direction_resid     = getResId(direction_name,R.drawable.class);
                            if(direction_resid > 0 && !sign_text.equals("")){
                                LinearLayout.LayoutParams direction_params = new LinearLayout.LayoutParams((int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,25f), (int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,25f));
                                direction_params.gravity                    = Gravity.CENTER_VERTICAL;
                                direction_params.leftMargin                 = (int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,5f);
                                //direction_params.bottomMargin                 = (int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,10);
                                icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                direction.setImageResource(direction_resid);
                                direction.setLayoutParams(direction_params);
                                item_layout.addView(direction);

                            }
                            String icon_name        = row_data.getJSONObject(m).getJSONArray("icon_urls").getString(0);
                            int icon_resid     = getResId(icon_name,R.drawable.class);
                            if(icon_resid > 0 && !sign_text.equals("")){
                                LinearLayout.LayoutParams icon_params = new LinearLayout.LayoutParams((int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,23.35f), (int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,23.35f));
                                icon_params.gravity                    = Gravity.CENTER_VERTICAL;
                                icon_params.leftMargin                 = (int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,4.9f);
                                //icon_params.bottomMargin                 = (int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,10);
                                icon.setImageResource(icon_resid);
                                icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                icon.setLayoutParams(icon_params);
                                item_layout.addView(icon);

                            }

                            LinearLayout.LayoutParams txt_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            txt_params.leftMargin                 = (int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,6.9f);
                            //txt_params.bottomMargin                 = (int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,10);
                            txt.setTextSize(13);
                            txt.setTypeface(null, Typeface.BOLD);
                            txt.setLayoutParams(txt_params);
                            txt.setText(sign_text);
                            txt.setGravity(Gravity.CENTER_VERTICAL);
                            txt.setTextColor(getResources().getColor(android.R.color.white));
                            item_layout.addView(txt);
                            row_layout.addView(item_layout);
                        }
                        signageLayout.addView(row_layout);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                signageLayout.setVisibility(View.VISIBLE);


    }

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
