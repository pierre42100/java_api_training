package fr.lernejo.navy_battle.prototypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

public class GameMap {
    private final Integer[] BOATS = {5, 4, 3, 3, 2};
    private final GameCell[][] map = new GameCell[10][10];

    public GameMap() {
        for (GameCell[] gameCells : map) {
            Arrays.fill(gameCells, GameCell.EMPTY);
        }

        // Fill the map
        buildMap();

        printMap();
    }

    public int getHeight() {
        return map[0].length;
    }

    public int getWidth() {
        return map.length;
    }

    private void buildMap() {
        var random = new Random();
        var boats = new ArrayList<>(Arrays.asList(BOATS));
        Collections.shuffle(boats);

        while (!boats.isEmpty()) {
            int boat = boats.get(0);

            int x = Math.abs(random.nextInt()) % getWidth();
            int y = Math.abs(random.nextInt()) % getHeight();
            var orientation = random.nextBoolean() ? BoatOrientation.HORIZONTAL : BoatOrientation.VERTICAL;

            if (!canFit(boat, x, y, orientation))
                continue;

            addBoat(boat, x, y, orientation);
            boats.remove(0);
        }

    }

    private boolean canFit(int length, int x, int y, BoatOrientation orientation) {
        if (x >= getWidth() || y >= getHeight() || getCell(x, y) != GameCell.EMPTY)
            return false;

        if (length == 0)
            return true;

        return switch (orientation) {
            case HORIZONTAL -> canFit(length - 1, x + 1, y, orientation);
            case VERTICAL -> canFit(length - 1, x, y + 1, orientation);
        };
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
