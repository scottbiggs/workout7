/**
 * Class that's used ONLY when making the Grid.
 *
 * This is the info that's published from doInBackground()
 * to onProgressUpdate() during the GridAsyncTask stuff.
 *
 * It contains all the information that add_row needs to add
 * a row to the grid.
 */
package com.sleepfuriously.hpgworkout;

public class GridRow {


	/** Name of the exercise. Null if not used. */
	String exer_name = null;
	/** database ID of the exercise */
	int exer_id = -1;
	/** The number of the significant column for this exericise. */
	int exer_sig_marker = -1;
	/** This array that holds the info for all the cells in this row. */
	GridElement tag_array[] = null;

}
