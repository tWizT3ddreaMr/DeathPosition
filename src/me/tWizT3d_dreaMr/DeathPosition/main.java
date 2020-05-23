package me.tWizT3d_dreaMr.DeathPosition;
import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin implements Listener {
  private File configf;
  
  private File locf;
  
  private FileConfiguration config;
  
  private FileConfiguration locations;
  
  public FileConfiguration getlocationsconfig() {
    return this.locations;
  }
  
  private void createFiles() throws InvalidConfigurationException {
    this.configf = new File(getDataFolder(), "config.yml");
    this.locf = new File(getDataFolder(), "locations.yml");
    if (!this.configf.exists()) {
      this.configf.getParentFile().mkdirs();
      saveResource("config.yml", false);
    } 
    if (!this.locf.exists()) {
      this.locf.getParentFile().mkdirs();
      saveResource("locations.yml", false);
    } 
    this.config = (FileConfiguration)new YamlConfiguration();
    this.locations = (FileConfiguration)new YamlConfiguration();
    try {
      this.config.load(this.configf);
      this.locations.load(this.locf);
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public void onEnable() {
    if (!getDataFolder().exists())
      getDataFolder().mkdir(); 
    try {
      createFiles();
    } catch (InvalidConfigurationException e) {
      e.printStackTrace();
    } 
    getConfig().addDefault("Enable-Chests", Boolean.valueOf(false));
    getConfig().addDefault("tWiz.NumberOfDeaths", 5);
    getConfig().options().copyDefaults(true);
    saveConfig();
    getLogger().info("Death Position Plugin has been enabled!");
    getServer().getPluginManager().registerEvents(this, (Plugin)this);
  }
  
  public void onDisable() {
    getLogger().info("Death Position Plugin has been disabled!");
    try {
      getlocationsconfig().save(this.locf);
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onPlayerDeathLowest(PlayerDeathEvent event) {
	    String deathMessage = event.getDeathMessage();
	    if (deathMessage == null)
	      return; 
    Player player = (Player) event.getEntity();

    int i2=getConfig().getInt("tWiz.NumberOfDeaths");
    for(int i=i2; i>=1;i--) {

    	if(i==1) {
    	    getlocationsconfig().set(player.getUniqueId() + "-dp1.x", player.getLocation().getX());
    	    getlocationsconfig().set(player.getUniqueId() + "-dp1.y", player.getLocation().getY());
    	    getlocationsconfig().set(player.getUniqueId() + "-dp1.z", player.getLocation().getZ());
    	    getlocationsconfig().set(player.getUniqueId() + "-dp1.cause",player.getLastDamageCause().getCause().toString());
    	}
    	else if(getlocationsconfig().get((player.getUniqueId()) + "-dp"+(i-1)+".x")!=null) {
    		getlocationsconfig().set(player.getUniqueId() + "-dp"+i+".x",getlocationsconfig().get((player.getUniqueId()) + "-dp"+(i-1)+".x"));
    	    getlocationsconfig().set(player.getUniqueId() + "-dp"+i+".y",getlocationsconfig().get((player.getUniqueId()) + "-dp"+(i-1)+".y"));
    	    getlocationsconfig().set(player.getUniqueId() + "-dp"+i+".z",getlocationsconfig().get((player.getUniqueId()) + "-dp"+(i-1)+".z"));
    	    if(getlocationsconfig().getString(player.getUniqueId() + "-dp"+(i-1)+".cause")!=null) {
    	    	getlocationsconfig().set(player.getUniqueId() + "-dp"+i+".cause",getlocationsconfig().get((player.getUniqueId()) + "-dp"+(i-1)+".cause"));
    	    }
    	}
    		
    }
    }
  
  public void DeathInv(Player player) {
    Inventory inv = Bukkit.createInventory(null, 35, ChatColor.GREEN + "GUI");
    player.openInventory(inv);
  }
  
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    Player player = (Player)sender;
    if (command.getName().equalsIgnoreCase("dp")) {
      if (args.length == 0) {
        if (getlocationsconfig().getString(player.getUniqueId() + "-dp1.x") == null) {
          player.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "DP" + ChatColor.DARK_AQUA + "]" + ChatColor.GRAY + " You have no logged death position yet");
          return true;
        } 
        double X =0;
        double Y =0;
        double Z =0;
        String How="";
        
        int i2=getConfig().getInt("tWiz.NumberOfDeaths");

        for(int i=1; i<=i2;i++) {
        	if(getlocationsconfig().getString(player.getUniqueId() + "-dp"+i+".x") != null) {
        	    X=getlocationsconfig().getDouble(player.getUniqueId() + "-dp"+i+".x");
        	    Y=getlocationsconfig().getDouble(player.getUniqueId() + "-dp"+i+".y");
        	    Z=getlocationsconfig().getDouble(player.getUniqueId() + "-dp"+i+".z");

        	    How=getlocationsconfig().getString(player.getUniqueId() + "-dp"+i+".cause");
        	    if(i>1)
        	    	player.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "DP"+ ChatColor.DARK_AQUA + "]" + ChatColor.WHITE + " Death Point "+i+" times ago");
        	    else
            	    player.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "DP"+ ChatColor.DARK_AQUA + "]" + ChatColor.WHITE + " Death Point "+i+" time ago");
        	    player.sendMessage(ChatColor.GRAY+"X: " + Math.round(X)+", Y: " + Math.round(Y)+", Z: " + Math.round(Z));
        	    if(getlocationsconfig().getString(player.getUniqueId() + "-dp"+i+".cause")!=null) {
        	    	player.sendMessage(ChatColor.GRAY+"Cause of death "+ChatColor.AQUA+How);
        	    }
        }
        }
        return true;
      } 
    return true;
  }
	return false;
  }
  }
