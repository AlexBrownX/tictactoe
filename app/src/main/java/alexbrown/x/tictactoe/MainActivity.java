package alexbrown.x.tictactoe;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private enum Piece {
        HUMAN_X("X"),
        HUMAN_O("O"),
        COMPUTER_O("O"),
        EMPTY("");

        private final String displayValue;

        Piece(final String displayValue) {
            this.displayValue = displayValue;
        }
    }

    private Vector<Vector<Piece>> gameBoard;
    private Piece currentPlayer;
    private Piece playerTwo;
    private boolean gameStarted;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        initBoard();
        initButtons(false);
        enableSwitches();
        setStartText();
        resetCurrentPlayerText();
    }

    private void startGame() {
        initButtons(true);
        disableSwitches();
        setRestartText();
        initPlayers();
        setCurrentPlayerText();
        printBoard();
        decideIfComputerShouldMove();
    }

    /**
     * Handles the moves from human players clicking board buttons.
     *
     * @param view the button the player selected
     */
    public void humanMove(final View view) {
        final String buttonTage = (String) view.getTag();
        final int row = Integer.parseInt(buttonTage.substring(0, 1));
        final int col = Integer.parseInt(buttonTage.substring(2, 3));
        final Button selectedButton = (Button) view;

        updateGameBoard(row, col);
        markButtonTaken(selectedButton);
        printBoard();

        if (boardContainsWin(row, col)) {
            endGame();
        } else {
            nextPlayer();
            decideIfComputerShouldMove();
        }
    }

    /**
     * Handles the moves for computer player.
     */
    public void computerMove() {
        final Pair<Integer, Integer> nextMove = calculateNextMove();

        if (nextMove == null) {
            drawGame();
        } else {
            updateGameBoard(nextMove.first, nextMove.second);
            markButtonTaken(getButton(nextMove.first, nextMove.second));
            printBoard();

            if (boardContainsWin(nextMove.first, nextMove.second)) {
                endGame();
            } else {
                nextPlayer();
                printBoard();
                decideIfComputerShouldMove();
            }
        }
    }

    /**
     * Finds the widget button corresponding to the give coordinates.
     *
     * @param row the row index
     * @param col the column index
     * @return the button widget
     */
    private Button getButton(final Integer row, final Integer col) {
        final String nextMoveButtonId = String.format("tile%s%s", row, col);
        return (Button) findViewById(getResources().getIdentifier(nextMoveButtonId, "id", getPackageName()));
    }

    /**
     * Calculates the next computer move. Looking first for winning moves,
     * vulnerable spaces, then generic pieces.
     *
     * @return the calculated position to choose
     */
    private Pair<Integer, Integer> calculateNextMove() {
        final Pair<Integer, Integer> winningMove = findWinningMove();

        if (winningMove != null) {
            Log.d("tictactoe", "Winning move found: " + winningMove);
            return winningMove;
        }

        final Pair<Integer, Integer> vulnerableSpace = findVulnerableSpace();

        if (vulnerableSpace != null) {
            Log.d("tictactoe", "Vulnerable space found: " + vulnerableSpace);
            return vulnerableSpace;
        }

        final Pair<Integer, Integer> nextMove = findNextMove();

        if (nextMove != null) {
            Log.d("tictactoe", "Next move found: " + nextMove);
            return nextMove;
        }

        return null;
    }

    /**
     * If the middle spot is free use that, otherwise pick first empty space.
     *
     * @return coordinates to identified space
     */
    private Pair<Integer, Integer> findNextMove() {
        if (gameBoard.get(1).get(1).equals(Piece.EMPTY)) {
            return Pair.create(1, 1);
        }

        for (int rowIndex = 0; rowIndex < gameBoard.size(); rowIndex++) {
            for (int colIndex = 0; colIndex < gameBoard.get(rowIndex).size(); colIndex++) {
                if (gameBoard.get(rowIndex).get(colIndex).equals(Piece.EMPTY)) {
                    return Pair.create(rowIndex, colIndex);
                }
            }
        }

        return null;
    }

    /**
     * Searches for a vulnerable space diagonally, horizontally and vertically.
     *
     * @return vulnerable space if found, or null if not found
     */
    private Pair<Integer, Integer> findVulnerableSpace() {
        final Pair<Integer, Integer> vulnerableDiagonalSpace = findDiagonalSpace(Piece.HUMAN_X);

        if (vulnerableDiagonalSpace != null) {
            return vulnerableDiagonalSpace;
        }

        final Pair<Integer, Integer> vulnerableHorizontalSpace = findHorizontalSpace(Piece.HUMAN_X);

        if (vulnerableHorizontalSpace != null) {
            return vulnerableHorizontalSpace;
        }

        return findVerticalSpace(Piece.HUMAN_X);
    }

    /**
     * Searches for a winning move diagonally, horizontally and vertically.
     *
     * @return winning move if found, or null if not found
     */
    private Pair<Integer, Integer> findWinningMove() {
        final Pair<Integer, Integer> diagonalWinningMove = findDiagonalSpace(currentPlayer);

        if (diagonalWinningMove != null) {
            return diagonalWinningMove;
        }

        final Pair<Integer, Integer> horizontalWinningMove = findHorizontalSpace(currentPlayer);

        if (horizontalWinningMove != null) {
            return horizontalWinningMove;
        }

        return findVerticalSpace(currentPlayer);
    }

    /**
     * Finds 2 in a row diagonally and returns position for third.
     *
     * @return coordinates identified or null
     */
    private Pair<Integer, Integer> findDiagonalSpace(final Piece pieceToCheck) {
        if (gameBoard.get(1).get(1).equals(pieceToCheck)) {
            if (gameBoard.get(0).get(0).equals(pieceToCheck) && gameBoard.get(2).get(2).equals(Piece.EMPTY)) {
                return Pair.create(2, 2);
            }

            if (gameBoard.get(0).get(2).equals(pieceToCheck) && gameBoard.get(2).get(0).equals(Piece.EMPTY)) {
                return Pair.create(2, 0);
            }

            if (gameBoard.get(2).get(2).equals(pieceToCheck) && gameBoard.get(0).get(0).equals(Piece.EMPTY)) {
                return Pair.create(0, 0);
            }

            if (gameBoard.get(2).get(0).equals(pieceToCheck) && gameBoard.get(0).get(2).equals(Piece.EMPTY)) {
                return Pair.create(0, 2);
            }
        }

        return null;
    }

    /**
     * TODO Optimise.
     * Finds 2 in a row horizontally and returns position for third.
     *
     * @return coordinates identified or null
     */
    private Pair<Integer, Integer> findHorizontalSpace(final Piece pieceToCheck) {
        // First Row
        if (gameBoard.get(0).get(0).equals(pieceToCheck) && gameBoard.get(0).get(1).equals(pieceToCheck) && gameBoard.get(0).get(2).equals(Piece.EMPTY)) {
            return Pair.create(0, 2);
        }

        // First Row
        if (gameBoard.get(0).get(0).equals(pieceToCheck) && gameBoard.get(0).get(2).equals(pieceToCheck) && gameBoard.get(0).get(1).equals(Piece.EMPTY)) {
            return Pair.create(0, 1);
        }

        // First Row
        if (gameBoard.get(0).get(1).equals(pieceToCheck) && gameBoard.get(0).get(2).equals(pieceToCheck) && gameBoard.get(0).get(0).equals(Piece.EMPTY)) {
            return Pair.create(0, 0);
        }

        // Second Row
        if (gameBoard.get(1).get(0).equals(pieceToCheck) && gameBoard.get(1).get(1).equals(pieceToCheck) && gameBoard.get(1).get(2).equals(Piece.EMPTY)) {
            return Pair.create(1, 2);
        }

        // Second Row
        if (gameBoard.get(1).get(0).equals(pieceToCheck) && gameBoard.get(1).get(2).equals(pieceToCheck) && gameBoard.get(1).get(1).equals(Piece.EMPTY)) {
            return Pair.create(1, 1);
        }

        // Second Row
        if (gameBoard.get(1).get(1).equals(pieceToCheck) && gameBoard.get(1).get(2).equals(pieceToCheck) && gameBoard.get(1).get(0).equals(Piece.EMPTY)) {
            return Pair.create(1, 0);
        }

        // Third Row
        if (gameBoard.get(2).get(0).equals(pieceToCheck) && gameBoard.get(2).get(1).equals(pieceToCheck) && gameBoard.get(2).get(2).equals(Piece.EMPTY)) {
            return Pair.create(2, 2);
        }

        // Third Row
        if (gameBoard.get(2).get(0).equals(pieceToCheck) && gameBoard.get(2).get(2).equals(pieceToCheck) && gameBoard.get(2).get(1).equals(Piece.EMPTY)) {
            return Pair.create(2, 1);
        }

        // Third Row
        if (gameBoard.get(2).get(1).equals(pieceToCheck) && gameBoard.get(2).get(2).equals(pieceToCheck) && gameBoard.get(2).get(0).equals(Piece.EMPTY)) {
            return Pair.create(2, 0);
        }

        return null;
    }

    /**
     * TODO Optimise.
     * Finds 2 in a row vertically and returns position for third.
     *
     * @return coordinates identified or null
     */
    private Pair<Integer, Integer> findVerticalSpace(final Piece pieceToCheck) {
        // First Column
        if (gameBoard.get(0).get(0).equals(pieceToCheck) && gameBoard.get(1).get(0).equals(pieceToCheck) && gameBoard.get(2).get(0).equals(Piece.EMPTY)) {
            return Pair.create(2, 0);
        }

        // First Column
        if (gameBoard.get(0).get(0).equals(pieceToCheck) && gameBoard.get(2).get(0).equals(pieceToCheck) && gameBoard.get(1).get(0).equals(Piece.EMPTY)) {
            return Pair.create(1, 0);
        }

        // First Column
        if (gameBoard.get(1).get(0).equals(pieceToCheck) && gameBoard.get(2).get(0).equals(pieceToCheck) && gameBoard.get(0).get(0).equals(Piece.EMPTY)) {
            return Pair.create(0, 0);
        }

        // Second Column
        if (gameBoard.get(0).get(1).equals(pieceToCheck) && gameBoard.get(1).get(1).equals(pieceToCheck) && gameBoard.get(2).get(1).equals(Piece.EMPTY)) {
            return Pair.create(2, 1);
        }

        // Second Column
        if (gameBoard.get(0).get(1).equals(pieceToCheck) && gameBoard.get(2).get(1).equals(pieceToCheck) && gameBoard.get(1).get(1).equals(Piece.EMPTY)) {
            return Pair.create(1, 1);
        }

        // Second Column
        if (gameBoard.get(1).get(1).equals(pieceToCheck) && gameBoard.get(2).get(1).equals(pieceToCheck) && gameBoard.get(0).get(1).equals(Piece.EMPTY)) {
            return Pair.create(0, 1);
        }

        // Third Column
        if (gameBoard.get(0).get(2).equals(pieceToCheck) && gameBoard.get(1).get(2).equals(pieceToCheck) && gameBoard.get(2).get(2).equals(Piece.EMPTY)) {
            return Pair.create(2, 2);
        }

        // Third Column
        if (gameBoard.get(0).get(2).equals(pieceToCheck) && gameBoard.get(2).get(2).equals(pieceToCheck) && gameBoard.get(1).get(1).equals(Piece.EMPTY)) {
            return Pair.create(1, 1);
        }

        // Third Column
        if (gameBoard.get(1).get(2).equals(pieceToCheck) && gameBoard.get(2).get(2).equals(pieceToCheck) && gameBoard.get(0).get(2).equals(Piece.EMPTY)) {
            return Pair.create(0, 2);
        }

        return null;
    }

    /**
     * Checks if the game is one using the coordinates of the selected piece.
     * First checks the row, then column then diagonals for matches with the current player.
     *
     * @param row the row index
     * @param col the column index
     */
    private boolean boardContainsWin(final int row, final int col) {
        if (checkRowForWin(row) || checkColumnForWin(row, col)) {
            return true;
        }

        if (currentPlayer.equals(gameBoard.get(1).get(1))) {
            if (currentPlayer.equals(gameBoard.get(0).get(0)) && currentPlayer.equals(gameBoard.get(2).get(2))) {
                return true;
            }

            if (currentPlayer.equals(gameBoard.get(2).get(0)) && currentPlayer.equals(gameBoard.get(0).get(2))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Sets the current player depending on the game mode.
     */
    private void nextPlayer() {
        if (Piece.COMPUTER_O.equals(playerTwo)) {
            currentPlayer = currentPlayer.equals(Piece.COMPUTER_O) ? Piece.HUMAN_X : Piece.COMPUTER_O;
        } else {
            currentPlayer = currentPlayer.equals(Piece.HUMAN_X) ? Piece.HUMAN_O : Piece.HUMAN_X;
        }

        setCurrentPlayerText();
    }

    /**
     * If the current player is the computer then initiate move.
     */
    private void decideIfComputerShouldMove() {
        if (Piece.COMPUTER_O.equals(currentPlayer)) {
            computerMove();
        }
    }

    /**
     * Creates a matrix of BOARD_SIZE x BOARD_SIZE and sets each value to EMPTY piece.
     */
    private void initBoard() {
        final int boardSize = 3;
        gameBoard = new Vector<>();

        for (int row = 0; row < boardSize; row++) {
            final Vector<Piece> rowPieces = new Vector<>(boardSize);

            for (int col = 0; col < boardSize; col++) {
                rowPieces.add(Piece.EMPTY);
            }

            gameBoard.add(rowPieces);
        }
    }

    /**
     * Sets the second player to either human or computer.
     * Also sets the current player based on user selections.
     */
    private void initPlayers() {
        final SwitchCompat twoPlayerSwitch = (SwitchCompat) findViewById(R.id.twoPlayerSwitch);
        playerTwo = twoPlayerSwitch.isChecked() ? Piece.HUMAN_O : Piece.COMPUTER_O;

        final SwitchCompat firstMoveSwitch = (SwitchCompat) findViewById(R.id.firstMoveSwitch);
        currentPlayer = twoPlayerSwitch.isChecked() ? Piece.HUMAN_X : firstMoveSwitch.isChecked() ? Piece.HUMAN_X : playerTwo;
    }

    /**
     * Prints the board in an easy to view format in the logs.
     *
     * E.g.
     *     EMPTY,EMPTY,EMPTY
     *     EMPTY,EMPTY,EMPTY
     *     EMPTY,EMPTY,EMPTY
     */
    private void printBoard() {
        final StringBuilder boardStringBuilder = new StringBuilder(String.format("BOARD: %s", System.lineSeparator()));

        for (int row = 0; row < gameBoard.size(); row++) {
            for (int col = 0; col < gameBoard.get(row).size(); col++) {
                final Piece piece = gameBoard.get(row).get(col);

                boardStringBuilder.append(String.format("%10s", piece));
                boardStringBuilder.append(col != 0 && col % 2 == 0 ? System.lineSeparator() : ",");
            }
        }

        Log.i("tictactoe", boardStringBuilder.toString());
    }

    /**
     * Clear text and enable all buttons.
     */
    private void initButtons(final boolean enabled) {
        for (Button button : getAllButtons()) {
            button.setText(Piece.EMPTY.displayValue);
            button.setEnabled(enabled);
        }
    }

    /**
     * Checks for a win on the given row.
     *
     * @param row the row index
     * @return true if all items in the row belong to the current player
     */
    private boolean checkRowForWin(final int row) {
        for (final Piece pieceToCheck : gameBoard.get(row)) {
            if (!pieceToCheck.equals(currentPlayer)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks for a win on the given column
     *
     * @param row the row index
     * @param col the column index
     * @return true if all items in the column belong to the current player
     */
    private boolean checkColumnForWin(final int row, final int col) {
        for (int rowIndex = 0; rowIndex < gameBoard.get(row).size(); rowIndex++) {
            if (!gameBoard.get(rowIndex).get(col).equals(currentPlayer)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Ends game by disabling buttons and declaring the winner.
     */
    private void endGame() {
        disableAllButtons();

        final TextView currentPlayerTextView = (TextView) findViewById(R.id.currentPlayerTextView);
        currentPlayerTextView.setText(String.format("Player %s wins!", this.currentPlayer.displayValue));
        printBoard();
    }

    /**
     * Ends game by disabling buttons and declaring a draw.
     */
    private void drawGame() {
        disableAllButtons();

        final TextView currentPlayerTextView = (TextView) findViewById(R.id.currentPlayerTextView);
        currentPlayerTextView.setText(R.string.draw);
        printBoard();
    }

    /**
     * Disable all the buttons.
     */
    private void disableAllButtons() {
        for (Button button : getAllButtons()) {
            button.setEnabled(false);
        }
    }

    /**
     * Gets the playable buttons by dynamic ID from matrix positions.
     *
     * @return the array of playable buttons
     */
    public List<Button> getAllButtons() {
        final ArrayList<Button> allButtons = new ArrayList<>();

        for (int row = 0; row < gameBoard.size(); row++) {
            for (int col = 0; col < gameBoard.get(row).size(); col++) {
                final String buttonId = String.format("tile%s%s", row, col);
                final Button button = (Button) findViewById(getResources().getIdentifier(buttonId, "id", getPackageName()));
                allButtons.add(button);
            }
        }

        return allButtons;
    }

    /**
     * Sets the text of the selected button to the display value
     * for the current player.
     *
     * @param button the selected button
     */
    private void markButtonTaken(final Button button) {
        button.setText(currentPlayer.displayValue);
        button.setEnabled(false);
    }

    /**
     * Updates the vector with the position selected.
     *
     * @param row the matrix row position
     * @param col the matrix column position
     */
    private void updateGameBoard(final int row, final int col) {
        gameBoard.get(row).set(col, currentPlayer);
    }

    /**
     * Handle start or reset button clicks.
     *
     * @param view the start and reset button widget
     */
    public void handleStartOrReset(final View view) {
        if (gameStarted) {
            gameStarted = false;
            init();
        } else {
            gameStarted = true;
            startGame();
        }
    }

    /**
     * Handle two player switch changes.
     *
     * @param view the two player switch widget
     */
    public void twoPlayerSwitched(final View view) {
        final SwitchCompat twoPlayerSwitch = (SwitchCompat) findViewById(R.id.twoPlayerSwitch);
        final SwitchCompat firstMoveSwitch = (SwitchCompat) findViewById(R.id.firstMoveSwitch);

        twoPlayerSwitch.setText(twoPlayerSwitch.isChecked() ? R.string.two_player : R.string.one_player);
        firstMoveSwitch.setEnabled(!twoPlayerSwitch.isChecked());
    }

    /**
     * Enables the two player option switch and if not checked will
     * also enable the first move switch.
     */
    private void enableSwitches() {
        final SwitchCompat twoPlayerSwitch = (SwitchCompat) findViewById(R.id.twoPlayerSwitch);
        twoPlayerSwitch.setEnabled(true);

        if (!twoPlayerSwitch.isChecked()) {
            final SwitchCompat firstMoveSwitch = (SwitchCompat) findViewById(R.id.firstMoveSwitch);
            firstMoveSwitch.setEnabled(true);
        }
    }

    /**
     * Disables both switches.
     */
    private void disableSwitches() {
        final SwitchCompat twoPlayerSwitch = (SwitchCompat) findViewById(R.id.twoPlayerSwitch);
        twoPlayerSwitch.setEnabled(false);

        final SwitchCompat firstMoveSwitch = (SwitchCompat) findViewById(R.id.firstMoveSwitch);
        firstMoveSwitch.setEnabled(false);
    }

    /**
     * Sets start button text to 'Start'
     */
    private void setStartText() {
        final Button startResetButton = (Button) findViewById(R.id.startResetButton);
        startResetButton.setText(R.string.start);
    }

    /**
     * Sets start button text to 'Restart'
     */
    private void setRestartText() {
        final Button startResetButton = (Button) findViewById(R.id.startResetButton);
        startResetButton.setText(R.string.restart);
    }

    /**
     * Sets current player text to the current player.
     */
    private void setCurrentPlayerText() {
        final TextView currentPlayerTextView = (TextView) findViewById(R.id.currentPlayerTextView);
        currentPlayerTextView.setText(String.format("Current Player: %s", this.currentPlayer.displayValue));
    }

    /**
     * Removes the current player text.
     */
    private void resetCurrentPlayerText() {
        final TextView currentPlayerTextView = (TextView) findViewById(R.id.currentPlayerTextView);
        currentPlayerTextView.setText("");
    }
}