package com.izoman.hcktool.beginner.networkscan;

/**
 * Created by umuts on 10/06/2017.
 */

public class ArpItem {
    String ip;
    String mac;
    ArpItem(String ip, String mac){
        this.ip = ip;
        this.mac = mac;
    }
}
