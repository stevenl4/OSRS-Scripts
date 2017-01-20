package main;


import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.prayer.Prayer;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.SceneObject;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import util.RunTimer;

import java.awt.*;
import java.util.List;




/**
 * Created by steven.luo on 16/01/2017.
 */

@ScriptManifest(category = Category.MINIGAME, name = "NMZ", author = "GB", version = 1.0, description = "Does NMZ")
public class Main extends AbstractScript {

    private boolean started;
    private long lastOverloadDose;
    private long lastPowerUpCheck;
    private boolean outOfPrayerPots = false;
    private boolean outOfOverloadPots = false;
    private boolean useSpec = false;
    private int lowPrayerThreshold = 20;
    private Area startArea = new Area (2601,3118,2612,3112,0);
    private Area dreamArea = new Area (0,100000,100000,0,3);
    private Area practiceArea = new Area (0, 100000, 100000, 0, 1);
    private Tile potionTile = new Tile(2605,3116,0);
    private Tile startTile;
    private Item mainWeapon;
    private Item mainShield;
    private Item specWeapon;
    private RunTimer timer;
    // TEST MODE
    private boolean testMode = false;
    // End TEST MODE
    private enum State {
        WALK_TO_START, GET_REQUIRED_ITEMS, START_DREAM, FIGHT, TEST
    }

    private State getState(){
//        Walk to starting area
//        check required items in inventory
//        start the dream
//        set a random standing tile
//        when activating a spark, set that as new tile
        if (testMode){
            return State.TEST;
        } else {
            if (!startArea.contains(getLocalPlayer()) && !dreamArea.contains(getLocalPlayer())) {
                return State.WALK_TO_START;
            }

            if (startArea.contains(getLocalPlayer())) {
                return State.START_DREAM;
            }

            if (dreamArea.contains(getLocalPlayer()) || practiceArea.contains(getLocalPlayer())){
                return State.FIGHT;
            } else {
                log("we are somewhere lost, stopping script");
                stop();
            }
        }

        return null;
    }

