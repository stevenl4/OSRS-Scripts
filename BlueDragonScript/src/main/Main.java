package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import util.PricedItem;
import util.RunTimer;
import util.ScriptVars;

import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Created by Steven on 2/12/2017.
 */

/**
 * on start - determine bank mode
 * if true, get required items
 * otherwise, walk to training area
 * search predefined spots for dragons
 * determine bank mode
 *
 * BANK MODE CAN ONLY BE TOGGLED AT 3 PLACES, including automatically set at start
 * 1) When HP is low and out of food -> true
 * 2) When Inventory is full and can't be freed up -> true
 * 3) After withdrawing all necessary items for a trip -> false
 */

@ScriptManifest(category = Category.COMBAT, name = "Blue Dragon Script", author = "GB" , version = 1.0, description = "Kill blue dragons en orge enclave")
public class Main extends AbstractScript {

    private NPC blueDragon;
    private Area broadBlueDragonArea = new Area(2550,9466,2612,9430,0);
    private Area blueDragonArea = new Area (2560,9452,2582,9433,0);
    private Area caveEntranceArea = new Area (2506,3042,2510,3038,0);
    private Area caveInsideArea = new Area(2586,9420,2591, 9408);
    private Tile cityEntranceTile = new Tile(2527,3072,0);
    private Area cityGateOutsideArea = new Area(2504,3063,2506,3062,0);
    private Area cityGateInsideArea = new Area(2503,3063,2503,3062,0);
    private Tile blueDragonSpawn0 = new Tile(2579,9446,0);
    private Tile blueDragonSpawn1 = new Tile(2569,9441,0);
    private Tile blueDragonSpawn2 = new Tile(2609,9461,0);
    private Tile blueDragonSpawn3 = new Tile();
    private Tile blueDragonSpawn4 = new Tile(2597,9432,0);

    private Area trainingArea;

    private java.util.List<Area> trainingAreas = new ArrayList<>();

    private long lastScanPlayerCount;
    private long lastAnimation;
    private long timeSinceLastAnimation;
    private long lastSearchGround;
    private long timeSinceLastSearchGround;
    private long lastAntifireDose;
    private long timeSinceLastAntifireDose;
    private long lootWaitTime;
    private java.util.List<PricedItem> lootTrack = new ArrayList<>();
    private GroundItem gi;
    ScriptVars sv = new ScriptVars();
    private RunTimer timer;
    private boolean bankMode;
    private boolean foodFound;

    private boolean cityEntranceReached;
    private boolean gatePassed;
    private boolean caveEntered;

    private enum State {
        WALK_TO_TRAINING, WALK_TO_BANK, FIGHT, LOOT, BANK, HOP
    }

    private Filter<GroundItem> itemFilter = gi -> {

        if (gi == null || !gi.exists() || gi.getName() == null){
            return false;
        }
        for (int i = 0; i < sv.loot.length; i++){
            if (gi.getName().equals(sv.loot[i]) && getLocalPlayer().getTile().getArea(sv.lootRadius).contains(gi)){
                return true;
            }
        }
        return false;
    };

    private State getState(){

        if (bankMode) {
            if (BankLocation.CASTLE_WARS.getArea(10).contains(getLocalPlayer())){
                return State.BANK;
            } else {
                return State.WALK_TO_BANK;
            }
        } else {

            if (!trainingArea.contains(getLocalPlayer())){
                return State.WALK_TO_TRAINING;
            } else {
               if (gi != null){
                   return State.LOOT;
               } else {
                   return State.FIGHT;
               }
            }
        }

    }
    @Override
    public void onStart() {

        sv.uniqueLoot = new String[] {"Ensouled dragon head","Dragon bones","Blue dragonhide", "Rune dagger", "Nature rune", "Grimy ranarr weed", "Grimy kwuarm", "Grimy avantoe" };
        for (Skill s : Skill.values()){
            getSkillTracker().start(s);
        }
        sv.loot = Stream.of(sv.uniqueLoot, sv.universalLoot).flatMap(Stream::of).distinct().toArray(String[]::new);
        sv.foodName = "Tuna";
        sv.requiredFoodAmt = 20;
        sv.useAntifirePot = false;
        sv.useCombatPot = true;
        timer = new RunTimer();
        if (broadBlueDragonArea.contains(getLocalPlayer())){
            bankMode = false;
        } else {

            bankMode = true;
        }
        for (int i = 0; i < sv.loot.length; i ++){
            log("Looting: " + sv.loot[i]);
            lootTrack.add(new PricedItem(sv.loot[i], getClient().getMethodContext(), false));
        }

        sv.started = true;
        blueDragon = null;
    }

