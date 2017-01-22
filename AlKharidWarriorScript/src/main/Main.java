package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import util.PricedItem;
import util.RunTimer;
import util.ScriptVars;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by steven.luo on 17/01/2017.
 */
@ScriptManifest(category = Category.COMBAT, name = "Al-Kharid Warrior Fighter", author = "GB", version = 1.0, description = "Fights warriors, picks up herbs, members only")
public class Main extends AbstractScript {

    ScriptVars sv = new ScriptVars();
    private RunTimer timer;
    private long lastSearchGround;
    private boolean searchGround;
    private boolean searchTarget;
    List<PricedItem> lootTrack = new ArrayList<PricedItem>();
    private long lootTimerStart = 0;
    private int totalProfit;
    private NPC guard;
    private Area trainingArea = new Area (3282,3176,3292,3168,0);

    private enum State {
        ATTACK, LOOT, WALK_TO_BANK, WALK_TO_TRAINING, BANK, EAT
    }

    private State getState() {


        if (System.currentTimeMillis() - lastSearchGround > 1500){
            searchGround = true;
        }
        // TODO this might mess up logic
        if (getLocalPlayer().getHealthPercent() < 65 && getInventory().contains(sv.foodName)){
            return State.EAT;
        }

        if (getInventory().isFull() || getLocalPlayer().getHealthPercent() < 35){
            if(BankLocation.AL_KHARID.getArea(4).contains(getLocalPlayer())){
                return State.BANK;
            } else {
                return State.WALK_TO_BANK;
            }
        }

        if (!trainingArea.contains(getLocalPlayer()) && !getLocalPlayer().isInCombat()){
            return State.WALK_TO_TRAINING;
        }

        if (trainingArea.contains(getLocalPlayer()) && searchGround){

            GroundItem gi = getGroundItems().closest(itemFilter);
            lastSearchGround = System.currentTimeMillis();
            searchGround = false;
            if ( gi != null ) {
                return State.LOOT;
            } else {
                return State.ATTACK;
            }
        } else {
            return State.ATTACK;
        }

    }

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

    Filter<NPC> guardFilter = new Filter<NPC>(){
        @Override
        public boolean match (NPC n){

            if (n == null || n.getActions() == null || n.getActions().length <= 0){
                return false;
            }
            if (n.getName() == null || !n.getName().equals("Al-Kharid warrior")){
                return false;
            }
            if (n.isInCombat()){
                Character c = n.getInteractingCharacter();
                if (c == null || c.getName() == null) {
                    return false;
                }

                if (c.getName().equals(getLocalPlayer().getName())){
                    return true;
                }

                return false;
            }
            return true;
        }
    };
    @Override
    public void onStart() {
        sv.loot = new String[]{"Grimy ranarr weed", "Grimy irit leaf", "Grimy avantoe", "Grimy kwuarm", "Grimy cadantine", "Grimy lantadyme",
                "Grimy dwarf weed"};

        for (int i = 0; i < sv.loot.length; i++) {
            lootTrack.add(new PricedItem(sv.loot[i], getClient().getMethodContext(), true));
        }
        getSkillTracker().start(Skill.DEFENCE);
        getSkillTracker().start(Skill.ATTACK);
        getSkillTracker().start(Skill.STRENGTH);
        getSkillTracker().start(Skill.RANGED);
        log("Starting al-kharid warrior killer");
        timer = new RunTimer();
        sv.started = true;

        searchGround = true;
        searchTarget = true;
    }

    @Override
    public int onLoop() {

        if(getLocalPlayer().isMoving() && getClient().getDestination() != null && getLocalPlayer().distance(getClient().getDestination()) > 3) {
            return Calculations.random(200,300);
        }

        if (!getWalking().isRunEnabled() && getWalking().getRunEnergy() > Calculations.random(30,50)){
            getWalking().toggleRun();
        }

        if (!getCombat().isAutoRetaliateOn()){
            getCombat().toggleAutoRetaliate(true);
        }

        switch (getState()){
            case ATTACK:
                attack();
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
            case EAT:
                eat();
                break;
        }
        updateLoot();
        return Calculations.random(200,400);
    }

