package com.elfocrash.roboto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.elfocrash.roboto.ai.AbyssWalkerAI;
import com.elfocrash.roboto.ai.FakePlayerAI;
import com.elfocrash.roboto.ai.FallbackAI;
import com.elfocrash.roboto.ai.HawkeyeAI;
import com.elfocrash.roboto.ai.NecromancerAI;
import com.elfocrash.roboto.ai.PhantomRangerAI;
import com.elfocrash.roboto.ai.PlainsWalkerAI;
import com.elfocrash.roboto.ai.SilverRangerAI;
import com.elfocrash.roboto.ai.SorcererAI;
import com.elfocrash.roboto.ai.SpellHowlerAI;
import com.elfocrash.roboto.ai.SpellSignerAI;
import com.elfocrash.roboto.ai.TreasureHunterAI;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportType;
import net.sf.l2j.gameserver.datatables.PlayerNameTable;
import net.sf.l2j.gameserver.datatables.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.SevenSigns;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.CabalType;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.SealType;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Clan.SubPledge;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.actor.template.PlayerTemplate;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.ClassRace;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.model.base.Sex;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.Siege.SiegeSide;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.Die;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ExStorageMaxCount;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeSkillList;
import net.sf.l2j.gameserver.network.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

/**
 * @author Elfocrash
 *
 */
public enum FakePlayerManager
{
	INSTANCE;
	
	private FakePlayerManager() {
		
	}
	
	public void initialise() {
		FakePlayerTaskManager.INSTANCE.initialise();
	}
		