    @Override
    public int onLoop() {
        // walking realism
        if(getLocalPlayer().isMoving() && getClient().getDestination() != null && getLocalPlayer().distance(getClient().getDestination()) > Calculations.random(2,3)){
            return Calculations.random(200,300);
        }

        // toggle run
        if (getWalking().getRunEnergy() > 30 && !getWalking().isRunEnabled()){
            getWalking().toggleRun();
        }

        if (!freeUpInventorySpace()){
            bankMode = true;
        }

        if (getLocalPlayer().isAnimating()){
            lastAnimation = System.currentTimeMillis();
        }


        // Set training area
        trainingArea = blueDragonSpawn0.getArea(5);
        timeSinceLastSearchGround = System.currentTimeMillis() - lastSearchGround;
        timeSinceLastAntifireDose = System.currentTimeMillis() - lastAntifireDose;
        timeSinceLastAnimation = System.currentTimeMillis() - lastAnimation;

        if (timeSinceLastSearchGround > 1500 && blueDragonArea.contains(getLocalPlayer())){
            log("searching GI from onLoop");
            lastSearchGround = System.currentTimeMillis();
            gi = getGroundItems().closest(itemFilter);
        }

        switch (getState()){
            case BANK:
                bank();
                break;
            case FIGHT:
                fight();
            case HOP:
            case LOOT:
                loot(gi);
                break;
            case WALK_TO_BANK:
                walkToBank();
                break;
            case WALK_TO_TRAINING:
                walkToTraining();
                break;
        }
        updateLoot();
        return Calculations.random(500,600);
    }

    private void walkToTraining(){

        if (broadBlueDragonArea.contains(getLocalPlayer())){
            getWalking().walk(trainingArea.getRandomTile());
        } else {

            if (!cityEntranceReached) {
                if (cityEntranceTile.getArea(2).contains(getLocalPlayer())) {
                    cityEntranceReached = true;
                } else {
                    log("Walking to city entrance");
                    getWalking().walk(cityEntranceTile);
                }
            } else {
                if (!gatePassed){
                    if (cityGateOutsideArea.contains(getLocalPlayer())){
                        // City Gate
                        GameObject cityGate = getGameObjects().closest(2788);
                        cityGate.interact("Open");
                        sleepUntil(() -> cityGateInsideArea.contains(getLocalPlayer()), 2500);
                        if (cityGateInsideArea.contains(getLocalPlayer())){
                            log ("passed city gate");
                            gatePassed = true;
                        }
                    } else {
                        log("Walking to city gate");
                        getWalking().walk(cityGateOutsideArea.getRandomTile());
                    }
                } else {
                    if (!caveEntered){
                        if (caveEntranceArea.contains(getLocalPlayer())){
                            // Cave Entrance
                            GameObject caveEntrance = getGameObjects().closest(2804);
                            caveEntrance.interact("Enter");
                            sleepUntil(() -> caveInsideArea.contains(getLocalPlayer()), 2500);
                            if (caveInsideArea.contains(getLocalPlayer())){

                                caveEntered = true;
                            }
                        } else {
                            log("Walking to cave");
                            getWalking().walk(caveEntranceArea.getRandomTile());
                        }
                    } else {
                        log("Walking to dragon spawn");
                        getWalking().walk(trainingArea.getRandomTile());
                    }
                }
            }
        }

    }


