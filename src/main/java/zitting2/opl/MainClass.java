package zitting2.opl;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.road.PlaneRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.CommRenderer;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;

public class MainClass {

    private static final int NUM_AGENTS = 5;


    public static void main(String[] args) {
        View.Builder viewBuilder = View.builder()
                .withTitleAppendix("Oefenzitting 2")
                .with(PlaneRoadModelRenderer.builder())
                .with(CommRenderer.builder()
                        .withReliabilityColors()
                        .withMessageCount());

        final Simulator sim = Simulator.builder()
                .addModel(RoadModelBuilders.plane())
                .addModel(CommModel.builder())
                .addModel(DefaultPDPModel.builder())
                .addModel(viewBuilder)
                .build();

        RoadModel roadModel = sim.getModelProvider().getModel(PlaneRoadModel.class);

        for (int i = 0; i < NUM_AGENTS; i++) {

            sim.register(
                    new Agent(sim.getRandomGenerator(), roadModel.getRandomPosition(sim.getRandomGenerator())));
        }
        sim.start();
    }
}
