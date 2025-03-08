package ch.unibas.dmi.dbis.cs108.example;

import ch.unibas.dmi.dbis.cs108.server.*;
/**
 * testing the HexMap and HexTiles classes
 */
public class Main {
    public static void main(String[] args){
        ServerMain.main(new String[]{"server", "5000"});
        ServerMain.main(new String[]{"client", "1:5000"});
    }
}