	public void spawnPlayer(int x, int y, int z) {
		FakePlayer activeChar = createRandomFakePlayer();
		World.getInstance().addPlayer(activeChar);
		handlePlayerClanOnSpawn(activeChar);		
		handlePlayerSevenSignsOnSpawn(activeChar);		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
			activeChar.setSpawnProtection(true);		
		activeChar.spawnMe(x,y,z);		
		sendBasicInfoToFake(activeChar);		
		activeChar.onPlayerEnter();		
		if (Olympiad.getInstance().playerInStadia(activeChar))
			activeChar.teleToLocation(TeleportType.TOWN);
		
		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false))
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);
		
		if (!activeChar.isGM() && (!activeChar.isInSiege() || activeChar.getSiegeState() < 2) && activeChar.isInsideZone(ZoneId.SIEGE))
			activeChar.teleToLocation(TeleportType.TOWN);
		
		assignDefaultAIToPlayer(activeChar);		
	}
	
	private void assignDefaultAIToPlayer(FakePlayer activeChar)
	{
		try
		{
			activeChar.setFakeAi(getAIbyClassId(activeChar.getClassId()).getConstructor(FakePlayer.class).newInstance(activeChar));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void sendBasicInfoToFake(FakePlayer activeChar)
	{
		activeChar.getMacroses().sendUpdate();
		activeChar.sendPacket(new UserInfo(activeChar));
		activeChar.sendPacket(new HennaInfo(activeChar));
		activeChar.sendPacket(new FriendList(activeChar));
		activeChar.sendPacket(new ItemList(activeChar, false));
		activeChar.sendPacket(new ShortCutInit(activeChar));
		activeChar.sendPacket(new ExStorageMaxCount(activeChar));
		
		if (activeChar.isAlikeDead())
			activeChar.sendPacket(new Die(activeChar));
		
		activeChar.updateEffectIcons();
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		activeChar.sendSkillList();
	}
	
	private static void handlePlayerSevenSignsOnSpawn(FakePlayer activeChar)
	{
		if (SevenSigns.getInstance().isSealValidationPeriod() && SevenSigns.getInstance().getSealOwner(SealType.STRIFE) != CabalType.NORMAL)
		{
			CabalType cabal = SevenSigns.getInstance().getPlayerCabal(activeChar.getObjectId());
			if (cabal != CabalType.NORMAL)
			{
				if (cabal == SevenSigns.getInstance().getSealOwner(SealType.STRIFE))
					activeChar.addSkill(FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
				else
					activeChar.addSkill(FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
			}
		}
		else
		{
			activeChar.removeSkill(FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
			activeChar.removeSkill(FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
		}
	}
	
	private static void handlePlayerClanOnSpawn(FakePlayer activeChar)
	{
		final L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			activeChar.sendPacket(new PledgeSkillList(clan));
			
			// Refresh player instance.
			clan.getClanMember(activeChar.getObjectId()).setPlayerInstance(activeChar);
			
			final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN).addCharName(activeChar);
			final PledgeShowMemberListUpdate update = new PledgeShowMemberListUpdate(activeChar);
			
			// Send packets to others members.
			for (Player member : clan.getOnlineMembers())
			{
				if (member == activeChar)
					continue;
				
				member.sendPacket(msg);
				member.sendPacket(update);
			}
			
			if (activeChar.getSponsor() != 0)
			{
				final Player sponsor = World.getInstance().getPlayer(activeChar.getSponsor());
				if (sponsor != null)
					sponsor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN).addCharName(activeChar));
			}
			else if (activeChar.getApprentice() != 0)
			{
				final Player apprentice = World.getInstance().getPlayer(activeChar.getApprentice());
				if (apprentice != null)
					apprentice.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN).addCharName(activeChar));
			}
			
			// Add message at connexion if clanHall not paid.
			final ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(clan);
			if (clanHall != null && !clanHall.getPaid())
				activeChar.sendPacket(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
			
			for (Castle castle : CastleManager.getInstance().getCastles())
			{
				final Siege siege = castle.getSiege();
				if (!siege.isInProgress())
					continue;
				
				final SiegeSide type = siege.getSide(clan);
				if (type == SiegeSide.ATTACKER)
					activeChar.setSiegeState((byte) 1);
				else if (type == SiegeSide.DEFENDER || type == SiegeSide.OWNER)
					activeChar.setSiegeState((byte) 2);
			}
			
			activeChar.sendPacket(new PledgeShowMemberListAll(clan, 0));
			
			for (SubPledge sp : clan.getAllSubPledges())
				activeChar.sendPacket(new PledgeShowMemberListAll(clan, sp.getId()));
			
			activeChar.sendPacket(new UserInfo(activeChar));
			activeChar.sendPacket(new PledgeStatusChanged(clan));
		}
	}
	
	public FakePlayer createRandomFakePlayer() {
		
		int objectId = IdFactory.getInstance().getNextId();
		//TODO: Add a wordlist
		String accountName = "AutoPilot";
		String playerName = "TotallyReal" + Rnd.get(100000);
		
		ClassId classId = getThirdClasses().get(Rnd.get(0, getThirdClasses().size() - 1));
		
		final PlayerTemplate template = CharTemplateTable.getInstance().getTemplate(classId);
		PcAppearance app = getRandomAppearance(template.getRace());
		FakePlayer player = new FakePlayer(objectId, template, accountName, app);
		
		player.setName(playerName);
		
		PlayerNameTable.getInstance().addPlayer(objectId, accountName, playerName, player.getAccessLevel().getLevel());
		player.setBaseClass(player.getClassId());
		
		setLevel(player, 81);
		
		player.giveAvailableSkills();
		
		player.setCurrentCp(player.getMaxCp());
		player.setCurrentHp(player.getMaxHp());
		player.setCurrentMp(player.getMaxMp());
		
		giveArmorsByClass(player);
		giveWeaponsByClass(player);
		
		return player;	
	}
	
	public void giveArmorsByClass(FakePlayer player) {
		List<Integer> itemIds = new ArrayList<>();
		switch(player.getClassId()) {
			case ARCHMAGE:
			case SOULTAKER:
			case HIEROPHANT:
			case ARCANA_LORD:
			case CARDINAL:
			case MYSTIC_MUSE:
			case ELEMENTAL_MASTER:
			case EVAS_SAINT:
			case STORM_SCREAMER:
			case SPECTRAL_MASTER:
			case SHILLIEN_SAINT:
			case DOMINATOR:
			case DOOMCRYER:
				itemIds = Arrays.asList(2407, 512, 5767, 5779, 858, 858, 889, 889, 920);
				break;
			case DUELIST:
			case DREADNOUGHT:
			case PHOENIX_KNIGHT:
			case SWORD_MUSE:
			case HELL_KNIGHT:
			case SPECTRAL_DANCER:
			case EVAS_TEMPLAR:
			case SHILLIEN_TEMPLAR:
			case TITAN:
			case MAESTRO:
				itemIds = Arrays.asList(6373, 6374, 6375, 6376, 6378, 858, 858, 889, 889, 920);
				break;
			case SAGGITARIUS:
			case ADVENTURER:
			case WIND_RIDER:
			case MOONLIGHT_SENTINEL:
			case GHOST_HUNTER:
			case GHOST_SENTINEL:
			case FORTUNE_SEEKER:
			case GRAND_KHAVATARI:
				itemIds = Arrays.asList(6379, 6380, 6381, 6382, 858, 858, 889, 889, 920);				
				break;
		default:
			break;
		}		
		for (int id : itemIds)
		{
			player.getInventory().addItem("Armors", id, 1, player, null);
			ItemInstance item = player.getInventory().getItemByItemId(id);
			//enchant the item??
			player.getInventory().equipItemAndRecord(item);
			player.getInventory().reloadEquippedItems();
			player.broadcastCharInfo();
		}
	}
	
	public void giveWeaponsByClass(FakePlayer player) {
		List<Integer> itemIds = new ArrayList<>();
		switch(player.getClassId()) {
			case FORTUNE_SEEKER:
			case GHOST_HUNTER:
			case WIND_RIDER:
			case ADVENTURER:
				itemIds = Arrays.asList(6590);
				break;			
			case SAGGITARIUS:
			case MOONLIGHT_SENTINEL:
			case GHOST_SENTINEL:
				itemIds = Arrays.asList(7577);
				break;			
			case PHOENIX_KNIGHT:
			case SWORD_MUSE:
			case HELL_KNIGHT:
			case EVAS_TEMPLAR:
			case SHILLIEN_TEMPLAR:		
				itemIds = Arrays.asList(6583, 6377);
				break;
			case MAESTRO:
				itemIds = Arrays.asList(6585,6377);
				break;				
			case TITAN:
				itemIds = Arrays.asList(6607);
				break;			
			case DUELIST:
			case SPECTRAL_DANCER:
				itemIds = Arrays.asList(6580);
				break;
			case DREADNOUGHT:
				itemIds = Arrays.asList(6599);
				break;			
			case ARCHMAGE:
			case SOULTAKER:
			case HIEROPHANT:
			case ARCANA_LORD:
			case CARDINAL:
			case MYSTIC_MUSE:
			case ELEMENTAL_MASTER:
			case EVAS_SAINT:
			case STORM_SCREAMER:
			case SPECTRAL_MASTER:
			case SHILLIEN_SAINT:
			case DOMINATOR:
			case DOOMCRYER:
				itemIds = Arrays.asList(6608);
				break;
			case GRAND_KHAVATARI:
				itemIds = Arrays.asList(6602);				
				break;
		default:
			break;				
		}
		for (int id : itemIds)
		{
			player.getInventory().addItem("Weapon", id, 1, player, null);
			ItemInstance item = player.getInventory().getItemByItemId(id);
			item.setEnchantLevel(Rnd.get(7, 20));		
			player.getInventory().equipItemAndRecord(item);
			player.getInventory().reloadEquippedItems();
		}
	}
	
	public List<ClassId> getThirdClasses(){	
		//removed summoner classes because fuck those guys
		List<ClassId> classes = new ArrayList<>();		
		
		/*classes.add(ClassId.EVAS_SAINT);
		classes.add(ClassId.SHILLIEN_TEMPLAR);
		classes.add(ClassId.SPECTRAL_DANCER);
		classes.add(ClassId.GHOST_HUNTER);

		classes.add(ClassId.DUELIST);
		classes.add(ClassId.DREADNOUGHT);
		classes.add(ClassId.PHOENIX_KNIGHT);
		classes.add(ClassId.HELL_KNIGHT);
		
		classes.add(ClassId.CARDINAL);
		classes.add(ClassId.HIEROPHANT);
		classes.add(ClassId.EVAS_TEMPLAR);
		classes.add(ClassId.SWORD_MUSE);
		
		classes.add(ClassId.TITAN);
		classes.add(ClassId.GRAND_KHAVATARI);
		classes.add(ClassId.DOMINATOR);
		classes.add(ClassId.DOOMCRYER);
		classes.add(ClassId.FORTUNE_SEEKER);
		classes.add(ClassId.MAESTRO);*/
		
		//classes.add(ClassId.ARCANA_LORD);
		//classes.add(ClassId.ELEMENTAL_MASTER);
		//classes.add(ClassId.SPECTRAL_MASTER);
		//classes.add(ClassId.SHILLIEN_SAINT);
		
		classes.add(ClassId.SAGGITARIUS); // done
		classes.add(ClassId.ARCHMAGE); // done
		classes.add(ClassId.SOULTAKER); // done
		classes.add(ClassId.MYSTIC_MUSE); // done
		classes.add(ClassId.STORM_SCREAMER); // done
		classes.add(ClassId.MOONLIGHT_SENTINEL); // done
		classes.add(ClassId.GHOST_SENTINEL); // done
		classes.add(ClassId.ADVENTURER); // done
		classes.add(ClassId.WIND_RIDER); // done
		
		return classes;
	}

	public PcAppearance getRandomAppearance(ClassRace race) {
		
		Sex randomSex = Rnd.get(1, 2) == 1 ? Sex.MALE : Sex.FEMALE;
		int hairStyle = Rnd.get(0, randomSex == Sex.MALE ? 4 : 6);				
		int hairColor = Rnd.get(0, 3);
		int faceId = Rnd.get(0, 2);
		
		return new PcAppearance((byte)faceId, (byte)hairColor, (byte)hairStyle, randomSex);
	}
	
	public void setLevel(FakePlayer player, int level) {
		if (level >= 1 && level <= Experience.MAX_LEVEL)
		{
			long pXp = player.getExp();
			long tXp = Experience.LEVEL[81];
			
			if (pXp > tXp)
				player.removeExpAndSp(pXp - tXp, 0);
			else if (pXp < tXp)
				player.addExpAndSp(tXp - pXp, 0);
		}
	}
	
	public Class<? extends FakePlayerAI> getAIbyClassId(ClassId classId) {
		Class<? extends FakePlayerAI> ai = getAllAIs().get(classId);
		if(ai == null)
			return FallbackAI.class;
		
		return ai;
	}
	
	public Map<ClassId, Class<? extends FakePlayerAI>> getAllAIs() {
		Map<ClassId, Class<? extends FakePlayerAI>> ais = new HashMap<>();
		ais.put(ClassId.STORM_SCREAMER, SpellHowlerAI.class);
		ais.put(ClassId.MYSTIC_MUSE, SpellSignerAI.class);
		ais.put(ClassId.ARCHMAGE, SorcererAI.class);
		ais.put(ClassId.SOULTAKER, NecromancerAI.class);
		ais.put(ClassId.SAGGITARIUS, HawkeyeAI.class);
		ais.put(ClassId.MOONLIGHT_SENTINEL, SilverRangerAI.class);		
		ais.put(ClassId.GHOST_SENTINEL, PhantomRangerAI.class);		
		ais.put(ClassId.ADVENTURER, TreasureHunterAI.class);
		ais.put(ClassId.WIND_RIDER, PlainsWalkerAI.class);
		ais.put(ClassId.GHOST_HUNTER, AbyssWalkerAI.class);
		
		return ais;
	}
	
	public int getFakePlayersCount() {
		return getFakePlayers().size();
	}
	
	public List<FakePlayer> getFakePlayers() {
		return World.getInstance().getPlayers().stream().filter(x-> x instanceof FakePlayer).map(x-> (FakePlayer)x).collect(Collectors.toList());
	}
		
	public int[][] getFighterBuffs()
	{
		return new int[][] {
			{1204 , 2}, //wind walk
			{264, 1}, // song of earth
			{1068, 3}, // might
			{1062, 2}, // besekers
			{1086, 2}, //haste
			{1388, 3}, //Greater Might
			{1036, 2}, // magic barrier
			{274, 1}, // dance of fire
			{273, 1},//dance of fury
			{268, 1}, // dance of wind		
			{1040, 3} //shield
		};
	}
	
	public int[][] getMageBuffs()
	{
		return new int[][] {
			{1204 , 2}, //wind walk
			{264, 1}, // song of earth
			{1085, 3}, // acumen
			{1062, 2}, // besekers
			{1059, 3}, //empower
			{1389, 3}, //Greater Shield
			{1036, 2}, // magic barrier
			{273, 1}, // dance of the mystic
			{276, 1},//dance of concentration
			{268, 1}, // song of wind			
			{1040, 3} //shield
		};
	}
}
