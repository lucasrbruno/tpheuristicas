/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package station;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author LucasRezende
 */
public class RandomGreedyHeuristic {
    int stationVar[][];
    int coverage[];
    int numCovVehicles, numStations;
    int NV, GRID;
    double percentual;
    int restrictedListSize;
    ArrayList<Vehicle> vehicles;

    public RandomGreedyHeuristic(int NV, int GRID, ArrayList<Vehicle> vehicles, double percentual) {
        this.stationVar = new int[GRID][GRID];
        this.coverage = new int[vehicles.size()];
        this.NV = NV;
        this.GRID = GRID;
        this.vehicles = vehicles;
        this.numCovVehicles = 0;
        this.numStations = 0;
        this.percentual = percentual;
        this.restrictedListSize = (int) (this.percentual * GRID * GRID);
        System.out.printf("GRID = %d\n", GRID);
        System.out.printf("tamanho lista = %d\n", this.restrictedListSize);
    }
    
     public void solve() {

        // Constructive Heuristic
        greedyRandomConstruction();

        
        System.out.println("\n*** Solution of Greedy Random Heuristic ***\n");

        System.out.println("Number of stations:" + numStations);
        System.out.println("Number of recharges:" + getNumRecharges());

        // Output
        for (int i = 0; i < GRID; i++) {
            for (int j = 0; j < GRID; j++) {
                if (stationVar[i][j] == 1) {
                    System.out.print(i + ";" + j + ";;");
                }
            }
        }

        System.out.println("\n");
        
        for (int i = 0; i < GRID; i++) {
            for (int j = 0; j < GRID; j++) {
                if (stationVar[i][j] == 1){
                    System.out.print("x");
                }
                else System.out.print(" ");
            }
            System.out.println();
        }
        
    }
     
