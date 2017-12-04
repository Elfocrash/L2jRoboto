package com.elfocrash.roboto.ai;

import java.util.Collections;
import java.util.List;

import com.elfocrash.roboto.FakePlayer;
import com.elfocrash.roboto.FakePlayerManager;
import com.elfocrash.roboto.model.OffensiveSpell;
import com.elfocrash.roboto.model.SupportSpell;

import javafx.util.Pair;
import net.sf.l2j.gameserver.model.ShotType;

/**
 * @author Elfocrash
 *
 */
public class BishopAI extends FakePlayerAI
{
	public BishopAI(FakePlayer character)
	{
		super(character);
	}
	
	@Override
	public void thinkAndAct()
	{
		
	}
	
	@Override
	protected ShotType getShotType()
	{
		return ShotType.BLESSED_SPIRITSHOT;
	}
	
	@Override
	protected List<OffensiveSpell> getOffensiveSpells()
	{		
		return Collections.emptyList();
	}
	
	@Override
	protected List<Pair<Integer, Double>> getHealingSpells()
	{		
		return Collections.emptyList();
	}
	
	@Override
	protected int[][] getBuffs()
	{
		return FakePlayerManager.INSTANCE.getMageBuffs();
	}	

	@Override
	protected List<SupportSpell> getSelfSupportSpells() {
		return Collections.emptyList();
	}
}
