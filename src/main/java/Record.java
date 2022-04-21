import java.util.HashMap;

// This class is from here - https://github.com/Mentathiel/KMeansJava
public class Record {
    HashMap<String, Double> record;
    Integer clusterNo;

    public Record(HashMap<String, Double> record){
        this.record = record;
    }

    public void setClusterNo(Integer clusterNo) {
        this.clusterNo = clusterNo;
    }

    public HashMap<String, Double> getRecord() {
        return record;
    }
}
