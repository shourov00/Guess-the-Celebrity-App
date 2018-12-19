package com.tutorial.shourov.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private String url = "http://www.posh24.se/kandisar";
    private String splitPart = "<div class=\"sidebarContainer\">";
    private int choosenCeleb = 0;
    private ImageView mImageView;
    private Button mButton0;
    private Button mButton1;
    private Button mButton2;
    private Button mButton3;

    private List<String> celebUrls = new ArrayList<>();
    private List<String> celebNames = new ArrayList<>();

    private int locationOfCorrectAns = 0;
    private String[] answers = new String[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.imageView);
        mButton0 = findViewById(R.id.button);
        mButton1 = findViewById(R.id.button1);
        mButton2 = findViewById(R.id.button2);
        mButton3 = findViewById(R.id.button3);

        executeDownlaoding();
    }

    public void celebritiesChoosen(View view){
        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAns))){
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            
            //update for next image
            generateRandomUrlsAndSetImageView();
        }else{
            Toast.makeText(this, "Wrong! It was "+celebNames.get(choosenCeleb), Toast.LENGTH_SHORT).show();

            //update for next image
            generateRandomUrlsAndSetImageView();
        }
    }

    private void executeDownlaoding() {
        DownloadCelebrities mDownloadCelebrities = new DownloadCelebrities();
        String result;
        try {

            result = mDownloadCelebrities.execute(url).get();
            downloadedContentSpliting(result);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void downloadedContentSpliting(String result) {
        //web content String manipulation
        String[] splitResult = result.split(splitPart);

        //now manipulate src and alt side
        //for urls
        Pattern pattern = Pattern.compile("<img src=\"(.*?)\"");
        Matcher matcher = pattern.matcher(splitResult[0]);

        while (matcher.find()) {
            celebUrls.add(matcher.group(1));
        }

        //for names
        pattern = Pattern.compile("alt=\"(.*?)\"");
        matcher = pattern.matcher(splitResult[0]);

        while (matcher.find()) {
            celebNames.add(matcher.group(1));
        }

        generateRandomUrlsAndSetImageView();
    }

    private void generateRandomUrlsAndSetImageView() {
        //generating random image
        Random random = new Random();
        choosenCeleb = random.nextInt(celebUrls.size()); // 1 or 0 or size

        //image loading
        ImageDownloader imageDownloader = new ImageDownloader();
        Bitmap image;
        try {
            image = imageDownloader.execute(celebUrls.get(choosenCeleb)).get();
            mImageView.setImageBitmap(image);//set the image to imageview

            generateAnswersForButtons(random);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void generateAnswersForButtons(Random random) {
        //generating names as answer for buttons
        locationOfCorrectAns = random.nextInt(4);

        int incorrectAnswer;
        for (int i = 0; i < 4; i++) {
            if (i == locationOfCorrectAns) {
                //update imageView and buttons
                answers[i] = celebNames.get(choosenCeleb);
            } else {
                //create random answer
                incorrectAnswer = random.nextInt(celebUrls.size());

                while (incorrectAnswer == choosenCeleb) {
                    incorrectAnswer = random.nextInt(celebUrls.size());
                }

                answers[i] = celebNames.get(incorrectAnswer);
            }
        }

        //set text to buttons
        mButton0.setText(answers[0]);
        mButton1.setText(answers[1]);
        mButton2.setText(answers[2]);
        mButton3.setText(answers[3]);
    }

    //downloading content from posh24.com
    public class DownloadCelebrities extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";

            try {

                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                Log.d("Exception", " : " + e);
            }

            return null;
        }
    }

    //downloading images from posh24.com
    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {

                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                connection.connect();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                return bitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
