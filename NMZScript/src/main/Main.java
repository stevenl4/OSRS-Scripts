package main;


import gui.Gui;
import gui.NmzGui;
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
import util.ScriptVars;

import java.awt.*;
import java.util.List;




/**
 * Created by steven.luo on 16/01/2017.
 */

@ScriptManifest(category = Category.MINIGAME, name = "NMZ Fighter", author = "GB", version = 1.1, description = "have Spec weapon in inventory slot 1, have potions ready")
public class Main extends AbstractScript {


    private long lastOverloadDose;
    private long lastRapidHeal;
    private long lastPowerUpCheck;
    private long lastPowerSurge;
    private long nextRapidHealFlick;
    private long dreamStartTimer;

    private long timeSinceLastOverloadDose;
    private long timeSinceLastPowerUpCheck;
    private long timeSinceLastPowerSurge;
    private long timeSinceLastRapidHeal;
    private long absorptionPointsLeft;
    private boolean outOfPrayerPots = false;
    private boolean outOfOverloadPots = false;
    private boolean useSpec = false;

    private int lowPrayerThreshold = 10;
    private int lowAbsorptionThreshold = 100;
    private Area startArea = new Area (2601,3118,2612,3112,0);
    private Area dreamArea = new Area (0,100000,100000,0,3);
    private Area practiceArea = new Area (0, 100000, 100000, 0, 1);
    private Tile startTile;
    private Item mainWeapon;
    private Item mainShield;
    private Item specWeapon;
    private RunTimer timer;
    private ScriptVars sv = new ScriptVars();

    private enum State {
        WALK_TO_START, GET_REQUIRED_ITEMS, START_DREAM, FIGHT, TEST
    }

    private State getState(){
//        Walk to starting area
//        check required items in inventory
//        start the dream
//        set a random standing tile
//        when activating a spark, set that as new tile
        if (sv.testMode){
            return State.TEST;
        } else {
            // Verify there is overload potion and prayer potion and overload potion
            if (!verifyEquipment()){
                return State.GET_REQUIRED_ITEMS;
            }

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

        NmzGui gui = new NmzGui(sv);
        gui.setVisible(true);
        while (!sv.started){
            sleep(1000);
        }

        if (!getCombat().isAutoRetaliateOn()) {
            log("turning on autoretaliate");
            getCombat().toggleAutoRetaliate(true);
        }
        lastOverloadDose = 0;
        lastPowerUpCheck = 0;
        mainWeapon = getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot());
        log("Main weapon: " + mainWeapon.getName());
        if (getEquipment().getItemInSlot(EquipmentSlot.SHIELD.getSlot()) != null){
            mainShield = getEquipment().getItemInSlot(EquipmentSlot.SHIELD.getSlot());
            log("Main shield: " + mainShield.getName());
        } else {
            mainShield = null;
        }

        if (getInventory().getItemInSlot(0).hasAction("Wield")){
            specWeapon = getInventory().getItemInSlot(0);
            log("Spec weapon: " + specWeapon.getName());
        } else {
            specWeapon = null;
        }

        // Toggle off absoprtionMethod if prayerMethod is selected

        log("Absorption Method: " + sv.absorptionMethod);
        log("Prayer Method: " + sv.prayerMethod);

        getSkillTracker().start(Skill.DEFENCE);
        getSkillTracker().start(Skill.ATTACK);
        getSkillTracker().start(Skill.STRENGTH);
        getSkillTracker().start(Skill.HITPOINTS);
        getSkillTracker().start(Skill.RANGED);
        timer = new RunTimer();



    }

