package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import util.PricedItem;
import util.RunTimer;
import util.ScriptVars;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Steven on 1/21/2017.
 */
@ScriptManifest(category = Category.THIEVING, name = "Thieving leveler script", author = "GB" , version = 1.0, description = "1-5 man, 5-25 tea, 25 - 40 silk, 40+ farmer")
public class Main extends AbstractScript {
    private Area lumbyArea = new Area (3229,3210,3239,3119,0);
    private Area draynorArea = new Area (3073,3259,3087,3240,0);
    private Area edgevilleLeverArea = new Area (3092,3478,3095,3475,0);
    private Area wildyLeverArea = new Area (3153,3926,3156,3922,0);
    private Area ardyLeverArea = new Area(2561,3313,2563,3309,0);
    private Tile teaStallTile = new Tile(3268,3410,0);
    private Tile silkStallTile = new Tile(2663,3316,0);
    private ScriptVars sv = new ScriptVars();
    private RunTimer timer;
    private NPC man;
    private NPC masterFarmer;
    private int hpToEatAt;
    private int hpToBankAt;
    private long stunnedTimerStart;
    private boolean edgevilleLeverAreaPassed;
    private boolean autoDetectLevel = true;
    java.util.List<PricedItem> lootTrack = new ArrayList<PricedItem>();

    private enum State {
        STEAL_FROM_MAN,
        WALK_TO_MAN,
        STEAL_FROM_TEA_STALL,
        WALK_TO_TEA_STALL,
        STEAL_FROM_SILK_STALL,
        WALK_TO_SILK_STALL,
        STEAL_FROM_MASTER_FARMER,
        WALK_TO_MASTER_FARMER,
        WALK_TO_BANK,
        BANK,
        WAIT_AND_EAT,
        TEST
    }

    private State getState(){
        int currentThievingLevel = getSkills().getRealLevel(Skill.THIEVING);
        boolean stealFromMan = false;
        boolean stealFromTeaStall = false;
        boolean stealFromSilkStall = false;
        boolean stealFromMasterFarmer = false;
        if (autoDetectLevel){
            if (currentThievingLevel < 5){
                stealFromMan = true;
            }

            if ((currentThievingLevel >= 5 && currentThievingLevel < 25) || (currentThievingLevel >= 5 && currentThievingLevel < 38 && silkStallTile.distance(getLocalPlayer()) > 400)){
                stealFromTeaStall = true;
            }

            if (currentThievingLevel >= 25 && currentThievingLevel < 38 && silkStallTile.distance(getLocalPlayer()) < 400){
                stealFromSilkStall = true;
            }

            if (currentThievingLevel >= 38){
                stealFromMasterFarmer = true;
            }
        }


        if (System.currentTimeMillis() - stunnedTimerStart < 2000 || (getSkills().getBoostedLevels(Skill.HITPOINTS) < hpToEatAt && getInventory().contains("Tuna")) ){
            return State.WAIT_AND_EAT;
        }


        // Got attacked, kill the guard
        if (getLocalPlayer().isInCombat() || getLocalPlayer().isInteractedWith()){
            if (!getCombat().isAutoRetaliateOn()) {
                getCombat().toggleAutoRetaliate(true);
            }
            return State.WAIT_AND_EAT;
        }
        // Testing Stealing From Man
        if (stealFromMan){
            if (!lumbyArea.contains(getLocalPlayer())) {
                if (man != null) {
                    return State.STEAL_FROM_MAN;
                } else {
                    return State.WALK_TO_MAN;
                }
            } else {
                return State.STEAL_FROM_MAN;
            }
        }

        if (stealFromTeaStall){
            if (!getLocalPlayer().getTile().equals(teaStallTile)){
                return State.WALK_TO_TEA_STALL;
            } else {
                return State.STEAL_FROM_TEA_STALL;
            }
        }

        if (stealFromSilkStall){
            if (!getLocalPlayer().getTile().equals(silkStallTile)){
                return State.WALK_TO_SILK_STALL;
            } else {
                return State.STEAL_FROM_SILK_STALL;
            }
        }

        if (stealFromMasterFarmer){
            if (getInventory().isFull() || getSkills().getBoostedLevels(Skill.HITPOINTS) < hpToBankAt){
                if (BankLocation.DRAYNOR.getArea(3).contains(getLocalPlayer())){
                    return State.BANK;
                } else {
                    return State.WALK_TO_BANK;
                }
            }

            if (!draynorArea.contains(getLocalPlayer())){
                // Steal
                if (masterFarmer != null) {
                    return State.STEAL_FROM_MASTER_FARMER;
                } else {
                    return State.WALK_TO_MASTER_FARMER;
                }
            } else {
                return State.STEAL_FROM_MASTER_FARMER;
            }
        } else {
            return null;
        }

//        return State.TEST;
    }
    @Override
    public void onStart() {
        sv.loot =  new String[] {"Ranarr seed", "Kwuarm seed", "Snapdragon seed", "Torstol seed"};

        for (int i = 0; i < sv.loot.length; i++) {
            log("Looting: " + sv.loot[i]);
            lootTrack.add(new PricedItem(sv.loot[i], getClient().getMethodContext(), false));
        }

        hpToEatAt = getSkills().getRealLevel(Skill.HITPOINTS) - 20;
        hpToBankAt = hpToEatAt - 15;
        getSkillTracker().start(Skill.THIEVING);
        timer = new RunTimer();
        sv.started = true;
    }

