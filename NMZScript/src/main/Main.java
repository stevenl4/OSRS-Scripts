package main;


import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.prayer.Prayer;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.NPC;

import java.util.List;




/**
 * Created by steven.luo on 16/01/2017.
 */

@ScriptManifest(category = Category.MINIGAME, name = "NMZ", author = "GB", version = 1.0, description = "Does NMZ")
public class Main extends AbstractScript {

    private Long lastOverloadDose;
    private boolean dreamStarted = false;
    private Area startArea = new Area ();
    private Area dreamArea = new Area ();
    private Tile startTile;

    private enum State {
        WALK_TO_START, GET_REQUIRED_ITEMS, START_DREAM, FIGHT
    }

    private State getState(){
//        Walk to starting area
//        check required items in inventory
//        start the dream
//        set a random standing tile
//        when activating a spark, set that as new tile
        if (!startArea.contains(getLocalPlayer()) && !dreamStarted) {
            return State.WALK_TO_START;
        }

        if (startArea.contains(getLocalPlayer())) {
            return State.START_DREAM;
        }

        if (getLocalPlayer().isInCombat() || dreamStarted){
            return State.FIGHT;
        }

    }

    @Override
    public void onStart() {
        if (!getCombat().isAutoRetaliateOn()) {
            log("turning on autoretaliate");
            getCombat().toggleAutoRetaliate(true);
        }
        super.onStart();
    }

    @Override
    public int onLoop() {
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
        }

        return Calculations.random(350, 550);
    }
    private void move(){

    }
    private void fight(){

        Long timeSinceLastOverloadDose = System.currentTimeMillis() - lastOverloadDose;
        // Check Prayer
        if (!getPrayer().isActive(Prayer.PROTECT_FROM_MELEE)){
            getPrayer().toggle(true, Prayer.PROTECT_FROM_MELEE);
        }

        if (getSkills().getBoostedLevels(Skill.PRAYER) >= 20 && timeSinceLastOverloadDose < 298000){
            antiban();
        }
        if (getSkills().getBoostedLevels(Skill.PRAYER) < 20) {
            for (int i = 1; i < 5; i ++){
                String potionName = "Prayer potion(" + i + ")";
                if (getInventory().contains(potionName)) {
                    getInventory().interact(potionName, "Drink");
                }
            }
        }
        // Drink Overload Potion
        if (timeSinceLastOverloadDose >= 300000 && getSkills().getBoostedLevels(Skill.HITPOINTS) > 60) {
            for (int i = 1; i < 5; i ++){
                String potionName = "Overload(" + i + ")";
                if (getInventory().contains(potionName)) {
                    if (getInventory().interact(potionName, "Drink")){
                        lastOverloadDose = System.currentTimeMillis();
                    }
                }
            }
        }
        // Check special?
        if (getCombat().getSpecialPercentage() == 100) {
            getCombat().toggleSpecialAttack(true);
        }


    }
    private void startDream() {

        startTile = getLocalPlayer().getTile();
        dreamStarted = true;
    }

    private void antiban() {
        int random = Calculations.random(1, 100);
        if (random < 20){
            if (!getTabs().isOpen(Tab.STATS)) {
                getTabs().open(Tab.STATS);
                if (random < 5) {
                    getSkills().hoverSkill(Skill.ATTACK);
                } else if (random < 10) {
                    getSkills().hoverSkill(Skill.STRENGTH);
                } else if (random < 15) {
                    getSkills().hoverSkill(Skill.DEFENCE);
                } else {
                    getSkills().hoverSkill(Skill.HITPOINTS);
                }
                sleep(Calculations.random(300, 1000);
            }
        } else if (random <=25) {
            if (!getTabs().isOpen(Tab.INVENTORY)){
                getTabs().open(Tab.INVENTORY);
            }
        } else if (random <= 30) {
            getCamera().rotateToEntity(getLocalPlayer());

        } else {
            if (getMouse().isMouseInScreen()){
                if (getMouse().moveMouseOutsideScreen()){
                    sleep(Calculations.random(500, 1000));
                }
            }
        }
    }

}
