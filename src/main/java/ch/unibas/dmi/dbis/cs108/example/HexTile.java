package ch.unibas.dmi.dbis.cs108.example;

/**
 * HexTile is the smallest element of the map. Multiple HexTiles form the HexMap.
 * @author vincent
 */
public class HexTile {
    /**
     * The private Row and Column
     */
    private int row, column;
    /**
     * The type of resource on the HexTile. Stored as a String right now, maybe changed to Class Resource later?
     * When making a Resource class, maybe add amount of resource there or in this class.
     */
    private String resource;
    /**
     * The building type currently placed on the HexTile. Stored as a String, maybe change to Building class
     * later that adds levels and funtionality to the buildings.
     */
    private String building;

    /**
     * Constructor of the class HexTile
     * @param column Column of the HexTile
     * @param row Row of the HexTile
     * @param resource Resources on the HexTile
     * @param building Building placed on the HexTile
     */
    public HexTile(int column, int row, String resource, String building) {
        this.column = column;
        this.row = row;
        this.resource = resource;
        this.building = building;
    }

    /**
     * sets the current building value to the parameter building
     * @param building building type placed
     */
    public void placeBuilding(String building){
        this.building = building;
    }

    /**
     * sets the current building value to null
     */
    public void removeBuilding(){
        this.building = null;
    }

    /**
     * this method gives all relevant pieces of information about the HexTile
     * @return String containing all variable values
     */
    public String getInfo(){
        return "Hexfeld (" + row + ", " + column + ") - Ressource: " + resource +
                (building != null ? ", Gebäude: " + building : "");
    }

}
