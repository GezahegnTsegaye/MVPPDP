/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unical.pdsp_v1.solution.trial1;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class uses meta heuristic method to analyze best route based on the cost
 *
 * @author gezu
 */
public class Solution {

    int NoOfVehicles;
    int NoOfOrders;
    int pickupNum;
    int deliveryNum;

    Vehicle[] Vehicles;
    Delivery[] deliveys;
    Pickup[] pickups;
    double Cost;

    //Tabu Variables
    public Vehicle[] VehiclesForBestSolution;
    double BestSolutionCost;

    public ArrayList<Double> PastSolutions;

    public Solution() {
    }

    Solution(int orderNum, int VechNum, int VechCap) {
        this.NoOfVehicles = VechNum;
        this.NoOfOrders = orderNum;
        this.Cost = 0;
        Vehicles = new Vehicle[NoOfVehicles];
        VehiclesForBestSolution = new Vehicle[NoOfVehicles];
        PastSolutions = new ArrayList<>();

        for (int i = 0; i < NoOfVehicles; i++) {
            Vehicles[i] = new Vehicle(i + 1, VechCap);
            VehiclesForBestSolution[i] = new Vehicle(i + 1, VechCap);
        }
    }

    Solution(int orderNum, int VehicleNum, int VehicleCapacity, int pickupNum, int deliveryNum) {
        this.NoOfVehicles = VehicleNum;
        this.NoOfOrders = orderNum;
        this.Cost = 0;
        Vehicles = new Vehicle[NoOfVehicles];
        VehiclesForBestSolution = new Vehicle[NoOfVehicles];
        PastSolutions = new ArrayList<>();

        for (int i = 0; i < NoOfVehicles; i++) {
            Vehicles[i] = new Vehicle(i + 1, VehicleCapacity);
            VehiclesForBestSolution[i] = new Vehicle(i + 1, VehicleCapacity);
        }
        for (int i = 0; i < pickupNum; i++) {
            pickups[i] = new Pickup(i + 1, "", pickupNum);
            VehiclesForBestSolution[i] = new Vehicle(i + 1, VehicleCapacity);
        }
        for (int i = 0; i < deliveryNum; i++) {
            deliveys[i] = new Delivery(i + 1, "", deliveryNum);
            VehiclesForBestSolution[i] = new Vehicle(i + 1, VehicleCapacity);
        }
    }

