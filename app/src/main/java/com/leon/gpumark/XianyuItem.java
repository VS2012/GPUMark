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
    private boolean isValid = true;

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
        if(title.contains("主机") || title.contains("整机")|| title.contains("台式") || title.contains("组装")
                || title.contains("笔记本") || title.contains("游戏本"))
            isValid = false;
        if(title.contains("求") || title.contains("收购") || title.contains("高价") || title.contains("回收")
                || title.contains("转卖") || title.contains("转手") || title.contains("尸体"))
            isValid = false;
        if(title.contains("冷头"))
            isValid = false;
        if(price <= 50 || price > 10000)
            isValid = false;
    }

    public boolean isValidItem(){
        return isValid;
    }

    public String toString(){
        return title + "\n" + detailURL + "\n" + price + "\n" + location + "\n" + briefDesc + "\n" + isValid;
    }
}