    @Override
    public void onStart() {
        if (!getCombat().isAutoRetaliateOn()) {
            log("turning on autoretaliate");
            getCombat().toggleAutoRetaliate(true);
        }
        lastOverloadDose = 0;
        lastPowerUpCheck = 0;
        mainWeapon = getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot());
        mainShield = getEquipment().getItemInSlot(EquipmentSlot.SHIELD.getSlot());
        specWeapon = getInventory().getItemInSlot(0);
        log("Main weapon: " + mainWeapon.getName());
        log("Main shield: " + mainShield.getName());
        log("Spec weapon: " + specWeapon.getName());
        getSkillTracker().start(Skill.DEFENCE);
        getSkillTracker().start(Skill.ATTACK);
        getSkillTracker().start(Skill.STRENGTH);
        getSkillTracker().start(Skill.HITPOINTS);
        getSkillTracker().start(Skill.RANGED);
        timer = new RunTimer();
        started = true;
    }

    @Override
    public int onLoop() {
        if(getLocalPlayer().isMoving() && getClient().getDestination() != null && getLocalPlayer().distance(getClient().getDestination()) > Calculations.random(2,3)){
            return Calculations.random(200,300);
        }


        switch (getState()){
            case WALK_TO_START:
                move();
                break;
            case START_DREAM:
                startDream();
                break;
            case FIGHT:
                fight();
                break;
            case TEST:
                test();
                // 2-3 second ticks for test mode
                return Calculations.random(2000,3000);
        }

        return Calculations.random(350, 550);
    }

    private void test(){
        log("X: " + getLocalPlayer().getX());
        log("X grid: " + getLocalPlayer().getGridX());
        log("X local: " + getLocalPlayer().getLocalX());
        stop();

    }
    private void move(){
        if (getWalking().walk(startArea.getRandomTile())){
            log("walking to starting area area");
            sleepUntil(() -> getClient().getDestination().distance(getLocalPlayer()) < Calculations.random(3,5) || getLocalPlayer().isStandingStill(), Calculations.random(900,2500));
        }
    }
    private void fight(){
        long timeSinceLastOverloadDose = System.currentTimeMillis() - lastOverloadDose;
        long timeSinceLastPowerUpCheck = System.currentTimeMillis() - lastPowerUpCheck;
        // Check Prayer
        if (!getPrayer().isActive(Prayer.PROTECT_FROM_MELEE)){
            log("turning on protect from melee");
            getPrayer().toggle(true, Prayer.PROTECT_FROM_MELEE);
        }

        if (!useSpec && (getSkills().getBoostedLevels(Skill.PRAYER) >= lowPrayerThreshold || outOfPrayerPots) && (timeSinceLastOverloadDose < 298000 || outOfOverloadPots)){
            antiban();
        }

        // Check prayer potion and set a level to use the next dose
        if (getSkills().getBoostedLevels(Skill.PRAYER) < lowPrayerThreshold && !outOfPrayerPots && !outOfOverloadPots) {
            outOfPrayerPots = true;
            for (int i = 1; i < 5; i ++){
                String potionName = "Prayer potion(" + i + ")";
                if (getInventory().contains(potionName)) {
                    getInventory().interact(potionName, "Drink");
                    sleepUntil(() -> getSkills().getBoostedLevels(Skill.PRAYER) >= lowPrayerThreshold, 800);
                    lowPrayerThreshold = Calculations.random(15,25);
                    outOfPrayerPots = false;
                    break;
                }
            }
        }
        // Drink Overload Potion
        if (timeSinceLastOverloadDose >= 300000 && getSkills().getBoostedLevels(Skill.HITPOINTS) > 60 && !outOfOverloadPots) {
            outOfOverloadPots = true;
            for (int i = 1; i < 5; i ++){
                String potionName = "Overload (" + i + ")";
                if (getInventory().contains(potionName)) {
                    if (getInventory().interact(potionName, "Drink")){
                        lastOverloadDose = System.currentTimeMillis();
                        sleepUntil(() -> getSkills().getBoostedLevels(Skill.STRENGTH) > getSkills().getRealLevel(Skill.STRENGTH), 800);
                        outOfOverloadPots = false;
                        break;
                    }
                }
            }
        }

        // Check for special spawns
        if (timeSinceLastPowerUpCheck >= 2000) {
            // Check Zapper
            log("Checking for zapper");

            GameObject goZapper = getGameObjects().closest(gO -> gO.getName().contains("Zapper") && gO.hasAction("Activate"));
            if (goZapper != null && goZapper.hasAction("Activate")) {
                log("Zapper found");
                getWalking().walk(goZapper);
                sleepUntil(() -> getLocalPlayer().distance(getClient().getDestination()) < 2, 5000);
                goZapper.interact();
                sleep(700);
                sleepUntil(() -> getLocalPlayer().isStandingStill(), 5000);
                log("Zapper activate");
            }

            // Check Power surge
            log("Checking for power surge");
            GameObject goPowerSurge = getGameObjects().closest(i -> i.getName().contains("Power surge") && i.hasAction("Activate"));
            if (goPowerSurge != null && goPowerSurge.hasAction("Activate")) {
                log("Power surge found");
                getWalking().walk(goPowerSurge);
                sleepUntil(() -> getLocalPlayer().distance(getClient().getDestination()) < 2, 5000);
                goPowerSurge.interact();
                sleep(700);
                sleepUntil(() -> getLocalPlayer().isStandingStill(), 5000);
                log("Power surge activated");
            }
            lastPowerUpCheck = System.currentTimeMillis();
        }
        // Start speccing
        if (getCombat().getSpecialPercentage() == 100) {
            useSpec = true;
        }

        if (getCombat().getSpecialPercentage() <= 20) {
            useSpec = false;
        }

        if (useSpec) {
            // Verify spec weapon is equipped
            if (!getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot()).getName().equals(specWeapon.getName())){
                getInventory().interact(specWeapon.getName(), "Wield");
                sleepUntil(() -> getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot()).getName().equals(specWeapon.getName()), 600);
            }

            if (!getCombat().isSpecialActive()){
                log("using special");
                sleep(Calculations.random(50,100));
                getCombat().toggleSpecialAttack(true);
            } else {
                log ("special clicked");
            }

        } else if (!useSpec) {
            // Switch to normal weapons
            if (getInventory().contains(mainWeapon.getName())){
                getInventory().interact(mainWeapon.getName(), "Wield");
                sleepUntil(() -> getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot()).getName().equals(mainWeapon.getName()), 600);
            }

            if (getInventory().contains(mainShield.getName())) {
                getInventory().interact(mainShield.getName(), "Wield");
                sleepUntil(() -> getEquipment().getItemInSlot(EquipmentSlot.SHIELD.getSlot()).getName().equals(mainWeapon.getName()), 600);
            }
        }

    }
    private void startDream() {
        NPC dominicOnion = getNpcs().closest("Dominic Onion");
        if (dominicOnion != null) {
            dominicOnion.interact("Dream");
            sleepUntil(() -> getLocalPlayer().isInteractedWith(),3000);

            if (getDialogues().getOptionIndex("Practice") > 0){
                getDialogues().clickOption("Rumble");
                sleepUntil(() -> getDialogues().getOptionIndex("Customisable - hard") > 0, 5000);
                if (getDialogues().getOptionIndex("Customisable - hard")>0){
                    getDialogues().clickOption("Customisable - hard");
                    sleepUntil(() -> getDialogues().canContinue(), 3000);
                    getDialogues().clickContinue();
                    sleepUntil(() -> getDialogues().getOptionIndex("Yes") > 0, 3000);
                    getDialogues().clickOption("Yes");
                    sleepUntil(() -> getDialogues().canContinue(), 3000);
                    getDialogues().clickContinue();
                    sleepUntil(() -> !getLocalPlayer().isInteractedWith(), 3000);
                    GameObject dreamPotion = getGameObjects().closest(26291);
                    if (dreamPotion != null){
                        dreamPotion.interact();
                        sleep( Calculations.random(6000,8000));
                        getWidgets().getWidget(129).getChild(6).getChild(9).interact();
                        sleepUntil(() -> dreamArea.contains(getLocalPlayer()), 5000);
                        log("got into dream");
                        sleep(Calculations.random(3000,5000));
                    }
                }

            } else {
                log("some dream already initiated");
                stop();
            }
        } else {
            log ("cannot find dream starter, stopping");
            stop();
        }
        startTile = getLocalPlayer().getTile();
        getPrayer().toggle(true, Prayer.PROTECT_FROM_MELEE);
        sleep(500);
        getPrayer().toggle(false, Prayer.PROTECT_FROM_MELEE);
        // Walk north
        int currentX = startTile.getX();
        int currentY = startTile.getY();
        Tile newStandingPosition = new Tile(currentX, currentY + Calculations.random(10,20));
        getWalking().walk(newStandingPosition);
        sleepUntil(() -> getLocalPlayer().isStandingStill(), 10000);
        // Chill for a few seconds before turning on prayer
        sleep(Calculations.random(5000,10000));
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
                sleep(Calculations.random(300, 600));
            }
        } else if (random <= 29) {
            if (!getTabs().isOpen(Tab.COMBAT)){
                getTabs().open(Tab.COMBAT);
                sleep(Calculations.random(100,500));
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
        if (started){
            g.drawString("State: " + getState().toString(), 5, 15);
            g.drawString("Runtime: " + timer.format(),5,30);
            g.drawString("Attack exp (p/h): " + getSkillTracker().getGainedExperience(Skill.ATTACK) + "(" + timer.getPerHour(getSkillTracker().getGainedExperience(Skill.ATTACK)) + ")",5,45);
            g.drawString("Strength exp (p/h): " + getSkillTracker().getGainedExperience(Skill.STRENGTH) + "(" + timer.getPerHour(getSkillTracker().getGainedExperience(Skill.STRENGTH)) + ")", 5,60);
            g.drawString("Def exp (p/h): " + getSkillTracker().getGainedExperience(Skill.DEFENCE) + "(" + timer.getPerHour(getSkillTracker().getGainedExperience(Skill.DEFENCE)) + ")", 5, 75);
            g.drawString("HP exp (p/h): " + getSkillTracker().getGainedExperience(Skill.HITPOINTS) + "(" + timer.getPerHour(getSkillTracker().getGainedExperience(Skill.HITPOINTS)) + ")", 5,90);
        }

    }
}
