package com.sucy.skill.dynamic.data;

public class CustomMeta {

    public String key;

    private Double value;

    public Long overTime;

    public Integer time;

    //这里传入的是tick 自动转换为时间戳 20Tick = 1s
    public CustomMeta(String key, Double value, Integer time) {
        this.key = key;
        this.value = value;
        this.overTime = System.currentTimeMillis() + (time * 50);
        this.time = time;
    }

    public boolean isTimerOut() {
        return overTime < System.currentTimeMillis() && time != -1;
    }

    public double getValue() {
        if (overTime < System.currentTimeMillis() && time != -1) {
            return 0;
        }
        return value;
    }

    public void setValue(Double target, String action) {
        if (action.isEmpty()) return;
        switch (action) {
            case "+": {
                this.value = getValue() + target;
                break;
            }
            case "-": {
                this.value = getValue() - target;
                break;
            }
            case "*": {
                this.value = getValue() * target;
                break;
            }
            case "/": {
                this.value = getValue() / target;
                break;
            }
            default: { break; }
        }
        // 续约
        this.overTime = System.currentTimeMillis() + (time * 50);
    }

}