    @Override
    public int onLoop() {

        if(getLocalPlayer().isMoving() && getClient().getDestination() != null && getClient().getDestination().distance(getLocalPlayer()) > 3) {
            return Calculations.random(300,500);
        }

        if (!getWalking().isRunEnabled() && getWalking().getRunEnergy() > Calculations.random(30,50)){
            getWalking().toggleRun();
        }
        switch (getState()){

            case TEST:
                test();
                break;
            case WAIT_AND_EAT:
                waitAndEat();
                break;
            case STEAL_FROM_MAN:
                stealFromMan();
                break;
            case WALK_TO_MAN:
                walkToMan();
                break;
            case WALK_TO_TEA_STALL:
                walkToTeaStall();
                break;
            case STEAL_FROM_TEA_STALL:
                stealFromTeaStall();
                break;
            case WALK_TO_SILK_STALL:
                walkToSilkStall();
                break;
            case STEAL_FROM_SILK_STALL:
                stealFromSilkStall();
                break;
            case WALK_TO_BANK:
                walkToBank();
                break;
            case BANK:
                bank();
                break;
            case STEAL_FROM_MASTER_FARMER:
                stealFromMasterFarmer();
                break;
            case WALK_TO_MASTER_FARMER:
                walkToMasterFarmer();
                break;
            default:
                stop();
        }
        updateLoot();
        return Calculations.random(450,700);
    }

    private void test(){
        log("this is a test, no state found");
    }

    private void bank(){
        log("banking");
        if (getBank().isOpen()){
            getBank().depositAllItems();
            sleepUntil(() -> !getInventory().isFull(), 1500);
            getBank().withdraw("Tuna", 10);
            sleepUntil(() -> getInventory().contains("Tuna"),1500);
            getBank().close();
        } else {
            getBank().open(BankLocation.DRAYNOR);
            sleepUntil(() -> getBank().isOpen(), 1500);
        }
    }

    private void walkToBank(){
        log("walking to bank");
        getWalking().walk(BankLocation.DRAYNOR.getArea(3).getRandomTile());
    }
    private void waitAndEat(){

        if (getSkills().getBoostedLevels(Skill.HITPOINTS) <= hpToEatAt && getInventory().contains("Tuna")) {
            log ("low health, eating");
            if (!getTabs().isOpen(Tab.INVENTORY)){
                getTabs().open(Tab.INVENTORY);
            }
            if (getInventory().contains("Tuna")){
                getInventory().get("Tuna").interact("Eat");
                sleepUntil(() -> !getLocalPlayer().isAnimating(), 1500);
            }
        } else {
            antiban();
        }
    }
    private void waitAndDrop(String itemName){
        if (getInventory().contains(itemName)){
            getInventory().dropAll(itemName);
            sleepUntil(() -> !getInventory().contains(itemName), 1500);
        } else {
            antiban();
        }
    }
    private void stealFromMasterFarmer() {
        if (masterFarmer == null || !masterFarmer.exists()){
            masterFarmer = getNpcs().closest(n -> n.getName().equals("Master Farmer") && n.hasAction("Pickpocket"));
        }

        int startingHp = getSkills().getBoostedLevels(Skill.HITPOINTS);
        sleep(Calculations.random(200,350));
        if (masterFarmer.interact("Pickpocket")){
            sleepUntil(() -> !getLocalPlayer().isAnimating(),1000);
            // Player gets stunned
            if (getSkills().getBoostedLevels(Skill.HITPOINTS) < startingHp && !getLocalPlayer().isInCombat()){
                stunnedTimerStart = System.currentTimeMillis();
            }
        }
    }

    private void walkToMasterFarmer() {
        log("walking to master farmer");
        getWalking().walk(draynorArea.getRandomTile());

    }
    private void stealFromSilkStall(){
        GameObject silkStall = getGameObjects().closest(g -> g.getName().equals("Silk stall") && g.exists() && g.hasAction("Steal-from") && g.distance(getLocalPlayer()) < 4);
        if (silkStall != null){
            silkStall.interact("Steal-from");
        } else {
            waitAndDrop("Silk");
        }
    }

