package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.interactive.GameObject;
import util.wood;

/**
 * Created by steven.luo on 10/01/2017.
 */
public class main extends AbstractScript {

    private int state = -1;
    private wood currentLog;
    private boolean drop = true;

    @Override
    public int onLoop() {
        if (state == 0) {
            cut();
        } else if (state == 1) {
            bank();
        } else if (state == 2) {
            drop();
        }
        return Calculations.random(300, 400);
    }


    @Override
    public void onStart() {
        super.onStart();
        state = 0;
        currentLog = wood.NORMAL;
    }

    private void cut() {
        if (!getInventory().isFull()) {
            GameObject gO = getGameObjects().closest(f -> f.getName().equals(currentLog.getTreeName()));
            if (getLocalPlayer().distance(gO) > 5) {
                getWalking().walk(gO);
                sleepUntil(() -> !getLocalPlayer().isMoving() || getLocalPlayer().distance(getClient().getDestination()) < 7, Calculations.random(4000, 6000));
            } else {
                if (gO.interact("Chop down")) {
                    sleepUntil(() -> !gO.exists() || !getLocalPlayer().isAnimating(), Calculations.random(12000, 15000));
                }
            }
        } else {
            if (drop) {
                state = 2;
            } else {
                state = 1;
            }
        }
    }

    private void bank() {
        if (getBank().isOpen()) {
            getBank().depositAllExcept(f -> f.getName().contains("axe"));
            getBank().close();
            sleepUntil(() -> !getBank().isOpen(), Calculations.random(2000, 800));
            state = 0;
        } else {
            if (getLocalPlayer().distance((getBank().getClosestBankLocation().getCenter())) > 5){
                if (getWalking().walk(getBank().getClosestBankLocation().getCenter())) {
                    sleepUntil(() -> !getLocalPlayer().isMoving() || getLocalPlayer().distance(getClient().getDestination()) < 8, Calculations.random(3500, 1500));
                }
            } else{
                getBank().open();
                sleepUntil(() -> getBank().isOpen(), Calculations.random(2000, 800));
            }
        }
    }

    private void drop() {
        if (getInventory().contains(currentLog.getLogName())) {
            getInventory().dropAll(currentLog.getLogName());
        } else {
            state = 0;
        }
    }
}
