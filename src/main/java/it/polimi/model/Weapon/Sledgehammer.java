package it.polimi.model.Weapon;

import it.polimi.model.*;
import it.polimi.model.Exception.MapException;
import it.polimi.model.Exception.NotInSameDirection;
import it.polimi.model.Exception.NotValidDistance;
import it.polimi.model.Exception.NotValidInput;

import java.util.ArrayList;

/**
 * The type Sledgehammer.
 */
public class Sledgehammer extends WeaponCard {
    
    private ArrayList<EnumColorCardAndAmmo> pulverizeModeCost;
    
    /**
     * Instantiates a new Sladgehammer card.
     * Sets the field color to YELLOW calling the constructor of weapon card (the super class).
     * Creates the list of recharge cost setting its value to YELLOW.
     * Creates the list of effects setting its value to BaseMode,PulverizeMode.
     * Creates the list of pulverize mode cost (cost of alternative fire mode) settings it to RED.
     */
    public Sledgehammer () {
    
        super("SLADGEHAMMER", EnumColorCardAndAmmo.YELLOW);
        ArrayList<EnumColorCardAndAmmo> rechargeCost = new ArrayList<EnumColorCardAndAmmo>();
        rechargeCost.add(EnumColorCardAndAmmo.YELLOW);
        setRechargeCost(rechargeCost);
        ArrayList<WeaponsEffect> weaponsEffects = new ArrayList<>();
        weaponsEffects.add(WeaponsEffect.BaseMode);
        weaponsEffects.add(WeaponsEffect.PulverizeMode);
        setWeaponEffects(weaponsEffects);
        pulverizeModeCost = new ArrayList<EnumColorCardAndAmmo>();
        pulverizeModeCost.add(EnumColorCardAndAmmo.RED);
        setOptional(false);
        setDescription("Basic Mode: Deal 2 damage to 1 target on your square.\n" +
                "in Pulverize Mode: Deal 3 damage to 1 target on your square, then move that target 0, 1, or 2 squares in one direction.\n" +
                "Notes: Remember that moves go through doors, but not walls.");
    }
    
    /**
     * get pulverizeModeCost
     *
     * @return the pulverize mode cost
     */
    public ArrayList<EnumColorCardAndAmmo> getPulverizeModeCost () {
        
        return pulverizeModeCost;
    }
    
    /**
     * Shoot a player who is on current player's square.
     *
     * @param map           the map of the game.
     * @param currentPlayer the current player.
     * @param target1       the player you want to shoot.
     * @throws NotValidDistance
     */
    public void baseMode (Map map, Player currentPlayer, Player target1) throws NotValidDistance {
        
        if (map.distance(currentPlayer, target1) == 0) {
            
            ArrayList<EnumColorPlayer> sledgehammerDamages = new ArrayList<>();
            sledgehammerDamages.add(currentPlayer.getColor());
            sledgehammerDamages.add(currentPlayer.getColor());
            target1.multipleDamages(sledgehammerDamages);
        } else {
            
            throw new NotValidDistance();
        }
    }
    
    /**
     * Shoot a player who is on current player's square and move he by one or two movements in the same direction.
     *
     * @param map           the map of the game.
     * @param currentPlayer the current player.
     * @param target1       the player you want to shoot.
     * @param destSquare    the square where shot player is moved.
     * @throws NotValidDistance
     * @throws NotInSameDirection
     * @throws MapException
     */
    public void pulverizeMode (Map map, Player currentPlayer, Player target1, Square destSquare) throws NotValidDistance, NotInSameDirection, MapException {
        
        Square target1Square = map.findPlayer(target1);
        
        if ((map.distance(currentPlayer, target1) == 0) && (map.distance(target1Square, destSquare) < 3) && (map.sameDirection(target1Square, destSquare))) {
            
            ArrayList<EnumColorPlayer> sledgehammerDamages = new ArrayList<>();
            sledgehammerDamages.add(currentPlayer.getColor());
            sledgehammerDamages.add(currentPlayer.getColor());
            sledgehammerDamages.add(currentPlayer.getColor());
            target1.multipleDamages(sledgehammerDamages);
            map.movePlayer(target1, destSquare);
        } else if (!(map.sameDirection(target1Square, destSquare))) {
            
            throw new NotInSameDirection();
        } else if ((map.distance(currentPlayer, target1) != 0) || (map.distance(target1Square, destSquare) >= 3)) {
            
            throw new NotValidDistance();
            
        }
    }
}

