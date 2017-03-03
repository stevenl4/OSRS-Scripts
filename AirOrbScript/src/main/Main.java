package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import util.RunTimer;
import util.ScriptVars;

import java.awt.*;

/**
 * Created by steven.luo on 28/02/2017.
 */
@ScriptManifest(category = Category.MISC, name = "Orb Runner", author = "GB", version = 1.0, description = "Runs orbs to your other niggas")
public class Main extends AbstractScript {

    ScriptVars sv = new ScriptVars();
    private RunTimer timer;
    private Player mainPlayer;
    private Area orbArea = new Area(3080,3580,3096,3560,0);
    private Area dungeonUpstairs = new Area(3094,3471,3096,3468,0);
    private Area dungeonDownstairs = new Area(3096,9869,3098,9867,0);
    private Area firstGate = new Area(3100,9911,3103,9907,0);
    private Area secondGate = new Area(3130,9917,3133,9914,0);
    private Area spiderArea = new Area(3117,9959,3125,9952,0);
    private Area orbLadderDownstairs = new Area(3087,9970,3090,9967,0);
    private long lastStaminaDose;

    private boolean dungeonUpstairsPassed;
    private boolean firstGatePassed;
    private boolean secondGatePassed;
    private boolean spiderAreaPassed;
    private boolean stockForRun;
    private boolean needGlory;
    private boolean needFood;
    private boolean needStamina;

    public enum State {
        TRADE, BANK, WALK_TO_MAIN, TEST
    }

    private State getState(){
        if (sv.testMode){
            return State.TEST;
        }

        if (orbArea.contains(getLocalPlayer())){
            if (getInventory().contains("Unpowered orb") || getInventory().isEmpty()){
                return State.TRADE;
            } else {
                return State.BANK;
            }
        } else {
            if (getInventory().isFull() && !getInventory().contains("Amulet of glory")){
                return State.WALK_TO_MAIN;
            } else {
                return State.BANK;
            }
        }

    }

    @Override
    public void onStart() {
        timer = new RunTimer();
        // Start Tracking All Skills
        for (Skill s : Skill.values()){
            getSkillTracker().start(s);
        }
        sv.mainName = "CurvedSpine";
        sv.foodName = "Tuna";
        sv.started = true;
        sv.testMode = false;

        if (sv.testMode){


        }
    }

    @Override
    public int onLoop() {
        if(getLocalPlayer().isMoving() && getClient().getDestination() != null && getLocalPlayer().distance(getClient().getDestination()) > Calculations.random(3,5)){
            return Calculations.random(200,300);
        }


        if (getCamera().getPitch() < 320){
            getCamera().rotateToPitch(Calculations.random(344, 383));
        }

        if (!getWalking().isRunEnabled() && getWalking().getRunEnergy() > 35){
            getWalking().toggleRun();
        }
        switch (getState()){
            case TRADE:
                trade();
                break;
            case BANK:
                bank();
                break;
            case WALK_TO_MAIN:
                walkToMain();
                break;
            case TEST:
                bank();
                break;
        }
        return Calculations.random(400,600);
    }

    private void selectMain(){
        log("Selecting main");
        int tryCount = 0;
        while (mainPlayer == null || !mainPlayer.exists()){
            if (tryCount > 50){
                break;
            }
           mainPlayer = getPlayers().closest(p -> p.getName().equals(sv.mainName));
            tryCount++;
            sleep(50);
        }

        if (mainPlayer != null){
            log("selected target: " + mainPlayer.getName());
        } else {
            log ("No nearby targets, move somewhere else");
            // Teleport back to bank and log out
        }
    }
    private void trade(){
        if (mainPlayer == null || !mainPlayer.exists()){
            selectMain();
        }
        boolean myItems = false;
        boolean theirItems = false;


        if (!getTrade().isOpen()){
            if (mainPlayer.interact("Trade with")){
                sleepUntil(() -> getTrade().isOpen(), 25000);
            }

        } else{

            log("trading items");

            if (getInventory().isEmpty()) {
                myItems = true;
            } else {
                if (!getTrade().contains(true, 81, "Cosmic rune")){
                    log("adding cosmics");

                    if (getTrade().addItem("Cosmic rune", 81)){
                        sleepUntil(() -> getTrade().contains(true, 81, "Cosmic rune"), 500);
                    }
                } else if (!getTrade().contains(true,27, "Unpowered orb")){
                    log("adding unpowered orb");
                    if (getTrade().addItem("Unpowered orb", 27)){
                        sleepUntil(() -> getTrade().contains(true, 27, "Unpowered orb"), 500);
                    }
                } else {
                    myItems = true;
                }

            }

            if (myItems){
                getTrade().acceptTrade();
            }

        }
    }