    private void fight(){

        // auto retaliate
        if (!getCombat().isAutoRetaliateOn()){
            getCombat().toggleAutoRetaliate(true);
        }

        // Relegate heal to outside of combat, eat until 80%
        // if need to eat during combat, eat at 50%, if no food, leave at 50%;
        if (getCombat().getHealthPercent() < 80 && !getLocalPlayer().isInCombat()){
            if (getInventory().contains(sv.foodName)){
                getInventory().interact(sv.foodName, "Eat");
            }
        }
        if (getCombat().getHealthPercent() < 50){
            if (getInventory().contains(sv.foodName)){
                getInventory().interact(sv.foodName, "Eat");
            } else {
                bankMode = true;
            }
        }


        // Wait for loot
        if (blueDragon != null){
            if (sv.waitForLoot && blueDragon.getHealthPercent() == 0 && blueDragon.exists()){
                lootWaitTime = 0;

                while (lootWaitTime <= 4000){
                    log("searching for ground item from fight");
                    gi = getGroundItems().closest(itemFilter);
                    lootWaitTime += 500;
                    sleep(500);
                    log ("Waiting: " + lootWaitTime);
                    if (gi != null){
                        log("ground item found");
                        return;
                    }
                }
            }
        }


        // Pots
        if (sv.useCombatPot && getSkills().getBoostedLevels(Skill.STRENGTH) == getSkills().getRealLevel(Skill.STRENGTH)){
            for (int i = 1; i < 5; i++){
                String potName = "Combat potion(" + i + ")";
                if (getInventory().contains(potName)){
                    getInventory().interact(potName, "Drink");
                    break;
                }
            }
        }

        if (sv.useAntifirePot && timeSinceLastAntifireDose > 690000){
            for (int i = 1; i < 5; i++){
                String potName = "Extended antifire (" + i + ")";
                if (getInventory().contains(potName)){
                    if (getInventory().interact(potName, "Drink")){
                        lastAntifireDose = System.currentTimeMillis();
                    }
                    break;
                }
            }
        }

        if (selectNewTarget(blueDragon)){
            log("Selecting new target");
            blueDragon = getNpcs().closest(n -> n.getName().equals("Blue dragon") && ((!n.isInCombat() &&
                                            n.hasAction("Attack") && !n.isInteractedWith() &&
                                            trainingArea.contains(n)) || n.isInteracting(getLocalPlayer())));
        }

        // Chill and attack
        if (getLocalPlayer().isInCombat() || getLocalPlayer().isInteractedWith()){
            if (blueDragon == null){
                blueDragon = getNpcs().closest(n -> n.getName().equals("Blue dragon") && n.isInteracting(getLocalPlayer()));
                if (blueDragon != null){
                    log("Selecting blue dragon attacking me as target");
                }
            }
            if (timeSinceLastAnimation > 8000 && blueDragon != null){
                blueDragon.interact("Attack");
                lastAnimation = System.currentTimeMillis();
            }

            if (getLocalPlayer().isInteracting(blueDragon)){
                antiban();
            }
        } else {
            if (blueDragon != null && blueDragon.getHealthPercent() > 0 && blueDragon.exists()){
                blueDragon.interact("Attack");
                sleepUntil(() -> blueDragon.isInCombat(), Calculations.random(400,800));
            }
        }

    }

    private boolean selectNewTarget(NPC target){
        // pick a new target when

        //  - my selected target is dead
        //  - my selected target is null
        if (target == null){
            log ("Selecting new target because current target is null");
            return true;
        }

        if (!target.exists()){
            log ("Selecting new target because current target does not exist");
            return true;
        }
        if (!sv.waitForLoot){
            if (target.getHealthPercent() == 0 ){
                log ("Selecting new target because current target has 0 hp");
                return true;
            }
        }

//        if (target == null  || !target.exists() || target.getHealthPercent() == 0) {
//            return true;
//        }

        //  - if multi-combat, select new target as long as current selected target is not already interacting with me
        //  - if single-combat, select new target only if the current selected target is not being interacted with
        if ((!target.isInteracting(getLocalPlayer()) && target.isInCombat())) {
            log ("Selecting new target because target is in combat but not interacting with me");
            return true;
        }

        return false;
    }

