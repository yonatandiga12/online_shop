package ServiceLayer;
public class Result<T> {
    private boolean error;
    private T value=null;


    private String message="";
    Result(boolean b,T val){
        error=b;value=val;

    }
    Result(boolean b,String msg){
        error=b;message=msg;

    }

    public boolean isError() {
        return error;
    }
    public String getMessage() {
        return message;
    }

    public T getValue() {
        return value;
    }


    public void setValue(T value) {
        this.value = value;
    }





}