    private void bank(){
        stockForRun = false;


        if (orbArea.contains(getLocalPlayer())){

            if (!getTabs().isOpen(Tab.EQUIPMENT)){
                if (getTabs().open(Tab.EQUIPMENT)){
                    sleepUntil(() -> getTabs().isOpen(Tab.EQUIPMENT),2000);

                }
            }
            if (getEquipment().getItemInSlot(EquipmentSlot.AMULET.getSlot()).interact("Edgeville")){
                log("teleporting to edgeville");
                sleepUntil(() -> !orbArea.contains(getLocalPlayer()), 2000);
            }
        }



        for (int i = 1; i<=6; i++){
            if (getInventory().contains("Amulet of glory(" + i + ")")){
                if (getInventory().interact("Amulet of glory(" + i + ")", "Wear")){
                    log ("equiping new glory");
                    int finalI = i;
                    sleepUntil(() -> !getInventory().contains("Amulet of glory(" + finalI + ")"), 1500);
                    break;
                }
            }
        }


        while (getInventory().contains(sv.foodName)){
            if (!getTabs().isOpen(Tab.INVENTORY)){
                if (getTabs().open(Tab.INVENTORY)){
                    sleepUntil(() -> getTabs().isOpen(Tab.INVENTORY), 1000);
                }
            }
            log("eating tuna");
            if (getInventory().interact(sv.foodName, "Eat")){
                sleep(Calculations.random(700,1000));
            }
        }


        // Pot stamina
        if (!getBank().isOpen()){

            for (int i = 1; i <= 4; i++){
                String potName = "Stamina potion(" + i + ")";
                if (getInventory().contains(potName)){
                    log("found stamina potion in inventory");
                    if (!getTabs().isOpen(Tab.INVENTORY)){
                        if (getTabs().open(Tab.INVENTORY)){
                            sleepUntil(() -> getTabs().isOpen(Tab.INVENTORY), 1000);
                        }
                    }

                    if (getInventory().interact(potName, "Drink")){
                        log("drinking stamina potion");
                        lastStaminaDose = System.currentTimeMillis();
                        sleep(300);

                    }
                }
            }
        }

        if (System.currentTimeMillis() - lastStaminaDose > 60000){
            needStamina = true;
        } else {
            needStamina = false;
        }




        if (BankLocation.EDGEVILLE.getArea(10).contains(getLocalPlayer())){
            // Check for health
            Integer healthMissing = getSkills().getRealLevel(Skill.HITPOINTS) - getSkills().getBoostedLevels(Skill.HITPOINTS);
            if (healthMissing > 9){
                needFood = true;
            } else {
                needFood = false;
            }


            // Check for amulet
            if (getEquipment().getItemInSlot(EquipmentSlot.AMULET.getSlot()) != null){
                Item currentAmulet = getEquipment().getItemInSlot(EquipmentSlot.AMULET.getSlot());
                if (!getTabs().isOpen(Tab.EQUIPMENT)){
                    if (getTabs().open(Tab.EQUIPMENT)){
                        sleepUntil(() -> getTabs().isOpen(Tab.EQUIPMENT),500);
                    }
                }
                if (currentAmulet.getName().equals("Amulet of glory")){

                    log("removing uncharged glory");

                    if (getEquipment().getItemInSlot(EquipmentSlot.AMULET.getSlot()).interact("Remove")){
                        sleepUntil(() -> getInventory().contains(currentAmulet), 1000);
                        needGlory = true;
                    }
                } else {
                    needGlory = false;
                }
            } else {
                needGlory = true;
            }

            // Get Orbs

            if (getBank().isOpen()){
                if (!getInventory().isEmpty()) {
                    getBank().depositAllItems();
                    sleepUntil(() -> getInventory().isEmpty(), 1500);
                }

                if (needFood){
                    Integer foodRequiredAmt = (healthMissing / 10) + 1;
                    getBank().withdraw(sv.foodName, foodRequiredAmt);
                    sleepUntil(() -> getInventory().contains(sv.foodName), 1000);
                }

                if (needGlory){
                    log("Need a new glory");
                    getBank().withdraw("Amulet of glory(4)");
                    sleepUntil(() -> getInventory().contains("Amulet of glory(4)"), 1000);
                }

                if (needStamina){

                    for (int i = 1; i <= 4; i++){
                        String potName = "Stamina potion(" + i + ")";
                        if (getBank().contains(potName)){
                            log ("withdrawing " + potName);
                            getBank().withdraw(potName);
                            sleepUntil(() -> getInventory().contains(potName), 1000);
                            break;
                        }
                    }
                }
                if (needFood || needGlory || needStamina){
                    getBank().close();
                    sleepUntil(() -> !getBank().isOpen(), 1000);
                } else {
                    if (!getInventory().contains("Cosmic rune")){
                        if (getBank().withdraw("Cosmic rune", 81)){
                            log("withdrawing cosmics");
                            sleepUntil(() -> getInventory().contains("Cosmic rune"), 1500);
                        }
                    }

                    if (!getInventory().contains("Unpowered orb")){

                        if (getBank().withdraw("Unpowered orb", 27)){
                            log("withdrawing unpowered orbs");
                            sleepUntil(() -> getInventory().contains("Unpowered orb"), 1500);
                        }
                    }

                    // Reset variables for walking
                    dungeonUpstairsPassed = false;
                    firstGatePassed = false;
                    secondGatePassed = false;
                    spiderAreaPassed = false;
                    mainPlayer = null;
                    log("Inventory filled, ready to go");
                }
            } else {
                if(getBank().openClosest()){
                    sleepUntil(() -> getBank().isOpen(), 3000);
                }
            }
        } else {
            getWalking().walk(BankLocation.EDGEVILLE.getArea(5).getRandomTile());
        }

    }

