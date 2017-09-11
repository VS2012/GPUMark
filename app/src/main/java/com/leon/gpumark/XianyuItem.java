package com.leon.gpumark;

/**
 * Created by haobo on 17.9.11.
 */

public class XianyuItem {
    public String keyWord;
    private String title;
    public String detailURL;
    public float price;
    public String location;
    public String briefDesc;
    private boolean isInvalid = false;

    public XianyuItem(String keyWord, String title, String detailURL, float price, String location, String briefDesc){
        this.keyWord = keyWord;
        this.title = title;
        this.detailURL = detailURL;
        this.price = price;
        this.location = location;
        this.briefDesc = briefDesc;
        checkInvalid();
    }

    public void setTitle(String title){
        this.title = title;
        checkInvalid();
    }

    private void checkInvalid(){
        if(title.contains("主机") || title.contains("笔记本"))
            isInvalid = true;
        if(title.contains("求购") || title.contains("收购"))
            isInvalid = true;
        if(price < 10 || price > 10000)
            isInvalid = true;
    }

    public boolean isInvalidItem(){
        return isInvalid;
    }

    public String toString(){
        return title + "\n" + detailURL + "\n" + price + "\n" + location + "\n" + briefDesc + "\n" + isInvalid;
    }
}
