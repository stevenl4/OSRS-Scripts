package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import util.PricedItem;
import util.RunTimer;
import util.ScriptVars;

import java.awt.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by steven.luo on 20/01/2017.
 */
public class Main extends AbstractScript {

    private Area druidArea = new Area();
    private Area druidEntranceArea = new Area();
    private Area shortcutWest = new Area();
    private Area shortcutEast = new Area();
    private boolean shortcutUsed;
    java.util.List<PricedItem> lootTrack = new ArrayList<PricedItem>();
    private long lastSearchGround;
    private long lootTimerStart;
    private boolean searchGround;
    ScriptVars sv = new ScriptVars();
    private RunTimer timer;

    Filter<GroundItem> itemFilter = new Filter<GroundItem>(){
        public boolean match(GroundItem gi){

            if (gi == null || !gi.exists() || gi.getName() == null){
                return false;
            }
            for (int i = 0; i < sv.loot.length; i++){
                if (gi.getName().equals(sv.loot[i])){
                    return true;
                }
            }

            return false;
        }
    };

    private enum State {
        WALK_TO_TRAINING, WALK_TO_BANK, FIGHT, LOOT, BANK
    }

    private State getState(){
        if (System.currentTimeMillis() - lastSearchGround > 1000){
            searchGround = true;
        }

        if (getInventory().isFull()){
            if (BankLocation.ARDOUGNE_WEST.getArea(4).contains(getLocalPlayer())){
                return State.BANK;
            } else {
                return State.WALK_TO_BANK;
            }
        }

        if (!druidArea.contains(getLocalPlayer())){
            return State.WALK_TO_TRAINING;
        } else {
            if (searchGround){
                GroundItem gi = getGroundItems().closest(itemFilter);
                lastSearchGround = System.currentTimeMillis();
                searchGround = false;
                if (gi != null ){
                    return  State.LOOT;
                }
            }
            return State.FIGHT;
        }
    }
    @Override
    public void onStart() {
        sv.uniqueLoot = new String[]{"Grimy avantoe", "Grimy irit leaf", "Grimy kwuarm", "Grimy ranarr weed", "Grimy lantadyme", "Grimy dwarf weed", "Grimy cadantine", "Law rune", "Nature rune", "Mithril bolts"};
        sv.loot = Stream.of(sv.uniqueLoot, sv.universalLoot).flatMap(Stream::of).distinct().toArray(String[]::new);

        for (int i = 0; i < sv.loot.length; i++) {
            lootTrack.add(new PricedItem(sv.loot[i], getClient().getMethodContext(), false));
        }

        if (!getCombat().isAutoRetaliateOn()){
            getCombat().toggleAutoRetaliate(true);
        }

        if (getSkills().getRealLevel(Skill.AGILITY) > 31){
            sv.useShortcut = true;
        }
        getSkillTracker().start(Skill.DEFENCE);
        getSkillTracker().start(Skill.ATTACK);
        getSkillTracker().start(Skill.STRENGTH);
        getSkillTracker().start(Skill.RANGED);
        timer = new RunTimer();
        sv.started = true;
    }

    @Override
    public int onLoop() {
        if(getLocalPlayer().isMoving() && getClient().getDestination() != null && getClient().getDestination().distance(getLocalPlayer()) > 3) {
            return Calculations.random(200,300);
        }

        if (!getWalking().isRunEnabled() && getWalking().getRunEnergy() > Calculations.random(30,50)){
            getWalking().toggleRun();
        }

        switch (getState()){
            case FIGHT:
                fight();
                break;
            case LOOT:
                loot();
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
        }
        updateLoot();
        return Calculations.random(400,600);
    }

    private void fight(){

        NPC druid = getNpcs().closest(n -> n.getName().equals("Chaos druid") &&
                                            !n.isInCombat() &&
                                            n.hasAction("Attack"));
        if (getLocalPlayer().isInCombat()){
            antiban();
        } else {
            if (druid != null) {
                druid.interact("Attack");
                sleepUntil(() -> druid.isInCombat(), 2000);
            }
        }
    }

    private void loot(){
        final GroundItem gi = getGroundItems().closest(itemFilter);
        if (gi != null && getMap().canReach(gi)) {
            log("looting " + gi.getName());
            gi.interact("Take");
            lootTimerStart = System.currentTimeMillis();
            sleepUntil(() -> !gi.exists(), 3000);
            if (System.currentTimeMillis() - lootTimerStart > 5000){
                //taking too long to loot, probably stuck
                getWalking().walk(gi.getTile());
            }
            if (!gi.exists()) {
                log("looted " + gi.getName());
            }
        }
    }

    private void walkToBank(){
        if (druidArea.contains(getLocalPlayer())){
            GameObject door = getGameObjects().closest(11723);
            if (door != null){
                door.interact("Open");
                sleepUntil(() -> !druidArea.contains(getLocalPlayer()), 1500);
            }
        } else {
            if (sv.useShortcut){
                if (!shortcutUsed){
                    // Walk towards West side of shortcut
                    if (shortcutWest.contains(getLocalPlayer())){
                        // TODO: Use shortcut to get to bank
                        sleepUntil(() -> shortcutEast.contains(getLocalPlayer()), 3000);
                    }
                    if (shortcutEast.contains(getLocalPlayer())){
                        shortcutUsed = true;
                    }

                } else {
                    getWalking().walk(BankLocation.ARDOUGNE_WEST.getArea(3).getRandomTile());
                }

            } else {
                getWalking().walk(BankLocation.ARDOUGNE_WEST.getArea(3).getRandomTile());
            }

        }
    }

    private void walkToTraining(){

        if (getBank().isOpen()){
            getBank().close();
            sleepUntil(() -> !getBank().isOpen(), 1500);
        }

        // Check if player climbed up ladder by accident

        // Check of player climbed down ladder by accident

        GameObject door = getGameObjects().closest(11723);
        if (getLocalPlayer().distance(door) < Calculations.random(4,6) && door != null){
            door.interact("Pick-lock");
            sleepUntil(() -> druidArea.contains(getLocalPlayer()), 1500);
        } else {
            // Check if shortcut is needed
            if (sv.useShortcut){
                if (!shortcutUsed){
                    // Walk to East side of short cut
                    if (shortcutEast.contains(getLocalPlayer())){
                        // TODO: Use shortcut to get to training

                        sleepUntil(() -> shortcutWest.contains(getLocalPlayer()),3000);
                    }
                    if (shortcutWest.contains(getLocalPlayer())){
                        shortcutUsed = true;
                    }

                } else {
                    getWalking().walk(druidEntranceArea.getRandomTile());
                }
            } else {
                getWalking().walk(druidEntranceArea.getRandomTile());
            }

        }
    }

    private void bank(){
        if (getBank().isOpen()){
            log("depositing all items");
            getBank().depositAllItems();
            sleepUntil(() -> getInventory().isEmpty(), 1000);
            shortcutUsed = false;
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
                sleepUntil(()-> !getLocalPlayer().isInCombat() || !getLocalPlayer().isAnimating(),Calculations.random(300, 500));
            }
        } else if (random <= 25) {
            if (!getTabs().isOpen(Tab.INVENTORY)){
                getTabs().open(Tab.INVENTORY);
            }
        } else {
            if (getMouse().isMouseInScreen()){
                if (getMouse().moveMouseOutsideScreen()){
                    sleepUntil(()-> !getLocalPlayer().isInCombat() || !getLocalPlayer().isAnimating(),Calculations.random(500, 3300));
                }
            }
        }
    }
    @Override
    public void onPaint(Graphics graphics) {
        super.onPaint(graphics);
    }
}