    private void loot(GroundItem gi){
        if (!getInventory().isFull()){
            if (gi != null && getMap().canReach(gi)) {
                log("looting " + gi.getName());
                gi.interact("Take");
                long lootTimerStart = System.currentTimeMillis();
                sleepUntil(() -> !gi.exists(), 2000);
                if (lootTimerStart - System.currentTimeMillis() > 3000 && gi.exists()){
                    getCamera().rotateToYaw(Calculations.random(370,383));
                    sleep(300);
                    getWalking().walkExact(gi.getTile());
                    log("Walking to item tile: " + gi.getTile().toString());
                    log("Player tile: " + getLocalPlayer().getTile().toString());
                    gi.interact();
                    sleepUntil(() -> !gi.exists(), 1000);
                }
            }
        } else {
            if (!freeUpInventorySpace()){
                bankMode = true;
            }
        }
    }
    private void bank(){
        // MUST GET REQUIRED INVENTORY
        if (getBank().isOpen()){
            // Dueling ring
            boolean duelRingFound;
            if (!getInventory().contains(i -> i.getName().contains("dueling"))){
                duelRingFound = false;
                for (int a = 1; a < 9; a++){
                    String ringName = "Ring of dueling(" + a + ")";
                    if (getBank().contains(ringName)){
                        getBank().withdraw(ringName, 1);
                        sleepUntil(() -> getInventory().contains(ringName), 1000);
                        if (getInventory().contains(ringName)){

                            duelRingFound = true;
                        }
                        break;
                    }
                }
            } else {
                duelRingFound = true;
            }

            // Food
            if (getBank().count(sv.foodName) > sv.requiredFoodAmt){
                if (getBank().withdraw(sv.foodName, sv.requiredFoodAmt)){
                    foodFound = true;
                }
            } else {
                foodFound = false;
            }

            // Combat pot
            boolean combatPotFound;
            if (sv.useCombatPot){
                if (!getInventory().contains(i -> i.getName().contains("Combat potion"))){
                    combatPotFound = false;
                    for (int a = 1; a < 5; a++){
                        String potName = "Combat potion(" + a + ")";
                        if (getBank().contains(potName)){
                            getBank().withdraw(potName, 1);
                            combatPotFound = true;
                            break;
                        }
                    }
                } else {
                    combatPotFound = true;
                }
            }


            // Extended antifire pot
            if (sv.useAntifirePot){
                boolean antifirePotFound;
                if (!getInventory().contains(i -> i.getName().contains("Extended antifire"))){
                    antifirePotFound = false;
                    for (int a = 1; a < 5; a++){
                        String potName = "Extended antifire (" + a + ")";
                        if (getBank().contains(potName)){
                            getBank().withdraw(potName, 1);
                            combatPotFound = true;
                            break;
                        }
                    }
                } else {
                    antifirePotFound = true;
                }
            }

            // Antifire shield
            boolean antifireShieldFound;
            if (getEquipment().getItemInSlot(EquipmentSlot.SHIELD.getSlot()) != null){
                if (getEquipment().getItemInSlot(EquipmentSlot.SHIELD.getSlot()).getName().contains("Dragonfire") || getEquipment().getItemInSlot(EquipmentSlot.SHIELD.getSlot()).getName().contains("Anti-dragon")) {
                    antifireShieldFound = true;
                } else {
                    if (getBank().contains("Dragonfire shield")){
                        getBank().withdraw("Dragonfire shield", 1);
                        sleepUntil(() -> getInventory().contains("Dragonfire shield"), 1000);
                        antifireShieldFound = true;
                    } else if (getBank().contains("Anti-dragon shield")){
                        getBank().withdraw("Anti-dragon shield");
                        sleepUntil(() -> getInventory().contains("Anti-dragon shield"), 1000);
                        antifireShieldFound = true;
                    } else {
                        antifireShieldFound = false;
                    }
                }
            } else {
                antifireShieldFound = false;
            }

            if (bankMode){

                if (getBank().isOpen()){
                    getBank().close();
                    sleepUntil(() -> !getBank().isOpen(), 1000);
                }

                if (!antifireShieldFound){
                    log("antifire shield not found, stopping");
                    stop();
                }

                if (!duelRingFound){
                    log("dueling ring not found, stopping");
                    stop();
                }

                if (!foodFound){
                    log("Not enough food, stopping");
                    stop();
                }

                if (getInventory().contains("Anti-dragon shield")){
                    getInventory().interact("Anti-dragon shield", "Wear");
                } else if (getInventory().contains("Dragonfire shield")){
                    getInventory().interact("Dragonfire shield", "Wear");
                }

                // Go to fight mode

                bankMode = false;
                blueDragon = null;
                cityEntranceReached = false;
                gatePassed = false;
                caveEntered = false;
            }
        } else {
            getBank().open(BankLocation.CASTLE_WARS);
            sleepUntil(() -> getBank().isOpen(), 1500);
            getBank().depositAllItems();
            sleepUntil(() -> getInventory().isEmpty(), 1000);
        }
    }

