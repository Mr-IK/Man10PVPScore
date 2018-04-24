package red.man10.man10pvpscore;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class Man10PVPscore extends JavaPlugin implements Listener {
    VaultManager val = null;
    public MySOLManager mysql;
    CustomConfig cc;
    Playercontain pc;
    Pointget pg;
    Pointset ps;
    Kill_add ka;
    Kill_get kg;
    Death_add da;
    Death_get dg;
    Kill_Stroke_get ksg;
    Kill_Stroke_set kss;
    String prefix = "§c[§4Man10§cPVP§eScore§c]";
    public FileConfiguration config1;
    public FileConfiguration config2;
    List<String> worlds ;
    private HashMap<UUID,String> playerState;
    private HashMap<UUID,UUID> renzokukill;
    private HashMap<UUID,ItemStack[]> kitpvp;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    getServer().getPluginManager().disablePlugin(this);
                    getServer().getPluginManager().enablePlugin(this);
                    getLogger().info("設定を再読み込みしました。");
                    return true;
                }
                getLogger().info("mps reload");
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
                Pointtotal pt = new Pointtotal();
                int pointtotal = pt.run(p);
                p.sendMessage(prefix+"§a鯖内合計流通point: §e"+pointtotal+"p");
                for(String key:get){
                    ranking++;
                    String name = null;
                    int point = 0;
                    int kill = 0;
                    int death = 0;
                    if(key == null){
                        break;
                    }
                    if(Bukkit.getPlayer(UUID.fromString(key))!=null){
                       name = Bukkit.getPlayer(UUID.fromString(key)).getName();
                       point = pg.run(Bukkit.getPlayer(UUID.fromString(key)));
                       kill = kg.run(Bukkit.getPlayer(UUID.fromString(key)));
                       death = dg.run(Bukkit.getPlayer(UUID.fromString(key)));
                    }else{
                        name = Bukkit.getOfflinePlayer(UUID.fromString(key)).getName();
                        point = pg.run(Bukkit.getOfflinePlayer(UUID.fromString(key)));
                        kill = kg.run(Bukkit.getOfflinePlayer(UUID.fromString(key)));
                        death = dg.run(Bukkit.getOfflinePlayer(UUID.fromString(key)));
                    }
                   p.sendMessage(prefix+"§e"+ranking+"位: §6"+name+" §e"+point+"p §a"+kill+"§f/§c"+death);
                }
                return true;
            }else if(args[0].equalsIgnoreCase("help")) {
                p.sendMessage(prefix + "§a=================User用 HELP=================");
                p.sendMessage(prefix + "§e/mps ranking : ランキングを表示");
                p.sendMessage(prefix + "§e/mps leave : kitpvp中にpvpをやめる");
                p.sendMessage(prefix + "§e/mps : スコアを表示");
                p.sendMessage(prefix + "§e/mps user [user名] : user情報を確認");
                p.sendMessage(prefix + "§e/mps pay [プレイヤー名] [送りたいpoint] : point送信");
                p.sendMessage(prefix + "§e/mps arena join [arena名] : arenaにjoin");
                p.sendMessage(prefix + "§e/mps arena list : arenalistを表示");
                p.sendMessage(prefix + "§a=================User用 HELP=================");
                return true;
            }else if(args[0].equalsIgnoreCase("adminhelp")) {
                if (!p.hasPermission("man10pvpscore.adminhelp")) {
                    p.sendMessage(prefix + "§cあなたにはadmin用helpを見る権限がありません！");
                    return true;
                }
                p.sendMessage(prefix + "§c=================Admin用 HELP=================");
                p.sendMessage(prefix + "§e/mps reload : configをreload");
                p.sendMessage(prefix + "§e/mps setmainspawn : メインスポーンを設定");
                p.sendMessage(prefix + "§e/mps arena create [arena名] : arenaを作成");
                p.sendMessage(prefix + "§e/mps arena remove [arena名] : arenaを削除");
                p.sendMessage(prefix + "§e/mps arena setloc [arena名] : arenaのlocationを再設定");
                p.sendMessage(prefix + "§e/mps arena addgroup [arena名] [group名] : arenaにgroupを追加");
                p.sendMessage(prefix + "§e/mps arena removegroup [arena名] [group名]: arenaからgroupを除外");
                p.sendMessage(prefix + "§e/mps arena pexmode [arena名] true/false : 権限モードのon/off");
                p.sendMessage(prefix + "§e/mps arena setkit [arena名] [キット名] : arenaのkitを設定。'none'でなし");
                p.sendMessage(prefix + "§e/mps kit create [kit名] : kitを作成");
                p.sendMessage(prefix + "§e/mps kit update [kit名] : kitを更新");
                p.sendMessage(prefix + "§e/mps kit remove [kit名] : kitを削除");
                p.sendMessage(prefix + "§eグループ名一覧: Baby,Kid,Player,Good_Player,Killer,Crazy_Killer,God_Killer");
                p.sendMessage(prefix + "§c=================Admin用 HELP=================");
                return true;
            }else if(args[0].equalsIgnoreCase("reload")) {
                if (!p.hasPermission("man10pvpscore.reload")) {
                    p.sendMessage(prefix + "§cあなたにはreloadを行う権限がありません！");
                    return true;
                }
                getServer().getPluginManager().disablePlugin(this);
                getServer().getPluginManager().enablePlugin(this);
                p.sendMessage(prefix+"§a設定を再読み込みしました。");
                return true;
            }else if(args[0].equalsIgnoreCase("leave")) {
                if(kitpvp.containsKey(p.getUniqueId())){
                    ItemStack[] inv = kitpvp.get(p.getUniqueId());
                    p.getInventory().clear();
                    p.getInventory().setContents(inv);
                    p.updateInventory();
                    kitpvp.remove(p.getUniqueId());
                }
                if(config1.contains("mainspawn")) {
                    Location loc = (Location) config1.get("mainspawn");
                    p.teleport(loc);
                }else {
                    p.teleport(p.getBedSpawnLocation());
                }
                p.sendMessage(prefix+"§apvpからleaveしました。");
                return true;
            }else if(args[0].equalsIgnoreCase("setmainspawn")){
                if (!p.hasPermission("man10pvpscore.setmainspawn")) {
                    p.sendMessage(prefix + "§cあなたにはmainspawnを設定する権限がありません！");
                    return true;
                }
                config1.set("mainspawn",p.getLocation());
                cc.saveConfig();
                p.sendMessage(prefix+"§a設定しました。");
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
            }else if(args[0].equalsIgnoreCase("user")) {
                String username = args[1];
                if(Bukkit.getPlayer(username) != null){
                    if (!pc.run(Bukkit.getPlayer(args[1]))) {
                        p.sendMessage(prefix + "§4そのプレイヤーは存在しません");
                        return true;
                    }
                    p.sendMessage(prefix+"§a"+username+"のPvPScore: §e"+pg.run(Bukkit.getPlayer(args[1]))+"p");
                    p.sendMessage(prefix+"§a"+username+"のPvPrank: §r"+getRank(pg.run(Bukkit.getPlayer(args[1]))));
                    return true;
                }else{
                    if (!pc.run(Bukkit.getOfflinePlayer(username))) {
                        p.sendMessage(prefix + "§4そのプレイヤーは存在しません");
                        return true;
                    }
                    p.sendMessage(prefix+"§a"+username+"のPvPScore: §e"+pg.run(Bukkit.getOfflinePlayer(username))+"p");
                    p.sendMessage(prefix+"§a"+username+"のPvPrank: §r"+getRank(pg.run(Bukkit.getOfflinePlayer(username))));
                    return true;
                }
            }else if(args[0].equalsIgnoreCase("kit")) {
                if(args[1].equalsIgnoreCase("list")) {
                    if (!p.hasPermission("man10pvpscore.kit.list")) {
                        p.sendMessage(prefix + "§cあなたにはkitのlistを表示する権限がありません！");
                        return true;
                    }
                    if(!config1.contains("kit")){
                        p.sendMessage(prefix + "§ckitが一つも存在しません");
                        return true;
                    }
                    for (String key : config1.getConfigurationSection("kit").getKeys(false)) {
                        p.sendMessage(prefix+key);
                    }
                    return true;
                }
            }
        }else if(args.length == 3) {
            if(args[0].equalsIgnoreCase("arena")) {
               if(args[1].equalsIgnoreCase("create")) {
                   if (!p.hasPermission("man10pvpscore.arena.create")) {
                       p.sendMessage(prefix + "§cあなたにはarenaを作成する権限がありません！");
                       return true;
                   }
                   String arenaname = args[2];
                   config1.set("arenas."+arenaname+".location",p.getLocation());
                   config1.set("arenas."+arenaname+".pexmode","false");
                   config1.set("arenas."+arenaname+".kit","none");
                   config1.set("arenas."+arenaname+".joingroup.§eBaby","true");
                   config1.set("arenas."+arenaname+".joingroup.§6Kid","true");
                   config1.set("arenas."+arenaname+".joingroup.§aPlayer","false");
                   config1.set("arenas."+arenaname+".joingroup.§cGood_Player","false");
                   config1.set("arenas."+arenaname+".joingroup.§4Killer","false");
                   config1.set("arenas."+arenaname+".joingroup.§4§lCrazy_Killer","false");
                   config1.set("arenas."+arenaname+".joingroup.§4§l§oGod_Killer","false");
                   cc.saveConfig();
                   p.sendMessage(prefix + "§e"+arenaname+"§aArenaを作成しました。");
                   return true;
               }else if(args[1].equalsIgnoreCase("join")) {
                   String arenaname = args[2];
                   if(!config1.contains("arenas."+arenaname)){
                       p.sendMessage(prefix + "§cそのarenaは存在しません！");
                       return true;
                   }
                   if(config1.getString("arenas."+arenaname+".pexmode").equalsIgnoreCase("false")) {
                       if (config1.getString("arenas." + arenaname + ".joingroup." + getRank(pg.run(p))).equalsIgnoreCase("true")) {
                           p.setBedSpawnLocation(p.getLocation(),true);
                           Location loc = (Location) config1.get("arenas." + arenaname + ".location");
                           p.teleport(loc);
                           p.sendMessage(prefix + "§e" + arenaname + "§aArenaにjoinしました。");
                           if(!config1.getString("arenas."+arenaname+".kit").equalsIgnoreCase("none")){
                               if(config1.contains("kit."+config1.getString("arenas."+arenaname+".kit"))){
                                   kitpvp.put(p.getUniqueId(),p.getInventory().getContents());
                                   p.getInventory().clear();
                                   Object inv = config1.get("kit."+config1.getString("arenas."+arenaname+".kit")+".inv");
                                   ItemStack[] inventory = null;
                                   if (inv instanceof ItemStack[]){
                                       inventory = (ItemStack[]) inv;
                                   } else if (inv instanceof List){
                                       List lista = (List) inv;
                                       inventory = (ItemStack[]) lista.toArray(new ItemStack[0]);
                                   }
                                   p.getInventory().setContents(inventory);
                               }
                           }
                           return true;
                       } else {
                           p.sendMessage(prefix + "§cあなたのランクが登録されていないためこのArenaにjoinできません！");
                           return true;
                       }
                   }else{
                       if (!p.hasPermission("man10pvpscore.join."+arenaname)) {
                           p.sendMessage(prefix + "§cあなたにはこのarenaにjoinする権限がありません！");
                           return true;
                       }
                       p.setBedSpawnLocation(p.getLocation(),true);
                       Location loc = (Location) config1.get("arenas." + arenaname + ".location");
                       p.teleport(loc);
                       p.sendMessage(prefix + "§e" + arenaname + "§aArenaにjoinしました。");
                       if(!config1.getString("arenas."+arenaname+".kit").equalsIgnoreCase("none")){
                           if(config1.contains("kit."+config1.getString("arenas."+arenaname+".kit"))){
                               kitpvp.put(p.getUniqueId(),p.getInventory().getContents());
                               p.getInventory().clear();
                               Object inv = config1.get("kit."+config1.getString("arenas."+arenaname+".kit")+".inv");
                               ItemStack[] inventory = null;
                               if (inv instanceof ItemStack[]){
                                   inventory = (ItemStack[]) inv;
                               } else if (inv instanceof List){
                                   List lista = (List) inv;
                                   inventory = (ItemStack[]) lista.toArray(new ItemStack[0]);
                               }
                               p.getInventory().setContents(inventory);
                           }
                       }
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
                   cc.saveConfig();
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
                   cc.saveConfig();
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
                payplayer.sendMessage(prefix+"§a"+p.getName()+"から§e"+pay+"p§a受け取りました。");
                return true;
            }else if(args[0].equalsIgnoreCase("kit")) {
                if(args[1].equalsIgnoreCase("create")) {
                    if (!p.hasPermission("man10pvpscore.kit.create")) {
                        p.sendMessage(prefix + "§cあなたにはkitを作成する権限がありません！");
                        return true;
                    }
                    String kitname = args[2];
                    if(config1.contains("kit."+kitname)){
                        p.sendMessage(prefix+"§4そのkitはすでに存在します");
                        p.sendMessage(prefix+"§ckitを更新: /mps kit update [kit名]");
                        return true;
                    }
                    PlayerInventory pinv = p.getInventory();
                    config1.set("kit."+kitname+".inv",pinv.getContents());
                    cc.saveConfig();
                    p.sendMessage(prefix+"§e"+kitname+"§akitを作成しました。");
                    return true;
                }else if(args[1].equalsIgnoreCase("update")) {
                    if (!p.hasPermission("man10pvpscore.kit.update")) {
                        p.sendMessage(prefix + "§cあなたにはkitを更新する権限がありません！");
                        return true;
                    }
                    String kitname = args[2];
                    if(!config1.contains("kit."+kitname)){
                        p.sendMessage(prefix+"§4そのkitは存在しません");
                        p.sendMessage(prefix+"§ckitを作成: /mps kit create [kit名]");
                        return true;
                    }
                    PlayerInventory pinv = p.getInventory();
                    config1.set("kit."+kitname+".inv",pinv.getContents());
                    cc.saveConfig();
                    p.sendMessage(prefix+"§e"+kitname+"§akitを更新しました。");
                    return true;
                }else if(args[1].equalsIgnoreCase("remove")) {
                    if (!p.hasPermission("man10pvpscore.kit.remove")) {
                        p.sendMessage(prefix + "§cあなたにはkitを削除する権限がありません！");
                        return true;
                    }
                    String kitname = args[2];
                    if(!config1.contains("kit."+kitname)){
                        p.sendMessage(prefix+"§4そのkitは存在しません");
                        p.sendMessage(prefix+"§ckitを作成: /mps kit create [kit名]");
                        return true;
                    }
                    config1.set("kit."+kitname,null);
                    cc.saveConfig();
                    p.sendMessage(prefix+"§e"+kitname+"§ckitを削除しました。");
                    return true;
                }else if(args[1].equalsIgnoreCase("pop")) {
                    if (!p.hasPermission("man10pvpscore.kit.pop")) {
                        p.sendMessage(prefix + "§cあなたにはkitを出す権限がありません！");
                        return true;
                    }
                    String kitname = args[2];
                    if(!config1.contains("kit."+kitname)){
                        p.sendMessage(prefix+"§4そのkitは存在しません");
                        p.sendMessage(prefix+"§ckitを作成: /mps kit create [kit名]");
                        return true;
                    }
                    p.getInventory().clear();
                    Object inv = config1.get("kit."+kitname+".inv");
                    ItemStack[] inventory = null;
                    if (inv instanceof ItemStack[]){
                        inventory = (ItemStack[]) inv;
                    } else if (inv instanceof List){
                        List lista = (List) inv;
                        inventory = (ItemStack[]) lista.toArray(new ItemStack[0]);
                    }
                    p.getInventory().setContents(inventory);
                    p.sendMessage(prefix+"§apopに成功しました");
                    return true;
                }
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
                    cc.saveConfig();
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
                    cc.saveConfig();
                    p.sendMessage(prefix + "§e"+arenaname+"§cArenaに"+groupname+"§cを除外しました。");
                    return true;
                }else if(args[1].equalsIgnoreCase("pexmode")) {
                    if (!p.hasPermission("man10pvpscore.arena.pexmode")) {
                        p.sendMessage(prefix + "§cあなたには権限設定の切り替えをする権限がありません！");
                        return true;
                    }
                    String arenaname = args[2];
                    if(!config1.contains("arenas."+arenaname)){
                        p.sendMessage(prefix + "§cそのarenaは存在しません！");
                        return true;
                    }
                    String trueorfalse = args[3];
                    if(!trueorfalse.equalsIgnoreCase("true")&&!trueorfalse.equalsIgnoreCase("false")){
                        p.sendMessage(prefix + "§ctrueかfalseで入力してください");
                        return true;
                    }
                    config1.set("arenas."+arenaname+".pexmode",trueorfalse);
                    cc.saveConfig();
                    p.sendMessage(prefix + "§e"+arenaname+"§aArenaのpexmodeを§e"+trueorfalse+"§aに設定しました。");
                    return true;
                }else if(args[1].equalsIgnoreCase("setkit")) {
                    if (!p.hasPermission("man10pvpscore.arena.kitset")) {
                        p.sendMessage(prefix + "§cあなたにはアリーナのkitを設定する権限がありません！");
                        return true;
                    }
                    String arenaname = args[2];
                    if(!config1.contains("arenas."+arenaname)){
                        p.sendMessage(prefix + "§cそのarenaは存在しません！");
                        return true;
                    }
                    String kitname = args[3];
                    if(!config1.contains("kit."+kitname)){
                        p.sendMessage(prefix+"§4そのkitは存在しません");
                        p.sendMessage(prefix+"§ckitを作成: /mps kit create [kit名]");
                        return true;
                    }
                    config1.set("arenas."+arenaname+".kit",kitname);
                    cc.saveConfig();
                    p.sendMessage(prefix+"§e"+arenaname+"§aArenaに§e"+kitname+"§akitを設定しました。");
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
        ka = new Kill_add();
        kg = new Kill_get();
        da = new Death_add();
        dg = new Death_get();
        ksg = new Kill_Stroke_get();
        kss = new Kill_Stroke_set();
        cc = new CustomConfig(this,"data.yml");
        getCommand("mps").setExecutor(this);
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        config2 = config;
        cc.saveDefaultConfig();
        FileConfiguration config3 = cc.getConfig();
        config1 = config3;
        worlds = config2.getStringList("pvpworlds");
        playerState = new HashMap<>();
        renzokukill = new HashMap<>();
        kitpvp = new HashMap<>();
    }

    @Override
    public void onDisable() {
        reloadConfig();
        cc.reloadConfig();
        for(Player p:Bukkit.getOnlinePlayers()){
            if(kitpvp.containsKey(p.getUniqueId())){
                ItemStack[] inv = kitpvp.get(p.getUniqueId());
                p.getInventory().clear();
                p.getInventory().setContents(inv);
                p.updateInventory();
                if(config1.contains("mainspawn")) {
                    Location loc = (Location) config1.get("mainspawn");
                    p.teleport(loc);
                }else {
                    p.teleport(p.getBedSpawnLocation());
                }
                p.sendMessage(prefix+"§aプラグイン停止のため、kitを元に戻しテレポートしました。");
                kitpvp.remove(p.getUniqueId());
            }
        }
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
    public void onLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        if(kitpvp.containsKey(p.getUniqueId())){
            ItemStack[] inv = kitpvp.get(p.getUniqueId());
            p.getInventory().clear();
            p.getInventory().setContents(inv);
            p.updateInventory();
            p.teleport(p.getBedSpawnLocation());
            if(config1.contains("mainspawn")) {
                Location loc = (Location) config1.get("mainspawn");
                p.teleport(loc);
            }else {
                p.teleport(p.getBedSpawnLocation());
            }
        }
    }
    @EventHandler
    public void commandCancel(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if(kitpvp.containsKey(p.getUniqueId())){
            if (p.isOp()) {
                return;
            }
            if (e.getMessage().equalsIgnoreCase("/mps leave")) {
                return;
            }
            p.sendMessage(prefix + "kitpvp中はコマンドの使用が認められていません！");
            e.setCancelled(true);
            return;
        }
    }
    @EventHandler
    public void playerChangeWorldEvent(PlayerChangedWorldEvent e){
        Player p = e.getPlayer();
        if(kitpvp.containsKey(p.getUniqueId())){
            p.sendMessage(prefix + "ワールド移動したため強制的にkitpvpを終了しました。");
            p.chat("/mps leave");
            return;
        }
    }
    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        if(!worlds.contains(event.getEntity().getWorld().getName())){
            return;
        }
        Player korosita = event.getEntity().getKiller();
        Player korosareta = event.getEntity();
        if(val.getBalance(korosita.getUniqueId())<200000){
            korosita.sendMessage(prefix+"§cあなたは"+korosareta.getName()+"をkillしたがあなたはお金を持っていなかった");
            korosareta.sendMessage(prefix+"§cあなたは"+korosita.getName()+"にkillされたが相手はお金を持っていなかった");
            return;
        }
        if(renzokukill.containsKey(korosita.getUniqueId())){
            UUID uuid = renzokukill.get(korosita.getUniqueId());
            if(uuid.equals(korosareta.getUniqueId())){
                korosita.sendMessage(prefix+"§cあなたは"+korosareta.getName()+"をkillしたが連続して同じ人だったため無効になった");
                korosareta.sendMessage(prefix+"§cあなたは"+korosita.getName()+"にkillされたが連続して同じ人だったため無効になった");
                return;
            }
        }
        if(kitpvp.containsKey(korosareta.getUniqueId())){
            ItemStack[] inv = kitpvp.get(korosareta.getUniqueId());
            korosareta.getInventory().clear();
            korosareta.getInventory().setContents(inv);
            korosareta.updateInventory();
            kitpvp.remove(korosareta.getUniqueId());
        }
        if(playerState.containsKey(korosita.getUniqueId())){
            korosita.sendMessage(prefix+"§aおめでとう！あなたは復活した！");
            korosareta.sendMessage(prefix+"§cあなたは"+korosita.getName()+"にkillされたが相手は復活戦モードだった");
            ps.run(korosita,1);
            playerState.remove(korosita.getUniqueId());
            return;
        }else if(playerState.containsKey(korosareta.getUniqueId())){
            korosareta.sendMessage(prefix+"§c残念！復活に失敗した！");
            korosita.sendMessage(prefix+"§aあなたは"+korosareta.getName()+"をkillしたが相手は復活戦モードだった");
            playerState.remove(korosareta.getUniqueId());
            return;
        }
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
            //ここまで、ポイント処理。ここから、金額処理
            double korositamoney = val.getBalance(korosita.getUniqueId());
            double korosaretamoney = val.getBalance(korosareta.getUniqueId());
            double getmoney = 0;
            if(korosaretamoney < 200000){
                getmoney = korosaretamoney / 2;
            }else{
                getmoney = 100000;
            }
            val.deposit(korosita.getUniqueId(),getmoney);
            val.withdraw(korosareta.getUniqueId(),getmoney);
            //ここまで、金額処理。次、kill・death・ストローク処理
            ka.run(korosita,1);
            da.run(korosareta,1);
            int stroke = ksg.run(korosita)+1;
            kss.run(korosita,stroke);
            if(stroke % 5 == 0){
                Bukkit.broadcastMessage(prefix+"§e§l"+korosita.getName()+"§c§lが§e§l"+stroke+"連続kill§c§l達成！");
                val.deposit(korosita.getUniqueId(),stroke*10000);
            }
            int korosaretastroke = ksg.run(korosareta);
            if(4 < korosaretastroke){
                Bukkit.broadcastMessage(prefix+"§e§l"+korosita.getName()+"§c§lが§e§l"+stroke+"連続kill§c§lを達成した§e§l"+korosareta.getName()+"§c§lをkillした！");
                val.deposit(korosita.getUniqueId(),korosaretastroke+10000);
            }
            kss.run(korosareta,0);
            //ここまで、kill・death・ストローク処理。次、結果整理
            korosita.sendMessage(prefix+"§aあなたは§e"+korosareta.getName()+"§aをkillし§e"+getpoint+"pと$"+getmoney+"§aを手に入れた");
            korosareta.sendMessage(prefix+"§cあなたは§e"+korosita.getName()+"§cにkillされ§e"+getpoint+"pと$"+getmoney+"§cを失った");
            return;
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
            String sql = "SELECT * FROM pvpdata WHERE uuid = '"+p.getUniqueId().toString()+"';";
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
            String sql = "SELECT * FROM pvpdata WHERE uuid = '"+p.getUniqueId().toString()+"';";
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
            String sql = "INSERT INTO "+mysql.DB+".pvpdata (point, uuid) VALUES (100 ,'"+p.getUniqueId().toString()+"' , 0 , 0, 0 );";
            mysql.execute(sql);
            return true;
        }
    }
    public class Pointset extends Thread {
        public boolean run(Player p,int point) {
            String sql = "UPDATE "+mysql.DB+".pvpdata set point = "+point+" where uuid = '"+p.getUniqueId().toString()+"';";
            mysql.execute(sql);
            return true;
        }
        public boolean run(OfflinePlayer p,int point) {
            String sql = "UPDATE "+mysql.DB+".pvpdata set point = "+point+" where uuid = '"+p.getUniqueId().toString()+"';";
            mysql.execute(sql);
            return true;
        }
    }
    public class Pointget extends Thread {
        public int run(Player p) {

            String sql = "SELECT * FROM "+mysql.DB+".pvpdata WHERE uuid = '"+p.getUniqueId().toString()+"';";
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

            String sql = "SELECT * FROM "+mysql.DB+".pvpdata WHERE uuid = '"+p.getUniqueId().toString()+"';";
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

            String sql = "SELECT * FROM "+mysql.DB+".pvpdata ORDER BY point DESC;";
            ResultSet rs = mysql.query(sql);
            String[] strings = new String[10];
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
    public class Pointtotal extends Thread {
        public int run(Player p) {

            String sql = "SELECT sum(point) FROM "+mysql.DB+".pvpdata;";
            ResultSet rs = mysql.query(sql);
            try {
                int get;
                    if (rs.next()) {
                        get = rs.getInt(1);
                    }else{
                        return 0;
                    }
                return get;
            } catch (SQLException e1) {
                e1.printStackTrace();
                return 0;
            } catch (NullPointerException e1) {
                e1.printStackTrace();
                return 0;
            }
        }
    }
    public class Kill_Stroke_get extends Thread {
        public int run(Player p) {

            String sql = "SELECT * FROM "+mysql.DB+".pvpdata WHERE uuid = '"+p.getUniqueId().toString()+"';";
            ResultSet rs = mysql.query(sql);
            try {
                if(rs.next()) {
                    int get = rs.getInt("kill_stroke");
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

            String sql = "SELECT * FROM "+mysql.DB+".pvpdata WHERE uuid = '"+p.getUniqueId().toString()+"';";
            ResultSet rs = mysql.query(sql);
            try {
                if(rs.next()) {
                    int get = rs.getInt("kill_stroke");
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
    public class Kill_Stroke_set extends Thread {
        public boolean run(Player p,int set) {
            String sql = "UPDATE "+mysql.DB+".pvpdata set kill_stroke = "+set+" where uuid = '"+p.getUniqueId().toString()+"';";
            mysql.execute(sql);
            return true;
        }
        public boolean run(OfflinePlayer p,int set) {
            String sql = "UPDATE "+mysql.DB+".pvpdata set kill_stroke = "+set+" where uuid = '"+p.getUniqueId().toString()+"';";
            mysql.execute(sql);
            return true;
        }
    }
    public class Kill_get extends Thread {
        public int run(Player p) {

            String sql = "SELECT * FROM "+mysql.DB+".pvpdata WHERE uuid = '"+p.getUniqueId().toString()+"';";
            ResultSet rs = mysql.query(sql);
            try {
                if(rs.next()) {
                    int get = rs.getInt("kill");
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

            String sql = "SELECT * FROM "+mysql.DB+".pvpdata WHERE uuid = '"+p.getUniqueId().toString()+"';";
            ResultSet rs = mysql.query(sql);
            try {
                if(rs.next()) {
                    int get = rs.getInt("kill");
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
    public class Death_get extends Thread {
        public int run(Player p) {

            String sql = "SELECT * FROM "+mysql.DB+".pvpdata WHERE uuid = '"+p.getUniqueId().toString()+"';";
            ResultSet rs = mysql.query(sql);
            try {
                if(rs.next()) {
                    int get = rs.getInt("death");
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

            String sql = "SELECT * FROM "+mysql.DB+".pvpdata WHERE uuid = '"+p.getUniqueId().toString()+"';";
            ResultSet rs = mysql.query(sql);
            try {
                if(rs.next()) {
                    int get = rs.getInt("death");
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
    public class Kill_add extends Thread {
        public boolean run(Player p,int add) {
            String sql = "UPDATE "+mysql.DB+".pvpdata set kill = kill+"+add+" where uuid = '"+p.getUniqueId().toString()+"';";
            mysql.execute(sql);
            return true;
        }
        public boolean run(OfflinePlayer p,int add) {
            String sql = "UPDATE "+mysql.DB+".pvpdata set kill = kill+"+add+" where uuid = '"+p.getUniqueId().toString()+"';";
            mysql.execute(sql);
            return true;
        }
    }
    public class Death_add extends Thread {
        public boolean run(Player p,int add) {
            String sql = "UPDATE "+mysql.DB+".pvpdata set death = death+"+add+" where uuid = '"+p.getUniqueId().toString()+"';";
            mysql.execute(sql);
            return true;
        }
        public boolean run(OfflinePlayer p,int add) {
            String sql = "UPDATE "+mysql.DB+".pvpdata set death = death+"+add+" where uuid = '"+p.getUniqueId().toString()+"';";
            mysql.execute(sql);
            return true;
        }
    }
}
