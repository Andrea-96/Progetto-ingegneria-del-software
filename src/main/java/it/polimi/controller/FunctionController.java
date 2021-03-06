package it.polimi.controller;


import com.sun.xml.internal.bind.v2.model.core.EnumConstant;
import it.polimi.model.*;
//chiedere perche devo importare tutto
import it.polimi.model.Exception.*;
import it.polimi.model.Map;
import it.polimi.model.PowerUp.Newton;
import it.polimi.model.PowerUp.TagBackGrenade;
import it.polimi.model.PowerUp.TargetingScope;
import it.polimi.model.PowerUp.Teleporter;
import it.polimi.model.Weapon.Electroscythe;
import it.polimi.view.RemoteView;

import java.rmi.RemoteException;
import java.util.*;


public class FunctionController {
    
    WeaponController weaponController;
    FunctionModel functionModel;
    private transient Timer timer;
    private transient Timer timerCurrentPlayer;
    private int delay;
    
    public FunctionController(FunctionModel functionModel,int delay){
        
        this.functionModel=functionModel;
        this.weaponController = new WeaponController(this);
        this.delay=delay;
        
    }
    
    /**
     * starts a timer when the state is LOBBY
     * when expired, go on start turn
     */
    private void startTimerLobby(){
        timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                   
                        functionModel.getGameModel().setActualPlayer(functionModel.getGameModel().getPlayers(true,true).get(0));
                        functionModel.getGameModel().setState(State.PUTSPAWN);
                    }
                }, delay
        );
    }
    
    private void startTimerCurrentPlayer(){
        timerCurrentPlayer = new Timer();
        timerCurrentPlayer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        
                        Player actual =functionModel.getGameModel().getActualPlayer();
                        int indexOfActual = functionModel.getGameModel().getPlayers(true,true).indexOf(actual);
                        try {
                            functionModel.getGameModel().getRemoteViews().get(indexOfActual).quitClient();
                        } catch (RemoteException e) {
                            //nothing to do
                        }
                        functionModel.getGameModel().getPlayers(true,true).get(indexOfActual).setOnlineModel(false);
                        functionModel.getGameModel().getRemoteViews().set(indexOfActual, null);
                        endTurn();
                    }
                }, 150000
        );
    }
    
    
    public void lobby() throws RemoteException {
    
        GameModel gameModel = this.functionModel.getGameModel();
        
        
        if (gameModel.getPlayers(true,true).size() == 3) {
            
            startTimerLobby();
            
            }else
                if(gameModel.getPlayers(true,true).size() == 5){
            
                timer.cancel();
                //now game can start
                gameModel.setActualPlayer(gameModel.getPlayers(true,true).get(0));
                
                gameModel.setState(State.PUTSPAWN);
            
            } else {
            
                gameModel.setState(State.LOBBY);
            }
   
    }
    
    public void checkActionCount(){
        
        if (functionModel.getGameModel().getActionCount()==2){
           
           setErrorState("YOU ALREADY USE 2 ACTION. YOU CAN ONLY GO ON MENU OR PASS TURN (HERE YOU CAN RECHARGE YOUR WEAPON)");
        }
        
    }
    
    public void choseAction(RemoteView view) throws RemoteException {
    
        int choice;
        functionModel.getGameModel().setMessageToCurrentView("");
        functionModel.getGameModel().setMessageToAllView("");
        functionModel.getGameModel().setErrorMessage("");
        
      
            
            choice = view.getIndex();
            
            switch (choice){
                
                case -1:
                    this.functionModel.getGameModel().setState(State.MENU);
                    break;
                case 1:
                    checkActionCount();
                    this.functionModel.getGameModel().setState(State.SELECTRUN);
                    break;
                case 2:
                    checkActionCount();
                    this.functionModel.getGameModel().setState(State.SELECTGRAB);
                    break;
                case 3:
                    
                    checkActionCount();
                    if(this.functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerWeapons().size()>0) {
                        
                        this.functionModel.getGameModel().setState(State.SELECTWEAPON);
                    } else {
                        
                        setErrorState("YOU HAVE NOT WEAPON TO SHOOT");
                        
                    }
                    break;
                case 4:
                    
                    if(this.functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerPowerUps().size()>0){
            
                        this.functionModel.getGameModel().setState(State.SELECTPOWERUP);
                    } else {
                        
                        setErrorState("YOU HAVE NOT POWER UP TO USE");
                    }
                    break;
                case 0:
                    
                    endActionGestor();
                    this.functionModel.getGameModel().setState(State.ENDACTIONSELECTION);
                    
            }
   
    
    }
    
    //error state
    public void errorState(RemoteView view) throws RemoteException {
        
        System.out.println("ERROR STATE-->\n"+"ERROR MESSAGE: "+ functionModel.getGameModel().getErrorMessage() +"\nRESTART IN STATE CHOICE STATE-->");
        view.resetInput();
        functionModel.getGameModel().resetGameModelParameter();
        resetParameterWeapon();
        switch (this.functionModel.getGameModel().getBeforeError()){
            
            case SELECTEFFECT:
                this.functionModel.getGameModel().setState(State.SELECTEFFECT);
                break;
            case SHOOT:
                this.functionModel.getGameModel().setState(State.SELECTEFFECT);
                break;
            case SELECTRECHARGE:
                if (functionModel.getGameModel().isEndTurn()){
                    
                    //if recharge don't go and player want to end turn go to respawn player
                    this.functionModel.getGameModel().setState(State.RESPWANPLAYERSELECTION);
                    
                }
            //case GRENADESELECTION:
                //this.functionModel.getGameModel().setState(State.GRENADESELECTION);
                
            default:
                this.functionModel.getGameModel().setState(State.MENU);
                break;
                
            
        }
    }
    
    //state gestor and map error gestor
    public void setErrorState(String string){
        
        if (functionModel.getGameModel().getAvailableEffect().contains(WeaponsEffect.BaseEffect) ||functionModel.getGameModel().getAvailableEffect().contains(WeaponsEffect.BaseMode)){
            functionModel.getGameModel().getAvailableEffect().removeAll(functionModel.getGameModel().getAvailableEffect());
        }
        this.functionModel.getGameModel().setErrorMessage(string);
        this.functionModel.getGameModel().setMessageToCurrentView(string);
        this.functionModel.getGameModel().setBeforeError(this.functionModel.getGameModel().getState());
        this.functionModel.getGameModel().setState(State.ERROR);
    }
    
    public void mapErrorGestor() throws RemoteException {
        
        this.functionModel.getGameModel().setMessageToCurrentView("YOUR INPUT IS NOT VALID");
        this.functionModel.getGameModel().setBeforeError(this.functionModel.getGameModel().getState());
        this.functionModel.getGameModel().setState(State.ERROR);
    }
    
    //start turn
    public void startTurn(RemoteView view) throws RemoteException {
        
        //set the lowest id to the current player
        this.functionModel.getGameModel().setActualPlayer(this.functionModel.getGameModel().getPlayers(true,true).get(0));
    
        functionModel.getGameModel().setSpawnPlayer(null);
        functionModel.getGameModel().resetSpawnedPLayer();
        
        startTimerCurrentPlayer();
        
        //put ammo and weapon card
        refreshMapEndTurn();
        
        //now game can start
        this.functionModel.getGameModel().setState(State.MENU);
        
    }
    
    private void refreshMapEndTurn(){
    
        //PUT CARD
        this.functionModel.refreshMapAmmoCard();
        this.functionModel.refreshMapWeaponCard();
        
    }
    
    public void resetParameterWeapon (){
    
        functionModel.getGameModel().setWeaponSelected(null);
        functionModel.getGameModel().setWeaponState(null);
        functionModel.getGameModel().setActualWeaponEffect(null);
        functionModel.getGameModel().setWeaponName(null);
        functionModel.getGameModel().setBeforeEffect(null);
        functionModel.getGameModel().getAvailableEffect().removeAll(functionModel.getGameModel().getAvailableEffect());
        functionModel.getGameModel().getWeaponToCharge().removeAll(functionModel.getGameModel().getWeaponToCharge());
    }
    
    public void menu(RemoteView view) throws RemoteException {
        
        if (view.getIndex()==1){
            
            functionModel.getGameModel().setState(State.CHOSEACTION);
            
        } else {
            
            functionModel.getGameModel().setState(State.MENU);
        }
    }
    
    //spawn player
    public void drawnPowerUp (int i) throws RemoteException {
    
        GameModel gameModel = this.functionModel.getGameModel();
        
        if (i==1) {
            
    
            for (Player a : gameModel.getPlayers(true,true)) {
                ArrayList<PowerUpCard> tempPowerUp = new ArrayList<>();
                tempPowerUp.add(gameModel.getPowerUpDeck().drawnPowerUpCard());
                tempPowerUp.add(gameModel.getPowerUpDeck().drawnPowerUpCard());
                //System.out.println(tempPowerUp.toString());
                a.setPowerUpCardsSpawn(tempPowerUp);
            }
            gameModel.setState(State.FIRSTSPAWN);
        } else {
    
            for (Player a : gameModel.getDeadPlayers()) {
                ArrayList<PowerUpCard> tempPowerUp = new ArrayList<>();
                tempPowerUp.add(gameModel.getPowerUpDeck().drawnPowerUpCard());
                //System.out.println(tempPowerUp.toString());
                a.setPowerUpCardsSpawn(tempPowerUp);
            }
        }
    }
    
    public void firstSpawn(RemoteView view) throws RemoteException {
        
        //put 2 power up for all palyer in game
        
        if (functionModel.getGameModel().getSpawnedPlayer()==functionModel.getGameModel().getPlayers(true,true).size()){
            
            //all player are spawned correctly. Now can start the turn.
            functionModel.getGameModel().setState(State.STARTTURN);
        } else {
            
            for (Player a : functionModel.getGameModel().getPlayers(true,true)) {
                
                if (a.getRow() == -1 && a.getColumn() == -1) {
                    
                    functionModel.getGameModel().setSpawnPlayer(a);
                    functionModel.getGameModel().setState(State.SELECTSPAWN);
                }
            }
        }
    }
    
    public void selectSpawn(RemoteView view) throws RemoteException {
        
        respawnPlayerController(functionModel.getGameModel().getSpawnPlayer(), view);
        functionModel.getGameModel().incrementSpawnedPlayer();
        functionModel.getGameModel().setState(State.FIRSTSPAWN);
        
    }
    
    public void respawnPlayerController (Player player, RemoteView view) throws RemoteException {
        
        int chosenPowerUp;
        EnumColorSquare colorSquare;
        PowerUpCard  powerUpCard;
        
        try {
            
            chosenPowerUp = view.getIndex();
            if (player.getPowerUpCardsSpawn().size()>chosenPowerUp){
                
                if(player.getPowerUpCardsSpawn().get(chosenPowerUp)!=null) {
        
                    //get the color to respawn
                    colorSquare = player.getPowerUpCardsSpawn().get(chosenPowerUp).getColorRespawn();
                    player.getPowerUpCardsSpawn().remove(chosenPowerUp);
                    //add player on generation square of the color chosed
                    this.functionModel.getGameModel().getMap().addPlayerOnSquare(this.functionModel.getGameModel().getMap().getGenerationSquare(colorSquare), player);
        
                    //add the other power up to player list
                    if (player.getPowerUpCardsSpawn().size() > 0) {
        
                        powerUpCard = player.getPowerUpCardsSpawn().get(0);
                        player.getPlayerBoard().getPlayerPowerUps().add(powerUpCard);
                        player.getPowerUpCardsSpawn().clear();
                    }
                    player.getPowerUpCardsSpawn().clear();
        
                } else {
    
                setErrorState("INPUT FOR SPAWN NOT CORRECT");
            }
            } else {
    
                setErrorState("INPUT FOR SPAWN NOT CORRECT");
            }
            
            
        } catch (MapException e) {
            
            mapErrorGestor();
            
        }
    }
    
    
    //run action
    public void runActionController (RemoteView view) throws RemoteException {
        
        //take necessary
        Map map= this.functionModel.getGameModel().getMap();
        Square inputSquare;
        
        try {
            
            inputSquare = map.getSquare(view.getRow(),view.getColumn());
            
            if(map.existInMap(inputSquare)) {
                
                this.functionModel.runFunctionModel(inputSquare);
                this.functionModel.getGameModel().setMessageToAllView("CURRENT PLAYER " + this.functionModel.getGameModel().getActualPlayer().getName().toString() +" MOVED IN SQUARE: " + inputSquare.toString());
                this.functionModel.getGameModel().setState(State.RUN);
                
            }
        } catch (MapException e) {
    
            mapErrorGestor();
            
        } catch (RunActionMaxDistLimitException e) {
            
            this.functionModel.getGameModel().setMessageToCurrentView("YOUR MOVE EXCED MAX DISTANCE LIMIT");
            this.functionModel.getGameModel().setBeforeError(this.functionModel.getGameModel().getState());
            this.functionModel.getGameModel().setState(State.ERROR);
        }
        
    }
    
    public void run(RemoteView view) throws RemoteException {
        
        view.resetInput();
        functionModel.getGameModel().incrementActionCount();
        this.functionModel.getGameModel().setState(State.MENU);
       
    }
    
    //gran action
    public void grabActionController (RemoteView view) throws RemoteException{
    
        //take necessary
        Map map = this.functionModel.getGameModel().getMap();
        
        //answer to view an input Square
        Square inputSquare;
        
        try {
            inputSquare = map.getSquare(view.getRow(), view.getColumn());
    
            int indexWeapon = -1;
    
            //temp variables
            if (map.existInMap(inputSquare)) {
        
            try {
                
                //guardo se la square è di generation, se si devo chidere alla view l'index dell'arma
                if (map.isGenerationSquare(inputSquare)) {
                    
                    GenerationSquare gen = (GenerationSquare) inputSquare;
                    if (gen.getWeaponList().size()>0) {
                        
                        if (view.getIndex2()!=-1){
                            functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerWeapons().remove(view.getIndex2());
                        }
    
                        //effective catch gia con l'index giusto se è una Generation Square
                        indexWeapon = view.getIndex();
                        this.functionModel.grabActionModel(inputSquare, indexWeapon);
                        this.functionModel.getGameModel().setState(State.GRAB);
                    } else {
    
                        setErrorState("THIS GENERATION SQUARE IS EMPTY");
                    }
                    
                } else {
    
                    NormalSquare asNormal = (NormalSquare) inputSquare;
                    if (asNormal.containAmmoCard()) {
    
                        this.functionModel.grabActionModel(inputSquare, indexWeapon);
                        this.functionModel.getGameModel().setState(State.GRAB);
                    } else {
                        
                        setErrorState("THIS SQUARE NOT CONTAIN AMMO CARD TO GRAB");
                    }
                }
        
                
                } catch(GrabActionMaxDistLimitException catchActionMaxDistExpetion){
                
                    setErrorState("YOUR MOVE FOR GRAB EXCED MAX DISTANCE LIMIT");
    
                } catch(GrabActionFullObjException e){
                
                    setErrorState("YOU DON'T HAVE MORE SPACE FOR GRAB OBJECT");
                } catch(MapException e){
    
                    mapErrorGestor();
                }
            }else{
    
                mapErrorGestor();
            }
        } catch(MapException e){
    
            mapErrorGestor();
        }
        
    }
    
    public void grab(RemoteView view) throws RemoteException {
    
        view.resetInput();
        functionModel.getGameModel().incrementActionCount();
        this.functionModel.getGameModel().setState(State.MENU);
        
    }
    
    //powerUp action
    public void selectPowerUp(RemoteView view) throws RemoteException {
        
        PowerUpCard powerUpCard;
        
        int i;
        
        i = view.getIndex();
        
        if (this.functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerPowerUps().get(i) != null) {
            
            powerUpCard = this.functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerPowerUps().get(i);
            
            if(!powerUpCard.getNameCard().equals("TAGBACK GRENADE")) {
                
                this.functionModel.getGameModel().setPowerUpSelected(powerUpCard);
                this.functionModel.getGameModel().setState(State.SELECTPOWERUPINPUT);
            } else {
                
                setErrorState("YOU CAN'T USE TAGBACK GRENADE IN YOUR TURN");
                
            } if (powerUpCard.getNameCard().equals("TARGETING SCOPE")){
        
                setErrorState("YOU CAN'T USE TARGETING SCOPE IN THIS MOMENT, ONLY AFTER SHOOT");
            }
        } else {
            
            mapErrorGestor();
        }
        
    }
    
    public void selectPowerUpInput (RemoteView view) throws RemoteException{

        //NEWTON
        if (Newton.class.equals(this.functionModel.getGameModel().getPowerUpSelected().getClass())) {
            Player targetPlayer;
            Square targetSquare;
            
            try {
                //get input
                targetPlayer = this.functionModel.getGameModel().getPlayerById(view.getTarget1());
                targetSquare = this.functionModel.getGameModel().getMap().getSquare(view.getRow(), view.getColumn());
                //effect
                this.functionModel.usePowerUpNewton((Newton)this.functionModel.getGameModel().getPowerUpSelected(),targetPlayer, targetSquare);
                functionModel.getGameModel().setMessageToAllView("CURRENT PLAYER " + functionModel.getGameModel().getActualPlayer().getName() +" USE POWER UP NEWTON");
                functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerPowerUps().remove(this.functionModel.getGameModel().getPowerUpSelected());
                functionModel.getGameModel().setState(State.USEPOWERUP);
            } catch (NotInSameDirection notInSameDirection) {
                
                setErrorState("ERROR: THE CHOSEN TARGETS ARE NOT IN THE SAME DIRECTION");
            } catch (NotValidDistance notValidDistance) {
                
                setErrorState("ERROR: THE CHOSEN TARGET IS NOT VISIBLE");
            } catch (MapException e) {
                mapErrorGestor();
            }
            //TELEPORTER
        } else if (this.functionModel.getGameModel().getPowerUpSelected().getClass().equals(Teleporter.class)) {
    
            Square targetSquare;
            try {
    
                //get input
                targetSquare = this.functionModel.getGameModel().getMap().getSquare(view.getRow(), view.getColumn());
                //effect
                this.functionModel.usePowerUpTeleporter((Teleporter) this.functionModel.getGameModel().getPowerUpSelected(), targetSquare);
                functionModel.getGameModel().setMessageToAllView("CURRENT PLAYER " + functionModel.getGameModel().getActualPlayer().getName() + " USE POWER UP TELEPORTER");
                functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerPowerUps().remove(this.functionModel.getGameModel().getPowerUpSelected());
                functionModel.getGameModel().setState(State.USEPOWERUP);
    
            } catch (MapException e) {
    
                mapErrorGestor();
            }
        }
    }
    
    public void usePowerUp(RemoteView view) throws RemoteException {
    
        view.resetInput();
        this.functionModel.getGameModel().setState(State.MENU);
    }
    
    
    //recharge
    public void setWeaponToCharge(){
        
        for (WeaponCard a: functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerWeapons()){
            
            if (!a.isCharge()){
                
                functionModel.getGameModel().getWeaponToCharge().add(a);
            }
        }
    }
    
    public void selectRechargeGestor(RemoteView view) throws RemoteException {
        
        if (functionModel.getGameModel().getWeaponToCharge().size()>0) {
            
            selectRecharge(view,1,null);
            functionModel.getGameModel().setState(State.RECHARGE);
        } else  if (functionModel.getGameModel().isEndTurn()) {
    
            //setta chi deve respawnare e parti con la routine di respawn
            functionModel.getGameModel().setState(State.SELECTRESPAWNPLAYER);
        }
    }
    
    public void selectRecharge (RemoteView view,int i,ArrayList<EnumColorCardAndAmmo> payExtra ) throws RemoteException {
        
        ArrayList<PowerUpCard> powerUpToPay = new ArrayList<>();
       
        try {
            
            if (i==1) {
                WeaponCard weaponToCharge = functionModel.getGameModel().getWeaponToCharge().get(view.getIndex());
                if (view.isBooleanChose()) {
        
                    if (view.getIndex2() != -1 && functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerPowerUps().size() <= view.getIndex2()) {
                        
                        powerUpToPay.add(functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerPowerUps().get(view.getIndex2()));
                   
                        if (view.getIndex3() != -1 && functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerPowerUps().size() <= view.getIndex3()) {
                            
                            powerUpToPay.add(functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerPowerUps().get(view.getIndex3()));
                
                            if (view.getIndex4() != -1 && functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerPowerUps().size() <= view.getIndex4()) {
                    
                                powerUpToPay.add(functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerPowerUps().get(view.getIndex4()));
                    
                                if (view.getIndex5() != -1 && functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerPowerUps().size() <= view.getIndex5()) {
                        
                                    powerUpToPay.add(functionModel.getGameModel().getActualPlayer().getPlayerBoard().getPlayerPowerUps().get(view.getIndex5()));
                        
                                }  else {
                                    throw new NotValidAmmoException();
                                }
                            }  else {
                                throw new NotValidAmmoException();
                            }
                        }  else {
                            throw new NotValidAmmoException();
                        }
                    }  else {
                        throw new NotValidAmmoException();
                    }
                }
            
                payAmmoController(weaponToCharge.getRechargeCost(), powerUpToPay);
                weaponToCharge.setCharge(true);
                functionModel.getGameModel().getWeaponToCharge().remove(weaponToCharge);
                
                if (functionModel.getGameModel().getWeaponToCharge().size()>0){
                    
                    functionModel.getGameModel().setState(State.ENDACTIONSELECTION);
                } else {
                    
                    functionModel.getGameModel().setState(State.RESPWANPLAYERSELECTION);
                }
                
            } else  if(i==2){
                
                payAmmoController(payExtra, powerUpToPay);
                functionModel.getGameModel().setState(State.SELECTSHOOTINPUT);
    
            }
        } catch (NotValidAmmoException e) {
            
            setErrorState("YOU HAVE NOT AMMO TO PAY, OR YOUR INPUT ABOUT POWER UP IS NOT CORRECT");
        }
    }
    
    public void payAmmoController (ArrayList<EnumColorCardAndAmmo> toPay , ArrayList<PowerUpCard> powerUpToPay) throws NotValidAmmoException {
        
        PlayerBoard actualPlayerBoard = functionModel.getGameModel().getActualPlayer().getPlayerBoard();
        boolean canGo = false;
        
        //check for pay
        //available
        int redAv = Collections.frequency(actualPlayerBoard.getAmmo(), EnumColorCardAndAmmo.RED);
        int yellowAv = Collections.frequency(actualPlayerBoard.getAmmo(), EnumColorCardAndAmmo.YELLOW);
        int blueAv = Collections.frequency(actualPlayerBoard.getAmmo(), EnumColorCardAndAmmo.BLU);
        //to pay
        int redToPay = Collections.frequency(toPay, EnumColorCardAndAmmo.RED);
        int yellowToPay = Collections.frequency(toPay, EnumColorCardAndAmmo.YELLOW);
        int blueToPay = Collections.frequency(toPay, EnumColorCardAndAmmo.BLU);
        
        //add powerUp value in available
        if (powerUpToPay.size()>0) {
            
            for (PowerUpCard power : powerUpToPay) {
                
                if (power.getColorPowerUpCard() == EnumColorCardAndAmmo.RED) {
                    
                    redAv++;
                }
                if (power.getColorPowerUpCard() == EnumColorCardAndAmmo.BLU) {
                    
                    blueAv++;
                }
                if (power.getColorPowerUpCard() == EnumColorCardAndAmmo.YELLOW) {
                    
                    yellowAv++;
                }
                
            }
        }
        
        if (redToPay > 0) {
            canGo = redToPay <= redAv;
        }
        if (blueToPay > 0) {
            canGo = blueToPay <= blueAv;
        }
        if (yellowToPay > 0) {
            canGo = yellowToPay <= yellowAv;
        }
        
        if(canGo) {
            
            if (powerUpToPay.size() != 0) {
                
                //pay with ammo and powerUp selected
                
                //first pay powerUp
                for (int i = 0; i < powerUpToPay.size(); i++) {
                    PowerUpCard power = powerUpToPay.get(i);
                    
                    if (toPay.contains(power.getColorPowerUpCard())) {
                        
                        powerUpToPay.remove(power);
                        actualPlayerBoard.getPlayerPowerUps().remove(power);
                        toPay.remove(power.getColorPowerUpCard());
                        i--;
                    }
                }
            }
            if (toPay.size() > 0) {
                
                // pay with ammo
                for (int i = 0; i < toPay.size(); i++) {
                    
                    EnumColorCardAndAmmo pay = toPay.get(i);
                    actualPlayerBoard.decreaseAmmo(pay);
                    toPay.remove(pay);
                    i--;
                }
            }
        } else {
            
            throw new NotValidAmmoException();
        }
    }
    
    public void recharge(RemoteView view) throws RemoteException {
        
        view.resetInput();
        if (!functionModel.getGameModel().isEndTurn()) {
            
            functionModel.getGameModel().setState(State.MENU);
        } else {
            
            functionModel.getGameModel().setState(State.ENDACTIONSELECTION);
        }
    }
    
    //end action
    public void endActionSelect(RemoteView view) throws RemoteException {
        
        if (view.isBooleanChose() && functionModel.getGameModel().getWeaponToCharge().size()>0){
            
            functionModel.getGameModel().setEndTurn(true);
            functionModel.getGameModel().setState(State.SELECTRECHARGE);
        } else {
            /*
            //use grenade
            grenadeGestor();
            if (functionModel.getGameModel().getPlayerDamagedWithGrenade().size()>0){
                
                functionModel.getGameModel().setUserGrenade(functionModel.getGameModel().getPlayerDamagedWithGrenade().get(0));
                functionModel.getGameModel().setState(State.GRENADESELECTION);
                
             */
           // } else {
    
                functionModel.getGameModel().setState(State.ENDACTION);
         //   }
            
        }
    }
    
    public void endActionGestor (){
    
        setWeaponToCharge();
        functionModel.getGameModel().setDeadPlayer();
        
    }
    
    public TagBackGrenade getCorrectGrenade(){
        TagBackGrenade tagBack;
        
        for (PowerUpCard a: functionModel.getGameModel().getUserGrenade().getPlayerBoard().getPlayerPowerUps()){
            
            if (a.getNameCard().equals("TAGBACK GRENADE")){
                
                return (TagBackGrenade) a;
            }
        }
        return null;
    }
    
    public TargetingScope getCorrectTargeting(){
       
        TargetingScope targetingScope;
        
        for (PowerUpCard a: functionModel.getGameModel().getUserGrenade().getPlayerBoard().getPlayerPowerUps()){
            
            if (a.getNameCard().equals("TARGETING SCOPE")){
                
                return (TargetingScope) a;
            }
        }
        return null;
    }
    
    public void grendadeSelection (RemoteView view) throws RemoteException {
    
        if (view.isBooleanChose()) {
        
            Player targetPlayer = functionModel.getGameModel().getActualPlayer();
            try {
               if (getCorrectGrenade()!=null) {
                   //effect
                   this.functionModel.usePowerUpTagBackGrenade(getCorrectGrenade(), targetPlayer);
                   functionModel.getGameModel().setMessageToAllView("PLAYER " + functionModel.getGameModel().getUserGrenade() + " USE POWER UP TAGBACK GRENADE ON CURRENT PLAYER " + functionModel.getGameModel().getActualPlayer().getName());
                   functionModel.getGameModel().setState(State.GRENADE);
               } else {
                   
                   setErrorState("INVALID GRENADE GESTOR");
               }
              
            } catch (NotVisibleTarget notVisibleTarget) {
    
                setErrorState("ERROR: THE CURRENT PLAYER IS NOT VISIBLE WHEN DAMAGED YOU");
    
            }
        
        } else {
    
            grenadeGestor();
            functionModel.getGameModel().setState(State.GRENADE);
        }
    }
    
    public void grenade() throws RemoteException {
    
        /*
        if (functionModel.getGameModel().getUserGrenadeCount()<functionModel.getGameModel().getPlayerDamagedWithGrenade().size()) {
        
            functionModel.getGameModel().getPlayerDamagedWithGrenadeVisibility().remove(functionModel.getGameModel().getUsedGrenade().indexOf(functionModel.getGameModel().getUsedGrenade()));
            functionModel.getGameModel().getPlayerDamagedWithGrenade().remove(functionModel.getGameModel().getUserGrenade());
            functionModel.getGameModel().incremanetGrenade();
            functionModel.getGameModel().setUserGrenade(functionModel.getGameModel().getPlayerDamagedWithGrenade().get(functionModel.getGameModel().getUserGrenadeCount()));
            functionModel.getGameModel().setState(State.GRENADESELECTION);
        } else {
            //Scroring dead playerboard
            
            
         */
            //clear original spawn
            functionModel.getGameModel().resetSpawnedPLayer();
            drawnPowerUp(2);
            deadPlayerGestor();
            functionModel.getGameModel().setState(State.SCORINGPLAYERBOARD);
        }
    //}
    
    public void grenadeGestor(){
    
        ArrayList<Player> playerDamaged = functionModel.getGameModel().getPlayerDamaged();
        ArrayList<Boolean> playerDamagedVisibility = functionModel.getGameModel().getPlayerDamagedWithGrenadeVisibility();
        
        if (playerDamaged.size()>0 && playerDamagedVisibility.size()>0 && playerDamaged.size()==playerDamagedVisibility.size()) {
            for (int i = 0; i < playerDamaged.size(); i++) {
                Player a = playerDamaged.get(i);
                Boolean b = playerDamagedVisibility.get(i);
        
                for (PowerUpCard c : a.getPlayerBoard().getPlayerPowerUps()) {
            
                    if (c.getNameCard().equals("TAGBACK GRENADE") && b) {
                
                        functionModel.getGameModel().getPlayerDamagedWithGrenade().add(a);
                    }
                }
            }
        }
    }
    
    //dead player
    public void deadPlayerGestor() throws RemoteException {
        
        if (functionModel.getGameModel().getDeadPlayers().size()>0) {
            
            scoringPlayerBoardController();
            functionModel.getGameModel().setActualDeadPLayer(functionModel.getGameModel().getDeadPlayers().get(0));
            functionModel.getGameModel().getActualDeadPLayer().getPowerUpCardsSpawn().clear();
            functionModel.getGameModel().getActualDeadPLayer().getPowerUpCardsSpawn().add(functionModel.getGameModel().getPowerUpDeck().drawnPowerUpCard());
            functionModel.getGameModel().setState(State.DEADPLAYERSELECT);
        } else {
            
            //finito di rianimare giocatori morti
            functionModel.getGameModel().setState(State.ENDTURN);
            
        }
     }
    
    public void deadPlayerSelect (RemoteView view) throws RemoteException {
        
        
        respawnPlayerController(functionModel.getGameModel().getActualDeadPLayer(), view);
        functionModel.getGameModel().getActualDeadPLayer().setAlive(true);
        functionModel.getGameModel().getActualDeadPLayer().setOverKill(false);
        functionModel.getGameModel().getActualDeadPLayer().setMarkToDead(false);
        functionModel.getGameModel().getDeadPlayers().remove(functionModel.getGameModel().getActualDeadPLayer());
        
        functionModel.getGameModel().setState(State.ENDACTION);
        
    }
    
    //END TURN
    public void  endTurn() {
        
        //now pass the turn
        functionModel.getGameModel().resetGameModelParameter();
        passTurn();
        functionModel.getGameModel().setState(State.MENU);
        
    }
    
    public void passTurn() {
    
        timerCurrentPlayer.cancel();
        startTimerCurrentPlayer();
        KillShotTrack killShotTrack = functionModel.getGameModel().getKillShotTrack();
    
        if (killShotTrack.skullNumber() == 0) {
            
            functionModel.finalScoring();
            
        } else {
            
            if (killShotTrack.skullNumber()==1){
                
                functionModel.getGameModel().setFinalFrezyMessage("FINAL FREZY MODE ACTIVATED");
            }
        
            //pass turn to next player
            refreshMapEndTurn();
            int act = functionModel.getGameModel().getActualPlayer().getId() - 1;
            do {
            
                act++;
                if (act == functionModel.getGameModel().getPlayers(true, true).size()) {
                    act = 0;
                }
            } while (functionModel.getGameModel().getRemoteViews().get(act) == null);
            functionModel.getGameModel().resetActionCount();
            resetParameterWeapon();
            functionModel.getGameModel().setActualPlayer(functionModel.getGameModel().getPlayers(true, true).get(act));
        }
    }
    
    //scoring
    public void scoringPlayerBoardController (){
        
        
        for (Player a:this.functionModel.getGameModel().getDeadPlayers()){
            //incasso una plancia alla volta e gestisco le mort
            this.functionModel.scoringPlayerBoard(a);
        }
    }
    
    
}


