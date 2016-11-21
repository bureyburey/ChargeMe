package com.burey.chargeme;

/**
 * Created by burey on 30/08/2015.
 */
public class Client {
    private int _id;
    private String name;
    private String total;
    private String client_number;
    private boolean is_toshav;
    private boolean isSelected;

    public Client(int _id, String name, String total, String client_number, boolean is_toshav) {
        this._id = _id;
        this.name = name;
        this.total = total;
        this.client_number = client_number;
        this.is_toshav = is_toshav;
        this.isSelected = false;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getClient_number() {
        return client_number;
    }

    public void setClient_number(String client_number) {
        this.client_number = client_number;
    }

    public boolean is_toshav() {
        return is_toshav;
    }

    public void setIs_toshav(boolean is_toshav) {
        this.is_toshav = is_toshav;
    }
}
