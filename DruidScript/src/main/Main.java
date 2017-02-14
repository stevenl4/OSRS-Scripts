package main;

import gui.Gui;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import util.PricedItem;
import util.RunTimer;
import util.ScriptVars;

import java.awt.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by steven.luo on 20/01/2017.
 */

@ScriptManifest(category = Category.COMBAT, name = "Chaos Druid Script", author = "GB" , version = 1.1, description = "Kills Chaos Druids in Ardy Tower, start in the bank")
public class Main extends AbstractScript {
    // TEST MODE -------------------------------------------------------------------------------------------------------
    private boolean testMode = false;
    // -----------------------------------------------------------------------------------------------------------------
    private Area druidArea = new Area(2560,3358,2564,3354,0);
    private Area druidEntranceArea = new Area(2565,3358,2566,3355,0);
    private Area druidTowerDownStairs = new Area (2563,9757,2567,9750,0);
    private Area druidTowerUpStairs = new Area (2560,3357,2561,3355,1);
    private NPC druid;
    java.util.List<PricedItem> lootTrack = new ArrayList<PricedItem>();
    private long lastSearchGround;
    private long lastScanPlayerCount;
    private long lootTimerStart;
    private int playersInArea;
    private GroundItem gi;
    ScriptVars sv = new ScriptVars();
    private RunTimer timer;

    Filter<GroundItem> itemFilter = gi -> {

        if (gi == null || !gi.exists() || gi.getName() == null){
            return false;
        }
        for (int i = 0; i < sv.loot.length; i++){
            if (gi.getName().equals(sv.loot[i])){
                return true;
            }
        }
        return false;
    };

    private enum State {
        WALK_TO_TRAINING, WALK_TO_BANK, FIGHT, LOOT, BANK, HOP, EAT, TEST
    }

