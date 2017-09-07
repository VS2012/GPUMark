package com.leon.gpumark;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private String[] gpuName = {"GTX 1080 Ti", "GTX 1080", "GTX 1070", "GTX 970"};
    private int[] gpuBenchmark = {13345, 11995, 11023, 8558};
    private float[] lowPrice = {5000, 3500, 2500, 970};
    private float[] highPrice = {6000, 4500, 3500, 1300};

    private LinearLayout chartContainer;
    private ArrayList<GraphicsCard> cardArrayList = new ArrayList<>();

    private RequestQueue requestQueue;
    private String[] url = {
            "https://www.videocardbenchmark.net/high_end_gpus.html",
            "https://www.futuremark.com/hardware/gpu"
    };
    private boolean removeNotebook = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chartContainer = (LinearLayout) findViewById(R.id.chart_container);
        requestQueue = Volley.newRequestQueue(this);

        updateData(1);
    }

    private void updateData(final int urlIndex) {
        Log.w("updateData", urlIndex + " " + url[urlIndex]);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url[urlIndex],
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.w("GPUMark", "onResponse");
                        //Log.w("GPUMark", response.length() + response);
                        switch (urlIndex){
                            case 0:
                                parsePassmarkData(response);
                                break;
                            case 1:
                                parseFuturemarkData(response);
                                break;
                        }
                        drawAllCharts();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.w("GPUMark", "onErrorResponse");
                        Toast.makeText(MainActivity.this, "网络错误，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(stringRequest);
    }

    private void parsePassmarkData(String data){
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
            Log.w("name", name);

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
            card.name = name;
            card.benchmark = benchmarkInt;
            card.lowPrice = 1000;
            card.highPrice = 2000;
            card.setType(namePrefix);
            cardArrayList.add(card);

            count ++;
        }
    }

    private void parseFuturemarkData(String data){
        int start = 0;
        int count = 0;
        while (start < data.length()){
            start = data.indexOf("nameBold", start + 1);
            if(start == -1)
                break;

            int hrefStart = start + 16;
            int hrefEnd = data.indexOf(">", hrefStart) - 1;
            String href = data.substring(hrefStart, hrefEnd);
            Log.w("href", href);

            int nameStart = hrefEnd + 2;
            int nameEnd = data.indexOf("<", nameStart);
            String name = data.substring(nameStart, nameEnd);
            Log.w("name", name);

            String company = name.split(" ")[0];

            int benchmarkStart = data.indexOf("barScore", nameEnd) + 11;
            int benchmarkEnd = data.indexOf("<", benchmarkStart);
            int benchmark = Integer.parseInt(data.substring(benchmarkStart, benchmarkEnd));
            Log.w("benchmark", benchmark + "");

            GraphicsCard card = new GraphicsCard(name, benchmark, href);
            card.highPrice = 2000;
            card.lowPrice = 1000;
            
            cardArrayList.add(card);
            if(!card.isNotebook && !card.company.equals("Intel"))
                count ++;
            if(count == 100)
                break;
        }
    }

    private void drawAllCharts(){
        /*for(int i = 0; i < gpuName.length; i ++){
            drawChart(i);
        }*/

        for(int i = 0; i < cardArrayList.size(); i ++){
            drawChart(cardArrayList.get(i));
        }
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
        series.setAnimated(true);

        graph.addSeries(series);
        graph.setTitle(card.name + " - " + card.benchmark);
        graph.setTitleTextSize(30);
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
}
