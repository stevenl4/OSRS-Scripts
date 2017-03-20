package main;
import gui.MagicGui;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.magic.Spell;
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
@ScriptManifest(category = Category.MAGIC, name = "Enchanting jewelry", author = "GB", version = 1.1, description = "Enchants items")
public class Main extends AbstractScript {

    ScriptVars sv = new ScriptVars();
    private long castCountCurse;
    private long castCountAlch;
    private long castCountEnchant;
    private long totalCastCount;
    private long nextExpCheck;
    private long antibanValue;
    private Normal enchantSpell;
    NPC target;
    private RunTimer timer;

    public enum State {
        CAST, WALK_TO_NPC, SELECT_TARGET, ENCHANT
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
        gui.setVisible(true);
        while (!sv.started){
            sleep(1000);
        }
        if (sv.npcLevel == 0){
            sv.npcLevel = 10000;
        }
        log("Started");
//        if (sv.alchItems.length == 0){
//            sv.highAlch = false;
//        }

        if (sv.enchant){
            if (sv.enchantItem.contains("Opal")){
                sv.enchantLevel = 1;
            } else if (sv.enchantItem.contains("Jade")){
                sv.enchantLevel = 2;
            } else if (sv.enchantItem.contains("Topaz")){
                sv.enchantLevel = 3;
            }
            log("ENCH Item: " + sv.enchantItem);
            log("ENCH LVL: " + sv.enchantLevel);
            switch (sv.enchantLevel){
                case 1:
                    enchantSpell = Normal.LEVEL_1_ENCHANT;
                    break;
                case 2:
                    enchantSpell = Normal.LEVEL_2_ENCHANT;
                    break;
                case 3:
                    enchantSpell = Normal.LEVEL_3_ENCHANT;
                    break;
                case 4:
                    enchantSpell = Normal.LEVEL_4_ENCHANT;
                    break;
                case 5:
                    enchantSpell = Normal.LEVEL_5_ENCHANT;
            }
        }

    }

    private State getState(){
        if (sv.curse){
            if (target == null || !target.exists()){
                return State.SELECT_TARGET;
            } else {
                return State.CAST;
            }
        } else if (sv.enchant){
            return State.ENCHANT;
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
                curseOrAlch();
                break;
            case ENCHANT:
                enchantJewelry();
                break;
        }


        return Calculations.random(700, 800);
    }
    private void selectTarget(){
        log("Selecting target");
        int tryCount = 0;
        if (getLocalPlayer().isInCombat()){
            target = getNpcs().closest(n -> n.isInteracting(getLocalPlayer()));
        }

        while (target == null || !target.exists()){
            if (tryCount > 50){
                break;
            }
            target = getNpcs().closest(n -> n.exists() && n.hasAction("Attack") && n.getLevel() < sv.npcLevel && n.isOnScreen() && !n.isInCombat() && !n.isInteractedWith());
            tryCount++;
            sleep(50);
        }

        if (target != null){
            log("selected target: " + target.getName());
        } else {
            log ("No nearby targets, move somewhere else");
            stop();
        }

    }

    private void enchantJewelry(){
        if (getInventory().contains(sv.enchantItem) && getInventory().contains("Cosmic rune")){
            if (getBank().isOpen()){
                getBank().close();
                sleepUntil(() -> !getBank().isOpen(), 1500);
            }

            if (verifyCanCastSpell(enchantSpell)){

                if (getMagic().castSpell(enchantSpell)){
                    if (getMagic().isSpellSelected()){
                        if (getInventory().interact(sv.enchantItem, "Cast")){
                            castCountEnchant++;
                            sleep(Calculations.random(600,800));
                        }
                    }
                }
            } else {
                log("Cant cast spell: " + enchantSpell.toString());
                stop();
            }
        } else {
            bank();
        }
    }

    private void bank(){
        if (getBank().isOpen()){
            if (!getInventory().onlyContains("Cosmic rune")){
                if (getBank().depositAllExcept("Cosmic rune")){
                    sleepUntil(() -> getInventory().onlyContains("Cosmic rune"), 1000);
                }
            }

            if (getBank().contains(sv.enchantItem)){
                if (getBank().withdrawAll(sv.enchantItem)){
                    sleepUntil(() -> getInventory().contains(sv.enchantItem), 1000);
                }
            } else {
                log ("Out of items to enchant, stopping");
                stop();
            }

        } else {
            getBank().openClosest();
            sleepUntil(() -> getBank().isOpen(),1000);
        }
    }

