package graph;


import com.github.rinde.rinsim.geom.*;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import sun.security.util.Length;

import java.util.Map;

public class GraphCreator {
    static final int LEFT_CENTER_U_ROW = 4;
    static final int LEFT_CENTER_L_ROW = 5;
    static final int LEFT_COL = 4;
    static final int RIGHT_CENTER_U_ROW = 2;
    static final int RIGHT_CENTER_L_ROW = 4;
    static final int RIGHT_COL = 0;

    static final int X_RIGHT = 44;
    static final int Y_LOWER = 80;

    private static final double VEHICLE_LENGTH = 2d;

    GraphCreator() {}

    static ImmutableTable<Integer, Integer, Point> createMatrix(int cols,
                                                                int rows, Point offset) {
        final ImmutableTable.Builder<Integer, Integer, Point> builder =
                ImmutableTable.builder();
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                builder.put(r, c, new Point(
                        offset.x + c * VEHICLE_LENGTH * 2,
                        offset.y + r * VEHICLE_LENGTH * 2));
            }
        }
        return builder.build();
    }

    static ListenableGraph<LengthData> createGraph() {
        final Graph<LengthData> g = new TableGraph<>();

        final Table<Integer, Integer, Point> leftMatrix = createMatrix(5, 10,
                new Point(0, 0));
        for (final Map<Integer, Point> column : leftMatrix.columnMap().values()) {
            Graphs.addBiPath(g, column.values());
        }
        Graphs.addBiPath(g, leftMatrix.row(LEFT_CENTER_U_ROW).values());
        Graphs.addBiPath(g, leftMatrix.row(LEFT_CENTER_L_ROW).values());

        final Table<Integer, Integer, Point> rightMatrix = createMatrix(10, 7,
                new Point(30, 6));
        for (final Map<Integer, Point> row : rightMatrix.rowMap().values()) {
            Graphs.addBiPath(g, row.values());
        }
        Graphs.addBiPath(g, rightMatrix.column(0).values());
        Graphs.addBiPath(g, rightMatrix.column(rightMatrix.columnKeySet().size()
                - 1).values());

        Graphs.addPath(g,
                rightMatrix.get(RIGHT_CENTER_U_ROW, RIGHT_COL),
                leftMatrix.get(LEFT_CENTER_U_ROW, LEFT_COL));
        Graphs.addPath(g,
                leftMatrix.get(LEFT_CENTER_L_ROW, LEFT_COL),
                rightMatrix.get(RIGHT_CENTER_L_ROW, RIGHT_COL));

        return new ListenableGraph<>(g);
    }

    public static ListenableGraph<LengthData> createSmallGraph() {
        return new ListenableGraph<>(createPart(new Point(0, 0)));
    }

    public static ListenableGraph<LengthData> createLargeGraph() {
        final Graph<LengthData> g = new TableGraph<>();
        final Graph<LengthData> upperLeft = createPart(new Point(0, 0));
        final Graph<LengthData> upperRight = createPart(new Point (X_RIGHT, 0));
        final Graph<LengthData> lowerLeft = createPart(new Point(0, Y_LOWER));
        final Graph<LengthData> lowerRight = createPart(new Point(X_RIGHT, Y_LOWER));
        g.merge(upperLeft);
        g.merge(upperRight);
        g.merge(lowerLeft);
        g.merge(lowerRight);

        //Connect upper and lower
        // Left
        Graphs.addPath(g, new Point(0, 76), new Point(0, Y_LOWER));
        Graphs.addPath(g, new Point(16, Y_LOWER), new Point(16, 76));
        //Right
        Graphs.addPath(g, new Point(X_RIGHT, 76), new Point(X_RIGHT, Y_LOWER));
        Graphs.addPath(g, new Point(16+X_RIGHT, Y_LOWER), new Point(16+X_RIGHT, 76));
        //Connect right and left
        // Upper
        Graphs.addBiPath(g, new Point(40, 0), new Point(X_RIGHT, 0));
        Graphs.addBiPath(g, new Point(40, 36), new Point(X_RIGHT, 36));
        Graphs.addBiPath(g, new Point(40, 44), new Point (X_RIGHT, 44));
        Graphs.addBiPath(g, new Point(40, 76), new Point(X_RIGHT, 76));
        // Lower
        Graphs.addBiPath(g, new Point(40, 0 + Y_LOWER), new Point(X_RIGHT, 0 + Y_LOWER));
        Graphs.addBiPath(g, new Point(40, 36 + Y_LOWER), new Point(X_RIGHT, 36 + Y_LOWER));
        Graphs.addBiPath(g, new Point(40, 44 + Y_LOWER), new Point (X_RIGHT, 44 + Y_LOWER));
        Graphs.addBiPath(g, new Point(40, 76 + Y_LOWER), new Point(X_RIGHT, 76 + Y_LOWER));

        return new ListenableGraph<LengthData>(g);
    }

    public static Graph<LengthData> createPart(Point p) {
        final Graph<LengthData> g = new TableGraph<>();
        //UPPERLEFTMATRIX
        final Table<Integer, Integer, Point> upperLeftMatrix = createMatrix(5, 10,
                new Point(p.x, p.y));

        for (final Map<Integer, Point> column : upperLeftMatrix.columnMap().values()) {
            Graphs.addBiPath(g, column.values());
        }

        Graphs.addBiPath(g, upperLeftMatrix.row(RIGHT_COL).values());
        Graphs.addBiPath(g, upperLeftMatrix.row(9).values());

        //LOWERLEFTMATRIX
        final Table<Integer, Integer, Point> lowerLeftMatrix = createMatrix(5, 9,
                new Point(p.x, p.y + 44));

        for (final Map<Integer, Point> column : lowerLeftMatrix.columnMap().values()) {
            Graphs.addBiPath(g, column.values());
        }

        Graphs.addBiPath(g, lowerLeftMatrix.row(0).values());
        Graphs.addBiPath(g, lowerLeftMatrix.row(8).values());


        //UPPERRIGHTMATRIX
        final Table<Integer, Integer, Point> rightMatrix = createMatrix(5, 20,
                new Point(p.x + 24, p.y));

        for (final Map<Integer, Point> column : rightMatrix.columnMap().values()) {
            Graphs.addBiPath(g, column.values());
        }

        Graphs.addBiPath(g, rightMatrix.row(11).values());
        Graphs.addBiPath(g, rightMatrix.row(19).values());

        Graphs.addBiPath(g, rightMatrix.row(RIGHT_COL).values());
        Graphs.addBiPath(g, rightMatrix.row(9).values());
        //FROM RIGHT TO LEFT
        Graphs.addPath(g,
                rightMatrix.get(0, 0),
                upperLeftMatrix.get(0, 4));
        //FROM LEFT TO RIGHT
        Graphs.addPath(g,
                upperLeftMatrix.get(9, 4),
                rightMatrix.get(7, 0));
        //FROM UPPER TO LOWER
        Graphs.addPath(g,
                upperLeftMatrix.get(9, 0),
                lowerLeftMatrix.get(0, 0));
        //FROM LOWER LEFT TO RIGHT
        Graphs.addPath(g,
                lowerLeftMatrix.get(7, 4),
                rightMatrix.get(19, 0));

        Graphs.addPath(g,
                rightMatrix.get(11, 0),
                lowerLeftMatrix.get(0, 4));
        return g;
    }
}