    private State getState(){






        if ((getInventory().isFull() || getLocalPlayer().getHealthPercent() < 40)){

            if (BankLocation.ARDOUGNE_WEST.getArea(4).contains(getLocalPlayer())){
                return State.BANK;
            } else {
                return State.WALK_TO_BANK;
            }
        } else {
            if (getLocalPlayer().getHealthPercent() < 65 && getInventory().contains(sv.foodName)){
                return State.EAT;
            } else {
                if (!druidArea.contains(getLocalPlayer())){
                    return State.WALK_TO_TRAINING;
                } else {
                    if (sv.hop && playersInArea > 1){
                        log("Returning State.HOP");
                        return State.HOP;
                    } else {

                        if (sv.hop && playersInArea > 1) {
                            log("I shouldn't be reached, but here I am");
                        }
                        if (gi != null) {

                            return State.LOOT;
                        } else {
                            return State.FIGHT;
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onStart() {

        Gui gui = new Gui(sv);
        gui.setVisible(true);
        while (!sv.started){
            sleep(1000);
        }

        sv.uniqueLoot = new String[]{"Grimy avantoe", "Grimy irit leaf", "Grimy kwuarm", "Grimy ranarr weed", "Grimy lantadyme", "Grimy dwarf weed", "Grimy cadantine", "Ensouled chaos druid head", "Law rune", "Nature rune", "Mithril bolts"};
        sv.loot = Stream.of(sv.uniqueLoot, sv.universalLoot).flatMap(Stream::of).distinct().toArray(String[]::new);

        for (int i = 0; i < sv.loot.length; i++) {
            log("Looting: " + sv.loot[i]);
            lootTrack.add(new PricedItem(sv.loot[i], getClient().getMethodContext(), false));
        }



        getSkillTracker().start(Skill.DEFENCE);
        getSkillTracker().start(Skill.ATTACK);
        getSkillTracker().start(Skill.STRENGTH);
        getSkillTracker().start(Skill.RANGED);
        timer = new RunTimer();
        sv.testMode = testMode;
        if (sv.testMode){
            log("THIS IS A TESTING MODE");
        }
    }

    @Override
    public int onLoop() {
        if (!getCombat().isAutoRetaliateOn()){
            getCombat().toggleAutoRetaliate(true);
        }

        if(getLocalPlayer().isMoving() && getClient().getDestination() != null && getClient().getDestination().distance(getLocalPlayer()) > 3) {
            return Calculations.random(200,300);
        }

        if (!getWalking().isRunEnabled() && getWalking().getRunEnergy() > Calculations.random(30,50)){
            getWalking().toggleRun();
        }

        if (System.currentTimeMillis() - lastScanPlayerCount > 5000){
            lastScanPlayerCount = System.currentTimeMillis();
            playersInArea = countPlayers();
        }

        if (System.currentTimeMillis() - lastSearchGround > 1000 && druidArea.contains(getLocalPlayer())){
            lastSearchGround = System.currentTimeMillis();
            gi = getGroundItems().closest(itemFilter);
        }

        switch (getState()){
            case TEST:
                test();
                break;
            case FIGHT:
                fight();
                break;
            case LOOT:
                loot(gi);
                break;
            case WALK_TO_BANK:
                walkToBank();
                break;
            case WALK_TO_TRAINING:
                walkToTraining();
                break;
            case BANK:
                bank();
                break;
            case HOP:
                hop();
                break;
            case EAT:
                eat();
                break;
        }

        updateLoot();
        return Calculations.random(200,300);
    }
    private void test(){
        log ("This is a test and im being reached");

    }
    private boolean freeUpInventorySpace() {

        if (getInventory().isFull()){
            if (!getTabs().isOpen(Tab.INVENTORY)){
                getTabs().open(Tab.INVENTORY);
            }
            if (getInventory().contains(sv.foodName)){
                log("Freeing up inventory space by eating");
                getInventory().interact(sv.foodName, "Eat");
                sleepUntil(() -> !getInventory().isFull(), 1500);
                return true;
            } else if (getInventory().contains("Bones")){
                log("Freeing up inventory space by burying bones");
                getInventory().interact("Bones", "Bury");
                sleepUntil(() -> !getInventory().isFull(), 1500);
                return true;
            } else {
                return false;
            }

        } else {
            return true;
        }


    }
    private void hop() {
        log("hopping worlds, too many people");
        World newWorld = getWorlds().getRandomWorld(w -> w.isMembers() && !w.isHighRisk() && !w.isLastManStanding() && !w.isDeadmanMode() && !w.isPVP() && !w.equals(getClient().getCurrentWorld()) && w.getMinimumLevel() < getSkills().getTotalLevel());
        while (newWorld.getRealID() == getClient().getCurrentWorld()){
            newWorld = getWorlds().getRandomWorld(w -> w.isMembers() && !w.isHighRisk() && !w.isLastManStanding() && !w.isDeadmanMode() && !w.isPVP() && !w.equals(getClient().getCurrentWorld()) && w.getMinimumLevel() < getSkills().getTotalLevel());
        }
        log("Hopping worlds to " + newWorld.getRealID());
        getWorldHopper().quickHop(newWorld.getRealID());
        sleepUntil(() -> getClient().getInstance().getScriptManager().getCurrentScript().getRandomManager().isSolving(), 5000);
    }



    private int countPlayers(){
        Player[] players = getClient().getPlayers();
        int playersInArea = 0;
        for ( Player player : players ){
            if (druidArea.contains(player)){
                playersInArea++;
            }
        }
        return playersInArea;
    }

    private void eat() {
        if (!getTabs().isOpen(Tab.INVENTORY)){
            getTabs().open(Tab.INVENTORY);
        }
        if (getInventory().contains(sv.foodName)){
            if (!getTabs().isOpen(Tab.INVENTORY)){
                getTabs().open(Tab.INVENTORY);
                sleepUntil(() -> getTabs().isOpen(Tab.INVENTORY),1000);
            }
            getInventory().get(sv.foodName).interact("Eat");
            sleepUntil(() -> !getLocalPlayer().isAnimating(), 1000);
        }
    }
    private void fight(){


        if (getCamera().getPitch() < 275){
            getCamera().rotateToPitch(Calculations.random(275,375));
        }

        // Single combat area fighting
        // Pick a target

        if (selectNewTarget(druid)){

            druid = getNpcs().closest(n -> n.getName().equals("Chaos druid") && !n.isInCombat() && n.hasAction("Attack") && !n.isInteractedWith());
        }

        // Use special
        if (getCombat().getSpecialPercentage() == 100){
            getCombat().toggleSpecialAttack(true);
        }

        if (getLocalPlayer().isInCombat() || getLocalPlayer().isInteractedWith()){

            if (getLocalPlayer().isAnimating() || getLocalPlayer().isInCombat() || getLocalPlayer().isInteracting(druid)){
                antiban();
            } else {
                druid.interact("Attack");
            }
        } else {
            // new target
            if (druid != null && druid.getHealthPercent() > 0 && druid.exists()) {
                druid.interact("Attack");
                sleepUntil(druid::isInCombat, Calculations.random(400,800));
            }
        }
    }

    private boolean selectNewTarget(NPC target){
        // pick a new target when

        //  - my selected target is dead
        //  - my selected target is null
        if (target == null){
//            log ("Selecting new target because current target is null");
            return true;
        }

        if (!target.exists()){
//            log ("Selecting new target because current target does not exist");
            return true;
        }

        if (target.getHealthPercent() == 0 ){
//            log ("Selecting new target because current target has 0 hp");
            return true;
        }
//        if (target == null  || !target.exists() || target.getHealthPercent() == 0) {
//            return true;
//        }

        //  - if multi-combat, select new target as long as current selected target is not already interacting with me
        //  - if single-combat, select new target only if the current selected target is not being interacted with
        if ((!target.isInteracting(getLocalPlayer()) && target.isInCombat())) {
//            log ("Selecting new target because target is in combat but not interacting with me");
            return true;
        }

        return false;
    }
    private void loot(GroundItem gi){
        if (gi != null && getMap().canReach(gi)) {
            log("looting " + gi.getName());
            gi.interact("Take");
            lootTimerStart = System.currentTimeMillis();
            sleepUntil(() -> !gi.exists(), 2000);
            if (System.currentTimeMillis() - lootTimerStart > 1500){
                //taking too long to loot, probably stuck
                getCamera().rotateToYaw(Calculations.random(370,383));
                sleep(300);
                getWalking().walkExact(gi.getTile());
                log("Walking to item tile: " + gi.getTile().toString());
                log("Player tile: " + getLocalPlayer().getTile().toString());
                gi.interact();
                if (gi.exists()){
                    fight();
                }
            }
        }
    }

    private void walkToBank(){
        if (!freeUpInventorySpace()){
            if (druidArea.contains(getLocalPlayer())){
                GameObject door = getGameObjects().closest(11723);
                if (door != null){
                    door.interact("Open");
                    sleepUntil(() -> !druidArea.contains(getLocalPlayer()), 1500);
                }
            } else {
                getWalking().walk(BankLocation.ARDOUGNE_WEST.getArea(3).getRandomTile());

            }
        }

    }

    private void walkToTraining(){
        log("Not in druid area");
        if (getBank().isOpen()){
            getBank().close();
            sleepUntil(() -> !getBank().isOpen(), 1500);
        }

        // Check if player climbed down ladder by accident
        if (druidTowerDownStairs.contains(getLocalPlayer())){
            log("Went down stairs by accident, going up now");
            GameObject ladder = getGameObjects().closest(l -> l.getName().equals("Ladder") && l.hasAction("Climb-up"));
            if (ladder != null) {
                ladder.interact();
                sleepUntil(() -> druidArea.contains(getLocalPlayer()), 1500);
                log("Went up successfully");
            }
        }
        // Check of player climbed up ladder by accident
        if (druidTowerUpStairs.contains(getLocalPlayer())){
            log("Went upstairs by accident, going down now");
            GameObject ladder = getGameObjects().closest(l -> l.getName().equals("Ladder") && l.hasAction("Climb-down"));
            if (ladder != null){
                ladder.interact();
                sleepUntil(() -> druidArea.contains(getLocalPlayer()), 1500);
                log("Went downstairs successfully");
            }
        }

        GameObject door = getGameObjects().closest(11723);

        if (!druidArea.contains(getLocalPlayer())){
            if (getLocalPlayer().distance(door) < Calculations.random(4,6) && door != null ){
                log ("Near door, pick-locking that shit");
                door.interact("Pick-lock");
                sleepUntil(() -> druidArea.contains(getLocalPlayer()), 1500);
                if (druidArea.contains(getLocalPlayer())){
                    log ("Got in successfully");
                }


            } else {
                getWalking().walk(druidEntranceArea.getRandomTile());

            }
        }
        // reset the target
        druid = null;
    }

    private void bank(){
        if (getBank().isOpen()){
            log("depositing all items");
            getBank().depositAllItems();
            sleepUntil(() -> getInventory().isEmpty(), 1000);
            if (sv.requiredFoodAmt > 0){
                getBank().withdraw(sv.foodName, sv.requiredFoodAmt);
                sleepUntil(() -> getInventory().contains(sv.foodName), 1500);
            }
            getBank().close();

        } else {
            getBank().open(BankLocation.ARDOUGNE_WEST);
            sleepUntil(() -> getBank().isOpen(), 1500);
        }
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
                    getSkills().hoverSkill(Skill.ATTACK);
                } else if (random < 8) {
                    getSkills().hoverSkill(Skill.STRENGTH);
                } else if (random < 12) {
                    getSkills().hoverSkill(Skill.DEFENCE);
                } else if (random < 16) {
                    getSkills().hoverSkill(Skill.RANGED);
                } else {
                    getSkills().hoverSkill(Skill.HITPOINTS);
                }
                sleepUntil(()-> !getLocalPlayer().isInCombat() || !getLocalPlayer().isInteractedWith(),Calculations.random(300, 500));
            }
        } else if (random <= 30) {
            if (!getTabs().isOpen(Tab.INVENTORY)){
                getTabs().open(Tab.INVENTORY);
            }
        } else {
            if (getMouse().isMouseInScreen()){
                if (getMouse().moveMouseOutsideScreen()){
                    sleepUntil(()-> !getLocalPlayer().isInCombat() || !getLocalPlayer().isInteractedWith(),Calculations.random(500, 3300));
                }
            }
        }
    }

    public long getGainedExperience(){
        long atk = getSkillTracker().getGainedExperience(Skill.ATTACK);
        long str = getSkillTracker().getGainedExperience(Skill.STRENGTH);
        long def = getSkillTracker().getGainedExperience(Skill.DEFENCE);
        long range = getSkillTracker().getGainedExperience(Skill.RANGED);
        return atk + str + def + range;
    }

    @Override
    public void onPaint(Graphics g) {
        if (sv.started) {
            g.drawString("State: " + getState().toString(),5,10);
            g.drawString("Runtime: " + timer.format(), 5, 30);
            g.drawString("Experience (p/h): " + getGainedExperience() + "(" + timer.getPerHour(getGainedExperience()) + ")", 5, 45);
            g.drawString("Players in area: " + countPlayers(), 5,60);

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
