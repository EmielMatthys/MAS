package experiment;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.experiment.Experiment;
import com.github.rinde.rinsim.experiment.PostProcessor;

import java.util.Set;

public class ExamplePostProcessor implements PostProcessor<String> {


    ExamplePostProcessor() {}

    @Override
    public String collectResults(Simulator sim, Experiment.SimArgs args) {
        final Set<Vehicle> vehicles = sim.getModelProvider()
                .getModel(RoadModel.class).getObjectsOfType(Vehicle.class);

        // Construct a result string based on the simulator state, of course, in
        // actual code the result should not be a string but a value object
        // containing the values of interest.
        final StringBuilder sb = new StringBuilder();
        if (vehicles.isEmpty()) {
            sb.append("No vehicles were added");
        } else {
            sb.append(vehicles.size()).append(" vehicles were added");
        }

        if (sim.getCurrentTime() >= args.getScenario().getTimeWindow().end()) {
            sb.append(", simulation has completed.");
        } else {
            sb.append(", simulation was stopped prematurely.");
        }
        return sb.toString();}

    @Override
    public FailureStrategy handleFailure(Exception e, Simulator sim, Experiment.SimArgs args) {
        return FailureStrategy.ABORT_EXPERIMENT_RUN;
    }
}
