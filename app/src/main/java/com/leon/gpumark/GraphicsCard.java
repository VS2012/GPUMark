package com.leon.gpumark;

import java.io.Serializable;

/**
 * Created by haobo on 17.9.6.
 */

public class GraphicsCard implements Serializable {
    public String fullName;
    public int benchmark;
    public int highPrice;
    public int lowPrice;
    public String company;
    public String type;
    public String subType;
    public String detailURL;
    public boolean isNotebook = false;
    public String keyWord;

    public GraphicsCard(String name, int benchmark, String detailURL){
        setFullName(name);
        this.benchmark = benchmark;
        this.detailURL = detailURL;
    }

    public GraphicsCard(){

    }

    public void setFullName(String cardName){
        fullName = cardName;
        String[] words = cardName.split(" ");
        company = words[0];

        if(fullName.contains("Notebook") || fullName.contains("Surface"))
            isNotebook = true;
        if(words[words.length - 1].contains("M"))
            isNotebook = true;

        if(fullName.contains("Titan"))
            keyWord = fullName.substring(fullName.indexOf("Titan")).replace(" ", "%20");
        else{
            StringBuilder builder = new StringBuilder();
            for(int i = 2; i < words.length; i ++){
                builder.append(words[i]).append("%20");
            }
            keyWord = builder.toString();
        }
            //keyWord = cardName.substring(words[0].length() + words[1].length());

        /*String prefix = cardName.split(" ")[0];
        switch (prefix){
            case "NVIDIA":
                company = "Nvidia";
                break;
            case "AMD":
                company = "AMD";
                break;
            case "ATI":
                company = "ATI";
            default:
                company = "Unknown";
        }*/
    }

    public void setType(String type) {
        this.type = type;
        switch (type){
            case "GeForce":
                company = "Nvidia";
                break;
            case "NVIDIA":
                company = "Nvidia";
                break;
            case "Quadro":
                company = "Nvidia";
                break;
            case "Radeon":
                company = "AMD";
                break;
            case "FirePro":
                company = "AMD";
                break;
        }
    }

    private void writeObject(java.io.ObjectOutputStream out){
        //Log.w("writeObject", fullName);
        try{
            out.write(fullName.getBytes());
            out.writeInt(benchmark);
            out.writeInt(highPrice);
            out.writeInt(lowPrice);
            out.write(company.getBytes());
            //out.write(type.getBytes());
            //out.write(subType.getBytes());
            out.write(detailURL.getBytes());
            out.writeBoolean(isNotebook);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    private void readObject(java.io.ObjectInputStream in){
        //Log.w("readObject", "");
        try{
            fullName = "test";
            benchmark = 1000;
            highPrice = 2000;
            lowPrice = 1000;
            company = "NVIDIA";
            detailURL = "g";
            isNotebook = false;
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void readObjectNoData(){

    }
}
