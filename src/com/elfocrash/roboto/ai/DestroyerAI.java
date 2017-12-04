package com.elfocrash.roboto.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.elfocrash.roboto.FakePlayer;
import com.elfocrash.roboto.FakePlayerManager;
import com.elfocrash.roboto.model.SupportSpell;
import com.elfocrash.roboto.model.OffensiveSpell;
import com.elfocrash.roboto.model.SpellUsageCondition;

import javafx.util.Pair;
import net.sf.l2j.gameserver.model.ShotType;

/**
 * @author Elfocrash
 *
 */
public class DestroyerAI extends FakePlayerAI
{
	public DestroyerAI(FakePlayer character)
	{
		super(character);
	}
	
	@Override
	public void thinkAndAct()
	{
		if(_fakePlayer.isDead()) {
			return;
		}
		
		applyDefaultBuffs();
		handleShots();
		selfSupportBuffs();
		tryTargetRandomCreatureByTypeInRadius(FakePlayerManager.INSTANCE.getTestTargetClass(), FakePlayerManager.INSTANCE.getTestTargetRange());		
		tryAttackingUsingFighterOffensiveSkill();
	}
	
	@Override
	protected ShotType getShotType()
	{
		return ShotType.SOULSHOT;
	}
	
	@Override
	protected List<OffensiveSpell> getOffensiveSpells()
	{
		List<OffensiveSpell> _offensiveSpells = new ArrayList<>();
		_offensiveSpells.add(new OffensiveSpell(315, SpellUsageCondition.NONE, 1));
		_offensiveSpells.add(new OffensiveSpell(190, SpellUsageCondition.NONE, 2));
		_offensiveSpells.add(new OffensiveSpell(362, SpellUsageCondition.NONE, 3));
		return _offensiveSpells; 
	}
	
	@Override
	public List<SupportSpell> getSelfSupportSpells()
	{
		List<SupportSpell> _selfSupportSpells = new ArrayList<>();
		_selfSupportSpells.add(new SupportSpell(139, SpellUsageCondition.LESSHPPERCENT, 30));
		_selfSupportSpells.add(new SupportSpell(176, SpellUsageCondition.LESSHPPERCENT, 30));
		return _selfSupportSpells;
	}
	
	@Override
	protected int[][] getBuffs()
	{
		return FakePlayerManager.INSTANCE.getFighterBuffs();
	}
	
	@Override
	protected List<Pair<Integer, Double>> getHealingSpells()
	{		
		return Collections.emptyList();
	}
}