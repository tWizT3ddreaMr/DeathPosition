package me.tWizT3d_dreaMr.DeathPosition;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class main extends JavaPlugin implements Listener {
  private File configf;

  private File locf;
  private Inventory inventory;
  
  private FileConfiguration config;
  public FileConfiguration configs;
  private static FileConfiguration locations;
  public static Plugin plugin;
  
  public static FileConfiguration getlocationsconfig() {
    return locations;
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
    locations = (FileConfiguration)new YamlConfiguration();
    try {
      this.config.load(this.configf);
      locations.load(this.locf);
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public void onEnable() {
	  plugin=this;
    if (!getDataFolder().exists())
      getDataFolder().mkdir(); 
    try {
      createFiles();
    } catch (InvalidConfigurationException e) {
      e.printStackTrace();
    } 
    getConfig().addDefault("Enable-Chests", Boolean.valueOf(false));
    getConfig().addDefault("tWiz.NumberOfDeaths", 5);
    getConfig().addDefault("SQL.host", "localhost");
    getConfig().addDefault("SQL.port", 3306);
    getConfig().addDefault("SQL.database", "DeathPositions");
    getConfig().addDefault("SQL.username", "username");
    getConfig().addDefault("SQL.password", "password");
    getConfig().addDefault("SQL.next", 1);

    getConfig().options().copyDefaults(true);
    saveConfig();
    String host=getConfig().getString("SQL.host");
	int port=getConfig().getInt("SQL.port");
	String database=getConfig().getString("SQL.database");
	String username=getConfig().getString("SQL.username");
	String password=getConfig().getString("SQL.password");
	int next=getConfig().getInt("SQL.next");
    getLogger().info("Death Position Plugin has been enabled!");
    getServer().getPluginManager().registerEvents(this, (Plugin)this);

	SQL(host, port, database, username, password, next,getConfig());
	configs=config;
  }
  
  public void onDisable() {
    getLogger().info("Death Position Plugin has been disabled!");
    try {
      getlocationsconfig().save(this.locf);
      getConfig().set("SQL.next", next);
      saveConfig();
      
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  @EventHandler
  public void onInvClick(InventoryClickEvent e) {
	  if(e.getInventory()!=inventory)
		  return;
	  ItemStack clickedItem = e.getCurrentItem();

      // verify current item is not null
      if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

      Player p = (Player) e.getWhoClicked();
      
      Inventory op=Bukkit.createInventory(null,45);
      for(ItemStack is:getInventory(clickedItem.getItemMeta().getDisplayName()))
    	  op.addItem(is);
      p.openInventory(op);
      
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
    		
    }try {
		SetInInventory(player);
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
  
  public void DeathInv(Player player) {
    Inventory inv = Bukkit.createInventory(null, 35, ChatColor.GREEN + "GUI");
    player.openInventory(inv);
  }
  
  @SuppressWarnings("deprecation")
public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	 if(!(sender instanceof Player)) return true;
    Player player = (Player)sender;
    if (command.getName().equalsIgnoreCase("dp")) {
      if (args.length == 0) {

    	  Check(player,player);
    	  return true;
      } 


      if(args.length==1) {
    	if(player.hasPermission("DeathPosition.Others")) {
			Player p=Bukkit.getPlayer(args[0]);
	    	  if(p!=null) {
	    		  player.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "DP" + ChatColor.DARK_AQUA + "]" + ChatColor.GRAY + " Death points for "+p.getName());
	    		  Check(p,player);
	    		  return true;
	    	  }
	    	  else {
	    		p=  Bukkit.getPlayer(Bukkit.getOfflinePlayer(args[0]).getUniqueId());
	    		if(p!=null) {
		    		  player.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "DP" + ChatColor.DARK_AQUA + "]" + ChatColor.GRAY + " Death points for "+p.getName());
		    		  Check(p,player);
		    		  return true;
		    	  } else {
		    		  player.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "DP" + ChatColor.DARK_AQUA + "] " + ChatColor.GRAY + "Can'f find player "+args[0]+" are they offline, and are you typing it right?");
		    		  return true;
		    	  }
	    		
	    	  }
    	}
    	
      }
      
      
      if(args.length==2&&player.hasPermission("DeathPosition.OpenInv")) {
    	  if(args[0].equalsIgnoreCase("OpenInv")) {
    		 inventory=Bukkit.createInventory(null, 9,"DP");
			 ArrayList<ItemStack> op=openGui(args[1]);
			 if(op!=null)
    		 for(ItemStack is:op) {
					 inventory.addItem(is);
				 } player.openInventory(inventory);
			
				 return true;
	
    	  }
    	  return true;
      }
      Check(player,player);
    return true;
  }
	return false;
  }
  public boolean Check(Player player,Player send) {

      if (getlocationsconfig().getString(player.getUniqueId() + "-dp1.x") == null) {
    	  send.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "DP" + ChatColor.DARK_AQUA + "]" + ChatColor.GRAY + " You have no logged death position yet");
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
      	    	send.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "DP"+ ChatColor.DARK_AQUA + "]" + ChatColor.WHITE + " Death Point "+i+" times ago");
      	    else
      	    	send.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "DP"+ ChatColor.DARK_AQUA + "]" + ChatColor.WHITE + " Death Point "+i+" time ago");
      	  send.sendMessage(ChatColor.GRAY+"X: " + Math.round(X)+", Y: " + Math.round(Y)+", Z: " + Math.round(Z));
      	    if(getlocationsconfig().getString(player.getUniqueId() + "-dp"+i+".cause")!=null) {
      	    	send.sendMessage(ChatColor.GRAY+"Cause of death "+ChatColor.AQUA+How);
      	    }
      }
      }
      return true;
    } 
  
  private Connection connection;
  private String host, database, username, password;
  private int port, next;
  private Statement statement;
  public void SQL(String host,int port, String database,String username,String password,int next,FileConfiguration fileConfiguration) {
  	this.host=host;
  	this.port=port;
  	this.database=database;
  	this.username=username;
  	this.password=password;
  	this.next=next;
  	try {
  		openConnection();
          statement = connection.createStatement();  
  	} catch (ClassNotFoundException e) {
  		// TODO Auto-generated catch block
  		e.printStackTrace();
  	} catch (SQLException e) {
  		// TODO Auto-generated catch block
  		e.printStackTrace();
  	}
  	try {
  		statement.execute("CREATE TABLE IF NOT EXISTS DeathPositionPlayers (PlayerUUID varchar(200),PlayerName varchar(200), InventoryID int)");
  		statement.execute("CREATE TABLE IF NOT EXISTS DeathPositionInventoryItems (InventoryID int, Item varchar(6000))");
  	} catch (SQLException e) {
  		// TODO Auto-generated catch block
  		e.printStackTrace();
  	}
  }
  public void openConnection() throws SQLException, ClassNotFoundException {
      if (connection != null && !connection.isClosed()) {
          return;
      }
   
      synchronized (this) {
          if (connection != null && !connection.isClosed()) {
              return;
          }
          Class.forName("com.mysql.jdbc.Driver");
          connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password);
      }
  }
  public void SetInInventory(Player p) throws SQLException {
  	String UUID=p.getUniqueId().toString();
  	String name=p.getName();
  	ArrayList<Integer> InvIds = new ArrayList<Integer>();
  	ArrayList<String> InvStrings = new ArrayList<String>();
  	for(ItemStack items:p.getInventory().getContents()) {
  		if(items!=null) InvStrings.add(ItemDecoder.serializeItemStack(items));
  	}
  	ResultSet result = statement.executeQuery("SELECT * FROM DeathPositionPlayers WHERE PlayerUUID = '"+UUID+"';");
  	while (result.next()) {	    
  	    InvIds.add(result.getInt("InventoryID"));
  	}
  	Collections.sort(InvIds, Collections.reverseOrder());
  	int lesthan=0;int i=0;
		for(int in:InvIds) {
		if(i<5)
			i++;
		else if(i==5) lesthan=in;
		}
		int use=lesthan;
  	BukkitRunnable r=null;
  	r = new BukkitRunnable() {
  	    @Override
  	    public void run() {
  	        //This is where you should do your database interaction
  	try {
		
		
	  		statement.executeUpdate("DELETE FROM DeathPositionPlayers WHERE PlayerUUID ='"+UUID+"' and InventoryID <"+(next-getConfig().getInt("tWiz.NumberOfDeaths"))+";");
	  		
	  		statement.executeUpdate("DELETE FROM DeathPositionInventoryItems WHERE InventoryID ="+use+";");
	  		
  		statement.executeUpdate("INSERT INTO DeathPositionPlayers (PlayerUUID, PlayerName, InventoryID) VALUES ('"+UUID+"','"+name+"',"+next+");");
  		

  		for(String s:InvStrings) {
  			statement.executeUpdate("INSERT INTO DeathPositionInventoryItems (InventoryID, Item) VALUES ("+next+",'"+s+"');");
  		}
  	} catch (SQLException e) {
  		// TODO Auto-generated catch block
  		e.printStackTrace();
  	}
  	
  	}
  	};
  	r.runTaskAsynchronously(this);
  	

  	next++;
  }
  public ArrayList<ItemStack> openGui(String name){
  	ArrayList<ItemStack>Inventory= new ArrayList<ItemStack>();

  	ArrayList<Integer>IDS= new ArrayList<Integer>();
  	
  	ResultSet result;
  		try {
			result = statement.executeQuery("SELECT * FROM DeathPositionPlayers WHERE PlayerName = '"+name+"';");

  		while(result.next()) {
  			IDS.add(result.getInt("InventoryID"));
  		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



  	for(int s:IDS) {
  		ItemStack i=new ItemStack(Material.COMMAND_BLOCK,1);
  		ItemMeta met=i.getItemMeta();
  		met.setDisplayName(""+s);
  		i.setItemMeta(met);
  		Inventory.add(i);
  	}
  	
  	if(Inventory.isEmpty()) return null;
  	return Inventory;
  }
  public ArrayList<ItemStack> getInventory(String InventoryID) {
  	ArrayList<ItemStack>Inventory= new ArrayList<ItemStack>();
  	ArrayList<String>InventoryStrings= new ArrayList<String>();
  	try {
  		  ResultSet result = statement.executeQuery("SELECT * FROM DeathPositionInventoryItems WHERE InventoryID = "+InventoryID+";");
  		    	while (result.next()) {
  		    	    String s= result.getString("Item");
  		    	    InventoryStrings.add(s);
  		    	}
  	    	} catch (SQLException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  	}
  	    
  	
  	for(String st:InventoryStrings) {
  		if(st!=null&&!st.isEmpty()) {
  			Inventory.add(ItemDecoder.deserializeItemStack(st));
  		}else Inventory.add(null);
  	}
  	return Inventory;
  }
  }
