package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.interactive.GameObject;

/**
 * Created by steven.luo on 09/01/2017.
 */
public class main extends AbstractScript {

    private int state;
    private String ore;
    private GameObject rock;
    private int count;

    @Override
    public void onStart() {
        ore = "Copper ore";
        count = 0;
        state = 0;
    }

    @Override
    public int onLoop() {
        if (state == 0) {
            mine();
        } else if (state == 1) {
            drop();
        } else {

        }
        return Calculations.random(500,1000);
    }

    private void mine() {
        if (!getInventory().isFull()) {
            rock = getGameObjects().closest(f -> f.getName().contains("Rock"));
            if (rock != null) {
                if (rock.interact("Mine")) {
                    sleepUntil(() -> getInventory().count(ore) > count, Calculations.random(2000, 3000));
                    if (getInventory().count(ore) > count) {
                        count++;
                    }
                }

            }
        } else {
            state = 1;
        }
    }

    private void drop() {
        if (getInventory().contains(ore)) {
            getInventory().dropAllExcept(i -> i.getName().contains("pickaxe"));
        } else {
            state = 0;
        }

    }
}
