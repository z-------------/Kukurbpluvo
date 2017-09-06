package yy.zacharyguard.kukurbpluvo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Main extends JavaPlugin {
	
	Logger logger = getLogger();
	
	List<String> gourdRemarks = new ArrayList<String>();
	
	@Override
    public void onEnable() {
		logger.info("Ready to bash.");
		
		gourdRemarks.add("You have been caught off GOURD!");
		gourdRemarks.add("Get BASHED!");
    }
   
    @Override
    public void onDisable() {
       
    }
    
    @Override
    public boolean onCommand(CommandSender sender,
            Command command,
            String label,
            String[] args) {
        if (command.getName().equalsIgnoreCase("bash")) {
        	
        	Entity targetPlayer;
        	
        	if (args.length == 1) { // has username argument
        		String targetUsername = args[0];
        		targetPlayer = (Entity) Bukkit.getPlayer(targetUsername);
        		if (targetPlayer == null) {
        			sender.sendMessage("Could not find user " + targetUsername + ".");
        			return false;
        		}
        	} else if (sender instanceof Player) { // no arguments and sender is Player
        		targetPlayer = (Entity) sender;
        	} else { // no arguments and sender is not Player
        		sender.sendMessage("Target username must be supplied if calling from console.");
        		return false;
        	}
        	
    		String randomGourdRemark = gourdRemarks.get((int) (Math.random() * gourdRemarks.size()));
        	targetPlayer.sendMessage(ChatColor.DARK_PURPLE + randomGourdRemark);
        	
    		Location playerLocation = targetPlayer.getLocation();double playerX = playerLocation.getX();
        	double playerY = playerLocation.getY();
        	double playerZ = playerLocation.getZ();
        	World world = playerLocation.getWorld();
            
            int highestBlockY = world.getHighestBlockYAt(playerLocation);
            
            double spawnCenterX = playerX;
            double spawnCenterY = (double) world.getMaxHeight();
            double spawnCenterZ = playerZ;
            
            if (highestBlockY > playerY) {
            	spawnCenterY = (double) (highestBlockY - 1);
            }
            
            int squareSize = 5;
            
            double spawnCornerX = spawnCenterX - 2 * ((squareSize - 1) / 2);
            double spawnCornerZ = spawnCenterZ - 2 * ((squareSize - 1) / 2);
            
            BukkitScheduler scheduler = getServer().getScheduler();
            
            HashMap<Integer, Integer> taskMap = new HashMap<Integer, Integer>();
            
            for (int i = 0; i < squareSize; i++) {
            	final int thisI = i;
            	for (int k = 0; k < squareSize; k++) {
            		final int thisK = k;
            		FallingBlock fallingBlock = world.spawnFallingBlock(
            				new Location(world, spawnCornerX + 2 * i, spawnCenterY, spawnCornerZ + 2 * k), 
            				new MaterialData(Material.PUMPKIN)
            				);
            		fallingBlock.setDropItem(false); // don't spawn drop entity if cannot place block
            		
            		Plugin thisPlugin = this;
            		
            		int taskID = scheduler.scheduleSyncRepeatingTask(thisPlugin, new Runnable() {
                        @Override
                        public void run() {
                            Block blockUnder = fallingBlock.getLocation().subtract(0, 1, 0).getBlock();
                            Material blockUnderType = blockUnder.getType();
                            if (blockUnderType != Material.AIR 
                            		&& blockUnderType != Material.WATER 
                            		&& blockUnderType != Material.STATIONARY_WATER) {
                            	scheduler.cancelTask(taskMap.get(squareSize * thisI + thisK));
                            	scheduler.runTaskLater(thisPlugin, new Runnable() {
                            		@Override
                            		public void run() {
                            			Location landedLocation = fallingBlock.getLocation();
                            			Block block = landedLocation.getBlock();
                                    	if (block.getType() == Material.PUMPKIN) {
                                    		block.setType(Material.AIR);
                                    		world.createExplosion(
                                    				landedLocation.getX(), 
                                    				landedLocation.getY(), 
                                    				landedLocation.getZ(), 
                                    				4F, false, false);
                                    	}
                            		}
                        		}, 40L);
                            }
                        }
                    }, 0L, 5L);
            		
            		taskMap.put(squareSize * thisI + k, taskID);
            	}
            }
            
            return true;
        }
        return false;
    }
	
}
