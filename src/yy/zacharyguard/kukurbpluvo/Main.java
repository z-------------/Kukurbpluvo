package yy.zacharyguard.kukurbpluvo;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Main extends JavaPlugin {
	
	Logger logger = getLogger();
	
	@Override
    public void onEnable() {
		logger.info("Ready to bash.");
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
        	Location playerLocation = ((Entity) sender).getLocation();
        	double playerX = playerLocation.getX();
        	double playerY = playerLocation.getY();
        	double playerZ = playerLocation.getZ();
        	World world = playerLocation.getWorld();
        	
            sender.sendMessage("Your location: " + playerX + " <y> " + playerZ);
            
            int highestBlockY = world.getHighestBlockYAt(playerLocation);
            
            sender.sendMessage("Y-coord of highest block: " + highestBlockY);
            
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
                            if (blockUnder.getType() != Material.AIR) {
                            	scheduler.cancelTask(taskMap.get(squareSize * thisI + thisK));
                            	scheduler.runTaskLater(thisPlugin, new Runnable() {
                            		@Override
                            		public void run() {
                            			Location landedLocation = fallingBlock.getLocation();
                            			Block block = landedLocation.getBlock();
                            			logger.info(block.getType().toString());
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