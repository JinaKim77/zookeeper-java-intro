import org.apache.zookeeper.WatchedEvent;

public class LeaderElection {

    private boolean leader = false;

    public static void main(String[] args) {
    }

    public void connectToZookeeper() {
    }

    public void volunteerForLeadership() {
    }

    public void electLeader() {
    }

    public void run() {
    }

    public void close(){
    }

//    @Override
    public void process(WatchedEvent watchedEvent) {

    }

    //******************************
    // Needed for tests, don't edit
    //*******************************

    public boolean isLeader() {
        return leader;
    }
}
