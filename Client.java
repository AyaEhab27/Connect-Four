/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connect.four;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javafx.event.ActionEvent;

public class Client extends Application {

    private static final int ROWS = 6;
    private static final int COLUMNS = 7;
    private static final int CELL_SIZE = 80;
    private static final int RADIUS = 30;

    private Circle[][] cells;
    private Button[] buttons;
    private int[][] board;
    private int currentPlayer;
    private boolean gameOver;

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public void start(Stage primaryStage) {
        cells = new Circle[ROWS][COLUMNS];
        buttons = new Button[COLUMNS];
        board = new int[ROWS][COLUMNS];
        currentPlayer = 1;
        gameOver = false;

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                Rectangle rectangle = new Rectangle(CELL_SIZE, CELL_SIZE);
                rectangle.setFill(Color.BLUE);
                Circle circle = new Circle(RADIUS);
                circle.setFill(Color.WHITE);
                cells[row][col] = circle;
                int c=col;

                Pane cellPane = new Pane(rectangle, circle);
                cellPane.setOnMouseClicked(e -> {
                    if (!gameOver) {
            for (int ro = ROWS - 1; ro>= 0; ro--) {
                if (board[ro][c] == 0) {
                    updateCell(ro, c);
                    checkForWin(ro, c);
                    switchPlayer();
                    sendMoveMessage(ro, c);
                    break;
                }
            }
        }
                        
                        });
                GridPane.setHalignment(cellPane, HPos.CENTER);
                gridPane.add(cellPane, col, row);
            }
        }

        for (int col = 0; col < COLUMNS; col++) {
            Button button = new Button("Drop");
            int co=col;
            button.setOnAction((ActionEvent e )-> {
                if (!gameOver) {
            for (int row = ROWS - 1; row >= 0; row--) {
                if (board[row][co] == 0) {
                    updateCell(row, co);
                    checkForWin(row, co);
                    switchPlayer();
                    sendMoveMessage(row, co);
                    break;
                }
            }
        }
                
                    });
            buttons[col] = button;
            gridPane.add(button, col, ROWS);
        }

        primaryStage.setScene(new Scene(gridPane));
        primaryStage.setTitle("Connect Four - Client");
        primaryStage.show();

        connectToServer();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("192.168.1.7", 12345);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            Thread receivingThread = new Thread(() -> {
                try {
                    while (true) {
                        Object receivedObject = inputStream.readObject();
                        if (receivedObject instanceof GameMessage) {
                            handleGameMessage((GameMessage) receivedObject);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });

            receivingThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGameMessage(GameMessage message) {
        Platform.runLater(() -> {
            if (message.getType() == MessageType.MOVE) {
                int row = message.getRow();
                int col = message.getColumn();
                updateCell(row, col);
                checkForWin(row, col);
                switchPlayer();
            } else if (message.getType() == MessageType.WIN) {
                int player = message.getPlayer();
                announceWinner(player);
            } else if (message.getType() == MessageType.DRAW) {
                announceDraw();
            }
        });
    }

    private void handleCellClick(int col) {
        if (!gameOver) {
            for (int row = ROWS - 1; row >= 0; row--) {
                if (board[row][col] == 0) {
                    updateCell(row, col);
                    checkForWin(row, col);
                    switchPlayer();
                    sendMoveMessage(row, col);
                    break;
                }
            }
        }
    }

    private void handleDropButton(int col) {
        handleCellClick(col);
    }

    private void updateCell(int row, int col) {
        int player = currentPlayer;
        board[row][col] = player;
        cells[row][col].setFill(player == 1 ? Color.RED : Color.YELLOW);
    }

    private void checkForWin(int row, int col) {
        int player = board[row][col];

        // Check horizontal
        int count = 0;
        for (int c = 0; c < COLUMNS; c++) {
            if (board[row][c] == player) {
                count++;
                if (count == 4) {
                    announceWinner(player);
                    return;
                }
            } else {
                count = 0;
            }
        }

        // Check vertical
        count = 0;
        for (int r = 0; r < ROWS; r++) {
            if (board[r][col] == player) {
                count++;
                if (count == 4) {
                    announceWinner(player);
                    return;
                }
            } else {
                count = 0;
            }
        }

        // Check diagonal (top-left to bottom-right)
        count = 0;
        for (int offset = -3; offset <= 3; offset++) {
            int r = row + offset;
            int c = col + offset;
            if (r >= 0 && r < ROWS && c >= 0 && c < COLUMNS && board[r][c] == player) {
                count++;
                if (count == 4) {
                    announceWinner(player);
                    return;
                }
            } else {
                count = 0;
            }
        }

        // Check diagonal (top-right to bottom-left)
        count = 0;
        for (int offset = -3; offset <= 3; offset++) {
            int r = row - offset;
            int c = col + offset;
            if (r >= 0 && r < ROWS && c >= 0 && c < COLUMNS && board[r][c] == player) {
                count++;
                if (count == 4) {
                    announceWinner(player);
                    return;
                }
            } else {
                count = 0;
            }
        }

        // Check for draw
        boolean draw = true;
        for (int c = 0; c < COLUMNS; c++) {
            if (board[0][c] == 0) {
                draw = false;
                break;
            }
        }
        if (draw) {
            announceDraw();
        }
    }

    private void switchPlayer() {
        currentPlayer = currentPlayer == 1 ? 2 : 1;
    }

    private void announceWinner(int player) {
        gameOver = true;
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Connect Four");
        alert.setHeaderText(null);
        alert.setContentText("Player " + player + " wins!");
        alert.showAndWait();
    }

    private void announceDraw() {
        gameOver = true;
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Connect Four");
        alert.setHeaderText(null);
        alert.setContentText("It's a draw!");
        alert.showAndWait();
    }

    private void sendMoveMessage(int row, int col) {
        try {
            GameMessage message = new GameMessage(MessageType.MOVE, currentPlayer, row, col);
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
