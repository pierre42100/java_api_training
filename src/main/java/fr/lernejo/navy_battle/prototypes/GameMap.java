package fr.lernejo.navy_battle.prototypes;

import java.util.*;
import java.util.stream.Collectors;

public class GameMap {
    private final Integer[] BOATS = {5, 4, 3, 3, 2};
    private final GameCell[][] map = new GameCell[10][10];
    private final List<List<Coordinates>> boats = new ArrayList<>();

    private final List<Coordinates> positionsToTest = new ArrayList<>();

    public GameMap(boolean fill) {
        for (GameCell[] gameCells : map) {
            Arrays.fill(gameCells, GameCell.EMPTY);
        }

        // Fill the map
        if (fill) {
            buildMap();
            printMap();
        }
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

    public GameCell getCell(Coordinates coordinates) {
        return getCell(coordinates.getX(), coordinates.getY());
    }

    public GameCell getCell(int x, int y) {
        if (x >= 10 || y >= 10)
            throw new RuntimeException("Invalidate coordinates!");

        return map[x][y];
    }

    public void setCell(Coordinates coordinates, GameCell newStatus) {
        map[coordinates.getX()][coordinates.getY()] = newStatus;

        if (newStatus == GameCell.SUCCESSFUL_FIRE) {
            positionsToTest.addAll(List.of(
                coordinates.plus(-1, 0),
                coordinates.plus(0, -1),
                coordinates.plus(1, 0),
                coordinates.plus(0, 1)
            ));
        }
    }


    public void addBoat(int length, int x, int y, BoatOrientation orientation) {
        var coordinates = new ArrayList<Coordinates>();

        while (length > 0) {

            map[x][y] = GameCell.BOAT;
            length--;

            coordinates.add(new Coordinates(x, y));

            switch (orientation) {
                case HORIZONTAL -> x++;
                case VERTICAL -> y++;
            }
        }

        boats.add(coordinates);
    }

    public void printMap() {
        System.out.println(" .... ");
        for (GameCell[] row : map) {
            System.out.println(Arrays.stream(row).map(GameCell::getLetter).collect(Collectors.joining(" ")));
        }
        System.out.println(" .... ");
    }

    public boolean hasShipLeft() {
        for (var row : map) {
            if (Arrays.stream(row).anyMatch(s -> s == GameCell.BOAT))
                return true;
        }
        return false;
    }

    public Coordinates getNextPlaceToHit() {
        Coordinates coordinates = null;
        if (positionsToTest.size() > 0) {
            coordinates = fireAroundSuccessfulHit();
        }

        if (coordinates == null)
            coordinates = lightHit();

        if (coordinates == null)
            coordinates = bruteForceHit();

        return coordinates;
    }

    private Coordinates fireAroundSuccessfulHit() {
        while(positionsToTest.size() > 0) {
            var pos = positionsToTest.remove(0);

            if (getCell(pos) == GameCell.EMPTY)
                return pos;
        }

        return null;
    }

    private Coordinates lightHit() {
        for (int i = 0; i < getWidth(); i++) {
            for (int j = i % 2; j < getHeight(); j += 2) {
                if (getCell(i, j) == GameCell.EMPTY)
                    return new Coordinates(i, j);
            }
        }

        return null;
    }

    private Coordinates bruteForceHit() {
        System.err.println("Brute force required!");
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                if (getCell(i, j) == GameCell.EMPTY)
                    return new Coordinates(i, j);
            }
        }

        throw new RuntimeException("The other player is a cheater and / or a liar!");
    }

    public FireResult hit(Coordinates coordinates) {
        if (getCell(coordinates) != GameCell.BOAT)
            return FireResult.MISS;

        var first = boats.stream().filter(s -> s.contains(coordinates)).findFirst();
        assert (first.isPresent());
        first.get().remove(coordinates);

        setCell(coordinates, GameCell.SUCCESSFUL_FIRE);

        return first.get().isEmpty() ? FireResult.SUNK : FireResult.HIT;
    }

}
