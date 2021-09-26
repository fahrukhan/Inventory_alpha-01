package com.example.inventory_alpha_01.adapter;
import android.graphics.Color;
import android.view.View;

public class MyAdapter {
    private boolean show=false;
    private int visibleCheckbox=View.VISIBLE;
    private boolean enableCheckbox=true;
    private String strKey="";
    private String strDisplay="";
    private String strListRight="";
    private String strListBottom="";
    private String strListTop="";
    private String strListType="";
    private String strPickupStatus="";
    private String registerId ="";
    private String jewerlyId ="";
    private String locationId="";
    private int backgroundColor= color.WHITE;
    private Object data="";
    private boolean checked=false;
    public interface color{
        int WHITE=0;
        int BLUE=0xFF89CFF0;
        int ASH=0xffcccccc;
        int GREEN=Color.parseColor("#c2efc4");
        int RED=Color.parseColor("#f3e2df");
    }
    public MyAdapter(String strKey, String strDisplay, String strListRight, String strListBottom, Object data){
        super();
        this.strKey=strKey;
        this.strDisplay=strDisplay;
        this.strListRight=strListRight;
        this.strListBottom=strListBottom;
        this.data=data;
    }
    public String getStrDisplay(){
        return strDisplay;
    }
    public void setStrDisplay(String strDisplay){
        this.strDisplay=strDisplay;
    }
    public Object getData(){
        return data;
    }
    public void setData(Object data){
        this.data=data;
    }
    public boolean isChecked(){
        return checked;
    }
    public void setChecked(boolean checked){
        this.checked=checked;
    }
    public String getStrKey(){
        return strKey;
    }
    public void setStrKey(String strKey){
        this.strKey=strKey;
    }
    public String getStrListRight(){
        return strListRight;
    }
    public void setStrListRight(String strListRight){
        this.strListRight=strListRight;
    }
    public String getStrListBottom(){
        return strListBottom;
    }
    public void setStrListBottom(String strListBottom){
        this.strListBottom=strListBottom;
    }
    public int getBackgroundColor(){
        return backgroundColor;
    }
    public void setBackgroundColor(int backgroundColor){
        this.backgroundColor=backgroundColor;
    }
    public boolean isShow(){
        return show;
    }
    public void setShow(boolean show){
        this.show=show;
    }
    public int getVisibleCheckbox(){
        return visibleCheckbox;
    }
    public void setVisibleCheckbox(int visibleCheckbox){
        this.visibleCheckbox=visibleCheckbox;
    }
    public boolean isEnableCheckbox(){
        return enableCheckbox;
    }
    public void setEnableCheckbox(boolean enableCheckbox){
        this.enableCheckbox=enableCheckbox;
    }

    public String getStrListTop() {
        return strListTop;
    }

    public void setStrListTop(String strListTop) {
        this.strListTop = strListTop;
    }

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    public String getJewerlyId() {
        return jewerlyId;
    }

    public void setJewerlyId(String jewerlyId) {
        this.jewerlyId = jewerlyId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getStrListType() {
        return strListType;
    }

    public void setStrListType(String strListType) {
        this.strListType = strListType;
    }

    public String getStrPickupStatus() {
        return strPickupStatus;
    }

    public void setStrPickupStatus(String strPickupStatus) {
        this.strPickupStatus = strPickupStatus;
    }
}
