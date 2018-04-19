package red.man10.man10pvpscore;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Man10PVPscore extends JavaPlugin implements Listener {
    VaultManager val = null;
    public MySOLManager mysql;
    Playercontain pc;
    Pointget pg;
    Pointset ps;
    String prefix = "§c[§4Man10§cPVP§eScore§c]";
    public FileConfiguration config1;
    List<String> worlds ;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    getServer().getPluginManager().disablePlugin(this);
                    getServer().getPluginManager().enablePlugin(this);
                    getLogger().info(ChatColor.GREEN + "設定を再読み込みしました。");
                    return true;
                }
                getLogger().info(ChatColor.RED + "mps reload");
                return true;
            }
        }
        Player p = (Player) sender;
        if(args.length == 0) {
            p.sendMessage(prefix+"§aあなたのPvPScore: §e"+pg.run(p)+"p");
            p.sendMessage(prefix+"§aあなたのPvPrank: §r"+getRank(pg.run(p)));
            return true;
        }else if(args.length == 1) {
            if(args[0].equalsIgnoreCase("ranking")) {
                Pointranking pr = new Pointranking();
                String[] get = pr.run(p);
                int ranking = 0;
                if(get == null){
                    p.sendMessage(prefix+"§cランキングが存在しません");
                    return true;
                }
                for(String key:get){
                    ranking++;
                    String name = null;
                    int point = 0;
                    if(key == null){
                        break;
                    }
                    if(Bukkit.getPlayer(UUID.fromString(key))!=null){
                       name = Bukkit.getPlayer(UUID.fromString(key)).getName();
                       point = pg.run(Bukkit.getPlayer(UUID.fromString(key)));
                    }else{
                        name = Bukkit.getOfflinePlayer(UUID.fromString(key)).getName();
                        point = pg.run(Bukkit.getOfflinePlayer(UUID.fromString(key)));
                    }
                   p.sendMessage(prefix+"§e"+ranking+"位: §6"+name+" §e"+point+"p");
                }
                return true;
            }else if(args[0].equalsIgnoreCase("getitem")) {
                if(pg.run(p)<5) {
                    p.sendMessage(prefix + "§cあなたにはpointが足りません！");
                    return true;
                }
                ps.run(p,pg.run(p)-5);
                ItemStack item = new ItemStack(Material.INK_SACK,1,(short)1);
                ItemMeta itemmeta = item.getItemMeta();
                itemmeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
                itemmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                itemmeta.setDisplayName("§4§lKiller's Ruby§r");
                List<String> kk = new ArrayList<String>();
                kk.add("§c殺人者のルビー。");
                kk.add("§c特別なアイテムと交換できる");
                itemmeta.setLore(kk);
                item.setItemMeta(itemmeta);
                p.getInventory().addItem(item);
                p.sendMessage(prefix + "§a5pとアイテムを交換しました");
                return true;
            }else if(args[0].equalsIgnoreCase("help")) {
                p.sendMessage(prefix + "§e/mps ranking : ランキングを表示");
                p.sendMessage(prefix + "§e/mps : スコアを表示");
                p.sendMessage(prefix + "§e/mps getitem : アイテムをゲット");
                p.sendMessage(prefix + "§e/mps arena join [arena名] : arenaにjoin");
                p.sendMessage(prefix + "§e/mps arena list : arenalistを表示");
                return true;
            }else if(args[0].equalsIgnoreCase("adminhelp")) {
                if (!p.hasPermission("man10pvpscore.adminhelp")) {
                    p.sendMessage(prefix + "§cあなたにはadmin用helpを見る権限がありません！");
                    return true;
                }
                p.sendMessage(prefix + "§c=================Admin用 HELP=================");
                p.sendMessage(prefix + "§e/mps set [user名] [設定ポイント] : pointをset");
                p.sendMessage(prefix + "§e/mps arena create [arena名] : arenaを作成");
                p.sendMessage(prefix + "§e/mps arena remove [arena名] : arenaを削除");
                p.sendMessage(prefix + "§e/mps arena setloc [arena名] : arenaのlocationを再設定");
                p.sendMessage(prefix + "§e/mps arena addgroup [arena名] [group名] : arenaにgroupを追加");
                p.sendMessage(prefix + "§e/mps arena removegroup [arena名] [group名]: arenaからgroupを除外");
                p.sendMessage(prefix + "§eグループ名一覧: Baby,Kid,Player,Good_Player,Killer,Crazy_Killer,God_Killer");
                p.sendMessage(prefix + "§c=================Admin用 HELP=================");
                return true;
            }
        }else if(args.length == 2) {
            if(args[0].equalsIgnoreCase("arena")) {
                if(args[1].equalsIgnoreCase("list")) {
                    if(!config1.contains("arenas")){
                        p.sendMessage(prefix + "§cまだarenaが存在しません！");
                        return true;
                    }
                    for (String key : config1.getConfigurationSection("arenas").getKeys(false)) {
                        p.sendMessage(prefix + "§e"+key);
                    }
                    return true;
                }
            }
        }else if(args.length == 3) {
            if(args[0].equalsIgnoreCase("set")) {
                if(!p.hasPermission("man10pvpscore.set")){
                    p.sendMessage(prefix + "§cあなたにはpointをsetする権限がありません！");
                    return true;
                }
                int get = 0;
                try {
                    get = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    p.sendMessage(prefix + "§4数字で指定してください。");
                    return true;
                }
                if(Bukkit.getPlayer(args[1]) != null){
                    if (!pc.run(Bukkit.getPlayer(args[1]))) {
                        p.sendMessage(prefix + "§4そのプレイヤーは存在しません");
                        return true;
                    }
                    ps.run(Bukkit.getPlayer(args[1]),get);
                    p.sendMessage(prefix + "§e"+Bukkit.getPlayer(args[1]).getName()+"§aのポイントを§e"+get+"p§aにsetしました");
                    return true;
                }else{
                    if (!pc.run(Bukkit.getOfflinePlayer(args[1]))) {
                        p.sendMessage(prefix + "§4そのプレイヤーは存在しません");
                        return true;
                    }
                    ps.run(Bukkit.getOfflinePlayer(args[1]),get);
                    p.sendMessage(prefix + "§e"+Bukkit.getPlayer(args[1]).getName()+"§aのポイントを§e"+get+"p§aにsetしました");
                    return true;
                }
            }else if(args[0].equalsIgnoreCase("arena")) {
               if(args[1].equalsIgnoreCase("create")) {
                   if (!p.hasPermission("man10pvpscore.arena.create")) {
                       p.sendMessage(prefix + "§cあなたにはarenaを作成する権限がありません！");
                       return true;
                   }
                   String arenaname = args[2];
                   config1.set("arenas."+arenaname+".location",p.getLocation());
                   config1.set("arenas."+arenaname+".joingroup.§eBaby","true");
                   config1.set("arenas."+arenaname+".joingroup.§6Kid","true");
                   config1.set("arenas."+arenaname+".joingroup.§aPlayer","false");
                   config1.set("arenas."+arenaname+".joingroup.§cGood_Player","false");
                   config1.set("arenas."+arenaname+".joingroup.§4Killer","false");
                   config1.set("arenas."+arenaname+".joingroup.§4§lCrazy_Killer","false");
                   config1.set("arenas."+arenaname+".joingroup.§4§l§oGod_Killer","false");
                   saveConfig();
                   p.sendMessage(prefix + "§e"+arenaname+"§aArenaを作成しました。");
                   return true;
               }else if(args[1].equalsIgnoreCase("join")) {
                   String arenaname = args[2];
                   if(!config1.contains("arenas."+arenaname)){
                       p.sendMessage(prefix + "§cそのarenaは存在しません！");
                       return true;
                   }
                   if(config1.getString("arenas."+arenaname+".joingroup."+getRank(pg.run(p))).equalsIgnoreCase("true")){
                       Location loc = (Location) config1.get("arenas."+arenaname+".location");
                       p.teleport(loc);
                       p.sendMessage(prefix + "§e"+arenaname+"§aArenaにjoinしました。");
                       return true;
                   }else {
                       p.sendMessage(prefix + "§cあなたのランクが登録されていないためこのArenaにjoinできません！");
                       return true;
                   }
               }else if(args[1].equalsIgnoreCase("setloc")) {
                   if (!p.hasPermission("man10pvpscore.arena.setloc")) {
                       p.sendMessage(prefix + "§cあなたにはarenaのlocationを再設定する権限がありません！");
                       return true;
                   }
                   String arenaname = args[2];
                   if(!config1.contains("arenas."+arenaname)){
                       p.sendMessage(prefix + "§cそのarenaは存在しません！");
                       return true;
                   }
                   config1.set("arenas."+arenaname+".location",p.getLocation());
                   saveConfig();
                   p.sendMessage(prefix + "§e"+arenaname+"§aArenaのロケーションを再設定しました。");
                   return true;
               }else if(args[1].equalsIgnoreCase("remove")) {
                   if (!p.hasPermission("man10pvpscore.arena.remove")) {
                       p.sendMessage(prefix + "§cあなたにはarenaを削除する権限がありません！");
                       return true;
                   }
                   String arenaname = args[2];
                   if(!config1.contains("arenas."+arenaname)){
                       p.sendMessage(prefix + "§cそのarenaは存在しません！");
                       return true;
                   }
                   config1.set("arenas."+arenaname,null);
                   saveConfig();
                   p.sendMessage(prefix + "§e"+arenaname+"§aArenaを削除しました。");
                   return true;
               }
            }else if(args[0].equalsIgnoreCase("pay")) {
                String payname = args[1];
                if(Bukkit.getPlayer(payname)==null){
                    p.sendMessage(prefix+"§4そのプレイヤーは現在オフラインです！");
                    return true;
                }
                Player payplayer = Bukkit.getPlayer(payname);
                int pay = 0;
                try {
                    pay = Integer.valueOf(args[2]);
                }catch(NumberFormatException e){
                    p.sendMessage(prefix+"§4ポイントは数字でお書きください！");
                    return true;
                }
                int ppoint = pg.run(p) - pay;
                ps.run(p,ppoint);
                int paypoint = pg.run(payplayer) + pay;
                ps.run(payplayer,paypoint);
                p.sendMessage(prefix+"§a"+payname+"に§e"+pay+"p§a送信しました。");
                payplayer.sendMessage(prefix+"§a"+payname+"から§e"+pay+"p§a受け取りました。");
                return true;
            }
        }else if(args.length == 4) {
            if(args[0].equalsIgnoreCase("arena")) {
                if(args[1].equalsIgnoreCase("addgroup")) {
                    if (!p.hasPermission("man10pvpscore.arena.addgroup")) {
                        p.sendMessage(prefix + "§cあなたにはarenaのグループを追加する権限がありません！");
                        return true;
                    }
                    String arenaname = args[2];
                    if(!config1.contains("arenas."+arenaname)){
                        p.sendMessage(prefix + "§cそのarenaは存在しません！");
                        return true;
                    }
                    String groupname = args[3];
                    if(groupname.equalsIgnoreCase("Baby")){
                        groupname = "§eBaby";
                    }else if(groupname.equalsIgnoreCase("Kid")){
                        groupname = "§6Kid";
                    }else if(groupname.equalsIgnoreCase("Player")){
                        groupname = "§aPlayer";
                    }else if(groupname.equalsIgnoreCase("Good_Player")){
                        groupname = "§cGood_Player";
                    }else if(groupname.equalsIgnoreCase("Killer")){
                        groupname = "§4Killer";
                    }else if(groupname.equalsIgnoreCase("Crazy_Killer")){
                        groupname = "§4§lCrazy_Killer";
                    }else if(groupname.equalsIgnoreCase("God_Killer")){
                        groupname = "§4§l§oGod_Killer";
                    }else {
                        p.sendMessage(prefix + "§cそのグループは存在しません！");
                        return true;
                    }
                    config1.set("arenas."+arenaname+".joingroup."+groupname,"true");
                    saveConfig();
                    p.sendMessage(prefix + "§e"+arenaname+"§aArenaに"+groupname+"§aを追加しました。");
                    return true;
                }else if(args[1].equalsIgnoreCase("removegroup")) {
                    if (!p.hasPermission("man10pvpscore.arena.removegroup")) {
                        p.sendMessage(prefix + "§cあなたにはarenaのグループを除外する権限がありません！");
                        return true;
                    }
                    String arenaname = args[2];
                    if(!config1.contains("arenas."+arenaname)){
                        p.sendMessage(prefix + "§cそのarenaは存在しません！");
                        return true;
                    }
                    String groupname = args[3];
                    if(groupname.equalsIgnoreCase("Baby")){
                        groupname = "§eBaby";
                    }else if(groupname.equalsIgnoreCase("Kid")){
                        groupname = "§6Kid";
                    }else if(groupname.equalsIgnoreCase("Player")){
                        groupname = "§aPlayer";
                    }else if(groupname.equalsIgnoreCase("Good_Player")){
                        groupname = "§cGood_Player";
                    }else if(groupname.equalsIgnoreCase("Killer")){
                        groupname = "§4Killer";
                    }else if(groupname.equalsIgnoreCase("Crazy_Killer")){
                        groupname = "§4§lCrazy_Killer";
                    }else if(groupname.equalsIgnoreCase("God_Killer")){
                        groupname = "§4§l§oGod_Killer";
                    }else {
                        p.sendMessage(prefix + "§cそのグループは存在しません！");
                        return true;
                    }
                    config1.set("arenas."+arenaname+".joingroup."+groupname,"false");
                    saveConfig();
                    p.sendMessage(prefix + "§e"+arenaname+"§cArenaに"+groupname+"§cを除外しました。");
                    return true;
                }
            }
        }
        p.sendMessage(prefix+"§fUnknown command. Type \"/mps help\" for help.");
        return true;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        val = new VaultManager(this);
        mysql = new MySOLManager(this, "MPS");
        pc = new Playercontain();
        pg = new Pointget();
        ps = new Pointset();
        getCommand("mps").setExecutor(this);
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        config1 = config;
        worlds = config1.getStringList("pvpworlds");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if(!pc.run(p)) {
            Playeradd ma = new Playeradd();
            ma.run(p);
        }
    }
    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        if(!worlds.contains(event.getEntity().getWorld().getName())){
            return;
        }
        Player korosita = event.getEntity().getKiller();
        Player korosareta = event.getEntity();
        if(korosita != null){
            int korositapoint = pg.run(korosita);
            int korosaretapoint = pg.run(korosareta);
            if(korosaretapoint == 0){
                korosita.sendMessage(prefix+"§aあなたは"+korosareta.getName()+"をkillしたが相手は0pだった");
                korosareta.sendMessage(prefix+"§cあなたは"+korosita.getName()+"にkillされたがあなたは0pだった");
                return;
            }
            if(korositapoint == 0){
                korosita.sendMessage(prefix+"§cあなたは"+korosareta.getName()+"をkillしたがあなたは0pだった");
                korosareta.sendMessage(prefix+"§aあなたは"+korosita.getName()+"にkillされたが相手は0pだった");
                return;
            }
            int getpoint = 0;
            if(korosaretapoint%2 == 0) {
                getpoint = korosaretapoint / 2;
            }else {
                getpoint =  ((int) (korosaretapoint / 2))+1;
            }
            ps.run(korosita,korositapoint+getpoint);
            ps.run(korosareta,korosaretapoint - getpoint);
            korosita.sendMessage(prefix+"§aあなたは"+korosareta.getName()+"をkillし"+getpoint+"pを手に入れた");
            korosareta.sendMessage(prefix+"§cあなたは"+korosita.getName()+"にkillされ"+getpoint+"pを失った");
        }
    }
    public String getRank(int point){
        if(point >= 0&&point < 10){
            return "§eBaby";
        }else if(point >= 10&&point < 50){
            return "§6Kid";
        }else if(point >= 50&&point < 100){
            return "§aPlayer";
        }else if(point >= 100&&point < 1000){
            return "§cGood_Player";
        }else if(point >= 1000&&point <10000){
            return "§4Killer";
        }else if(point >= 10000&&point <100000){
            return "§4§lCrazy_Killer";
        }else if(point >= 100000){
            return "§4§l§oGod_Killer";
        }else {
            return null;
        }
    }
    public class Playercontain extends Thread {
        public boolean run(Player p) {
            String sql = "SELECT * FROM playerdata WHERE uuid = '"+p.getUniqueId().toString()+"';";
            ResultSet rs = mysql.query(sql);
            try {
                if(rs.next()) {
                    // UUIDが一致するユーザが見つかった
                    return true;
                }
                return false;
            } catch (SQLException e1) {
                e1.printStackTrace();
                return false;
            } catch (NullPointerException e1) {
                e1.printStackTrace();
                return false;
            }
        }
        public boolean run(OfflinePlayer p) {
            String sql = "SELECT * FROM playerdata WHERE uuid = '"+p.getUniqueId().toString()+"';";
            ResultSet rs = mysql.query(sql);
            try {
                if(rs.next()) {
                    // UUIDが一致するユーザが見つかった
                    return true;
                }
                return false;
            } catch (SQLException e1) {
                e1.printStackTrace();
                return false;
            } catch (NullPointerException e1) {
                e1.printStackTrace();
                return false;
            }
        }
    }
    public class Playeradd extends Thread {
        public boolean run(Player p) {
            String sql = "INSERT INTO "+mysql.DB+".playerdata (point, uuid) VALUES (100 ,'"+p.getUniqueId().toString()+"' );";
            mysql.execute(sql);
            return true;
        }
    }
    public class Pointset extends Thread {
        public boolean run(Player p,int point) {
            String sql = "UPDATE "+mysql.DB+".playerdata set point = "+point+" where uuid = '"+p.getUniqueId().toString()+"';";
            mysql.execute(sql);
            return true;
        }
        public boolean run(OfflinePlayer p,int point) {
            String sql = "UPDATE "+mysql.DB+".playerdata set point = "+point+" where uuid = '"+p.getUniqueId().toString()+"';";
            mysql.execute(sql);
            return true;
        }
    }
    public class Pointget extends Thread {
        public int run(Player p) {

            String sql = "SELECT * FROM "+mysql.DB+".playerdata WHERE uuid = '"+p.getUniqueId().toString()+"';";
            ResultSet rs = mysql.query(sql);
            try {
                if(rs.next()) {
                    int get = rs.getInt("point");
                    return get;
                }
                return 0;
            } catch (SQLException e1) {
                e1.printStackTrace();
                return 0;
            } catch (NullPointerException e1) {
                e1.printStackTrace();
                return 0;
            }
        }
        public int run(OfflinePlayer p) {

            String sql = "SELECT * FROM "+mysql.DB+".playerdata WHERE uuid = '"+p.getUniqueId().toString()+"';";
            ResultSet rs = mysql.query(sql);
            try {
                if(rs.next()) {
                    int get = rs.getInt("point");
                    return get;
                }
                return 0;
            } catch (SQLException e1) {
                e1.printStackTrace();
                return 0;
            } catch (NullPointerException e1) {
                e1.printStackTrace();
                return 0;
            }
        }
    }
    public class Pointranking extends Thread {
        public String[] run(Player p) {

            String sql = "SELECT * FROM "+mysql.DB+".playerdata ORDER BY point DESC;";
            ResultSet rs = mysql.query(sql);
            String[] strings = new String[9];
            try {
               for(int i = 0; i <= 9; i++) {
                   if (rs.next()) {
                       String get = rs.getString("uuid");
                       strings[i] = get;
                   }else{
                       return strings;
                   }
               }
                return strings;
            } catch (SQLException e1) {
                e1.printStackTrace();
                return null;
            } catch (NullPointerException e1) {
                e1.printStackTrace();
                return null;
            }
        }
    }

}
