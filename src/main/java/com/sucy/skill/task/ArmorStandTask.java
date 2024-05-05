package com.sucy.skill.task;

import com.sucy.skill.api.armorstand.ArmorStandManager;
import com.sucy.skill.thread.AbstractRepeatThread;

public class ArmorStandTask extends AbstractRepeatThread {

    public ArmorStandTask() {
        super(1,1);
    }

    @Override
    public void run() {
        ArmorStandManager.tick();
    }
}
