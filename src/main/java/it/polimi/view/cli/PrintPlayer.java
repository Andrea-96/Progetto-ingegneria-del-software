package it.polimi.view.cli;

import it.polimi.model.Player;

import java.io.Serializable;
import java.util.ArrayList;

public class PrintPlayer implements Serializable {

    /**
     * Print Player's attributes.
     * @param player   the player to printList
     */
    public static void print(Player player){

        System.out.println();
        System.out.println();
        System.out.println("********************************");
        PrintPlayerInfo.print(player);
        PrintScore.print(player);
        System.out.println("ALIVE: " +player.isAlive());
        PrintAmmo.print(player.getPlayerBoard().getAmmo(),1);
        System.out.println();
        System.out.println("  POWER UP: ");
        PrintPowerUp.print(player.getPlayerBoard().getPlayerPowerUps(),false);
        System.out.println();
        System.out.println("  WEAPONS: ");
        PrintWeapon.printList(player.getPlayerBoard().getPlayerWeapons(),false);
        System.out.println();
        System.out.println("********************************");
    }

    /**
     * Print Players' attributes.
     * @param players   the player to printList
     */
    public static void print(ArrayList<Player> players){

        for(Player p : players){

            PrintPlayerInfo.print(p);
        }
    }
}
