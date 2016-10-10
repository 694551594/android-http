/**
  * Copyright 2016 aTool.org 
  */
package cn.yhq.http.sample;

/**
 * Auto-generated: 2016-10-10 15:33:4
 *
 * @author aTool.org (i@aTool.org)
 * @website http://www.atool.org/json2javabean.php
 */
public class WeatherInfo {

    private String desc;
    private int status;
    private Data data;
    public void setDesc(String desc) {
         this.desc = desc;
     }
     public String getDesc() {
         return desc;
     }

    public void setStatus(int status) {
         this.status = status;
     }
     public int getStatus() {
         return status;
     }

    public void setData(Data data) {
         this.data = data;
     }
     public Data getData() {
         return data;
     }

}