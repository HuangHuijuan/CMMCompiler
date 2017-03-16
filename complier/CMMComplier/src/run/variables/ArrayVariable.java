package run.variables;

import java.util.ArrayList;

/**
 * Created by Gatev on 2016/1/2.
 */
public class ArrayVariable {
    private String arrayName;
    private String type;
    private int length;
    private ArrayList<String> values;
    private int level;

    public ArrayVariable(){}
    public ArrayVariable(String arrayName, String type, int length, ArrayList<String> values, int level){
        this.arrayName = arrayName;
        this.type = type;
        this.length = length;
        this.values = values;
        this.level = level;
    }
    public void setArrayName(String arrayName){
        this.arrayName = arrayName;
    }
    public void setType(String type){
        this.type = type;
    }
    public void setLength(int length){
        this.length = length;
    }
    public void setValues(ArrayList<String> values){
        this.values = values;
    }
    public void setLevel(int level){
        this.level = level;
    }
    public String getArrayName(){
        return arrayName;
    }
    public String getType(){
        return type;
    }
    public int getLength(){
        return length;
    }
    public ArrayList<String> getValues(){
        return values;
    }
    public int getLevel(){
        return level;
    }
}
