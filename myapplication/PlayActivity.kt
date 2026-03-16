package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.ThreadLocalRandom

class PlayActivity : AppCompatActivity() {
    private var selectedColor = COLORS[0]
    private var lastRowLayout: LinearLayout? = null // Default selected color

    var colors: MutableList<Int> = ArrayList()
    var count = 0;

    override fun onCreate(savedInstanceState: Bundle?) {

        for (i in 0..4) {
            val random = ThreadLocalRandom.current().nextInt(0, 6)
            when (random) {
                1 -> colors.add(R.color.purple)
                2 -> colors.add(R.color.red)
                3 -> colors.add(R.color.yellow)
                4 -> colors.add(R.color.green)
                5 -> colors.add(R.color.blue)
                6 -> colors.add(R.color.orange)
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        // Add color selection buttons dynamically
        createColorSelectionBar()


        addNewRow() // Call the method to add a new row
        count++;

        // Set OnClickListener for "Submit Guess" button (assuming it is added in XML)
        findViewById<View>(R.id.submitGuessButton).setOnClickListener { checkGuess() }


    }

    private fun checkGuess() {
        if (lastRowLayout == null) {
            Toast.makeText(this, "No rows added yet!", Toast.LENGTH_SHORT).show()
            return
        }

        var allFilled = true
        var numCorrect = 0
        var numSortaCorrect = 0
        val correctPositions = BooleanArray(NUM_COLS)

        // Debug: Print out the colors array with actual color values
        for (colorRes in colors) {
            val color = resources.getColor(colorRes) // Get actual color value
            Log.d("DEBUG", "Generated color in colors list: $color")
        }

        // Check for exact matches
        for (i in 0 until NUM_COLS) {
            val pegButton = lastRowLayout!!.getChildAt(i) as Button
            val pegColor = pegButton.backgroundTintList!!.defaultColor

            // Check if the peg button has been filled (not black or unfilled color)
            if (pegColor == resources.getColor(R.color.black)) {
                allFilled = false // Found an unfilled button
                break // No need to check further, exit loop
            }

            // Debug: Log each button's color and the color being compared
            Log.d("DEBUG", "Peg button color at index $i: $pegColor")
            val targetColor = resources.getColor(colors[i]!!) // Get the target color from resources
            Log.d("DEBUG", "Target color at index $i: $targetColor")

            // Check if pegColor is equal to targetColor
            if (pegColor == targetColor) {
                numCorrect++
                correctPositions[i] = true // Mark as correct
            }
        }

        // Check for partial matches (colors in the wrong position)
        for (i in 0 until NUM_COLS) {
            if (!correctPositions[i]) {  // Only check if not an exact match
                val pegColor =
                    (lastRowLayout!!.getChildAt(i) as Button).backgroundTintList!!
                        .defaultColor

                for (j in 0 until NUM_COLS) {
                    if (!correctPositions[j]) {
                        val targetColor = resources.getColor(colors[j]!!) // Get the target color
                        if (pegColor == targetColor) {
                            numSortaCorrect++
                            correctPositions[j] = true // Mark as counted
                            break
                        }
                    }
                }
            }
        }

        if (allFilled) {
            // Get reference to the 2x2 small boxes layout (the fifth view in the row)

            val smallBoxLayout = lastRowLayout!!.getChildAt(4) as LinearLayout
            var boxIndex = 0

            // Set red for correct matches and grey for almost correct matches
            for (i in 0 until numCorrect) {
                val smallBoxButton =
                    (smallBoxLayout.getChildAt(boxIndex / 2) as LinearLayout).getChildAt(boxIndex % 2) as Button
                smallBoxButton.backgroundTintList = resources.getColorStateList(R.color.red)
                boxIndex++
            }
            for (i in 0 until numSortaCorrect) {
                val smallBoxButton =
                    (smallBoxLayout.getChildAt(boxIndex / 2) as LinearLayout).getChildAt(boxIndex % 2) as Button
                smallBoxButton.backgroundTintList = resources.getColorStateList(R.color.grey)
                boxIndex++
            }

            if (numCorrect == 4) {
                endGameDialog(true);
            }

            if(count < NUM_ROWS) {
                addNewRow()
                count++
            } else if(count == NUM_ROWS && numCorrect == 4) {
                endGameDialog(true)
            } else {
                endGameDialog(false)
            }

        }
    }

    protected fun addNewRow() {
        // Get the reference to the main layout for rows
        val playRowsLinearLayout = findViewById<LinearLayout>(R.id.PlayRowsLinearLayout)

        // Create a new LinearLayout for the row
        val rowLayout = LinearLayout(this)
        rowLayout.orientation = LinearLayout.HORIZONTAL
        rowLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Create buttons for each peg in the new row
        for (j in 0 until NUM_COLS) {
            val pegButton = Button(this)
            // Set a fixed width and height, with padding to make the buttons narrower
            val params = LinearLayout.LayoutParams(
                160, 160 // Set width and height in pixels; adjust as needed
            )
            params.setMargins(7, 7, 7, 7) // Optional: Add margin around each button
            pegButton.layoutParams = params

            // Set padding for a consistent look
            pegButton.setPadding(8, 8, 8, 8)

            pegButton.backgroundTintList = resources.getColorStateList(R.color.purple_700)
            pegButton.contentDescription = "Peg button newRow-$j"

            // Set OnClickListener to change the color of the peg to selectedColor only if it's the last row
            pegButton.setOnClickListener { // Only allow color change for the last row
                if (rowLayout === lastRowLayout) {
                    pegButton.backgroundTintList = resources.getColorStateList(selectedColor)
                }
            }

            rowLayout.addView(pegButton) // Add button to the new row
        }

        // Create a LinearLayout to hold the 2x2 smaller boxes for the fifth box
        val smallBoxLayout = LinearLayout(this)
        smallBoxLayout.orientation =
            LinearLayout.VERTICAL // Set vertical orientation to stack two rows
        smallBoxLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Create two horizontal rows, each with two small buttons (to form a 2x2 grid)
        for (row in 0..1) {
            val smallRowLayout = LinearLayout(this)
            smallRowLayout.orientation = LinearLayout.HORIZONTAL // Horizontal row of 2 buttons
            smallRowLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // Add two small buttons to each horizontal row
            for (i in 0..1) {
                val smallBoxButton = Button(this)
                val smallBoxParams = LinearLayout.LayoutParams(
                    50, 50 // Size for each smaller box (50x50)
                )
                smallBoxParams.setMargins(4, 4, 4, 4) // Margin between the small boxes
                smallBoxButton.layoutParams = smallBoxParams

                // Initially set the background color to grey (indicating no match)
                smallBoxButton.backgroundTintList = resources.getColorStateList(R.color.black)

                // Add the smaller box button to the current row layout
                smallRowLayout.addView(smallBoxButton)
            }

            // Add each horizontal row to the main smallBoxLayout
            smallBoxLayout.addView(smallRowLayout)
        }

        // Add the smallBoxLayout to the rowLayout (fifth box off to the side)
        rowLayout.addView(smallBoxLayout)

        // Add the new row (with the fifth box) to the main layout
        playRowsLinearLayout.addView(rowLayout)
        lastRowLayout = rowLayout // Update the lastRowLayout reference
    }

    private fun createColorSelectionBar() {
        val colorSelectionBar = findViewById<LinearLayout>(R.id.ColorSelectionBar)

        for (color in COLORS) {
            val colorButton = Button(this)
            // Set a fixed width and height, with padding to make the buttons narrower
            val params = LinearLayout.LayoutParams(
                90, 90 // Set width and height in pixels; adjust as needed
            )
            params.setMargins(8, 8, 8, 8) // Optional: Add margin around each button
            colorButton.layoutParams = params

            // Set padding for a consistent look
            colorButton.setPadding(8, 8, 8, 8)
            colorButton.backgroundTintList = resources.getColorStateList(color)

            // Set OnClickListener to set selectedColor when this button is clicked
            colorButton.setOnClickListener {
                selectedColor = color // Update the selected color
            }

            colorSelectionBar.addView(colorButton) // Add color button to selection bar
        }
    }

    //menu option for play activity
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_play, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.play_how_to -> {
                // launch how to site
                goToURL("https://www.wikihow.com/Play-Mastermind")
                true
            }
            R.id.exit -> {
                exitDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // exit when game is over
    fun endGameDialog(winner: Boolean) {
        // creating custom dialog
        val dialog = android.app.Dialog(this@PlayActivity)
        // setting content view to dialog
        if (winner) {
            dialog.setContentView(R.layout.win_dialog)
        } else {
            dialog.setContentView(R.layout.lost_dialog)
        }

        // getting reference of TextView
        val dialogButton = dialog.findViewById(R.id.textViewOk) as TextView
        // click listener for Okay Bye
        dialogButton.setOnClickListener {
            finish()
        }
        // show the exit dialog
        dialog.show()
    }

    //ask if user want's to exit
    fun exitDialog() {
        // creating custom dialog
        val dialog = android.app.Dialog(this@PlayActivity)
        // setting content view to dialog
        dialog.setContentView(R.layout.exit_dialog)
        // getting reference of TextView
        val dialogButtonYes = dialog.findViewById(R.id.textViewYes) as TextView
        val dialogButtonNo = dialog.findViewById(R.id.textViewNo) as TextView
        // click listener for No
        dialogButtonNo.setOnClickListener { // dismiss the dialog
            dialog.dismiss()
        }
        // click listener for Yes
        dialogButtonYes.setOnClickListener { // dismiss the dialog and exit the exit
            dialog.dismiss()
            finish()
        }

        // show the exit dialog
        dialog.show()
    }

    // go to how to play page
    fun goToURL(site: String) {
        try {
            startActivity( Intent(Intent.ACTION_VIEW, Uri.parse(site)))
        } catch (e: Exception) {
            Toast.makeText(getApplicationContext(),"no website linked", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val NUM_ROWS = 10 // Define the number of rows
        private const val NUM_COLS = 4 // Define the number of buttons in each row
        private val COLORS = intArrayOf(
            R.color.purple, R.color.red, R.color.yellow, R.color.green, R.color.blue,
            R.color.orange
        )
    }
}