    private void walkToBank(){
        if (broadBlueDragonArea.contains(getLocalPlayer())){
            log("teleporting to CW");
            // Use ring of duelling: REQUIRED
            Item duelingRing = getInventory().get(i -> i.hasAction("Rub") && i.getName().contains("duel"));

            if (duelingRing.interact("Rub")){
                sleepUntil(() -> getDialogues().getOptionIndex("Castle Wars Arena.") > 0, 1000);
                if (getDialogues().getOptionIndex("Castle Wars Arena.") > 0){
                    getDialogues().clickOption("Castle Wars Arena.");
                    sleepUntil(() -> BankLocation.CASTLE_WARS.getArea(10).contains(getLocalPlayer()), 4000);
                }
            }
        } else {
            log("walking to CW");
            getWalking().walk(BankLocation.CASTLE_WARS.getArea(10).getRandomTile());
        }
    }

    private void updateLoot(){
        for(PricedItem p : lootTrack){
            p.update();
        }
    }

    private boolean freeUpInventorySpace(){
        if (getInventory().isFull()){
            if (!getTabs().isOpen(Tab.INVENTORY)){
                getTabs().open(Tab.INVENTORY);
            }
            if (getInventory().contains(sv.foodName)){
                log("Freeing up inventory space by eating");
                getInventory().interact(sv.foodName, "Eat");
                sleepUntil(() -> !getInventory().isFull(), 1500);
                return true;
            } else if (getInventory().contains("Vial")){
                log ("Freeing up inventory space by dropping vial");
                getInventory().dropAll("Vial");
                sleepUntil(() -> !getInventory().isFull(), 1500);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private void antiban() {
        int random = Calculations.random(1, 100);
        long tmpValue = 0;
        long antibanValue = 0;
        if (random < 20) {
            if (!getTabs().isOpen(Tab.STATS)) {
                getTabs().open(Tab.STATS);
                for (Skill s : Skill.values()){
                    if (getSkillTracker().getGainedExperience(s) > 0){
                        antibanValue += getSkillTracker().getGainedExperience(s);
                    }
                }

                if (antibanValue > 0){
                    long checkValue = Calculations.random(1,antibanValue);
                    for (Skill s : Skill.values()){
                        if (getSkillTracker().getGainedExperience(s) > 0){
                            tmpValue += getSkillTracker().getGainedExperience(s);
                            if (tmpValue >= checkValue){
                                getSkills().hoverSkill(s);
                                break;
                            }
                        }
                    }
                }
                sleepUntil(() -> !getLocalPlayer().isInCombat() || !getLocalPlayer().isAnimating(), Calculations.random(300, 500));
            }

        } else if (random <= 25) {
            if (!getTabs().isOpen(Tab.INVENTORY)) {
                getTabs().open(Tab.INVENTORY);
                sleep(Calculations.random(300, 600));
            }
        } else if (random <= 29) {
            if (!getTabs().isOpen(Tab.COMBAT)) {
                getTabs().open(Tab.COMBAT);
                sleep(Calculations.random(100, 500));
            }
        } else if (random <= 35) {
            // rotate camera

            getCamera().rotateToTile(getLocalPlayer().getSurroundingArea(4).getRandomTile());
            getCamera().rotateToPitch(Calculations.random(275, 383));
        } else {
            if (getMouse().isMouseInScreen()) {
                if (getMouse().moveMouseOutsideScreen()) {
                    sleepUntil(() -> !getLocalPlayer().isInCombat() || !getLocalPlayer().isAnimating(), Calculations.random(500, 1000));
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
        if (sv.started){
            g.drawString("State: " + getState().toString(),5,10);
            g.drawString("Runtime: " + timer.format(), 5, 30);
            g.drawString("Experience (p/h): " + getGainedExperience() + "(" + timer.getPerHour(getGainedExperience()) + ")", 5, 45);
            if (getLocalPlayer().getInteractingCharacter() != null){
                g.drawString("Interacting: " + getLocalPlayer().getInteractingCharacter().getName(), 5, 60);
            }

        }

        for (int i = 0; i < lootTrack.size(); i++){
            PricedItem p = lootTrack.get(i);
            if (p != null){
                String name = p.getName();
                g.drawString(name + " (p/h): " + p.getAmount() + "(" + timer.getPerHour(p.getAmount()) + ")" , 400, (i + 1)* 15);
            }
        }
    }

}
