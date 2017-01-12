package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import org.dreambot.api.wrappers.items.Item;
import util.RunTimer;
import util.ScriptVars;
import gui.Gui;

import java.awt.*;


/**
 * Created by steven.luo on 11/01/2017.
 */

@ScriptManifest(category = Category.MISC, name = "Feather Buyer", author = "GB", version = 1.0, description = "Buys Feather 12:21")
public class Main extends AbstractScript {

    private boolean hopWorlds = false;
    private final int[] f2pWorlds = new int[]{1,8,16,26,35,81,82,83,84,85,93,94};
    private long startGP;
    private RunTimer timer;
    private int purchasedCount = 0;
    private int spent = 0;
    private long profUpdate = 0;
    private int lastProfit = 0;

    private Area shopArea = new Area(3011,3228, 3018,3219, 0);
    private Area geArea = new Area(3159,3486,3169,3477,0);

    ScriptVars sv = new ScriptVars();
    boolean started = false;

    private enum State {
        BUY, OPEN_PACKS, HOP, WALK_TO_SHOP, WALK_TO_GE
    }
    @Override
    public void onStart() {
        Gui gui = new Gui(sv);
        gui.setVisible(true);
        while (!sv.started) {
            sleep(1000);
        }

        timer = new RunTimer();
        startGP = getInventory().count("Coins");
        started = true;

    }

    @Override
    public int onLoop() {

        if(getLocalPlayer().isMoving() && getClient().getDestination() != null && getLocalPlayer().distance(getClient().getDestination()) > 3)
            return Calculations.random(200,300);

        if(!getWalking().isRunEnabled() && getWalking().getRunEnergy() > Calculations.random(30,50)){
            getWalking().toggleRun();
        }
        switch (getState()){
            case BUY:
                buy();
                return Calculations.random(sv.minBuySleep, sv.maxBuySleep);
            case OPEN_PACKS:
                openPacks();
                break;
            case WALK_TO_SHOP:
                walkToShop();
                break;
            case WALK_TO_GE:
                walkToGE();
                break;
            case HOP:
                hop();
                break;
        }
        return Calculations.random(450,700);
    }

    private State getState(){
        long gp = getInventory().count("Coins");

        if (gp < sv.minGP) {
            return State.WALK_TO_GE;
        }
        if (hopWorlds){
            return State.HOP;
        }
        if (getInventory().contains(sv.packName) && getInventory().isFull()){
            return State.OPEN_PACKS;
        } else if (!shopArea.contains(getLocalPlayer())){
            return State.WALK_TO_SHOP;
        } else {
            return State.BUY;
        }
    }

    private void buy(){
        final Shop s = getShop();
        log("Buying packs");
        if (!s.isOpen()){
            s.open(sv.shopId);
            sleepUntil(() -> s.isOpen(), 1500);
        } else {
            Item pack = s.get(sv.packName);
            if (pack != null && pack.getAmount() > sv.minAmt && !getInventory().isFull()){
                if (s.purchase(sv.packName, 1)) {
                    purchasedCount++;
                    sleep(Calculations.random(50,100));
                } else {
                    sleepUntil(() -> pack.getAmount() > 1, 1500);
                }
            } else if (pack.getAmount() < sv.minAmt && sv.hopWorlds) {
                hopWorlds = true;
            } else {
                if (s.close()){
                    log ("Done purchasing, going to open packs");
                }
            }
        }
    }

    private void openPacks(){
        final Shop s = getShop();
        log("Opening packs");
        if (s.isOpen()){
            s.close();
        } else {
            for (int i = 0 ; i < 28; i++){
                Item item = getInventory().getItemInSlot(i);
                if (item != null && item.getName().equals(sv.packName)){
                    if (getInventory().slotInteract(i, "Open")){
                        sleep(Calculations.random(sv.minOpenSleep,sv.maxOpenSleep));
                    }
                }
            }
        }
    }
    private void walkToShop(){
        if (getWalking().walk(shopArea.getRandomTile())){
            log("Walking to shop");
            sleepUntil(() -> getClient().getDestination().distance() < Calculations.random(3,6) || getLocalPlayer().isStandingStill(), Calculations.random(2000,4000));
        }

    }
    private void hop(){

        int hopTo = f2pWorlds[Calculations.random(0, f2pWorlds.length-1)];
        while (hopTo == getClient().getCurrentWorld()){
            hopTo = f2pWorlds[Calculations.random(0, f2pWorlds.length-1)];
        }
        log("Hopping worlds to " + hopTo);
        getWorldHopper().quickHop(hopTo);

        sleepUntil(() -> getClient().getInstance().getScriptManager().getCurrentScript().getRandomManager().isSolving(), 30000);
        hopWorlds = false;
    }
    private void walkToGE(){
        if (getWalking().walk(geArea.getRandomTile())){
            log("Walking to GE");
            sleepUntil(() -> getClient().getDestination().distance() < Calculations.random(3,6) || getLocalPlayer().isStandingStill(), Calculations.random(4000,5000));
        }
        sleepUntil(() -> !getLocalPlayer().isMoving(), 1000);
    }

    public int getProfit(){
        if(!getClient().isLoggedIn()|| getInventory().count(sv.packName) <= 0 || getInventory().count("Coins") <= sv.minGP) {
            return lastProfit;
        }
        // if lastProfit is 0 or 600ms has passed since last update, update lastProfit
        if (lastProfit == 0 || System.currentTimeMillis() - profUpdate > 600) {
            int itemValue = sv.perItem;
            spent = (int)startGP - getInventory().count("Coins");
            lastProfit = itemValue * purchasedCount - spent;
            profUpdate = System.currentTimeMillis();
        }
        return lastProfit;
    }
    public void onPaint(Graphics g){
        if (started){
            if (getState() != null){
                g.drawString("State: " + getState().toString() , 5, 10);
            }
            int profit = getProfit();
            g.drawString(sv.packName + " bought (p/h): " + purchasedCount + "(" + timer.getPerHour(purchasedCount) + ")", 5, 65 );
            g.drawString("GP Made (p/h): " + profit + "(" + timer.getPerHour(profit) + ")", 5,80);
            g.drawString("Runtime: " + timer.format(), 5,95);
            g.drawString("Burn Rate (p/h): " + timer.getPerHour(spent), 5, 110);
        }
    }


}
