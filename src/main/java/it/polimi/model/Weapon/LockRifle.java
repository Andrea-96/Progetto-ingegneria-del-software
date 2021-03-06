package it.polimi.model.Weapon;

import it.polimi.model.*;
import it.polimi.model.Exception.NotVisibleTarget;


import java.util.ArrayList;

public class LockRifle extends WeaponCard {

    private ArrayList<EnumColorCardAndAmmo>secondLockCost;

    /**
     * Instantiates a new Lock Rifle card.
     * Sets the field color to BLU calling the constructor of weapon card (the super class).
     * Creates the list of recharge cost settings its value to BLU,BLU.
     * Creates the list of effects setting its value to BaseEffect,SecondLockEffect.
     * Creates the list of second lock cost(cost of optional effect 1) settings it to RED.
     */
    public LockRifle(){

        super("LOCK RIFLE", EnumColorCardAndAmmo.BLU);
        ArrayList<EnumColorCardAndAmmo>rechargeCost=new ArrayList<EnumColorCardAndAmmo>();
        rechargeCost.add(EnumColorCardAndAmmo.BLU);
        rechargeCost.add(EnumColorCardAndAmmo.BLU);
        setRechargeCost(rechargeCost);
        ArrayList<WeaponsEffect> weaponEffects=new ArrayList<>();
        weaponEffects.add(WeaponsEffect.BaseEffect);
        weaponEffects.add(WeaponsEffect.SecondLockEffect);
        setWeaponEffects(weaponEffects);
        secondLockCost=new ArrayList<EnumColorCardAndAmmo>();
        secondLockCost.add(EnumColorCardAndAmmo.RED);
        setOptional(true);
        setDescription("Basic Effect: Deal 2 damage and 1 mark to 1 target you can see.\n" +
                "with Second Lock: Deal 1 mark to a different target you can see.");
    }

    /**
     * get secondLockCost
     */
    public ArrayList<EnumColorCardAndAmmo> getSecondLockCost() {

        return secondLockCost;
    }

    /**
     * Shoot and mark a player who current player can see.
     *
     * @param map the map of the game.
     * @param currentPlayer the current player.
     * @param target1 the first player you want to shoot.
     * @throws NotVisibleTarget
     */
    public void baseEffect(Map map, Player currentPlayer, Player target1) throws NotVisibleTarget {

        if(map.isVisible(currentPlayer,target1)){

            ArrayList<EnumColorPlayer> lockRifleDamages= new ArrayList<EnumColorPlayer>();
            lockRifleDamages.add(currentPlayer.getColor());
            lockRifleDamages.add(currentPlayer.getColor());
            target1.multipleDamagesSingleMark(lockRifleDamages,currentPlayer.getColor());
        }else{

            throw new NotVisibleTarget();
        }
    }

    /**
     * Mark a second player (different by the first) who current player can see.
     *
     * @param map the map of the game.
     * @param currentPlayer the current player.
     * @param target2 the second player you want to shoot.
     * @throws NotVisibleTarget
     */
    public void secondLockEffect(Map map, Player currentPlayer, Player target2) throws NotVisibleTarget {

        if (map.isVisible(currentPlayer, target2)){

            target2.singleMark(currentPlayer.getColor());
        }else{

            throw new NotVisibleTarget();
        }
    }

}
