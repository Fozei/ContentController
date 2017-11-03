package com.ewedo.contentcontroller.bean;

/**
 * Created by fozei on 17-11-3.
 */

public class SimpleResponse {
    private final Order order;
    private String message;
    private int state;

    public SimpleResponse() {
        this.order = new Order();
    }


    public Order getOrder() {
        return order;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public class Order{
        private int type;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

}
