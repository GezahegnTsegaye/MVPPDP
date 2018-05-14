/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unical.pdsp_v1.solution.trial1;

import java.util.ArrayList;

/**
 *
 * @author gezu-pc
 */
public class Delivery {

    public int delId;
    public String dropOpenTime;
    public int dropServiceDuration;
    public ArrayList<Node> Route = new ArrayList<>();

    public Delivery(int delId, String dropOpenTime, int dropServiceDuration) {
        this.delId = delId;
        this.dropOpenTime = dropOpenTime;
        this.dropServiceDuration = dropServiceDuration;
    }
    
    
}
