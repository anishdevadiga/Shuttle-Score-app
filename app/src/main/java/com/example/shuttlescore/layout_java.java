package com.example.shuttlescore;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Stack; // Add this

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class layout_java extends AppCompatActivity {
    private int player1Score = 0;
    private int player2Score = 0;
    private boolean isPlayer1Serving = true;

    private TextView player1ScoreText;
    private TextView player2ScoreText;
    private TextView serveIndicatorText;
    private TextView loveAllText;

    private TextView block1, block2, block3, block4;

    private Stack<Integer> scoreHistory;

    private int totalMatches = 3;
    private int currentMatch = 1;
    private int player1MatchesWon = 0;
    private int player2MatchesWon = 0;

    private String player1Name = "Player 1";
    private String player2Name = "Player 2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_court);

        player1ScoreText = findViewById(R.id.player1);
        player2ScoreText = findViewById(R.id.player2);
        serveIndicatorText = findViewById(R.id.player1Serve);
        loveAllText = findViewById(R.id.loveAll);

        block1 = findViewById(R.id.block1);
        block2 = findViewById(R.id.block2);
        block3 = findViewById(R.id.block3);
        block4 = findViewById(R.id.block4);

        resetBlockColors();

        scoreHistory = new Stack<>();

        Button buttonTop = findViewById(R.id.buttonTop);
        Button buttonBottom = findViewById(R.id.buttonBottom);
        Button undoButton = findViewById(R.id.undoButton);
        Button finishButton = findViewById(R.id.finishButton);
        Button resetButton = findViewById(R.id.comment);

        loveAllText.setText("Love all");

        updateServeIndicator();
        updateBlockColors();

        // Ask for player names and the number of matches
//        askForPlayerNames();
        askForNumberOfMatches();

        // Get the player names from the intent
        Intent intent = getIntent();
        player1Name = intent.getStringExtra("player1Name");
        player2Name = intent.getStringExtra("player2Name");

        // Continue with setting up views and other logic
        player1ScoreText = findViewById(R.id.player1);
        player2ScoreText = findViewById(R.id.player2);

        // Increment Player 1's score
        buttonTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player1Score++;
                scoreHistory.push(1);
                isPlayer1Serving = true;
                updateScoreDisplay();
                updateServeIndicator();
                updateBlockColors();
            }
        });

        // Increment Player 2's score
        buttonBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player2Score++;
                scoreHistory.push(2);
                isPlayer1Serving = false;
                updateScoreDisplay();
                updateServeIndicator();
                updateBlockColors();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });

        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoLastPoint();
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishGameManually();
            }
        });
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExitDialog();
            }
        });
        handleBackPress();
    }

    private void askForPlayerNames() {
        // Create a dialog to input both player names
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Player Names");

        // Create a layout to hold two input fields
        final EditText inputPlayer1 = new EditText(this);
        inputPlayer1.setHint("Player 1 Name");
        inputPlayer1.setInputType(InputType.TYPE_CLASS_TEXT);

        final EditText inputPlayer2 = new EditText(this);
        inputPlayer2.setHint("Player 2 Name");
        inputPlayer2.setInputType(InputType.TYPE_CLASS_TEXT);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(inputPlayer1);
        layout.addView(inputPlayer2);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("OK", null); // Set to null initially

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Prevent dialog from being dismissed

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String player1Input = inputPlayer1.getText().toString().trim();
                String player2Input = inputPlayer2.getText().toString().trim();

                // Validate player names
                if (player1Input.isEmpty() || player2Input.isEmpty()) {
                    inputPlayer1.setError("Please enter a name for Player 1");
                    inputPlayer2.setError("Please enter a name for Player 2");
                } else {
                    player1Name = player1Input;
                    player2Name = player2Input;
                    updateScoreDisplay();
                    askForNumberOfMatches();
                    dialog.dismiss(); // Dismiss only if inputs are valid
                }
            });
        });

        dialog.show();
    }

    private void askForNumberOfMatches() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the number of matches");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Number of matches");
        builder.setView(input);

        builder.setPositiveButton("OK", null); // Set to null initially

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Prevent dialog from being dismissed

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String inputMatches = input.getText().toString().trim();

                // Validate the number of matches input
                if (inputMatches.isEmpty() || Integer.parseInt(inputMatches) <= 0) {
                    input.setError("Please enter a valid number of matches (greater than 0)");
                } else {
                    totalMatches = Integer.parseInt(inputMatches);
                    dialog.dismiss(); // Dismiss only if input is valid
                }
            });
        });

        dialog.show();
    }

    private void updateScoreDisplay() {
        player1ScoreText.setText(player1Name + ": " + player1Score);
        player2ScoreText.setText(player2Name + ": " + player2Score);

        if (player1Score == 0 && player2Score == 0) {
            loveAllText.setText("Love all");
        } else if (player1Score == player2Score && player1Score != 0) {
            loveAllText.setText(player1Score + " all");
        } else {
            loveAllText.setText(player1Score + ", " + player2Score);
        }

        checkForWinner();
    }

    private void updateServeIndicator() {
        if (isPlayer1Serving) {
            serveIndicatorText.setText(player1Name + " to serve");
        } else {
            serveIndicatorText.setText(player2Name + " to serve");
        }
        updateBlockColors(); // Update the block colors whenever the serve changes
    }

    private void updateBlockColors() {
        resetBlockColors();

        // Check if the game is at the start state.
        if (player1Score == 0 && player2Score == 0) {
            if (isPlayer1Serving) {
                block2.setBackgroundColor(Color.GREEN); // Player 1 starts serving from right
            } else {
                block3.setBackgroundColor(Color.GREEN); // Player 2 starts serving from right
            }
            return;
        }

        // Determine which blocks to highlight based on the player serving.
        if (isPlayer1Serving) {
            serveIndicatorText.setText(player1Name + " to serve");

            if (player1Score % 2 == 0) {
                // Player 1 serves from the right side.
                block2.setBackgroundColor(Color.GREEN);
            } else {
                // Player 1 serves from the left side.
                block1.setBackgroundColor(Color.GREEN);
            }
        } else {
            serveIndicatorText.setText(player2Name + " to serve");

            if (player2Score % 2 == 0) {
                // Player 2 serves from the right side.
                block3.setBackgroundColor(Color.GREEN);
            } else {
                // Player 2 serves from the left side.
                block4.setBackgroundColor(Color.GREEN);
            }
        }
    }

    private void resetBlockColors() {
        block1.setBackgroundColor(Color.WHITE);
        block2.setBackgroundColor(Color.WHITE);
        block3.setBackgroundColor(Color.WHITE);
        block4.setBackgroundColor(Color.WHITE);
    }

    private void checkForWinner() {
        if ((player1Score >= 21 || player2Score >= 21) && Math.abs(player1Score - player2Score) >= 2) {
            if (player1Score > player2Score) {
                player1MatchesWon++;
                showWinner("Congrats " + player1Name + "! You won this match!");
            } else {
                player2MatchesWon++;
                showWinner("Congrats " + player2Name + "! You won this match!");
            }
        }
    }

    private void showWinner(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Match Over")
                .setMessage(message)
                .setPositiveButton("Next Match", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        proceedToNextMatch();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(false) // This makes the dialog non-cancelable
                .show();
    }

    private void proceedToNextMatch() {
        if (currentMatch < totalMatches) {
            currentMatch++;
            resetGame();
            TextView gameTitle = findViewById(R.id.gameTitle1);
            gameTitle.setText("GAME " + currentMatch);
        } else {
            showFinalResult();
        }
    }



    private void showFinalResult() {
        String finalMessage;
        if (player1MatchesWon > player2MatchesWon) {
            finalMessage = "Congrats " + player1Name + "! You won the series!";
        } else if (player2MatchesWon > player1MatchesWon) {
            finalMessage = "Congrats " + player2Name + "! You won the series!";
        } else {
            finalMessage = "It's a draw!";
        }

        new AlertDialog.Builder(this)
                .setTitle("Series Over")
                .setMessage(finalMessage)
                .setPositiveButton("Start New Series", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        resetGameSeries();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(false) // This makes the dialog non-cancelable
                .show();
    }

    private void resetGameSeries() {
        // Ask the user again for the number of matches in the new series
//        askForPlayerNames(); // Also ask for player names again for the new series

        // Reset the series tracking
        currentMatch = 1;
        player1MatchesWon = 0;
        player2MatchesWon = 0;
        resetGame(); // Reset the game for the next match
        TextView gameTitle = findViewById(R.id.gameTitle1);
        gameTitle.setText("GAME " + currentMatch); // Update the game title back to Game 1
    }

    private void resetGame() {
        player1Score = 0;
        player2Score = 0;
        scoreHistory.clear(); // Clear the score history on reset
        loveAllText.setText("Love all"); // Reset "Love all"
        updateScoreDisplay();
        updateServeIndicator(); // Reset serve to initial state
        updateBlockColors(); // Reset block colors for 0-0 start
    }

    private void undoLastPoint() {
        if (!scoreHistory.isEmpty()) {
            int lastPoint = scoreHistory.pop();

            if (lastPoint == 1 && player1Score > 0) {
                player1Score--;
                isPlayer1Serving = false;
            } else if (lastPoint == 2 && player2Score > 0) {
                player2Score--;
                isPlayer1Serving = true;
            }

            updateScoreDisplay();
            updateServeIndicator();
            updateBlockColors();
        }
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit the match")
                .setMessage("Are you sure you want to Exit?")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Exit the activity (finish the current match)
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog, stay in the current match
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)  // Prevent dismissing by tapping outside the dialog
                .show();
    }
    private void handleBackPress() {
        // Register a callback for the system back button press
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show the exit confirmation dialog when the system back button is pressed
                showExitDialog();
            }
        };

        // Add the callback to the OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);
    }


    private void finishGameManually() {
        if (player1Score > player2Score) {
            player1MatchesWon++;
            showWinner("Congrats " + player1Name + "! You won this match!");
        } else if (player2Score > player1Score) {
            player2MatchesWon++;
            showWinner("Congrats " + player2Name + "! You won this match!");
        } else {
            showWinner("It's a draw!");
        }
    }
}