    private void walkToSilkStall(){

//        if (!edgevilleLeverAreaPassed) {
//            if (edgevilleLeverArea.contains(getLocalPlayer())){
//                log ("pulling edgeville lever");
//                GameObject leverEdge = getGameObjects().closest(g -> g.getName().equals("Lever") && g.hasAction("Pull"));
//                leverEdge.interact("Pull");
//                sleepUntil(() -> wildyLeverArea.contains(getLocalPlayer()), 2000);
//                if (wildyLeverArea.contains(getLocalPlayer())){
//                    log("pulling wildy lever");
//                    GameObject leverWildy = getGameObjects().closest(g -> g.getName().equals("Lever") && g.hasAction("Pull"));
//                    leverWildy.interact("Pull");
//                    sleepUntil(() -> ardyLeverArea.contains(getLocalPlayer()), 2000);
//                    if (ardyLeverArea.contains(getLocalPlayer())){
//                        log ("successfully made it to ardy");
//                        edgevilleLeverAreaPassed = true;
//                        // Open door
//                        GameObject door = getGameObjects().closest(g -> g.getName().equals("Door") && g.hasAction("Open") && g.distance(getLocalPlayer()) < 5);
//                        if (door != null){
//                            door.interact("Open");
//                            sleepUntil(() -> door.hasAction("Close"), 2000);
//                        }
//                    }
//                }
//            } else {
//                log("walking to edgeville lever area");
//                getWalking().walk(edgevilleLeverArea.getRandomTile());
//            }
//        } else {
//            log("walking to silk stall");
//            getWalking().walkExact(silkStallTile);
//        }

        log("walking to silk stall");
        getWalking().walkExact(silkStallTile);
    }
    private void stealFromTeaStall(){
        GameObject teaStall = getGameObjects().closest(g -> g.getName().equals("Tea stall") && g.exists() && g.hasAction("Steal-from"));
        if (teaStall != null){
            teaStall.interact("Steal-from");
        } else {
            waitAndDrop("Cup of tea");
        }
    }

    private void walkToTeaStall(){
        log("Walking to tea stall");
        getWalking().walkExact(teaStallTile);
//        if (!teaStallTile.getArea(4).contains(getLocalPlayer())){
//            getWalking().walk(teaStallTile.getArea(4).getRandomTile());
//        } else {
//            getWalking().walkExact(teaStallTile);
//        }
    }
    private void stealFromMan(){
        if (man == null || !man.exists()){
            man = getNpcs().closest(n -> (n.getName().equals("Man") || n.getName().equals("Woman") && n.hasAction("Pickpocket")));
        }

        int startingHp = getSkills().getBoostedLevels(Skill.HITPOINTS);
        if (man.interact("Pickpocket")){
            sleepUntil(() -> !getLocalPlayer().isAnimating(),1000);
            // Player gets stunned
            if (getSkills().getBoostedLevels(Skill.HITPOINTS) < startingHp && !getLocalPlayer().isInCombat()){
                stunnedTimerStart = System.currentTimeMillis();
            }
        }
    }

    private void walkToMan(){
        log("Walking to man");
        getWalking().walk(lumbyArea.getRandomTile());
        // reset targeting
        man = null;
    }

    private void updateLoot(){
        for(PricedItem p : lootTrack){
            p.update();
        }
    }
    private void antiban() {
        int random = Calculations.random(1, 100);
        if (random < 20){
            if (!getTabs().isOpen(Tab.STATS)) {
                getTabs().open(Tab.STATS);
                if (random < 4) {
                    getSkills().hoverSkill(Skill.THIEVING);
                } else if (random < 8) {
                    getSkills().hoverSkill(Skill.THIEVING);
                } else if (random < 12) {
                    getSkills().hoverSkill(Skill.THIEVING);
                } else if (random < 16) {
                    getSkills().hoverSkill(Skill.THIEVING);
                } else {
                    getSkills().hoverSkill(Skill.THIEVING);
                }
                sleepUntil(()-> !getLocalPlayer().isInCombat() || !getLocalPlayer().isAnimating(),Calculations.random(500, 1000));
            }
        } else if (random <= 30) {
            if (!getTabs().isOpen(Tab.INVENTORY)){
                getTabs().open(Tab.INVENTORY);
            }
        } else {
            if (getMouse().isMouseInScreen()){
                if (getMouse().moveMouseOutsideScreen()){
                    sleepUntil(()-> !getLocalPlayer().isInCombat() || !getLocalPlayer().isAnimating(),Calculations.random(500, 1000));
                }
            }
        }
    }
    @Override
    public void onPaint(Graphics g) {
        if (sv.started){
            g.drawString("State: " + getState().toString(), 5, 10);
            g.drawString("Run Time: " + timer.format(), 5, 30);
            g.drawString("Exp (p/h) : " + getSkillTracker().getGainedExperience(Skill.THIEVING) + "(" + timer.getPerHour(getSkillTracker().getGainedExperience(Skill.THIEVING)) + ")", 5, 45);
            g.drawString("Level Start (gained): " + getSkillTracker().getStartLevel(Skill.THIEVING) + "(" + getSkillTracker().getGainedLevels(Skill.THIEVING) + ")", 5,60);
            g.drawString("Eating at: " + hpToEatAt,5,75);
            g.drawString("Bank at: " + hpToBankAt, 5,90);

            for (int i = 0; i < lootTrack.size(); i++){
                PricedItem p = lootTrack.get(i);
                if (p != null){
                    String name = p.getName();
                    g.drawString(name + " (p/h): " + p.getAmount() + "(" + timer.getPerHour(p.getAmount()) + ")" , 400, (i + 1)* 15);
                }
            }
        }

    }
}
