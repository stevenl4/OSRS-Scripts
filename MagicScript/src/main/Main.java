package main;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.NPC;
import util.RunTimer;
import util.ScriptVars;

import java.awt.*;

/**
 * Created by steven.luo on 25/01/2017.
 */
@ScriptManifest(category = Category.MAGIC, name = "Magic Script", author = "GB", version = 1.0, description = "Curses closest NPC")
public class Main extends AbstractScript {

    ScriptVars sv = new ScriptVars();
    private long castCount;
    private long nextExpCheck;
    NPC target;
    private RunTimer timer;

    public enum State {
        CAST, WALK_TO_NPC, SELECT_TARGET
    }

    @Override
    public void onStart() {
        timer = new RunTimer();
        getSkillTracker().start(Skill.MAGIC);
        nextExpCheck = Calculations.random(300,500);
        sv.started = true;
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
            target = getNpcs().closest(n -> n.exists() && n.hasAction("Attack") && n.getLevel() < 20 && n.distance(getLocalPlayer()) < 10 && n.isOnScreen());
            log("selected target: " + target.getName());
        }

    }
    private void cast(){
        int action = Calculations.random(1,100);
        if (castCount >= nextExpCheck){
            if (!getTabs().isOpen(Tab.STATS)) {
                getTabs().open(Tab.STATS);
                sleepUntil(() -> getTabs().isOpen(Tab.STATS), 1000);
            }

            getSkills().hoverSkill(Skill.MAGIC);
            sleep(Calculations.random(400, 600));
            nextExpCheck = nextExpCheck + Calculations.random(400, 1000);

        }

        if (getInventory().contains("Water rune") && getInventory().contains("Body rune")){
            if (action < 9){
                log ("Antiban");
                antiban();
            } else {
                if (!getTabs().isOpen(Tab.MAGIC)){
                    getTabs().open(Tab.MAGIC);
                    sleepUntil(() -> getTabs().isOpen(Tab.CLAN),1500);
                } else {
                    if (target != null && target.exists() && target.getHealthPercent()>0){
                        log("Casting curse on: " + target.getName());
                        if (getMagic().castSpellOn(Normal.CURSE, target)){
                            castCount++;
                            sleep(Calculations.random(800,1200));
                        }
                    }
                }
            }


        } else {
            log("out of runes");
            stop();
        }
    }
    private void antiban() {
        int random = Calculations.random(1, 100);
        if (random < 20) {
            if (!getTabs().isOpen(Tab.STATS)) {
                getTabs().open(Tab.STATS);
                if (random < 4) {
                    getSkills().hoverSkill(Skill.MAGIC);
                } else if (random < 8) {
                    getSkills().hoverSkill(Skill.MAGIC);
                } else if (random < 12) {
                    getSkills().hoverSkill(Skill.MAGIC);
                } else if (random < 16) {
                    getSkills().hoverSkill(Skill.MAGIC);
                } else {
                    getSkills().hoverSkill(Skill.MAGIC);
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
            g.drawString("Cast Count: " + castCount + " | next Exp Check" + nextExpCheck, 5, 75 );
        }
    }
}
