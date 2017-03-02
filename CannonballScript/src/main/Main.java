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
import util.PricedItem;
import util.RunTimer;
import util.ScriptVars;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by steven.luo on 01/03/2017.
 */
@ScriptManifest(category = Category.SMITHING, name = "Cannonball creator", author = "GB", version = 1.0, description = "Creates cannonballs")
public class Main  extends AbstractScript{

    java.util.List<PricedItem> lootTrack = new ArrayList<PricedItem>();
    ScriptVars sv = new ScriptVars();
    private RunTimer timer;
    private int first;
    private int second;
    private boolean levelUp;
    private long lastBarSmithed;
    private Area furnaceArea = new Area(3107,3500,3109,3498,0);
    private Area bankArea = new Area(3094,3497,3098,3494,0);
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


        lootTrack.add(new PricedItem("Cannonball", getClient().getMethodContext(), false));

        sv.started = true;
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
        updateLoot();
        return Calculations.random(600,700);
    }

    private void updateLoot(){
        for(PricedItem p : lootTrack){
            p.update();
        }
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
        if (getCamera().getYaw() > 300 && getCamera().getYaw() < 700){

        } else {
            getCamera().rotateToYaw(Calculations.random(301,699));
        }
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
                    lastBarSmithed = System.currentTimeMillis();
                }
            } else {
                if (!getInventory().isItemSelected()){
                    if (getInventory().interact("Steel bar", "Use")){
                        sleepUntil(() -> getInventory().isItemSelected(), 1000);
                    }
                } else {
                    GameObject furnace = getGameObjects().closest("Furnace");
                    log("looking for furnace");
                    if (furnace != null){
                        if (furnace.interact("Use")){
                            // Sleep until the widget is available
                            sleep(Calculations.random(500,1000));
                        }
                    }
                }
            }
        } else {
            antiban();
        }

    }

    private void walkToFurnace(){
        if (!furnaceArea.contains(getLocalPlayer())){
            log("Walking to furnace area");
            getWalking().walk(furnaceArea.getRandomTile());
        }
    }

    private void antiban(){
        if (getMouse().isMouseInScreen()){
            getMouse().moveMouseOutsideScreen();
        }
    }

    @Override
    public void onPaint(Graphics g) {
        if (sv.started) {
            g.drawString("State: " + getState().toString(),5,10);
            g.drawString("Runtime: " + timer.format(), 5, 30);
            g.drawString("Experience (p/h): " + getSkillTracker().getGainedExperience(Skill.SMITHING) + "(" + timer.getPerHour(getSkillTracker().getGainedExperience(Skill.SMITHING)) + ")", 5, 45);

            for (int i = 0; i < lootTrack.size(); i++){
                PricedItem p = lootTrack.get(i);
                if (p != null){
                    String name = p.getName();
                    g.drawString(name + " (p/h): " + p.getAmount() + "(" + timer.getPerHour(p.getAmount()) + ")" , 5, (i + 1)* 15 +60);
                }
            }
        }

    }
}