    private void walkToMain(){

        if (!dungeonUpstairsPassed){
            if(dungeonUpstairs.contains(getLocalPlayer())){
                if (getCamera().getPitch() > 1200 && getCamera().getPitch() < 1400){

                } else {
                    getCamera().rotateToPitch(Calculations.random(1201,1400));
                }
                GameObject trapdoor = getGameObjects().closest(gO -> gO.getName().equals("Trapdoor"));
                if (trapdoor != null){
                    if (trapdoor.hasAction("Open")){
                        if (trapdoor.interact("Open")){
                            sleepUntil(() -> trapdoor.hasAction("Climb-down"), 2000);
                        }
                    }

                    if(trapdoor.interact("Climb-down")){
                        log("Climbing down stairs to dungeon");
                        sleepUntil(() -> dungeonDownstairs.contains(getLocalPlayer()), 5000);
                        if (dungeonDownstairs.contains(getLocalPlayer())){
                            dungeonUpstairsPassed = true;
                        }
                    }
                }
            } else {
                log("walking to dungeonDownstairsArea");
                getWalking().walk(dungeonUpstairs.getRandomTile());
            }
        } else {

            if (!firstGatePassed){
                if (firstGate.contains(getLocalPlayer())){
                    GameObject gate = getGameObjects().closest(g -> g.getName().equals("Gate") && g.hasAction("Open") && g.distance(getLocalPlayer()) < 10);
                    if (gate != null){
                        if (gate.interact("Open")){
                            sleepUntil(() -> gate.hasAction("Close"), 1500);
                        }
                    }

                    firstGatePassed = true;
                } else {
                    log("walking to firstGateArea");
                    getWalking().walk(firstGate.getRandomTile());
                }
            } else {

                if (!secondGatePassed){
                    if (secondGate.contains(getLocalPlayer())){
                        GameObject gate = getGameObjects().closest(g -> g.getName().equals("Gate") && g.hasAction("Open") && g.distance(getLocalPlayer()) < 8);
                        if (gate != null){
                            if (gate.interact("Open")){
                                sleepUntil(() -> !secondGate.contains(getLocalPlayer()), 2000);
                                if (!secondGate.contains(getLocalPlayer())){
                                    secondGatePassed = true;
                                }
                            }
                        }
                    } else {
                        log("walking to secondGateArea");
                        getWalking().walk(secondGate.getRandomTile());
                    }
                } else{
                    if (!getWalking().isRunEnabled() && getWalking().getRunEnergy() > 10){
                        getWalking().toggleRun();
                    }
                    if (!spiderAreaPassed){
                        if (spiderArea.contains(getLocalPlayer())){
                            spiderAreaPassed = true;
                        } else {
                            log("walking to spiderArea");
                            getWalking().walk(spiderArea.getRandomTile());
                        }
                    } else {
                        if (orbLadderDownstairs.contains(getLocalPlayer())) {
                            GameObject ladder = getGameObjects().closest(gO -> gO.hasAction("Climb-up") && gO.getName().equals("Ladder"));
                            if (ladder != null){
                                if (ladder.interact("Climb-up")){
                                    log("climbing up ladder to orb area");
                                    sleepUntil(() -> orbArea.contains(getLocalPlayer()), 3000);

                                }
                            }
                        } else {
                            log("walking to orbLadderDownstairsArea");
                            getWalking().walk(orbLadderDownstairs.getRandomTile());
                        }
                    }
                }
            }
        }

    }

    @Override
    public void onPaint(Graphics g) {
        if (sv.started){
            g.drawString("State: " + getState().toString(), 5, 15);
            g.drawString("Runtime: " + timer.format(),5,30);
            if (mainPlayer != null && mainPlayer.exists()){

                g.drawString("Main: " + mainPlayer.getName(), 5, 45);
            }

        }

    }
}