    @Override
    public int onLoop() {
        if(getLocalPlayer().isMoving() && getClient().getDestination() != null && getLocalPlayer().distance(getClient().getDestination()) > Calculations.random(2,3)){
            return Calculations.random(200,300);
        }

        if (!dreamArea.contains(getLocalPlayer())){
            if (getPrayer().isActive(Prayer.PROTECT_FROM_MELEE)){
                getPrayer().toggle(false, Prayer.PROTECT_FROM_MELEE);
            }

            if (getPrayer().isActive(Prayer.RAPID_HEAL)){
                getPrayer().toggle(false,Prayer.RAPID_HEAL);
            }
        }

        if (getCamera().getYaw() < 300){
            getCamera().rotateToPitch(Calculations.random(301,383));
        }
        switch (getState()){
            case GET_REQUIRED_ITEMS:
                getRequiredEquipment();
                break;
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
        long absoprtionPointsLeft;

        if (getWidgets().getWidget(202).getChild(1).getChild(9).getText() != ""){
            log("Text:" +getWidgets().getWidget(202).getChild(1).getChild(9).getText()+ "[end]");
            absoprtionPointsLeft = Integer.parseInt(getWidgets().getWidget(202).getChild(1).getChild(9).getText());
        } else {
            absoprtionPointsLeft = 0;
        }

        log("AbsorptionPointsLeft: " + absoprtionPointsLeft);

    }
    private void move(){
        if (getWalking().walk(startArea.getRandomTile())){
            log("walking to starting area area");
            sleepUntil(() -> getClient().getDestination().distance(getLocalPlayer()) < Calculations.random(3,5) || getLocalPlayer().isStandingStill(), Calculations.random(900,2500));
    }
    }
    private void fight(){
        timeSinceLastOverloadDose = System.currentTimeMillis() - lastOverloadDose;
        timeSinceLastPowerUpCheck = System.currentTimeMillis() - lastPowerUpCheck;
        timeSinceLastPowerSurge = System.currentTimeMillis() - lastPowerSurge;
        timeSinceLastRapidHeal = System.currentTimeMillis() - lastRapidHeal;


        // Check Prayer
        if (sv.prayerMethod){
            if (!getPrayer().isActive(Prayer.PROTECT_FROM_MELEE) && System.currentTimeMillis() - dreamStartTimer > 25000) {
                getPrayer().toggle(true, Prayer.PROTECT_FROM_MELEE);
            }
        }

        if (sv.absorptionMethod){
            if (getPrayer().isActive(Prayer.RAPID_HEAL)){
                getPrayer().toggle(false, Prayer.RAPID_HEAL);
                sleepUntil(() -> !getPrayer().isActive(Prayer.RAPID_HEAL), 300);
            }

            if (getWidgets().getWidget(202).getChild(1).getChild(9).getText() != ""){
                absorptionPointsLeft = Integer.parseInt(getWidgets().getWidget(202).getChild(1).getChild(9).getText());
            } else {
                absorptionPointsLeft = 0;
            }
            // Check prayer, make sure prayer can be turned on
            // Turn it on near the end, when you have less than 20 absorptions left
            if ((!getPrayer().isActive(Prayer.PROTECT_FROM_MELEE) && (getSkills().getBoostedLevels(Skill.HITPOINTS) > sv.maxHp + 1) || absorptionPointsLeft < 20)){
                if (System.currentTimeMillis() - dreamStartTimer > 25000){
                    if (getSkills().getBoostedLevels(Skill.PRAYER) > 0 ){
                        log("Turning on Prayer because hp > " + sv.maxHp);
                        getPrayer().toggle(true, Prayer.PROTECT_FROM_MELEE);
                        sleepUntil(() -> getPrayer().isActive(Prayer.PROTECT_FROM_MELEE), 400);
                    }
                }

            }

            // If during absorption potion phase, turn off when equal to maxHp
            // If out of absorption phase, turn off when at least 20 absorptionpoints
            if (getPrayer().isActive(Prayer.PROTECT_FROM_MELEE)){
                if (!outOfOverloadPots){
                    if (getSkills().getBoostedLevels(Skill.HITPOINTS) <= sv.maxHp){
                        getPrayer().toggle(false, Prayer.PROTECT_FROM_MELEE);
                    }
                } else {
                    if (absorptionPointsLeft >= 20){
                        getPrayer().toggle(false, Prayer.PROTECT_FROM_MELEE);
                    }
                }
            }

            if (getPrayer().isActive(Prayer.PROTECT_FROM_MELEE) && getSkills().getBoostedLevels(Skill.HITPOINTS) <= sv.maxHp && !outOfOverloadPots){
                getPrayer().toggle(false, Prayer.PROTECT_FROM_MELEE);
            }

            // Guzzle cake only when between 1 and 51 hp
            if ((getSkills().getBoostedLevels(Skill.HITPOINTS) > sv.maxHp && getSkills().getBoostedLevels(Skill.HITPOINTS) < 51) || outOfOverloadPots){
                if ((timeSinceLastOverloadDose > 8000 && timeSinceLastOverloadDose < 299000) || (outOfOverloadPots && getSkills().getBoostedLevels(Skill.HITPOINTS) > sv.maxHp)) {
                    getInventory().interact("Dwarven rock cake", "Guzzle");
                }
            }

            // Drink Absorption Potion
            if (sv.exitWhenOutOfOverload && outOfOverloadPots){

            } else {

                if (absorptionPointsLeft < lowAbsorptionThreshold){
                    for (int i = 1; i < 5; i++){
                        String potionName = "Absorption (" + i + ")";
                        if (getInventory().contains(potionName)) {
                            if (getInventory().interact(potionName, "Drink")){
                                lowAbsorptionThreshold = Calculations.random(65, 135);
                                break;
                            }
                        }
                    }
                }
            }

            if (timeSinceLastRapidHeal > nextRapidHealFlick) {
                getPrayer().flick(Prayer.RAPID_HEAL, Calculations.random(250,350));
                nextRapidHealFlick = Calculations.random(40000,50000);
                lastRapidHeal = System.currentTimeMillis();
            }

        }

        // Check when to use antiban
        if (!useSpec && (timeSinceLastOverloadDose < 298000 || outOfOverloadPots)){
            if ((sv.prayerMethod && (getSkills().getBoostedLevels(Skill.PRAYER) >= lowPrayerThreshold) || outOfPrayerPots)){
                antiban();
            }

            if (sv.absorptionMethod && getSkills().getBoostedLevels(Skill.HITPOINTS) <= sv.maxHp){
                antiban();
            }
        }

        // Check prayer potion and set a level to use the next dose
        if (getSkills().getBoostedLevels(Skill.PRAYER) < lowPrayerThreshold && !outOfPrayerPots) {
            if (!sv.exitWhenOutOfOverload || !outOfOverloadPots && sv.exitWhenOutOfOverload){
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
        }

        // Drink Overload Potion
        if (timeSinceLastOverloadDose >= 300000 && getSkills().getBoostedLevels(Skill.HITPOINTS) >= 51 && !outOfOverloadPots) {
            outOfOverloadPots = true;
            for (int i = 1; i < 5; i ++){
                String potionName = "Overload (" + i + ")";
                if (getInventory().contains(potionName)) {
                    if (getInventory().interact(potionName, "Drink")){
                        log("Drinking overload");
                        lastOverloadDose = System.currentTimeMillis();
                        sleepUntil(() -> getSkills().getBoostedLevels(Skill.STRENGTH) > getSkills().getRealLevel(Skill.STRENGTH), 800);
                        outOfOverloadPots = false;
                        break;
                    }
                }
            }
        }


        // Check for special spawns
        if (timeSinceLastPowerUpCheck >= 1500) {
            // Check Zapper
            GameObject goZapper = getGameObjects().closest(gO -> gO.getName().contains("Zapper") && gO.hasAction("Activate"));
            if (goZapper != null && goZapper.hasAction("Activate") && sv.useZapper) {
                log("Zapper found");
                getWalking().walk(goZapper);
                sleepUntil(() -> getLocalPlayer().distance(getClient().getDestination()) < 3, 3000);
                goZapper.interact();
                sleep(700);
                sleepUntil(() -> !goZapper.exists(), 1000);
                log("Zapper activate");
                getCamera().rotateToPitch(Calculations.random(350,383));
            }

            // Check recurrent damange
            GameObject goRecurrentDamage = getGameObjects().closest(gO -> gO.getName().contains("Recurrent damage") && gO.hasAction("Activate"));
            if (goRecurrentDamage != null && goRecurrentDamage.hasAction("Activate") && sv.useConcurrentDamage){
                log("Recurrent damage found");
                getWalking().walk(goRecurrentDamage);
                sleepUntil(() -> getLocalPlayer().distance(getClient().getDestination()) < 3,3000);
                goRecurrentDamage.interact();
                sleep(700);
                sleepUntil(() -> !goRecurrentDamage.exists(), 1000);
                log("Recurrent damage activated");
                getCamera().rotateToPitch(Calculations.random(340, 383));

            }

            // Check Power surge
            GameObject goPowerSurge = getGameObjects().closest(i -> i.getName().contains("Power surge") && i.hasAction("Activate"));
            if (goPowerSurge != null && goPowerSurge.hasAction("Activate") && sv.usePowerSurge) {
                log("Power surge found");
                getWalking().walk(goPowerSurge);
                sleepUntil(() -> getLocalPlayer().distance(getClient().getDestination()) < 3, 3000);
                goPowerSurge.interact();
                sleep(700);
                sleepUntil(() -> !goPowerSurge.exists(), 1000);
                log("Power surge activated");
                getCamera().rotateToPitch(Calculations.random(350,383));
                if (!goPowerSurge.exists()){
                    lastPowerSurge = System.currentTimeMillis();
                }
            }
            lastPowerUpCheck = System.currentTimeMillis();
        }


        // Start speccing
        if (sv.useSpecialOnlyOnPowerUp){
            if (timeSinceLastPowerSurge <= 46000 ) {
                useSpec = true;
            } else {
                useSpec = false;
            }
        } else {
            if (getCombat().getSpecialPercentage() == 100 || !(getCombat().getSpecialPercentage() < sv.specMinPercent) || timeSinceLastPowerSurge <= 46000 ) {
                useSpec = true;
            } else {
                useSpec = false;
            }
        }



        if (useSpec) {
            // Verify spec weapon is equipped
            if (specWeapon != null){
                if (!getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot()).getName().equals(specWeapon.getName())){
                    getInventory().interact(specWeapon.getName(), "Wield");
                    sleepUntil(() -> getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot()).getName().equals(specWeapon.getName()), 600);
                }
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

            if (mainShield != null){
                if (getInventory().contains(mainShield.getName())) {
                    getInventory().interact(mainShield.getName(), "Wield");
                    sleepUntil(() -> getEquipment().getItemInSlot(EquipmentSlot.SHIELD.getSlot()).getName().equals(mainWeapon.getName()), 600);
                }
            }

        }

    }
    private boolean verifyEquipment() {
        // TODO select potions in GUI and match here
        if (dreamArea.contains(getLocalPlayer())){
            return true;
        } else {
            // Check overload
            if (getInventory().contains("Overload (4)")){
                if (sv.prayerMethod) {
                    if (getInventory().contains("Prayer potion(4)")){
                        return true;
                    } else {
                        return false;
                    }
                } else if (sv.absorptionMethod){
                    if (getInventory().contains("Absorption (4)") && getInventory().contains("Dwarven rock cake")){
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    private void getRequiredEquipment() {
        // TODO get potions
        // Getting potions
        log("restock not available yet, stopping script");
        stop();

        // Get overloads
        // Get prayer
        // Get absorption
    }
    private void startDream() {
        dreamStartTimer = 0;
        NPC dominicOnion = getNpcs().closest("Dominic Onion");
        if (dominicOnion != null) {
            dominicOnion.interact("Dream");
            sleepUntil(() -> getDialogues().getOptionIndex("Rumble") > 0,10000);

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
        log("player starting X: " + startTile.getX() + " | Y: " + startTile.getY());

        // Walk north
        int currentX = startTile.getX();
        int currentY = startTile.getY();
        Tile newStandingPosition = new Tile(currentX - Calculations.random(5,10), currentY + Calculations.random(10,20), 3);
        getWalking().walk(newStandingPosition);
        sleepUntil(() -> getLocalPlayer().getTile().distance(startTile) > 0 , 3000);
        dreamStartTimer = System.currentTimeMillis();
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
        if (sv.started){
            g.drawString("State: " + getState().toString(), 5, 15);
            g.drawString("Runtime: " + timer.format(),5,30);
            g.drawString("Attack exp (p/h): " + getSkillTracker().getGainedExperience(Skill.ATTACK) + "(" + timer.getPerHour(getSkillTracker().getGainedExperience(Skill.ATTACK)) + ")",5,45);
            g.drawString("Strength exp (p/h): " + getSkillTracker().getGainedExperience(Skill.STRENGTH) + "(" + timer.getPerHour(getSkillTracker().getGainedExperience(Skill.STRENGTH)) + ")", 5,60);
            g.drawString("Def exp (p/h): " + getSkillTracker().getGainedExperience(Skill.DEFENCE) + "(" + timer.getPerHour(getSkillTracker().getGainedExperience(Skill.DEFENCE)) + ")", 5, 75);
            g.drawString("HP exp (p/h): " + getSkillTracker().getGainedExperience(Skill.HITPOINTS) + "(" + timer.getPerHour(getSkillTracker().getGainedExperience(Skill.HITPOINTS)) + ")", 5,90);
            g.drawString("Ranged exp (p/h): " + getSkillTracker().getGainedExperience(Skill.RANGED) + "(" + timer.getPerHour(getSkillTracker().getGainedExperience(Skill.RANGED)) + ")", 5, 105);

            g.drawString("Next prayer drink: " + lowPrayerThreshold, 5, 220);
            g.drawString("Next absorption drink: " + lowAbsorptionThreshold, 5, 235);
            g.drawString("Time since last flick: " + nextRapidHealFlick, 5, 250);
            g.drawString("Overload timer: " + timeSinceLastOverloadDose, 5, 265);

        }

    }
}