    private void curseOrAlch(){
        Normal spellToCast;
        int action = Calculations.random(1,100);
        boolean castAlch;
        boolean castCurse;


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
        if (getSkills().getRealLevel(Skill.MAGIC) < 55){
            sv.highAlch = false;
        }

        if (!getTabs().isOpen(Tab.MAGIC)){
            getTabs().open(Tab.MAGIC);
            sleepUntil(() -> getTabs().isOpen(Tab.MAGIC), 1000);
        }

        if (verifyCanCastSpell(Normal.CURSE)){
            spellToCast = Normal.CURSE;
        } else {
            if (verifyCanCastSpell(Normal.WEAKEN)){
                spellToCast = Normal.WEAKEN;
            } else {
                spellToCast = Normal.CONFUSE;
            }
        }

        if (sv.curse && verifyCanCastSpell(spellToCast)){
            castCurse = true;
        } else {
            castCurse = false;
        }

        if (sv.highAlch && verifyCanCastSpell(Normal.HIGH_LEVEL_ALCHEMY) && highAlchTarget() != null){
            castAlch = true;
        } else {
            castAlch = false;
        }

        if (!castAlch && !castCurse){
            log("Out of runes/items, stopping");
            stop();
        }
        if (castCurse){

            if (action < sv.antibanRate){
                log ("Antiban");
                antiban();
            } else {
                if (target != null && target.exists() && target.getHealthPercent()>0){

                    if (getMagic().castSpellOn(spellToCast, target)){
                        castCountCurse++;
                        if (!castAlch){
                            sleep(Calculations.random(800,900));
                        }
                    }
                }
            }
        }


        if (castAlch){

            if (getMagic().castSpell(Normal.HIGH_LEVEL_ALCHEMY)){
                if (!getTabs().isOpen(Tab.INVENTORY)){
                    getTabs().open(Tab.INVENTORY);
                    sleepUntil(() -> getTabs().isOpen(Tab.INVENTORY), 500);
                }
                if (getMagic().isSpellSelected()){

                    if (getInventory().interact(highAlchTarget(), "Cast")){
                        castCountAlch++;
                        if (!castCurse){
                            sleep(Calculations.random(1200,1300));
                        }
                    }
                }
            }
        }
    }
    private String highAlchTarget(){
        String target = null;
        for (String i : sv.alchItems){

            if (getInventory().contains(i)){
                log ("Found " + i + " in inventory");
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
        boolean requireAirRune = true;
        int waterRuneAmt = 0;
        int earthRuneAmt = 0;
        int fireRuneAmt = 0;
        int airRuneAmt = 0;
        int bodyRuneAmt = 0;
        int natureRuneAmt = 0;
        int cosmicRuneAmt = 0;
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

        if (getInventory().contains("Cosmic rune")){
            cosmicRuneAmt = getInventory().count("Cosmic rune");
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

        if (currentStaff.contains("air") || currentStaff.contains("dust") || currentStaff.contains("mist") || currentStaff.contains("smoke")){
            requireAirRune = false;
        }
        if (currentTome.contains("fire")){
            requireFireRune = false;
        }

        if (spellName.equals(Normal.CURSE) && getSkills().getBoostedLevels(Skill.MAGIC) >= 19){
            if (bodyRuneAmt >= 1){
                if (!requireWaterRune || waterRuneAmt >= 2){
                    if (!requireEarthRune || earthRuneAmt >= 3){
                        canCast = true;
                    }
                }
            }
        }

        if (spellName.equals(Normal.WEAKEN) && getSkills().getBoostedLevels(Skill.MAGIC) >= 11){
            if (bodyRuneAmt >= 1){
                if (!requireWaterRune || waterRuneAmt >= 3){
                    if (!requireEarthRune || earthRuneAmt >= 2){
                        canCast = true;
                    }
                }
            }
        }

        if (spellName.equals(Normal.CONFUSE) && getSkills().getBoostedLevels(Skill.MAGIC) >= 3){
            if (bodyRuneAmt >= 1){
                if (!requireWaterRune || waterRuneAmt > 3){
                    if(!requireEarthRune || earthRuneAmt >=2){
                        canCast = true;
                    }

                }
            }
        }

        if (spellName.equals(Normal.HIGH_LEVEL_ALCHEMY)){
            if (natureRuneAmt >= 1){
                if (!requireFireRune || fireRuneAmt >= 5){
                    for (String i : sv.alchItems){
                        if (getInventory().contains(i)){
                            canCast = true;
                        }
                    }
                }
            }
        }

        if (spellName.equals(Normal.LOW_LEVEL_ALCHEMY)){
            if (natureRuneAmt >= 1){
                if (!requireFireRune || fireRuneAmt >=3){
                    for (String i : sv.alchItems){
                        if (getInventory().contains(i)){
                            canCast = true;
                        }
                    }
                }
            }
        }

        if (spellName.equals(Normal.LEVEL_1_ENCHANT)){
            if (cosmicRuneAmt >= 1){
                if (!requireWaterRune || waterRuneAmt >= 1){
                    canCast = true;
                }
            }
        }

        if (spellName.equals(Normal.LEVEL_2_ENCHANT)){
            if (cosmicRuneAmt >= 1){
                if (!requireAirRune || airRuneAmt >= 1){
                    canCast = true;
                }
            }
        }
        if (spellName.equals(Normal.LEVEL_3_ENCHANT)){
            if (cosmicRuneAmt >=1){
                if( !requireFireRune || fireRuneAmt >=1 ){
                    canCast = true;
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
    @Override
    public void onPaint(Graphics g) {
        if (sv.started){
            g.drawString("State: " + getState().toString(), 5, 15);
            g.drawString("Runtime: " + timer.format(),5,30);
            g.drawString("Magic exp (p/h): " + getSkillTracker().getGainedExperience(Skill.MAGIC) + "(" + timer.getPerHour(getSkillTracker().getGainedExperience(Skill.MAGIC)) + ")",5,45);
            g.drawString("Curse count (p/h): " + castCountCurse + " (" + timer.getPerHour(castCountCurse) +")", 5,60);
            g.drawString("Enchant count (p/h): " + castCountEnchant + " (" + timer.getPerHour(castCountEnchant) + ")", 5,75);
        }
    }
}
