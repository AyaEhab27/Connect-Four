/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connect.four;
import java.io.Serializable;

public class GameMessage implements Serializable {
    private MessageType type;
    private int player;
    private int row;
    private int column;

    public GameMessage(MessageType type, int player, int row, int column) {
        this.type = type;
        this.player = player;
        this.row = row;
        this.column = column;
    }

    public MessageType getType() {
        return type;
    }

    public int getPlayer() {
        return player;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}
