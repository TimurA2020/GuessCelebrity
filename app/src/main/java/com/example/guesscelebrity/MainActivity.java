package com.example.guesscelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    String celebrityUrl = "https://www.imdb.com/list/ls052283250/";

    String[] imageUrls = new String[100];
    String[] celebNames = new String[100];

    private int questionNumber;
    private int correctAnswer;

    private Button button;
    private Button button2;
    private Button button3;
    private Button button4;

    private ArrayList<Button> buttons;

    String stringHtml;

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        //buttons
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        buttons = new ArrayList<>();
        buttons.add(button);
        buttons.add(button2);
        buttons.add(button3);
        buttons.add(button4);

        //getting html source code
        HtmlTask htmlTask = new HtmlTask();
        try {
            stringHtml = htmlTask.execute(celebrityUrl).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        compilePatterns();

        playGame();

    }

    private void compilePatterns(){
        Pattern pattern = Pattern.compile("height=\"209\"src=\"(.*?)\"width=\"140\"");
        Matcher matcher = pattern.matcher(stringHtml);
        int i = 0;
        while (matcher.find()){
            imageUrls[i] = matcher.group(1);
            i++;
        }

        Pattern namePattern = Pattern.compile("img alt=\"(.*?)\"height=");
        Matcher nameMatcher = namePattern.matcher(stringHtml);
        int j = 0;
        while (nameMatcher.find()){
            celebNames[j] = nameMatcher.group(1);
            j++;
        }
    }



    private void playGame(){
        generateQuestion();
        DownloadImageTask imageTask = new DownloadImageTask();
        try {
            imageView.setImageBitmap(imageTask.execute(imageUrls[questionNumber]).get());
            for(int i = 0; i < buttons.size(); i++){
                if(i == correctAnswer){
                    buttons.get(i).setText(celebNames[questionNumber]);
                }else{
                    int wrongAnswer = generateWrongAnswers();
                    buttons.get(i).setText(celebNames[wrongAnswer]);
                }
            }
            } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void generateQuestion(){
        questionNumber = (int) (Math.random() * celebNames.length);
        correctAnswer = (int) (Math.random() * buttons.size());
    }

    private int generateWrongAnswers(){
        return (int) (Math.random() * celebNames.length);
    }

    public void onClickAnswer(View view){
        Button clickedButton = (Button) view;
        String tag = clickedButton.getTag().toString();
        if(Integer.parseInt(tag) == correctAnswer){
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Incorrect! The right answer is + " + celebNames[questionNumber], Toast.LENGTH_SHORT).show();
        }
        playGame();
    }

    private static class HtmlTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection connection = null;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = bufferedReader.readLine();
                while (line != null){
                    stringBuilder.append(line);
                    line = bufferedReader.readLine();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(connection != null) {
                    connection.disconnect();
                }
            }

            return stringBuilder.toString();
        }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {
            HttpURLConnection connection = null;
            Bitmap bitmap = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(connection != null){
                    connection.disconnect();
                }
            }

            return bitmap;
        }
    }
}