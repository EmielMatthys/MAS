package experiment;

import Test.SimpleAgent;
import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.Model;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.experiment.Experiment;
import com.github.rinde.rinsim.experiment.PostProcessor;
import Test.Package;
import com.github.rinde.rinsim.pdptw.common.StatsTracker;
import sun.java2d.pipe.SpanShapeRenderer;

import java.io.*;
import java.util.List;
import java.util.Set;


public class ExamplePostProcessor implements PostProcessor<String> {


    ExamplePostProcessor() {}

    @Override
    public String collectResults(Simulator sim, Experiment.SimArgs args) {
        try {
            FileReader fr = new FileReader("pickuptimes.txt");
            BufferedReader reader = new BufferedReader(fr);

            FileWriter fw = new FileWriter("pickuptimes.txt");
            BufferedWriter writer = new BufferedWriter((fw));
            List<Model<?>> models = sim.getModels().asList();

            while (reader.readLine() != null) { }




        } catch (IOException e) {

        }

        Set<Model<?>> models = sim.getModels();

        long delT = 0;
        long pickT = 0;

        for (Model<?> model : models ) {
            if (model instanceof StatsTracker) {
                delT = ((StatsTracker) model).getStatistics().deliveryTardiness;
                pickT = ((StatsTracker) model).getStatistics().pickupTardiness;
            }
        }

        final Set<SimpleAgent> vehicles = sim.getModelProvider()
                .getModel(RoadModel.class).getObjectsOfType(SimpleAgent.class);

        final Set<Package> packages = sim.getModelProvider()
                .getModel(RoadModel.class).getObjectsOfType(Package.class);

        long avgTardiness = 0;

        for (SimpleAgent vehicle: vehicles) {
            avgTardiness = avgTardiness + vehicle.tardiness;
        }

        avgTardiness = avgTardiness / 20;



        // Construct a result string based on the simulator state, of course, in
        // actual code the result should not be a string but a value object
        // containing the values of interest.
        final StringBuilder sb = new StringBuilder();
        //sb.append("Average Tardiness: ").append(avgTardiness);
        sb.append("DeliveryTardiness: ").append(delT);
        sb.append(" PickupTardiness: ").append(pickT);

        return sb.toString();}

    @Override
    public FailureStrategy handleFailure(Exception e, Simulator sim, Experiment.SimArgs args) {
        return FailureStrategy.ABORT_EXPERIMENT_RUN;
    }
}
