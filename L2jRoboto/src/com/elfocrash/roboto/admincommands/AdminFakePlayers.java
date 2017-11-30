package com.elfocrash.roboto.admincommands;

import com.elfocrash.roboto.FakePlayerManager;
import com.elfocrash.roboto.FakePlayerTaskManager;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Elfocrash
 *
 */
public class AdminFakePlayers implements IAdminCommandHandler
{
	private final String fakesFolder = "data/html/admin/fakeplayers/";
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_takecontrol",
		"admin_releasecontrol",
		"admin_fakes",
		"admin_spawnrandom"
	};
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showFakeDashboard(Player activeChar) {
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(fakesFolder + "index.htm");
		html.replace("%fakecount%", FakePlayerManager.INSTANCE.getFakePlayersCount());
		html.replace("%taskcount%", FakePlayerTaskManager.INSTANCE.getTaskCount());
		activeChar.sendPacket(html);
	}
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_fakes"))
		{
			showFakeDashboard(activeChar);
		}
		
		if (command.startsWith("admin_spawnrandom")) {
			FakePlayerManager.INSTANCE.spawnPlayer(activeChar.getX(),activeChar.getY(),activeChar.getZ());
			if(command.contains(" ")) {
				String arg = command.split(" ")[1];
				if(arg.equalsIgnoreCase("htm")) {
					showFakeDashboard(activeChar);
				}
			}
		}
		return true;
	}
}