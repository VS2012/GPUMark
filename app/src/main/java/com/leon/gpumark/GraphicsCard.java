package com.leon.gpumark;

/**
 * Created by haobo on 17.9.6.
 */

public class GraphicsCard {
    public String name;
    public int benchmark;
    public int highPrice;
    public int lowPrice;
    public String company;
    public String type;
    public String subType;
    public String detailURL;
    public boolean isNotebook = false;

    public GraphicsCard(String name, int benchmark, String detailURL){
        this.name = name;
        this.benchmark = benchmark;
        this.detailURL = detailURL;
        String[] nameSplit = name.split(" ");
        company = nameSplit[0];

        if(name.contains("Notebook") || name.contains("Surface"))
            isNotebook = true;

        if(nameSplit[nameSplit.length - 1].contains("M"))
            isNotebook = true;
    }

    public GraphicsCard(){

    }

    public void setName(String cardName){
        name = cardName;
        String prefix = cardName.split(" ")[0];
        switch (prefix){
            case "NVIDIA":
                company = "Nvidia";
                break;
            case "AMD":
                company = "AMD";
                break;
            default:
                company = "Unknown";
        }
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
}
