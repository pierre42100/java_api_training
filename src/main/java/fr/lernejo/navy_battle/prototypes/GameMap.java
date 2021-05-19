package fr.lernejo.navy_battle.prototypes;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GameMap {
    private final GameCell[][] map = new GameCell[10][10];

    public GameMap() {
        for (GameCell[] gameCells : map) {
            Arrays.fill(gameCells, GameCell.EMPTY);
        }

        printMap();
    }

    public GameCell getCell(int x, int y) {
        if (x >= 10 || y >= 10)
            throw new RuntimeException("Invalidate coordinates!");

        return map[x][y];
    }


    public void addBoat(int length, int x, int y, BoatOrientation orientation) {
        while (length > 0) {
            if (getCell(x, y) != GameCell.EMPTY)
                throw new RuntimeException("Cannot add a boat at the location (" + x + ";" + y + ") : the cell is busy!");

            map[x][y] = GameCell.BOAT;
            length--;

            switch (orientation) {
                case HORIZONTAL -> x++;
                case VERTICAL -> y++;
            }
        }
    }

    public void printMap() {
        System.out.println(" .... ");
        for (GameCell[] row : map) {
            System.out.println(Arrays.stream(row).map(GameCell::getLetter).collect(Collectors.joining(" ")));
        }
        System.out.println(" .... ");
    }
}
