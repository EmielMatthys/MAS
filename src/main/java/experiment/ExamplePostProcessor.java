package experiment;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.Model;
import com.github.rinde.rinsim.experiment.Experiment;
import com.github.rinde.rinsim.experiment.PostProcessor;
import com.github.rinde.rinsim.pdptw.common.StatsTracker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class ExamplePostProcessor implements PostProcessor<String> {


    private int NUM_AGVS;

    ExamplePostProcessor(int agv) {
        this.NUM_AGVS = agv;
    }

    @Override
    public String collectResults(Simulator sim, Experiment.SimArgs args) {
        long delT = 0;
        long pickT = 0;

        List<Model<?>> models = sim.getModels().asList();
        for (Model<?> model : models ) {
            if (model instanceof StatsTracker) {
                delT = ((StatsTracker) model).getStatistics().deliveryTardiness;
                pickT = ((StatsTracker) model).getStatistics().pickupTardiness;
            }
        }

        try {

            FileWriter fwPick = new FileWriter("pickuptimes_" + NUM_AGVS + ".txt", true);
            BufferedWriter writerPick = new BufferedWriter(fwPick);
            FileWriter fwDel = new FileWriter("deliverytimes_" + NUM_AGVS +".txt", true);
            BufferedWriter writerDel = new BufferedWriter(fwDel);


            final StringBuilder sbPick = new StringBuilder();
            sbPick.append(delT);

            writerPick.write(sbPick.toString());
            writerPick.newLine();

            final StringBuilder sbDel = new StringBuilder();
            sbDel.append(pickT);
            writerDel.newLine();



            writerPick.close();
            fwPick.close();
            writerDel.close();
            fwDel.close();

        } catch (IOException e) {

        }


        // Construct a result string based on the simulator state, of course, in
        // actual code the result should not be a string but a value object
        // containing the values of interest.
        final StringBuilder sb = new StringBuilder();
        sb.append("DeliveryTardiness: ").append(delT);
        sb.append(" PickupTardiness: ").append(pickT);

        return sb.toString();}

    @Override
    public FailureStrategy handleFailure(Exception e, Simulator sim, Experiment.SimArgs args) {
        return FailureStrategy.ABORT_EXPERIMENT_RUN;
    }
}
