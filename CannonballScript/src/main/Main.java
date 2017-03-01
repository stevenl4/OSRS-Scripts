package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import util.RunTimer;
import util.ScriptVars;

import java.util.List;

/**
 * Created by steven.luo on 01/03/2017.
 */
@ScriptManifest(category = Category.SMITHING, name = "Cannonball creator", author = "GB", version = 1.0, description = "Creates cannonballs")
public class Main  extends AbstractScript{

    ScriptVars sv = new ScriptVars();
    private RunTimer timer;
    private int first;
    private int second;
    private boolean levelUp;
    private long lastBarSmithed;
    private Area furnaceArea = new Area();
    private Area bankArea = BankLocation.EDGEVILLE.getArea(8);
    public enum State {
        SMITH, BANK, WALK_TO_FURNACE, TEST
    }

    private State getState() {
        if (sv.testMode) {
            return State.TEST;
        }

        if (getInventory().contains("Steel bar") && getInventory().contains("Ammo mould")){
            if (!furnaceArea.contains(getLocalPlayer())){
                return State.WALK_TO_FURNACE;
            } else {
                return State.SMITH;
            }
        } else {
            return State.BANK;
        }



    }
    @Override
    public void onStart() {
        timer = new RunTimer();
        // Start Tracking All Skills
        for (Skill s : Skill.values()){
            getSkillTracker().start(s);
        }
    }

    @Override
    public int onLoop() {


        if(getLocalPlayer().isMoving() && getClient().getDestination() != null && getLocalPlayer().distance(getClient().getDestination()) > Calculations.random(3,5)){
            return Calculations.random(200,300);
        }


        if (!getWalking().isRunEnabled() && getWalking().getRunEnergy() > 35){
            getWalking().toggleRun();
        }

        levelUp = false;

        while (getDialogues().canContinue()){
            getDialogues().clickContinue();
            levelUp = true;
            sleep(Calculations.random(200,300));
        }


        switch (getState()){
            case BANK:
                bank();
                break;
            case SMITH:
                smith();
                break;
            case WALK_TO_FURNACE:
                walkToFurnace();
                break;
            case TEST:
        }
        return Calculations.random(600,700);
    }

    private void bank(){


        if (bankArea.contains(getLocalPlayer())){
            if (getBank().isOpen()){

                if (!getInventory().onlyContains("Ammo mould", "Cannonball")){
                    if (getBank().depositAllExcept("Ammo mould", "Cannonball")){
                        log ("deposting all items not ammo mould or cannonball");
                        sleep(Calculations.random(600,800));
                    }
                }

                if (!getInventory().contains("Ammo mould")){
                    if (getBank().contains("Ammo mould")){
                        if (getBank().withdraw("Ammo mould")){
                            log("withdrawing ammo mould");
                            sleepUntil(() -> getInventory().contains("Ammo mould"), 1500);
                        }
                    } else {
                        log("No ammo mould.  Stopping");
                        stop();
                    }
                }

                if (getBank().contains("Steel bar")){
                    if(getBank().withdrawAll("Steel bar")){
                        log ("withdrawing steel bars");
                        sleepUntil(() -> getInventory().contains("Steel bar"), 1500);
                    }
                }
            } else {
                if (getBank().openClosest()){
                    sleepUntil(() -> getBank().isOpen(), 2000);
                }
            }

        } else {
            log("walking to bank");
            getWalking().walk(bankArea.getRandomTile());
        }

    }

    private void smith(){

        first = getInventory().count("Steel bar");

        if(first < second){
            lastBarSmithed = System.currentTimeMillis();
        }

        second = getInventory().count("Steel bar");

        if (System.currentTimeMillis() - lastBarSmithed > 10000 || levelUp){
            List<WidgetChild> widgetChildList = getWidgets().getWidgetChildrenContainingText("Steel bar");
            if (!widgetChildList.isEmpty()){
                if (widgetChildList.get(0).interact("Make All")){
                    sleepUntil(() -> getLocalPlayer().isAnimating(), 1500);
                }
            } else {
                if (!getInventory().isItemSelected()){
                    if (getInventory().interact("Steel bar", "Use")){
                        sleepUntil(() -> getInventory().isItemSelected(), 1000);
                    }
                } else {
                    GameObject furnace = getGameObjects().closest("Furnace");
                    if (furnace != null){
                        if (furnace.interact("Use")){
                            // Sleep until the widget is available
                        }
                    }
                }
            }
        } else {
            antiban();
        }

    }

    private void walkToFurnace(){
        if (!furnaceArea.getCenter().getArea(3).contains(getLocalPlayer())){
            log("Walking to furnace area");
            getWalking().walk(furnaceArea.getCenter().getArea(3).getRandomTile());
        }
    }

    private void antiban(){
        if (getMouse().isMouseInScreen()){
            getMouse().moveMouseOutsideScreen();
        }
    }
}
