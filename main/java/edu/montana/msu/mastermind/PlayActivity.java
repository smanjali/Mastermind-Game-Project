package edu.montana.msu.mastermind;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.ThreadLocalRandom;

public class PlayActivity extends AppCompatActivity {

    private static final int NUM_ROWS = 1;    // Define the number of rows
    private static final int NUM_COLS = 4;    // Define the number of buttons in each row
    private static final int[] COLORS = {
            R.color.purple_200, R.color.red, R.color.yellow, R.color.green, R.color.blue,
            R.color.teal_700
    };
    private int selectedColor = COLORS[0];
    private LinearLayout lastRowLayout;// Default selected color

    List<Integer> colors = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        for(int i = 0; i < 5; i++){
            int random = ThreadLocalRandom.current().nextInt(0, 6);
            switch (random){
                case 1:
                    colors.add(R.color.purple_200);
                    break;
                case 2:
                    colors.add(R.color.red);
                    break;
                case 3:
                    colors.add(R.color.yellow);
                    break;
                case 4:
                    colors.add(R.color.green);
                    break;
                case 5:
                    colors.add(R.color.blue);
                    break;
                case 6:
                    colors.add(R.color.teal_700);
                    break;
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        // Add color selection buttons dynamically
        createColorSelectionBar();

        addNewRow();  // Call the method to add a new row

        // Set OnClickListener for "Submit Guess" button (assuming it is added in XML)
        findViewById(R.id.submitGuessButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkGuess();
            }
        });
    }

    private void checkGuess() {
        if (lastRowLayout == null) {
            Toast.makeText(this, "No rows added yet!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean allFilled = true;
        int numCorrect = 0;
        int numSortaCorrect = 0;
        boolean[] correctPositions = new boolean[NUM_COLS];

        // Debug: Print out the colors array with actual color values
        for (int colorRes : colors) {
            int color = getResources().getColor(colorRes);  // Get actual color value
            Log.d("DEBUG", "Generated color in colors list: " + color);
        }

        // Check for exact matches
        for (int i = 0; i < NUM_COLS; i++) {
            Button pegButton = (Button) lastRowLayout.getChildAt(i);
            int pegColor = pegButton.getBackgroundTintList().getDefaultColor();

            // Check if the peg button has been filled (not black or unfilled color)
            if (pegColor == getResources().getColor(R.color.black)) {
                allFilled = false;  // Found an unfilled button
                break;  // No need to check further, exit loop
            }

            // Debug: Log each button's color and the color being compared
            Log.d("DEBUG", "Peg button color at index " + i + ": " + pegColor);
            int targetColor = getResources().getColor(colors.get(i)); // Get the target color from resources
            Log.d("DEBUG", "Target color at index " + i + ": " + targetColor);

            // Check if pegColor is equal to targetColor
            if (pegColor == targetColor) {
                numCorrect++;
                correctPositions[i] = true;  // Mark as correct
            }
        }

        // Check for partial matches (colors in the wrong position)
        for (int i = 0; i < NUM_COLS; i++) {
            if (!correctPositions[i]) {  // Only check if not an exact match
                int pegColor = ((Button) lastRowLayout.getChildAt(i)).getBackgroundTintList().getDefaultColor();

                for (int j = 0; j < NUM_COLS; j++) {
                    if (!correctPositions[j]) {
                        int targetColor = getResources().getColor(colors.get(j));  // Get the target color
                        if (pegColor == targetColor) {
                            numSortaCorrect++;
                            correctPositions[j] = true;  // Mark as counted
                            break;
                        }
                    }
                }
            }
        }

        if (allFilled) {

            // Get reference to the 2x2 small boxes layout (the fifth view in the row)
            LinearLayout smallBoxLayout = (LinearLayout) lastRowLayout.getChildAt(4);
            int boxIndex = 0;

            // Set red for correct matches and grey for almost correct matches
            for (int i = 0; i < numCorrect; i++) {
                Button smallBoxButton = (Button) ((LinearLayout) smallBoxLayout.getChildAt(boxIndex / 2)).getChildAt(boxIndex % 2);
                smallBoxButton.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                boxIndex++;
            }
            for (int i = 0; i < numSortaCorrect; i++) {
                Button smallBoxButton = (Button) ((LinearLayout) smallBoxLayout.getChildAt(boxIndex / 2)).getChildAt(boxIndex % 2);
                smallBoxButton.setBackgroundTintList(getResources().getColorStateList(R.color.grey));
                boxIndex++;
            }

            if (numCorrect == 4) {
                Toast.makeText(this, "Congrats! U did it. Proud of you pal!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, numCorrect + " correct, " + numSortaCorrect + " partially correct", Toast.LENGTH_SHORT).show();
            }
            addNewRow();
        } else {
            Toast.makeText(this, "Please fill all boxes before submitting your guess!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void addNewRow() {
        // Get the reference to the main layout for rows
        LinearLayout playRowsLinearLayout = findViewById(R.id.PlayRowsLinearLayout);

        // Create a new LinearLayout for the row
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Create buttons for each peg in the new row
        for (int j = 0; j < NUM_COLS; j++) {
            Button pegButton = new Button(this);
            // Set a fixed width and height, with padding to make the buttons narrower
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    160, 160  // Set width and height in pixels; adjust as needed
            );
            params.setMargins(8, 8, 8, 8);  // Optional: Add margin around each button
            pegButton.setLayoutParams(params);

            // Set padding for a consistent look
            pegButton.setPadding(8, 8, 8, 8);

            pegButton.setBackgroundTintList(getResources().getColorStateList(R.color.black));
            pegButton.setContentDescription("Peg button newRow-" + j);

            // Set OnClickListener to change the color of the peg to selectedColor only if it's the last row
            pegButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Only allow color change for the last row
                    if (rowLayout == lastRowLayout) {
                        pegButton.setBackgroundTintList(getResources().getColorStateList(selectedColor));
                    }
                }
            });

            rowLayout.addView(pegButton);  // Add button to the new row
        }

        // Create a LinearLayout to hold the 2x2 smaller boxes for the fifth box
        LinearLayout smallBoxLayout = new LinearLayout(this);
        smallBoxLayout.setOrientation(LinearLayout.VERTICAL); // Set vertical orientation to stack two rows
        smallBoxLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Create two horizontal rows, each with two small buttons (to form a 2x2 grid)
        for (int row = 0; row < 2; row++) {
            LinearLayout smallRowLayout = new LinearLayout(this);
            smallRowLayout.setOrientation(LinearLayout.HORIZONTAL); // Horizontal row of 2 buttons
            smallRowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Add two small buttons to each horizontal row
            for (int i = 0; i < 2; i++) {
                Button smallBoxButton = new Button(this);
                LinearLayout.LayoutParams smallBoxParams = new LinearLayout.LayoutParams(
                        50, 50  // Size for each smaller box (50x50)
                );
                smallBoxParams.setMargins(4, 4, 4, 4); // Margin between the small boxes
                smallBoxButton.setLayoutParams(smallBoxParams);

                // Initially set the background color to grey (indicating no match)
                smallBoxButton.setBackgroundTintList(getResources().getColorStateList(R.color.black));

                // Add the smaller box button to the current row layout
                smallRowLayout.addView(smallBoxButton);
            }

            // Add each horizontal row to the main smallBoxLayout
            smallBoxLayout.addView(smallRowLayout);
        }

        // Add the smallBoxLayout to the rowLayout (fifth box off to the side)
        rowLayout.addView(smallBoxLayout);

        // Add the new row (with the fifth box) to the main layout
        playRowsLinearLayout.addView(rowLayout);
        lastRowLayout = rowLayout;  // Update the lastRowLayout reference
    }

    private void createColorSelectionBar() {
        LinearLayout colorSelectionBar = findViewById(R.id.ColorSelectionBar);

        for (int color : COLORS) {
            Button colorButton = new Button(this);
            // Set a fixed width and height, with padding to make the buttons narrower
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    80, 80  // Set width and height in pixels; adjust as needed
            );
            params.setMargins(8, 8, 8, 8);  // Optional: Add margin around each button
            colorButton.setLayoutParams(params);

            // Set padding for a consistent look
            colorButton.setPadding(8, 8, 8, 8);
            colorButton.setBackgroundTintList(getResources().getColorStateList(color));

            // Set OnClickListener to set selectedColor when this button is clicked
            colorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedColor = color;   // Update the selected color
                }
            });

            colorSelectionBar.addView(colorButton);  // Add color button to selection bar
        }
    }
}