    private void updateLoot(){
        for(PricedItem p : lootTrack){
            p.update();
        }
    }

    private void eat() {

        if (getInventory().contains(sv.foodName)){
            if (getTabs().isOpen(Tab.INVENTORY)){
                getTabs().open(Tab.INVENTORY);
            }
            getInventory().get(sv.foodName).interact("Eat");
            sleepUntil(() -> !getLocalPlayer().isAnimating(), 1000);
        }
    }

    private void attack(){
        if (getCamera().getPitch() < 275){
            getCamera().rotateToPitch(Calculations.random(275,375));
        }


        // Single combat area fighting
        // Pick a target

        if (selectNewTarget(guard)){
            log("selecting new target");
            guard = getNpcs().closest(guardFilter);
        }


        if (getLocalPlayer().isInCombat() || getLocalPlayer().isInteractedWith()){
            if (getLocalPlayer().isAnimating() || getLocalPlayer().isInCombat()){
                antiban();
            } else {
                log("re-engaging target");
                guard.interact("Attack");
            }
        } else {
            if (guard != null && guard.getHealthPercent() > 0 && guard.exists()) {
                log ("target selected");
                guard.interact("Attack");
                sleepUntil(guard::isInCombat, Calculations.random(400,800));
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

        if (target.getHealthPercent() == 0 ){
            log ("Selecting new target because current target has 0 hp");
            return true;
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
    private void walkToTraining(){
        if (getWalking().walk(trainingArea.getRandomTile())){
            log("walking to training area");
            sleepUntil(() -> getLocalPlayer().distance(getClient().getDestination()) < Calculations.random(3,5) || !getLocalPlayer().isMoving(), Calculations.random(900,2500));
        }
    }
    private void walkToBank() {
        if (getWalking().walk(BankLocation.AL_KHARID.getArea(4).getRandomTile())){
            log("Walking to bank");
            sleepUntil(() -> getLocalPlayer().distance(getClient().getDestination()) < Calculations.random(3,5) || !getLocalPlayer().isMoving(), Calculations.random(900,2500));
        }
    }
    private void bank() {
        if (getBank().isOpen()){
            log("depositing all items");
            getBank().depositAllItems();
            sleepUntil(() -> getInventory().isEmpty(), 1000);
            getBank().withdraw(sv.foodName, sv.requiredFoodAmt);
            sleepUntil(() -> getInventory().contains(sv.foodName), 1000);
            getBank().close();

        } else {
            getBank().open(BankLocation.AL_KHARID);
            sleepUntil(() -> getBank().isOpen(), 1500);
        }
    }

    private void loot() {

        log("running an item filter from loot");
        final GroundItem gi = getGroundItems().closest(itemFilter);
        if (gi != null) {
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

    public long getGainedExperience(){
        long atk = getSkillTracker().getGainedExperience(Skill.ATTACK);
        long str = getSkillTracker().getGainedExperience(Skill.STRENGTH);
        long def = getSkillTracker().getGainedExperience(Skill.DEFENCE);
        long range = getSkillTracker().getGainedExperience(Skill.RANGED);
        return atk + str + def + range;
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

    public void onPaint(Graphics g){

        if (sv.started){
            // limit to once every 3 seconds
            totalProfit = 0;
            if (getState() != null){
                g.drawString("State: " + getState().toString() , 5, 10);
            }
            g.drawString("Runtime: " + timer.format(), 5, 30);
            g.drawString("Experience (p/h): " + getGainedExperience() + "(" + timer.getPerHour(getGainedExperience()) + ")", 5, 45);

            if (!getWorlds().f2p().contains(getClient().getCurrentWorld())) {
                for (int i = 0; i < lootTrack.size(); i++){
                    PricedItem p = lootTrack.get(i);
                    if (p != null){
                        String name = p.getName();
                        g.drawString(name + " (p/h): " + p.getAmount() + "(" + timer.getPerHour(p.getAmount()) + ")" , 400, (i + 2)* 15);
                        totalProfit += p.getValue();
                    }
                }
                g.drawString("Total Profit (p/h): " + totalProfit + "(" + timer.getPerHour(totalProfit) + ")", 400, 15);
            }



        }
    }
}
