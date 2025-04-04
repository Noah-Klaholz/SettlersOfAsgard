package ch.unibas.dmi.dbis.cs108.example;

/**
 * HexMap is one of the core elements of the Game. It contains and manages the HexTiles.
 *
 * @author vincent
 */
public class HexMap {

    /**
     * declaring the list of possible resources as an array of string.
     */
    private final String[] possibleResources = {"Runen", "Energie"};

    /**
     * constructor of the HexMap class. calls the generateMap method
     *
     * @param rows    number of rows
     * @param columns number of columns
     */
    public HexMap(int rows, int columns) {
        generateMap(rows, columns);
    }

    /**
     * generating the map. this still has to be implemented
     *
     * @param rows    number of rows
     * @param columns number of columns
     */
    public void generateMap(int rows, int columns) {
        //TODO
    }

    /**
     * print the info of the tiles (using the getInfo method of HexTile)
     */
    public void printMap() {
        //TODO
    }

}