package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import util.wood;

/**
 * Created by steven.luo on 10/01/2017.
 */


@ScriptManifest(author = "GB", category = Category.WOODCUTTING, description = "cuts closest tree", name = "GB WoodCutter", version = 1.0)
public class main extends AbstractScript {


    private wood currentLog;
    private boolean drop = true;
    private int count = 0;
    private enum State {
        CUT, DROP, BANK
    };

    private State getState() {
        // If your inventory is full, you should be banking
        if (getInventory().isFull()){
            log("Banking");
            return State.BANK;
        } else if (drop && getInventory().contains(f -> f.getName().equals(currentLog.getLogName())) ) {
            log("Dropping logs");
            return State.DROP;
        } else {
            log("Cutting");
            return State.CUT;
        }

    }

    @Override
    public int onLoop() {
        count++;
        log(String.valueOf(count));
        switch (getState()){
            case CUT:
                cut();
                break;
            case DROP:
                drop();
                break;
            case BANK:
                bank();
                break;
        }
        return Calculations.random(500, 1000);
    }


    @Override
    public void onStart() {
        super.onStart();
        currentLog = wood.NORMAL;
    }

    private void cut() {
        if (!getInventory().isFull()) {
            GameObject gO = getGameObjects().closest(f -> f.getName().equals(currentLog.getTreeName()));
            if (getLocalPlayer().distance(gO) > 5) {
                log("Walking towards designated tree");
                getWalking().walk(gO);
                sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(4000, 6000));
            } else {
                if (gO.interact("Chop down")) {
                    log("Chopping");
                    sleepUntil(() -> !gO.exists() || !getLocalPlayer().isAnimating(), Calculations.random(12000, 15000));
                }
            }
        }
    }

    private void bank() {
        if (getBank().isOpen()) {
            log("Bank open, depositing");
            getBank().depositAllExcept(f -> f.getName().contains("axe"));
            getBank().close();
            sleepUntil(() -> !getBank().isOpen(), Calculations.random(2000, 800));
        } else {
            if (getLocalPlayer().distance((getBank().getClosestBankLocation().getCenter())) > 5){
                if (getWalking().walk(getBank().getClosestBankLocation().getCenter())) {
                    log("Walking towards bank");
                    sleepUntil(() -> !getLocalPlayer().isMoving() || getLocalPlayer().distance(getClient().getDestination()) < 8, Calculations.random(3500, 5000));
                }
            } else{
                log("Opening bank");
                getBank().open();
                sleepUntil(() -> getBank().isOpen(), Calculations.random(2000, 800));
            }
        }
    }

    private void drop() {
        if (getInventory().contains(currentLog.getLogName())) {
            log("Dropping logs");
            getInventory().dropAll(currentLog.getLogName());
        }
    }
}