    public boolean UnassignedOrdersExists(Node[] Nodes) {
        for (int i = 1; i < Nodes.length; i++) {
            if (!Nodes[i].IsRouted) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param Nodes
     * @param CostMatrix
     * @return
     */
    public String GreedySolution(Node[] Nodes, double[][] CostMatrix) {

        double CandCost, EndCost;
        int VehIndex = 0;

        while (UnassignedOrdersExists(Nodes)) {

            int orderIndex = 0;
            Node Candidate = null;
            double minCost = (float) Double.MAX_VALUE;

            if (Vehicles[VehIndex].Route.isEmpty()) {
                Vehicles[VehIndex].AddNode(Nodes[0]);
            }

            for (int i = 1; i <= NoOfOrders; i++) {
                if (Nodes[i].IsRouted == false) {
                    if (Vehicles[VehIndex].CheckIfFits(Nodes[i].demand)) {
                        CandCost = CostMatrix[Vehicles[VehIndex].CurLoc][i];
                        if (minCost > CandCost) {
                            minCost = CandCost;
                            orderIndex = i;
                            Candidate = Nodes[i];
                        }
                    }
                }
            }

            if (Candidate == null) {
                //Not a single Orders Fits
                if (VehIndex + 1 < Vehicles.length) //We have more vehicles to assign
                {
                    if (Vehicles[VehIndex].CurLoc != 0) {//End this route
                        EndCost = CostMatrix[Vehicles[VehIndex].CurLoc][0];
                        Vehicles[VehIndex].AddNode(Nodes[0]);
                        this.Cost += EndCost;
                    }
                    VehIndex = VehIndex + 1; //Go to next Vehicle
                } else {
                    System.exit(0);
                }
            } else {
                Vehicles[VehIndex].AddNode(Candidate);//If a fitting Orders is Found
                Nodes[orderIndex].IsRouted = true;
                this.Cost += minCost;
            }
        }

        EndCost = CostMatrix[Vehicles[VehIndex].CurLoc][0];
        Vehicles[VehIndex].AddNode(Nodes[0]);
        this.Cost += EndCost;
        return null;
    }

    /**
     *
     * @param TABU_Horizon
     * @param CostMatrix
     */
    public void TabuSearch(int TABU_Horizon, double[][] CostMatrix) {

        //We use 1-0 exchange move
        ArrayList<Node> RouteFrom;
        ArrayList<Node> RouteTo;

        int MovingNodeDemand = 0;

        int VehIndexFrom, VehIndexTo;
        double BestNCost, neighborhoodCost;

        int SwapIndexA = -1, SwapIndexB = -1, SwapRouteFrom = -1, SwapRouteTo = -1;

        int MAX_ITERATIONS = 200;
        int iteration_number = 0;

        int DimensionOrders = CostMatrix[1].length;
        int TABU_Matrix[][] = new int[DimensionOrders + 1][DimensionOrders + 1];

        BestSolutionCost = this.Cost; //Initial Solution Cost

        boolean Termination = false;

        while (!Termination) {
            iteration_number++;
            BestNCost = Double.MAX_VALUE;

            for (VehIndexFrom = 0; VehIndexFrom < this.Vehicles.length; VehIndexFrom++) {
                RouteFrom = this.Vehicles[VehIndexFrom].Route;
                int RoutFromLength = RouteFrom.size();
                for (int i = 1; i < RoutFromLength - 1; i++) { //Not possible to move depot!

                    for (VehIndexTo = 0; VehIndexTo < this.Vehicles.length; VehIndexTo++) {
                        RouteTo = this.Vehicles[VehIndexTo].Route;
                        int RouteTolength = RouteTo.size();
                        for (int j = 0; (j < RouteTolength - 1); j++) {//Not possible to move after last Depot!

                            MovingNodeDemand = RouteFrom.get(i).demand;

                            if ((VehIndexFrom == VehIndexTo) || this.Vehicles[VehIndexTo].CheckIfFits(MovingNodeDemand)) {
                                //If we assign to a different route check capacity constrains
                                //if in the new route is the same no need to check for capacity

                                if (((VehIndexFrom == VehIndexTo) && ((j == i) || (j == i - 1))) == false) // Not a move that Changes solution cost
                                {
                                    double MinusCost1 = CostMatrix[RouteFrom.get(i - 1).NodeId][RouteFrom.get(i).NodeId];
                                    double MinusCost2 = CostMatrix[RouteFrom.get(i).NodeId][RouteFrom.get(i + 1).NodeId];
                                    double MinusCost3 = CostMatrix[RouteTo.get(j).NodeId][RouteTo.get(j + 1).NodeId];

                                    double AddedCost1 = CostMatrix[RouteFrom.get(i - 1).NodeId][RouteFrom.get(i + 1).NodeId];
                                    double AddedCost2 = CostMatrix[RouteTo.get(j).NodeId][RouteFrom.get(i).NodeId];
                                    double AddedCost3 = CostMatrix[RouteFrom.get(i).NodeId][RouteTo.get(j + 1).NodeId];

                                    //Check if the move is a Tabu! - If it is Tabu break
                                    if ((TABU_Matrix[RouteFrom.get(i - 1).NodeId][RouteFrom.get(i + 1).NodeId] != 0)
                                            || (TABU_Matrix[RouteTo.get(j).NodeId][RouteFrom.get(i).NodeId] != 0)
                                            || (TABU_Matrix[RouteFrom.get(i).NodeId][RouteTo.get(j + 1).NodeId] != 0)) {
                                        break;
                                    }

                                    neighborhoodCost = AddedCost1 + AddedCost2 + AddedCost3
                                            - MinusCost1 - MinusCost2 - MinusCost3;

                                    if (neighborhoodCost < BestNCost) {
                                        BestNCost = neighborhoodCost;
                                        SwapIndexA = i;
                                        SwapIndexB = j;
                                        SwapRouteFrom = VehIndexFrom;
                                        SwapRouteTo = VehIndexTo;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (int t = 0; t < TABU_Matrix[0].length; t++) {
                for (int p = 0; p < TABU_Matrix[0].length; p++) {
                    if (TABU_Matrix[t][p] > 0) {
                        TABU_Matrix[t][p]--;
                    }
                }
            }

            RouteFrom = this.Vehicles[SwapRouteFrom].Route;
            RouteTo = this.Vehicles[SwapRouteTo].Route;
            this.Vehicles[SwapRouteFrom].Route = null;
            this.Vehicles[SwapRouteTo].Route = null;

            Node SwapNode = RouteFrom.get(SwapIndexA);

            int NodeIDBefore = RouteFrom.get(SwapIndexA - 1).NodeId;
            int NodeIDAfter = RouteFrom.get(SwapIndexA + 1).NodeId;
            int NodeID_F = RouteTo.get(SwapIndexB).NodeId;
            int NodeID_G = RouteTo.get(SwapIndexB + 1).NodeId;

            Random TabuRan = new Random();
            int RendomDelay1 = TabuRan.nextInt(5);
            int RendomDelay2 = TabuRan.nextInt(5);
            int RendomDelay3 = TabuRan.nextInt(5);

            TABU_Matrix[NodeIDBefore][SwapNode.NodeId] = TABU_Horizon + RendomDelay1;
            TABU_Matrix[SwapNode.NodeId][NodeIDAfter] = TABU_Horizon + RendomDelay2;
            TABU_Matrix[NodeID_F][NodeID_G] = TABU_Horizon + RendomDelay3;

            RouteFrom.remove(SwapIndexA);

            if (SwapRouteFrom == SwapRouteTo) {
                if (SwapIndexA < SwapIndexB) {
                    RouteTo.add(SwapIndexB, SwapNode);
                } else {
                    RouteTo.add(SwapIndexB + 1, SwapNode);
                }
            } else {
                RouteTo.add(SwapIndexB + 1, SwapNode);
            }

            this.Vehicles[SwapRouteFrom].Route = RouteFrom;
            this.Vehicles[SwapRouteFrom].load -= MovingNodeDemand;

            this.Vehicles[SwapRouteTo].Route = RouteTo;
            this.Vehicles[SwapRouteTo].load += MovingNodeDemand;

            PastSolutions.add(this.Cost);

            this.Cost += BestNCost;
//            System.out.println("RendomDelay3 = " + BestNCost);

            if (this.Cost < BestSolutionCost) {
                SaveBestSolution();
            }

            if (iteration_number == MAX_ITERATIONS) {
                Termination = true;
            }
        }

        this.Vehicles = VehiclesForBestSolution;
        this.Cost = BestSolutionCost;

        try {
            PrintWriter writer = new PrintWriter("PastSolutionsTabu.txt", "UTF-8");
            writer.println("Solutions" + "\t");
            for (int i = 0; i < PastSolutions.size(); i++) {
                writer.println(PastSolutions.get(i) + "\t");
            }
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
        }
    }

    public void SaveBestSolution() {
        BestSolutionCost = Cost;
        for (int j = 0; j < NoOfVehicles; j++) {
            VehiclesForBestSolution[j].Route.clear();
            if (!Vehicles[j].Route.isEmpty()) {
                int RoutSize = Vehicles[j].Route.size();
                for (int k = 0; k < RoutSize; k++) {
                    Node n = Vehicles[j].Route.get(k);
                    VehiclesForBestSolution[j].Route.add(n);
                }
            }
        }
    }
/**
     * Every time a route is modified due to an inter-route move we perform an
     * intra-route local search using classical TSP neighborhood structures,
     * more precisely, Or-opt, 2-opt and exchange. Finally, the perturbation
     * mechanisms consist of performing multiple Swap(1,1) or Shift(1,1) moves.
     * The Shift (1,1) consists of moving a costumer from a route r1 to a route
     * r2 and vice-versa.
     * @param Nodes
     * @param CostMatrix
     * @return
     */
    public void InterRouteLocalSearch(Node[] Nodes, double[][] CostMatrix) {

        //We use 1-0 exchange move
        ArrayList<Node> RouteFrom;
        ArrayList<Node> RouteTo;

        int MovingNodeDemand = 0;

        int VehIndexFrom, VehIndexTo;
        double BestNCost, neighborhoodCost;

        int SwapIndexA = -1, SwapIndexB = -1, SwapRouteFrom = -1, SwapRouteTo = -1;

        int MAX_ITERATIONS = 1000000;
        int iteration_number = 0;

        boolean Termination = false;

        while (!Termination) {
            iteration_number++;
            BestNCost = Double.MAX_VALUE;

            for (VehIndexFrom = 0; VehIndexFrom < this.Vehicles.length; VehIndexFrom++) {
                RouteFrom = this.Vehicles[VehIndexFrom].Route;
                int RoutFromLength = RouteFrom.size();
                for (int i = 1; i < RoutFromLength - 1; i++) { //Not possible to move depot!

                    for (VehIndexTo = 0; VehIndexTo < this.Vehicles.length; VehIndexTo++) {
                        RouteTo = this.Vehicles[VehIndexTo].Route;
                        int RouteTolength = RouteTo.size();
                        for (int j = 0; (j < RouteTolength - 1); j++) {//Not possible to move after last Depot!

                            MovingNodeDemand = RouteFrom.get(i).demand;
                            if ((VehIndexFrom == VehIndexTo) || this.Vehicles[VehIndexTo].CheckIfFits(MovingNodeDemand)) {
                                if (((VehIndexFrom == VehIndexTo) && ((j == i) || (j == i - 1))) == false) // Not a move that Changes solution cost
                                {
                                    double MinusCost1 = CostMatrix[RouteFrom.get(i - 1).NodeId][RouteFrom.get(i).NodeId];
                                    double MinusCost2 = CostMatrix[RouteFrom.get(i).NodeId][RouteFrom.get(i + 1).NodeId];
                                    double MinusCost3 = CostMatrix[RouteTo.get(j).NodeId][RouteTo.get(j + 1).NodeId];

                                    double AddedCost1 = CostMatrix[RouteFrom.get(i - 1).NodeId][RouteFrom.get(i + 1).NodeId];
                                    double AddedCost2 = CostMatrix[RouteTo.get(j).NodeId][RouteFrom.get(i).NodeId];
                                    double AddedCost3 = CostMatrix[RouteFrom.get(i).NodeId][RouteTo.get(j + 1).NodeId];

                                    neighborhoodCost = AddedCost1 + AddedCost2 + AddedCost3
                                            - MinusCost1 - MinusCost2 - MinusCost3;

                                    if (neighborhoodCost < BestNCost) {
                                        BestNCost = neighborhoodCost;
                                        SwapIndexA = i;
                                        SwapIndexB = j;
                                        SwapRouteFrom = VehIndexFrom;
                                        SwapRouteTo = VehIndexTo;

                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (BestNCost < 0) {// If Best Neightboor Cost is better than the current

                RouteFrom = this.Vehicles[SwapRouteFrom].Route;
                RouteTo = this.Vehicles[SwapRouteTo].Route;
                this.Vehicles[SwapRouteFrom].Route = null;
                this.Vehicles[SwapRouteTo].Route = null;

                Node SwapNode = RouteFrom.get(SwapIndexA);

                RouteFrom.remove(SwapIndexA);

                if (SwapRouteFrom == SwapRouteTo) {
                    if (SwapIndexA < SwapIndexB) {
                        RouteTo.add(SwapIndexB, SwapNode);
                    } else {
                        RouteTo.add(SwapIndexB + 1, SwapNode);
                    }
                } else {
                    RouteTo.add(SwapIndexB + 1, SwapNode);
                }

                this.Vehicles[SwapRouteFrom].Route = RouteFrom;
                this.Vehicles[SwapRouteFrom].load -= MovingNodeDemand;

                this.Vehicles[SwapRouteTo].Route = RouteTo;
                this.Vehicles[SwapRouteTo].load += MovingNodeDemand;

                PastSolutions.add(this.Cost);
                this.Cost += BestNCost;
            } else {
                Termination = true;
            }

            if (iteration_number == MAX_ITERATIONS) {
                Termination = true;
            }
        }
        PastSolutions.add(this.Cost);

        try {
            PrintWriter writer = new PrintWriter("PastSolutionsInter.txt", "UTF-8");
            for (int i = 0; i < PastSolutions.size(); i++) {
                writer.println(PastSolutions.get(i) + "\t");
            }
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
        }
    }

    
//    public String IntraRouteLocalSearch(Node[] Nodes, double[][] CostMatrix) {
//
//        //We use 1-0 exchange move
//        ArrayList<Node> rt;
//        double BestNCost, neighborhoodCost;
//
//        int SwapIndexA = -1, SwapIndexB = -1, SwapRoute = -1;
//
//        int MAX_ITERATIONS = 1000000;
//        int iteration_number = 0;
//
//        boolean Termination = false;
//
//        while (!Termination) {
//            iteration_number++;
//            BestNCost = Double.MAX_VALUE;
//
//            for (int VehIndex = 0; VehIndex < this.Vehicles.length; VehIndex++) {
//                rt = this.Vehicles[VehIndex].Route;
//                int RoutLength = rt.size();
//                System.out.println("RoutLength = " + RoutLength);
//                for (int i = 1; i < RoutLength - 1; i++) { //Not possible to move depot!
//
//                    for (int j = 0; (j < RoutLength - 1); j++) {//Not possible to move after last Depot!
//
//                        if ((j != i) && (j != i - 1)) { // Not a move that cHanges solution cost
//
//                            double MinusCost1 = CostMatrix[rt.get(i - 1).NodeId][rt.get(i).NodeId];
//                            double MinusCost2 = CostMatrix[rt.get(i).NodeId][rt.get(i + 1).NodeId];
//                            double MinusCost3 = CostMatrix[rt.get(j).NodeId][rt.get(j + 1).NodeId];
//
//                            double AddedCost1 = CostMatrix[rt.get(i - 1).NodeId][rt.get(i + 1).NodeId];
//                            double AddedCost2 = CostMatrix[rt.get(j).NodeId][rt.get(i).NodeId];
//                            double AddedCost3 = CostMatrix[rt.get(i).NodeId][rt.get(j + 1).NodeId];
//
//                            neighborhoodCost = AddedCost1 + AddedCost2 + AddedCost3
//                                    - MinusCost1 - MinusCost2 - MinusCost3;
//
//                            if (neighborhoodCost < BestNCost) {
//                                BestNCost = neighborhoodCost;
//                                SwapIndexA = i;
//                                SwapIndexB = j;
//                                SwapRoute = VehIndex;
//
//                            }
//                        }
//                    }
//                }
//            }
//
//            if (BestNCost < 0) {
//
//                rt = this.Vehicles[SwapRoute].Route;
//
//                Node SwapNode = rt.get(SwapIndexA);
//
//                rt.remove(SwapIndexA);
//
//                if (SwapIndexA < SwapIndexB) {
//                    rt.add(SwapIndexB, SwapNode);
//                } else {
//                    rt.add(SwapIndexB + 1, SwapNode);
//                }
//
//                PastSolutions.add(this.Cost);
//                this.Cost += BestNCost;
//            } else {
//                Termination = true;
//            }
//
//            if (iteration_number == MAX_ITERATIONS) {
//                Termination = true;
//            }
//        }
//        PastSolutions.add(this.Cost);
//
////       
//        return null;
//    }

    /**
     * print the solutions based on the labels we defined in the above this
     * method will display the result on console
     *
     * @param Solution_Label
     * @return
     */
    public String SolutionPrint(String Solution_Label) {
        System.out.println("=========================================================");
        System.out.println(Solution_Label + "\n");

        for (int j = 0; j < NoOfVehicles; j++) {
            if (!Vehicles[j].Route.isEmpty()) {
                System.out.print("Vehicle " + j + ":");
                int RoutSize = Vehicles[j].Route.size();
                for (int k = 0; k < RoutSize; k++) {
                    if (k == RoutSize - 1) {
                        System.out.print(Vehicles[j].Route.get(k).NodeId);
                    } else {
                        System.out.print(Vehicles[j].Route.get(k).NodeId + "=>");
                    }
                }
                System.out.println();
            }
        }
        System.out.println("\nSolution Cost " + this.Cost + "\n");
        return null;
    }

//public void 
}
