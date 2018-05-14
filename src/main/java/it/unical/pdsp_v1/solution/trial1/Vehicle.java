/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unical.pdsp_v1.solution.trial1;

import java.util.ArrayList;

/**
 *
 * @author gezu
 */
public class Vehicle {

    public int VehId;
    public ArrayList<Node> Route = new ArrayList<>();
    public int capacity;
    public int load;
    public int CurLoc;
    public boolean Closed;

    public Vehicle(int id, int cap) {
        this.VehId = id;
        this.capacity = cap;
        this.load = 0;
        this.CurLoc = 0; //In depot Initially
        this.Closed = false;
        this.Route.clear();
    }

    /**
     * Add Order to Vehicle Route
     * Route is an ArrayList to collect each index
     * @param order 
     */
    public void AddNode(Node order)
    {
        Route.add(order);
        this.load += order.demand;
        this.CurLoc = order.NodeId;
    }

    /**
     * Check if we have Capacity have a Violation
     *
     * @param dem
     * @return
     */
    public boolean CheckIfFits(int dem) {
        return ((load + dem <= capacity));
    }
}
