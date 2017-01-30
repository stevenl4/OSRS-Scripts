package main;
import gui.MagicGui;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;
import util.RunTimer;
import util.ScriptVars;

import java.awt.*;

/**
 * Created by steven.luo on 25/01/2017.
 */
@ScriptManifest(category = Category.MAGIC, name = "Magic Script", author = "GB", version = 1.0, description = "Curses closest NPC")
public class Main extends AbstractScript {

    ScriptVars sv = new ScriptVars();
    private long castCountCurse;
    private long castCountAlch;
    private long totalCastCount;
    private long nextExpCheck;
    private long antibanValue;

    NPC target;
    private RunTimer timer;

    public enum State {
        CAST, WALK_TO_NPC, SELECT_TARGET
    }

    @Override
    public void onStart() {
        timer = new RunTimer();
        // Start Tracking All Skills
        for (Skill s : Skill.values()){
            getSkillTracker().start(s);
        }
        getSkillTracker().start(Skill.MAGIC);
        nextExpCheck = Calculations.random(300,500);

        MagicGui gui = new MagicGui(sv);
        gui.frame.setVisible(true);
        while (!sv.started){
            sleep(1000);
        }
        if (sv.alchItems.length == 0){
            sv.highAlch = false;
        }

    }

    private State getState(){
        if (target == null || !target.exists()){
            return State.SELECT_TARGET;
        } else {
            return State.CAST;
        }

    }
    @Override
    public int onLoop() {
        if (getCombat().isAutoRetaliateOn()){
            getCombat().toggleAutoRetaliate(false);
            sleepUntil(() -> !getCombat().isAutoRetaliateOn(), 1000);
        }
        totalCastCount = castCountAlch + castCountCurse;
        switch (getState()){
            case SELECT_TARGET:
                selectTarget();
                break;
            case CAST:
                cast();
                break;
        }


        return Calculations.random(300, 500);
    }
    private void selectTarget(){
        while (target == null || !target.exists()){
            target = getNpcs().closest(n -> n.exists() && n.hasAction("Attack") && n.getLevel() < sv.npcLevel && n.distance(getLocalPlayer()) < 10 && n.isOnScreen() && n.isInCombat() && !n.isInteractedWith());
            log("selected target: " + target.getName());
        }

    }
    private void cast(){
        Normal spellToCast;
        int action = Calculations.random(1,100);
        if (totalCastCount >= nextExpCheck){
            if (!getTabs().isOpen(Tab.STATS)) {
                getTabs().open(Tab.STATS);
                sleepUntil(() -> getTabs().isOpen(Tab.STATS), 1000);
            }

            getSkills().hoverSkill(Skill.MAGIC);
            sleep(Calculations.random(400, 600));
            nextExpCheck = nextExpCheck + Calculations.random(400, 1000);
        }

        // Verify that cursing and alching is possible
        if (getSkills().getRealLevel(Skill.MAGIC) >= 55){
            sv.highAlch = true;
        }

        if (getMagic().canCast(Normal.CURSE)){
            spellToCast = Normal.CONFUSE;
        } else {
            if (getMagic().canCast(Normal.WEAKEN)){
                spellToCast = Normal.WEAKEN;
            } else {
                spellToCast = Normal.CONFUSE;
            }
        }


        if (sv.curse && getMagic().canCast(spellToCast)){
            if (action < sv.antibanRate){
                log ("Antiban");
                antiban();
            } else {
                if (target != null && target.exists() && target.getHealthPercent()>0){
                    log("Casting curse on: " + target.getName());
                    if (getMagic().castSpellOn(spellToCast, target)){
                        castCountCurse++;
                        sleep(Calculations.random(1200,1300));
                    }
                }
            }
        }

        if (sv.highAlch && getMagic().canCast(Normal.HIGH_LEVEL_ALCHEMY) && highAlchTarget() != null){

            if (getMagic().castSpell(Normal.HIGH_LEVEL_ALCHEMY)){
                if (!getTabs().isOpen(Tab.INVENTORY)){
                    getTabs().open(Tab.INVENTORY);
                    sleepUntil(() -> getTabs().isOpen(Tab.INVENTORY), 500);
                }
                if (getMagic().isSpellSelected()){
                    // TODO find out what the action is
                    if (getInventory().interact(highAlchTarget(), "Cast")){
                        castCountAlch++;
                        sleep(Calculations.random(500,1000));
                    }
                }
            }
        }
    }
    private String highAlchTarget(){
        String target = null;
        for (String i : sv.alchItems){
            if (getInventory().contains(i)){
                target = getInventory().get(i).getName();
                break;
            }
        }
        return target;
    }
    private boolean verifyCanCastSpell(Normal spellName){
        boolean requireWaterRune = true;
        boolean requireEarthRune = true;
        boolean requireFireRune = true;

        int waterRuneAmt = 0;
        int earthRuneAmt = 0;
        int fireRuneAmt = 0;
        int bodyRuneAmt = 0;
        int natureRuneAmt = 0;
        String currentTome = "";
        String currentStaff = "";

        boolean canCast = false;
        if (getInventory().contains("Water rune")){
            waterRuneAmt = getInventory().count("Water rune");
        }

        if (getInventory().contains("Earth rune")){
            earthRuneAmt = getInventory().count("Earth rune");

        }

        if (getInventory().contains("Fire rune")){
            fireRuneAmt = getInventory().count("Fire rune");
        }

        if (getInventory().contains("Body rune")){
            bodyRuneAmt = getInventory().count("Body rune");
        }

        if (getInventory().contains("Nature rune")){
            natureRuneAmt = getInventory().count("Nature rune");
        }



        if (getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot()) != null){
            currentStaff = getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot()).getName().toLowerCase();
        }


        if (getEquipment().getItemInSlot(EquipmentSlot.SHIELD.getSlot()) != null){
            currentTome = getEquipment().getItemInSlot(EquipmentSlot.SHIELD.getSlot()).getName().toLowerCase();
        }



        if (currentStaff.contains("water") || currentStaff.contains("steam") || currentStaff.contains("mud")){
            requireWaterRune = false;
        }

        if (currentStaff.contains("earth") || currentStaff.contains("lava") || currentStaff.contains("mud")){
            requireEarthRune = false;
        }

        if (currentStaff.contains("fire") || currentStaff.contains("lava") || currentStaff.contains("steam")){
            requireFireRune = false;
        }

        if (currentTome.contains("fire")){
            requireFireRune = false;
        }

        if (spellName.equals(Normal.CURSE)){
            if (bodyRuneAmt > 1){
                if (!requireWaterRune || waterRuneAmt > 2){
                    if (!requireEarthRune || earthRuneAmt > 3){
                        canCast = true;
                    }
                }
            }
        }

        if (spellName.equals(Normal.HIGH_LEVEL_ALCHEMY)){
            if (natureRuneAmt > 1){
                if (!requireFireRune || fireRuneAmt > 5){
                    for (String i : sv.alchItems){
                        if (getInventory().contains(i)){
                            canCast = true;
                        }
                    }
                }
            }
        }

        return canCast;
    }
    private void antiban() {
        int random = Calculations.random(1, 100);
        long tmpValue = 0;
        antibanValue = 0;
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
        } else {
            if (getMouse().isMouseInScreen()) {
                if (getMouse().moveMouseOutsideScreen()) {
                    sleepUntil(() -> !getLocalPlayer().isInCombat() || !getLocalPlayer().isAnimating(), Calculations.random(500, 1000));
                }
            }
        }
    }
    @Override
    public void onPaint(Graphics g) {
        if (sv.started){
            g.drawString("State: " + getState().toString(), 5, 15);
            g.drawString("Runtime: " + timer.format(),5,30);
            g.drawString("Magic exp (p/h): " + getSkillTracker().getGainedExperience(Skill.MAGIC) + "(" + timer.getPerHour(getSkillTracker().getGainedExperience(Skill.MAGIC)) + ")",5,45);
            g.drawString("Target: " + target.getName(),5,60);
            g.drawString("Cast Count: " + totalCastCount + " | next Exp Check" + nextExpCheck, 5, 75 );
        }
    }
}
