package com.leon.gpumark;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String[] gpuName = {"GTX 1080 Ti", "GTX 1080", "GTX 1070", "GTX 970"};
    private int[] gpuBenchmark = {13345, 11995, 11023, 8558};
    private float[] lowPrice = {5000, 3500, 2500, 970};
    private float[] highPrice = {6000, 4500, 3500, 1300};

    private static final String dataFileName = "ALL";
    private static final String NVIDIADataFileName = "NVIDIA";
    private static final String AMDDataFileName = "AMD";

    private ProgressBar progressLoading;
    private ViewPropertyAnimator loadingAnimator;

    private ScrollView scrollView;
    private LinearLayout chartContainer;

    private ArrayList<ArrayList<XianyuItem>> xianyuItemList = new ArrayList<>();
    private ArrayList<GraphicsCard> cardArrayList = new ArrayList<>();
    private HashMap<String, GraphicsCard> allCardMap = new HashMap<>();

    private ArrayList<GraphicsCard> NVIDIAList = new ArrayList<>();
    private ArrayList<GraphicsCard> AMDList = new ArrayList<>();
    private int currentCompany;

    private RequestQueue requestQueue;
    private String futuremarkURL = "https://www.futuremark.com/hardware/gpu";
    /*private String[] url = {
            "https://www.videocardbenchmark.net/high_end_gpus.html",
            "https://www.futuremark.com/hardware/gpu"
    };*/
    private static final String xianyuRequestURL = "http://s.ershou.taobao.com/list/list.htm?q=";
    private boolean removeNotebook = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressLoading = (ProgressBar) findViewById(R.id.progress_loading);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);

        //progressLoading.setVisibility(View.INVISIBLE);
        progressLoading.setAlpha(0);
        loadingAnimator = progressLoading.animate();
        loadingAnimator.setDuration(300);
        chartContainer = (LinearLayout) findViewById(R.id.chart_container);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        currentCompany = 0;

        requestQueue = Volley.newRequestQueue(this);
        updateData(1);
    }

    private void updateData(final int urlIndex) {

        //progressLoading.setVisibility(View.VISIBLE);
        loadingAnimator.alpha(1);
        Log.w("updateData", futuremarkURL);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, futuremarkURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.w("GPUMark", "onResponse");
                        //Log.w("GPUMark", response.length() + response);
                        /*switch (urlIndex){
                            case 0:
                                parsePassmarkData(response);
                                break;
                            case 1:
                                parseFuturemarkData(response);
                                break;
                        }*/
                        //progressLoading.setVisibility(View.INVISIBLE);
                        parseFuturemarkData(response);
                        loadingAnimator.alpha(0);
                        drawAllCharts();
                        requestPriceData(0);
                        //requestAllPriceData();
                        saveData();
                        //drawAllAsync();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.w("GPUMark", "onErrorResponse");
                        //progressLoading.setVisibility(View.INVISIBLE);
                        loadingAnimator.alpha(0);
                        Toast.makeText(MainActivity.this, "网络错误，请稍后重试", Toast.LENGTH_SHORT).show();
                        readData();
                        drawAllCharts();
                    }
                }
        );
        requestQueue.add(stringRequest);
    }

    private void parsePassmarkData(String data) {
        int count = 0;
        int start = 0;
        while (start+1 < data.length()){
            start = data.indexOf("gpu.php", start + 1);
            if(start == -1)
                break;
            if(count % 2 == 1){
                count ++;
                continue;
            }

            int nameStart = data.indexOf(">", start) + 1;
            int nameEnd = data.indexOf("<", nameStart);
            String name = data.substring(nameStart, nameEnd);
            Log.w("fullName", name);

            String namePrefix = name.split(" ")[0];
            switch (namePrefix){
                case "GeForce":
                    break;
                case "NVIDIA":
                    break;
                case "Quadro":
                    break;
                case "Radeon":
                    break;
                case "FirePro":
                    break;
                default:
                    continue;
            }

            int benchmarkStart = data.indexOf("</span>", nameEnd) + 7;
            int benchmarkEnd = data.indexOf("<", benchmarkStart);
            String benchmark = data.substring(benchmarkStart, benchmarkEnd);
            benchmark = benchmark.replaceAll(",", "");
            int benchmarkInt = Integer.parseInt(benchmark);
            Log.w("benchmark", benchmark);
            if(benchmarkInt < 2500)
                break;

            GraphicsCard card = new GraphicsCard();
            card.fullName = name;
            card.benchmark = benchmarkInt;
            card.lowPrice = 1000;
            card.highPrice = 2000;
            card.setType(namePrefix);
            cardArrayList.add(card);

            count ++;
        }
    }

    private void parseFuturemarkData(String data){
        int start = 5012;
        int count = 0;
        while (start < data.length()){
            start = data.indexOf("nameBold", start + 1);
            if(start == -1)
                break;

            int hrefStart = start + 16;
            int hrefEnd = data.indexOf(">", hrefStart) - 2;
            String href = "https://www.futuremark.com" + data.substring(hrefStart, hrefEnd);
            Log.w("href", href);

            int nameStart = hrefEnd + 3;
            int nameEnd = data.indexOf("<", nameStart);
            String name = data.substring(nameStart, nameEnd);
            Log.w("fullName", name);

            //String company = name.split(" ")[0];

            int benchmarkStart = data.indexOf("barScore", nameEnd) + 11;
            int benchmarkEnd = data.indexOf("<", benchmarkStart);
            int benchmark = Integer.parseInt(data.substring(benchmarkStart, benchmarkEnd));
            Log.w("benchmark", benchmark + "");

            GraphicsCard card = new GraphicsCard(name, benchmark, href);
            card.highPrice = 2000;
            card.lowPrice = 1000;
            
            cardArrayList.add(card);
            allCardMap.put(card.keyWord, card);

            if(!card.isNotebook && !card.company.equals("Intel"))
                count ++;
            switch (card.company){
                case "NVIDIA":
                    NVIDIAList.add(card);
                    break;
                case "AMD":
                    AMDList.add(card);
                    break;
            }
            if(count == 100)
                break;
        }
    }

    private void showLoading(Boolean show){
        if(show){
            progressLoading.setAlpha(0);
            progressLoading.setVisibility(View.VISIBLE);
            loadingAnimator.alpha(1);
        }
        else{
            progressLoading.setAlpha(1);
            loadingAnimator.alpha(0);
            loadingAnimator.withEndAction(new Runnable() {
                @Override
                public void run() {
                    progressLoading.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private void drawAllAsync(){
        for(int i = 0; i < cardArrayList.size(); i ++){
            new DrawChartTask().execute(cardArrayList.get(i));
        }
    }

    private void drawAllCharts(){
        /*for(int i = 0; i < gpuName.length; i ++){
            drawChart(i);
        }*/
        currentCompany = 0;
        for(int i = 0; i < cardArrayList.size(); i ++){
            drawChart(cardArrayList.get(i));
        }
    }

    private void drawNVIDIACharts(){
        for(int i = 0; i < NVIDIAList.size(); i ++)
            drawChart(NVIDIAList.get(i));
    }

    private void drawAMDCharts(){
        for(int i = 0; i < AMDList.size(); i ++)
            drawChart(AMDList.get(i));
    }

    private void clearCharts(){
        chartContainer.removeAllViews();
    }

    private void drawChart(GraphicsCard card){

        if(removeNotebook){
            if(card.isNotebook || card.company.equals("Intel"))
                return;
        }

        GraphView graph = new GraphView(this);
        graph.setMinimumHeight(400);
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        graph.setLayoutParams(lparams);
        chartContainer.addView(graph);

        graph.getViewport().setMinX(card.lowPrice);
        graph.getViewport().setMaxX(card.highPrice);
        graph.getViewport().setScalable(true);

        DataPoint[] dataPoint = new DataPoint[2];
        dataPoint[0] = new DataPoint(card.lowPrice, (float)card.benchmark / card.lowPrice);
        dataPoint[1] = new DataPoint(card.highPrice, (float)card.benchmark / card.highPrice);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoint);
        //series.setAnimated(true);

        graph.addSeries(series);
        graph.setTitle(card.fullName + " - " + card.benchmark);
        graph.setTitleTextSize(30);

        graph.setAlpha(0);
        ViewPropertyAnimator animator = graph.animate();
        animator.setDuration(500);
        animator.alpha(0.9f);
    }

    private void drawChart(int index){

        drawChart(cardArrayList.get(index));

        /*GraphView graph = new GraphView(this);
        graph.setMinimumHeight(400);
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        graph.setLayoutParams(lparams);
        chartContainer.addView(graph);
        //layout.addView(graph);

        graph.getViewport().setMinX(lowPrice[index]);
        graph.getViewport().setMaxX(highPrice[index]);
        graph.getViewport().setScalable(true);

        DataPoint[] dataPoint = new DataPoint[2];
        dataPoint[0] = new DataPoint(lowPrice[index], (float)gpuBenchmark[index] / lowPrice[index]);
        dataPoint[1] = new DataPoint(highPrice[index], (float)gpuBenchmark[index] / highPrice[index]);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoint);
        series.setAnimated(true);

        graph.addSeries(series);
        graph.setTitle(gpuName[index] + " - " + gpuBenchmark[index]);
        graph.setTitleTextSize(10);*/
        //ViewPropertyAnimator animator = graph.animate();

    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        Log.w("onItemSelected", pos + "");
        if(currentCompany == pos)
            return;
        chartContainer.removeAllViews();
        currentCompany = pos;
        switch (pos){
            case 0:
                drawAllCharts();
                break;
            case 1:
                drawNVIDIACharts();
                break;
            case 2:
                drawAMDCharts();
                break;
        }
        scrollView.scrollTo(0, 0);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private void saveData() {

        try {
            FileOutputStream fos = openFileOutput(dataFileName, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(cardArrayList);
            os.close();
            fos.close();

            fos = openFileOutput(NVIDIADataFileName, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(NVIDIAList);
            os.close();
            fos.close();

            fos = openFileOutput(AMDDataFileName, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(AMDList);
            os.close();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readData(){

        try {
            FileInputStream fis = openFileInput(dataFileName);
            ObjectInputStream is = new ObjectInputStream(fis);
            cardArrayList = (ArrayList<GraphicsCard>) is.readObject();
            is.close();
            fis.close();

            fis = openFileInput(NVIDIADataFileName);
            is = new ObjectInputStream(fis);
            NVIDIAList = (ArrayList<GraphicsCard>) is.readObject();
            is.close();
            fis.close();

            fis = openFileInput(AMDDataFileName);
            is = new ObjectInputStream(fis);
            AMDList = (ArrayList<GraphicsCard>) is.readObject();
            is.close();
            fis.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestAllPriceData(){
        for(int i = 0; i < cardArrayList.size(); i ++){
            requestPriceData(i);
        }
    }

    private void requestPriceData(final int index){
        loadingAnimator.alpha(1);
        final String keyWord = cardArrayList.get(index).keyWord;
        Log.w("requestPriceData", xianyuRequestURL + keyWord);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, xianyuRequestURL + keyWord,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.w("requestPriceData", "onResponse " + keyWord);
                        loadingAnimator.alpha(0);
                        parsePriceData(response, keyWord);
                        if(index < cardArrayList.size() - 1){
                            int next = index;
                            next ++;
                            requestPriceData(next);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.w("requestPriceData", "onErrorResponse " + keyWord);
                        error.printStackTrace();
                        //progressLoading.setVisibility(View.INVISIBLE);
                        loadingAnimator.alpha(0);
                        Toast.makeText(MainActivity.this, "网络错误，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(stringRequest);
    }

    private void parsePriceData(String data, String keyWord){
        Log.w("parsePriceData", keyWord);
        ArrayList<XianyuItem> list = new ArrayList<>();
        int start = 51200;
        while(start < data.length()) {

            start = data.indexOf("item-info", start + 1);
            if (start == -1)
                break;

            int hrefStart = data.indexOf("href", start) + 6; //存疑
            int hrefEnd = data.indexOf("target", hrefStart) - 2;
            String href = "http:" + data.substring(hrefStart, hrefEnd);
            //Log.w("priceData-href", href);

            int titleStart = data.indexOf("title", hrefEnd) + 6;
            int titleEnd = data.indexOf(">", titleStart);
            String title = data.substring(titleStart, titleEnd);
            //Log.w("priceData-title", title);

            int priceStart = data.indexOf("item-price", titleEnd) + 59; //存疑
            int priceEnd = data.indexOf("<", priceStart);
            String priceStr = data.substring(priceStart, priceEnd);
            float price = Float.parseFloat(priceStr);
            //Log.w("priceData-price", priceStr + "," + price);

            int locationStart = data.indexOf("item-location", priceEnd) + 15;
            int locationEnd = data.indexOf("<", locationStart);
            String location = data.substring(locationStart, locationEnd);
            //Log.w("priceData-location", location);

            int briefDescStart = data.indexOf("brief-desc", locationEnd) + 12;
            int briefDescEnd = data.indexOf("</", briefDescStart);
            String briefDesc = data.substring(briefDescStart, briefDescEnd);
            //Log.w("priceData-briefDesc", briefDesc);

            XianyuItem item = new XianyuItem(keyWord, title, href, price, location, briefDesc);
            list.add(item);
            Log.w("list.size wtf ", list.size() + keyWord);
        }
        Log.w("list.size ", list.size() + keyWord);
        analyzePriceData(list, keyWord);
        xianyuItemList.add(list);
    }

    private void analyzeAllPriceData(){
        for(int i = 0; i < xianyuItemList.size(); i ++){
            //analyzePriceData(xianyuItemList.get(i));
        }
    }

    private void analyzePriceData(ArrayList<XianyuItem> list, String keyWord){
        Log.w("analyzePriceData", keyWord + " " + list.size());
        GraphicsCard card = allCardMap.get(keyWord);
        int count = 0;
        float totalPrice = 0;
        for(int i = 0; i < list.size(); i ++){
            XianyuItem item = list.get(i);
            if(item.isValidItem()){
                count ++;
                totalPrice += item.price;
            }
        }
        Log.w("wtf ", count + " " + totalPrice);
        float averagePrice = totalPrice / count;
        Log.w("analyzePriceData", keyWord + " " + averagePrice);
    }

    private class DrawChartTask extends AsyncTask<GraphicsCard, Integer, GraphicsCard> {
        protected GraphicsCard doInBackground(GraphicsCard ... card) {
            final GraphicsCard graphicsCard = card[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(graphicsCard.isNotebook || graphicsCard.company.equals("Intel"))
                        return;
                    GraphView graph = new GraphView(getApplicationContext());
                    graph.setMinimumHeight(400);
                    LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    graph.setLayoutParams(lparams);
                    chartContainer.addView(graph);

                    graph.getViewport().setMinX(graphicsCard.lowPrice);
                    graph.getViewport().setMaxX(graphicsCard.highPrice);
                    graph.getViewport().setScalable(true);

                    DataPoint[] dataPoint = new DataPoint[2];
                    dataPoint[0] = new DataPoint(graphicsCard.lowPrice, (float)graphicsCard.benchmark / graphicsCard.lowPrice);
                    dataPoint[1] = new DataPoint(graphicsCard.highPrice, (float)graphicsCard.benchmark / graphicsCard.highPrice);
                    LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoint);
                    //series.setAnimated(true);

                    graph.addSeries(series);
                    graph.setTitle(graphicsCard.fullName + " - " + graphicsCard.benchmark);
                    graph.setTitleTextSize(30);

                    graph.setAlpha(0);
                    ViewPropertyAnimator animator = graph.animate();
                    animator.setDuration(500);
                    animator.alpha(0.9f);
                }
            });
            return card[0];
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(GraphicsCard card) {
            /*if(card.isNotebook || card.company.equals("Intel"))
                return;
            GraphView graph = new GraphView(getApplicationContext());
            graph.setMinimumHeight(400);
            LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            graph.setLayoutParams(lparams);
            chartContainer.addView(graph);

            graph.getViewport().setMinX(card.lowPrice);
            graph.getViewport().setMaxX(card.highPrice);
            graph.getViewport().setScalable(true);

            DataPoint[] dataPoint = new DataPoint[2];
            dataPoint[0] = new DataPoint(card.lowPrice, (float)card.benchmark / card.lowPrice);
            dataPoint[1] = new DataPoint(card.highPrice, (float)card.benchmark / card.highPrice);
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoint);
            series.setAnimated(true);

            graph.addSeries(series);
            graph.setTitle(card.fullName + " - " + card.benchmark);
            graph.setTitleTextSize(30);*/
        }
    }

}
