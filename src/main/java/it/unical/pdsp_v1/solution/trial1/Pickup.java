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
public class Pickup {

    public int puckId;
    public String pickupTime;
    public int serviceDuration;
    public ArrayList<Node> Route = new ArrayList<>();

    Pickup(int puckId, String pickupTime, int serviceDuration) {
        this.puckId = puckId;
        this.pickupTime = pickupTime;
        this.serviceDuration = serviceDuration;
    }

}