     public void greedyRandomConstruction(){
         
        //int[][] vehicleDensity = new int[GRID][GRID];
        int score[][] = new int[GRID][GRID];
        
        // Vehicle density in each cell
        /*for (Vehicle v : vehicles){ // for each car            
            for (Move m : v.getTrace()){            
                vehicleDensity[m.getSquare().getCoordX()][m.getSquare().getCoordY()] ++;
            }
        }
        //System.out.printf("Size of restricted list and percentual: %d   %f\n",restrictedListSize, percentual);
        System.out.println("Number of vehicles per square:\n\n");
        for (int i = 0; i < GRID; i++) {
            for (int j = 0; j < GRID; j++) {
                //if (vehicleDensity[i][j] == 1){
                    //System.out.print(i+ ";" + j + ";;");
                    System.out.printf("%5d ", vehicleDensity[i][j]);
                //}
            }
            System.out.println();
        }*/
        ArrayList<Square> listaRestrita = new ArrayList<Square>();
        
          while (numCovVehicles < NV) {   //enquanto a frota toda estiver coberta pelos pontos
              
               //System.out.println("coverage: " + (double)numCovVehicles/(double)NV);
            
            // clean scores
            for (int i = 0; i < GRID; i++) {
                for (int j = 0; j < GRID; j++) {
                    score[i][j] = 0;
                }
            }
            // update scores
            for (Vehicle v : vehicles) {

                if (coverage[v.getID()] == 0) {
                    double battery = v.getInitBattery();
                    double traveledDist = 0;

                    for (Move m : v.getTrace()) {

                        if (battery < v.getRouteSize() - traveledDist  && battery < Global.BATTERY_CAPACITY * 0.1 && stationVar[m.getSquare().getCoordX()][m.getSquare().getCoordY()] == 0) {
                            score[m.getSquare().getCoordX()][m.getSquare().getCoordY()]++;
                            battery = Global.BATTERY_CAPACITY;
                        } 
                        else if (battery < v.getRouteSize() - traveledDist  && battery < Global.BATTERY_CAPACITY * 0.1 && stationVar[m.getSquare().getCoordX()][m.getSquare().getCoordY()] == 1) {
                            battery = Global.BATTERY_CAPACITY;
                        }
                        battery -= m.getDepletion();
                        traveledDist += m.getDistance();
                    }
                }
            }
            int cont = 0;  
            while(cont < restrictedListSize){
                // place highest
                int max = -1, best_i = -1, best_j = -1;
                for (int i = 0; i < GRID; i++) {
                    for (int j = 0; j < GRID; j++) {
                        if (!esta_na_ListaRestrita(listaRestrita, i, j) && stationVar[i][j] == 0 && score[i][j] > max) {
                            max = score[i][j];
                            best_i = i;
                            best_j = j;
                        }
                    }
                }
                listaRestrita.add(new Square(cont, best_i, best_j));
                //System.out.printf("Adicionada na lista: [%d][%d]. Contador em %d\n",best_i, best_j, cont);
                cont++;
            }
            
            Random gerador = new Random();
            int random_id = gerador.nextInt(restrictedListSize);
            
            // adiciona valor aleatorio da lista restrita
            stationVar[listaRestrita.get(random_id).getCoordX()][listaRestrita.get(random_id).getCoordY()] = 1;
            //System.out.printf("Adicionada estacao em [%d][%d]\n",listaRestrita.get(random_id).getCoordX(), listaRestrita.get(random_id).getCoordY());
            numStations++;
            
            listaRestrita.clear();
            // Update coverage
            updateCoverage();
            
            //System.out.printf("Cobertura de veiculos: %d de %d\n",numCovVehicles, NV);
            
        }
     }
    private boolean esta_na_ListaRestrita(ArrayList<Square> listaRestrita, int x, int y){
        for (Square sq : listaRestrita){
            if (sq.getCoordX() == x && sq.getCoordY() == y){
                return true;
            }    
        }
        return false;
     }
     private void updateCoverage(){
            
        for (Vehicle v : vehicles){
            if (coverage[v.getID()] == 0){
                if (v.getRouteSize() <= v.getInitBattery()){    //se o veiculo tem bateria para a sua rota
                    coverage[v.getID()] = 1;
                    numCovVehicles++;
                }
                else if (isCovered(v)){     //se foi carregado
                    coverage[v.getID()] = 1;
                    numCovVehicles++;
                }
            }            
        }
    }
    
    private boolean isCovered(Vehicle v){
        
        boolean isCov = true;
        
        double battery = v.getInitBattery();
        double traveledDist = 0;
                
        for(Move m : v.getTrace()){
                        
            // update battery
            if (stationVar[m.getSquare().getCoordX()][m.getSquare().getCoordY()] == 1 && battery < v.getRouteSize() - traveledDist && battery < Global.BATTERY_CAPACITY * 0.1){ // If there is a station and vehicle needs to recharge
                battery = Global.BATTERY_CAPACITY;
            }
            battery -= m.getDepletion();
            traveledDist += m.getDistance();
                            
            // check battery ok
            if (battery < 0) {
                isCov = false;
                break;
            }
        }
                
        return isCov;
    }
    
    private int getNumRecharges(){
        
        int total = 0;
    
        for (Vehicle v : vehicles){
            double battery = v.getInitBattery();
            double traveledDist = 0;
            int numRec = 0;

            for (Move m : v.getTrace()) {
            
                // update battery
                if (stationVar[m.getSquare().getCoordX()][m.getSquare().getCoordY()] == 1 && battery < v.getRouteSize() - traveledDist && battery < Global.BATTERY_CAPACITY * 0.1){ // If there is a station and vehicle needs to recharge
                    battery = Global.BATTERY_CAPACITY;
                    numRec++;
                }
                battery -= m.getDepletion();
                traveledDist += m.getDistance();

            }
            total += numRec;
            //System.out.println("vehicle " + v.getID() + ": " + numRec);
        }
        return total;
    } 
    
}
