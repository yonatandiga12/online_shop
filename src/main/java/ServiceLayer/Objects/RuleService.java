package ServiceLayer.Objects;

public class RuleService {



    private int id;
    private String info;

    public RuleService(String info){

        String[] splitInfo = info.split(":");
        this.id = Integer.parseInt(splitInfo[0]);

        int index = 1;
        this.info = "";
        while(index < splitInfo.length){
            this.info += splitInfo[index];
            index++;
        }
    }

    public int getId() {
        return id;
    }

    public String getInfo() {
        return info;
    }
}
