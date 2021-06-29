package tech.redmaple.smishsmash.sms;

public class SMS {
    private String _id;
    private String _address;
    private String _msg;
    private String _time;

    public String getId(){
        return _id;
    }
    public String getAddress(){
        return _address;
    }
    public String getMsg(){
        return _msg;
    }
    public String getTime(){
        return _time;
    }

    public void setId(String id){
        _id = id;
    }
    public void setAddress(String address){
        _address = address;
    }
    public void setMsg(String msg){
        _msg = msg;
    }
    public void setTime(String time){
        _time = time;
    }
}
