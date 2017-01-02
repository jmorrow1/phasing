package screens;

/**
 * 
 * @author James Morrow
 *
 */
interface CellEventHandler {

    /**
     * Copies the PhrasePicture associated with the given cell, and adds it to
     * the end of the list of phrase pictures.
     * 
     * @param cell
     */
    public void copy(Cell cell);

    /**
     * Gets the PhrasePicture associated with the given cell, and makes it the
     * currentPhrasePicture.
     * 
     * @param cell
     */
    // public void load(Cell cell);

    /**
     * Deletes the PhrasePicture associated with the given cell.
     * 
     * @param cell
     */
    public void delete(Cell cell);

    /**
     * Makes a blank new PhrasePicture and adds it to the end of a list.
     */
    public void newPhrase();

    /**
     * Makes a new PhrasePicture with a randomly-generated Phrase and adds it to
     * the end of alist.
     */
    public void generatePhrase();
}