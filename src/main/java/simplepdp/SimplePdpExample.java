package simplepdp;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.*;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.github.rinde.rinsim.ui.renderers.WarehouseRenderer;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.commons.math3.random.RandomGenerator;

import javax.measure.unit.SI;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public class SimplePdpExample {

    private static final double VEHICLE_LENGTH = 2d;
    private static final int NUM_PARCELS = 2;
    private static final long SERVICE_DURATION = 60000;
    private static final double PARCEL_CAPACITY = 1;
    private static final double NEW_PARCEL_PROB = 0.007;
    private static final int NUM_TRUCKS = 2;

    public static void main(String[] args) {
        run();
    }

    public static Simulator run() {

        View.Builder view = View.builder()
                .with(WarehouseRenderer.builder()
                        .withOneWayStreetArrows()
                        .withNodes())
                .with(RoadUserRenderer.builder()
                        .withImageAssociation(
                                Parcel.class, "/graphics/perspective/deliverypackage3.png")
                        .withImageAssociation(
                                AGV.class, "/graphics/flat/forklift2.png")
                )
                .withTitleAppendix("Simple PDP example");
        //TODO: construct renderer for embarking/disembarking and stuff

        final Simulator simulator = Simulator.builder()
            .addModel(RoadModelBuilders.dynamicGraph(GraphCreator.createSimpleGraph())
                    .withCollisionAvoidance()
                    .withDistanceUnit(SI.METER)
                    .withVehicleLength(VEHICLE_LENGTH))
            .addModel(DefaultPDPModel.builder())
            .addModel(view)
            .build();

        final RandomGenerator rng = simulator.getRandomGenerator();
        final RoadModel roadModel = simulator.getModelProvider().getModel(RoadModel.class);

        for(int i = 0; i < NUM_PARCELS; i++){
            simulator.register(new Parcel(Parcel.builder(roadModel.getRandomPosition(rng),
                roadModel.getRandomPosition(rng))
                .serviceDuration(SERVICE_DURATION)
                .neededCapacity(PARCEL_CAPACITY)
                .deliveryDuration(2)
                .pickupDuration(2)
                .buildDTO()));
        }

        for(int i = 0; i < NUM_TRUCKS; i++){
            simulator.register(new AGV(roadModel.getRandomPosition(rng)));
        }

        simulator.addTickListener(new TickListener() {
            @Override
            public void tick(TimeLapse timeLapse) {
                if(rng.nextDouble() < NEW_PARCEL_PROB){
                    simulator.register(new Parcel(Parcel.builder(roadModel.getRandomPosition(rng), roadModel.getRandomPosition(rng))
                    .serviceDuration(SERVICE_DURATION)
                    .neededCapacity(PARCEL_CAPACITY)
                    .buildDTO()));
                }
            }

            @Override
            public void afterTick(TimeLapse timeLapse) {}
        });

        simulator.start();
        return simulator;
    }


    static class GraphCreator {
        static final int LEFT_CENTER_U_ROW = 4;
        static final int LEFT_CENTER_L_ROW = 5;
        static final int LEFT_COL = 4;
        static final int RIGHT_CENTER_U_ROW = 2;
        static final int RIGHT_CENTER_L_ROW = 4;
        static final int RIGHT_COL = 0;

        GraphCreator() {
        }

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

        static ListenableGraph<LengthData> createSimpleGraph() {
            final Graph<LengthData> g = new TableGraph<>();

            final Table<Integer, Integer, Point> matrix = createMatrix(8, 6,
                    new Point(0, 0));

            for (int i = 0; i < matrix.columnMap().size(); i++) {

                final Iterable<Point> path;
                if (i % 2 == 0) {
                    path = Lists.reverse(newArrayList(matrix.column(i).values()));
                } else {
                    path = matrix.column(i).values();
                }
                Graphs.addPath(g, path);
            }

            Graphs.addPath(g, matrix.row(0).values());
            Graphs.addPath(g, Lists.reverse(newArrayList(matrix.row(
                    matrix.rowKeySet().size() - 1).values())));

            return new ListenableGraph<>(g);
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
    }
}