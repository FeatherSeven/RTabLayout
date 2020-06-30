package com.ruigu.tablayout;

public class TabEntity {
    String text;
    String msg;
    int selectedIcon;
    int icon;
    Object tag;

    public TabEntity(String s) {
        text = s;
    }

    public TabEntity(String text, int icon) {
        this.text = text;
        this.icon = icon;
    }

    public TabEntity(String text, int selectedIcon, int icon) {
        this.text = text;
        this.selectedIcon = selectedIcon;
        this.icon = icon;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getSelectedIcon() {
        return selectedIcon;
    }

    public void setSelectedIcon(int selectedIcon) {
        this.selectedIcon = selectedIcon;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}