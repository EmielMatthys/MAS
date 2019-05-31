import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import delegate.DelegateExample;
import experiment.ExperimentExample;
import simple.AGVExample;
import simple.SimpleAgent;

public class Main {

    @Parameter(names = {"--test", "-t"})
    private boolean runTests;

    @Parameter(names = {"--big", "-b"})
    private boolean bigMap;

    public static void main(String[] args) {
        Main main = new Main();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);
        main.run();
    }

    private void run() {

        if(runTests){
            ExperimentExample.run(false, false);
            ExperimentExample.run(bigMap, true);
        }
        else
        {
            AGVExample.run(false, bigMap);
            DelegateExample.run(bigMap);
        }
    }
}
