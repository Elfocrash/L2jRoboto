package com.elfocrash.roboto.ai;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.elfocrash.roboto.FakePlayer;
import com.elfocrash.roboto.model.BotSkill;
import com.elfocrash.roboto.model.HealingSpell;
import com.elfocrash.roboto.model.OffensiveSpell;
import com.elfocrash.roboto.model.SupportSpell;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.ShotType;
import net.sf.l2j.gameserver.model.SpawnLocation;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.MoveToLocation;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;
import net.sf.l2j.gameserver.network.serverpackets.StopRotation;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;

/**
 * @author Elfocrash
 *
 */
public abstract class FakePlayerAI
{
	protected final FakePlayer _fakePlayer;
		
	protected volatile boolean _clientMoving;
	protected volatile boolean _clientAutoAttacking;
	private long _moveToPawnTimeout;
	protected int _clientMovingToPawnOffset;	
	private boolean _isPickingMageSpell = false;
	
	public FakePlayerAI(FakePlayer character)
	{
		_fakePlayer = character;
		setup();
		applyDefaultBuffs();
	}
	
	public abstract void thinkAndAct(); 
	protected abstract ShotType getShotType();
	protected abstract List<OffensiveSpell> getOffensiveSpells();
	protected abstract List<HealingSpell> getHealingSpells();
	protected abstract List<SupportSpell> getSelfSupportSpells();
	protected abstract int[][] getBuffs();
	
	public boolean isPickingMageSpell() {
		return _isPickingMageSpell;
	}
	
	public void setup() {
		_fakePlayer.setIsRunning(true);
	}
	
	protected void applyDefaultBuffs() {
		for(int[] buff : getBuffs()){
			try {
				Map<Integer, L2Effect> activeEffects = Arrays.stream(_fakePlayer.getAllEffects())
					.filter(x-> x.getSkillType() == L2SkillType.BUFF)
					.collect(Collectors.toMap(x-> x.getSkill().getId(), x->x));
			
			if(!activeEffects.containsKey(buff[0]))
				SkillTable.getInstance().getInfo(buff[0], buff[1]).getEffects(_fakePlayer, _fakePlayer);
			else {
				if((activeEffects.get(buff[0]).getPeriod() - activeEffects.get(buff[0]).getTime()) <= 20) {
					SkillTable.getInstance().getInfo(buff[0], buff[1]).getEffects(_fakePlayer, _fakePlayer);
				}
			}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void tryTargetRandomCreatureByTypeInRadius(Class<? extends Creature> creatureClass, int radius)
	{
		if(_fakePlayer.getTarget() == null) {
			List<Creature> targets = _fakePlayer.getKnownTypeInRadius(creatureClass, radius).stream().filter(x->!x.isDead()).collect(Collectors.toList());
			if(!targets.isEmpty()) {
				Creature target = targets.get(Rnd.get(0, targets.size() -1 ));
				_fakePlayer.setTarget(target);				
			}
		}else {
			if(((Creature)_fakePlayer.getTarget()).isDead())
			_fakePlayer.setTarget(null);
		}	
	}

	protected void tryAttackingUsingMageOffensiveSkill() {
		if(_fakePlayer.getTarget() != null)
		{
			L2Skill skill = _fakePlayer.getSkill(getRandomAvaiableMageSpellForTarget().getSkillId());
			if(skill != null)
				castSpell(skill);
		}
	}
	
	protected void tryAttackingUsingFighterOffensiveSkill()
	{
		if(_fakePlayer.getTarget() != null && _fakePlayer.getTarget() instanceof Creature) {			
			if(getOffensiveSpells() != null && !getOffensiveSpells().isEmpty()) {
				L2Skill skill = getRandomAvaiableFighterSpellForTarget();			
				if(skill != null) {
					castSpell(skill);
				}
			}
			
			_fakePlayer.forceAutoAttack((Creature)_fakePlayer.getTarget());
		}
	}
	
	protected void handleShots() {
		if(_fakePlayer.getInventory().getItemByItemId(getShotId()) != null) {
			if(_fakePlayer.getInventory().getItemByItemId(getShotId()).getCount() <= 20) {
				_fakePlayer.getInventory().addItem("", getShotId(), 500, _fakePlayer, null);			
			}
		}else {
			_fakePlayer.getInventory().addItem("", getShotId(), 500, _fakePlayer, null);
		}
		
		if(_fakePlayer.getAutoSoulShot().isEmpty()) {
			_fakePlayer.addAutoSoulShot(getShotId());
			_fakePlayer.sendPacket(new ExAutoSoulShot(getShotId(), 1));
			
			if (_fakePlayer.getActiveWeaponItem() != _fakePlayer.getFistsWeaponItem() /*&& item.getItem().getCrystalType() == _fakePlayer.getActiveWeaponItem().getCrystalType()*/)
				_fakePlayer.rechargeShots(true, true);
		}	
	}
	
	public HealingSpell getRandomAvaiableHealingSpellForTarget() {

		List<HealingSpell> spellsOrdered = getHealingSpells().stream().sorted((o1, o2)-> Integer.compare(o1.getPriority(), o2.getPriority())).collect(Collectors.toList());
		int skillListSize = spellsOrdered.size();
		BotSkill skill = waitAndPickAvailablePrioritisedSpell(spellsOrdered, skillListSize);
		return (HealingSpell)skill;
	}	
	
	protected BotSkill getRandomAvaiableMageSpellForTarget() {		
		
		List<OffensiveSpell> spellsOrdered = getOffensiveSpells().stream().sorted((o1, o2)-> Integer.compare(o1.getPriority(), o2.getPriority())).collect(Collectors.toList());
		int skillListSize = spellsOrdered.size();
		
		BotSkill skill = null;
		try {
			skill = waitAndPickAvailablePrioritisedSpell(spellsOrdered, skillListSize);	
		}catch(NullPointerException ex) {
			ex.printStackTrace();
		}
		
		return skill;
	}

	private BotSkill waitAndPickAvailablePrioritisedSpell(List<? extends BotSkill> spellsOrdered, int skillListSize) {
		int skillIndex = 0;		
		BotSkill botSkill = spellsOrdered.get(skillIndex);
		_fakePlayer.getCurrentSkill().setCtrlPressed(!_fakePlayer.getTarget().isInsideZone(ZoneId.PEACE));
		L2Skill skill = _fakePlayer.getSkill(botSkill.getSkillId());
		while(!_fakePlayer.checkUseMagicConditions(skill,true,false)) {
			
			_isPickingMageSpell = true;
			if(_fakePlayer.isDead() || _fakePlayer.isOutOfControl()) {
				_isPickingMageSpell = false;
				return null;
			}
			if((skillIndex < 0) || (skillIndex >= skillListSize)) {
				skillIndex = 0;				
			}
			skill = _fakePlayer.getSkill(spellsOrdered.get(skillIndex).getSkillId());
			botSkill = spellsOrdered.get(skillIndex);
			skillIndex++;			
		}
		
		_isPickingMageSpell = false;
		return botSkill;
	}
	
	protected L2Skill getRandomAvaiableFighterSpellForTarget() {	
		int maxRetries = 10;
		int retries = 0;
		List<OffensiveSpell> spellsOrdered = getOffensiveSpells().stream().sorted((o1, o2)-> Integer.compare(o1.getPriority(), o2.getPriority())).collect(Collectors.toList());
		int skillIndex = 0;
		int skillListSize = spellsOrdered.size();
		
		L2Skill skill = _fakePlayer.getSkill(spellsOrdered.get(skillIndex).getSkillId());
		
		_fakePlayer.getCurrentSkill().setCtrlPressed(!_fakePlayer.getTarget().isInsideZone(ZoneId.PEACE));		
		while(!_fakePlayer.checkUseMagicConditions(skill,true,false) && retries <= maxRetries) {
			if((skillIndex < 0) || (skillIndex >= skillListSize)) {
				skillIndex = 0;				
			}
			skill = _fakePlayer.getSkill(spellsOrdered.get(skillIndex).getSkillId());
			retries++;
			skillIndex++;
		}

		return skill;
	}
	
	protected void selfSupportBuffs() {
		List<Integer> activeEffects = Arrays.stream(_fakePlayer.getAllEffects())
				.map(x->x.getSkill().getId())
				.collect(Collectors.toList()); 
		
		for(SupportSpell selfBuff : getSelfSupportSpells()) {
			if(activeEffects.contains(selfBuff.getSkillId()))
				continue;
			
			L2Skill skill = SkillTable.getInstance().getInfo(selfBuff.getSkillId(), _fakePlayer.getSkillLevel(selfBuff.getSkillId()));
			
			if(!_fakePlayer.checkUseMagicConditions(skill,true,false))
				continue;
			
			switch(selfBuff.getCondition()) {
				case LESSHPPERCENT:
					if(Math.round(100.0 / _fakePlayer.getMaxHp() * _fakePlayer.getCurrentHp()) <= selfBuff.getConditionValue()) {
						castSelfSpell(skill);						
					}						
					break;
				case NONE:
					castSelfSpell(skill);		
				default:
					break;				
			}
			
		}
	}
		
	public void castSpell(L2Skill skill) {
		if(!_fakePlayer.isCastingNow()) {		
			
			if (skill.getTargetType() == SkillTargetType.TARGET_GROUND)
			{
				if (maybeMoveToPosition((_fakePlayer).getCurrentSkillWorldPosition(), skill.getCastRange()))
				{
					_fakePlayer.setIsCastingNow(false);
					return;
				}
			}
			else
			{
				if(_fakePlayer.getTarget() != null )
					if(!GeoEngine.getInstance().canSeeTarget(_fakePlayer, _fakePlayer.getTarget())){
					_fakePlayer.getFakeAi().moveToPawn(_fakePlayer.getTarget(), 50);
					return;
				}
				
				if (checkTargetLost(_fakePlayer.getTarget()))
				{
					if (skill.isOffensive() && _fakePlayer.getTarget() != null)
						_fakePlayer.setTarget(null);
					
					_fakePlayer.setIsCastingNow(false);
					return;
				}
				
				if (_fakePlayer.getTarget() != null && maybeMoveToPawn(_fakePlayer.getTarget(), skill.getCastRange()))
				{
					_fakePlayer.setIsCastingNow(false);
					return;
				}
				
				if (_fakePlayer.isSkillDisabled(skill)) {}					
			}
			
			if (skill.getHitTime() > 50 && !skill.isSimultaneousCast())
				clientStopMoving(null);
			
			_fakePlayer.doCast(skill);
		}
	}
	
	protected void castSelfSpell(L2Skill skill) {
		if(!_fakePlayer.isCastingNow() && !_fakePlayer.isSkillDisabled(skill)) {		
			
			
			if (skill.getHitTime() > 50 && !skill.isSimultaneousCast())
				clientStopMoving(null);
			
			_fakePlayer.doCast(skill);
		}
	}
	
	protected void clientStopMoving(SpawnLocation loc)
	{
		if (_fakePlayer.isMoving())
			_fakePlayer.stopMove(loc);
		
		_clientMovingToPawnOffset = 0;
		
		if (_clientMoving || loc != null)
		{
			_clientMoving = false;
			
			_fakePlayer.broadcastPacket(new StopMove(_fakePlayer));
			
			if (loc != null)
				_fakePlayer.broadcastPacket(new StopRotation(_fakePlayer.getObjectId(), loc.getHeading(), 0));
		}
	}
	
	protected boolean checkTargetLost(WorldObject target)
	{
		if (target instanceof Player)
		{
			final Player victim = (Player) target;
			if (victim.isFakeDeath())
			{
				victim.stopFakeDeath(true);
				return false;
			}
		}
		
		if (target == null)
		{
			_fakePlayer.getAI().setIntention(CtrlIntention.ACTIVE);
			return true;
		}
		return false;
	}
	
	protected boolean maybeMoveToPosition(Location worldPosition, int offset)
	{
		if (worldPosition == null)
		{
			return false;
		}
		
		if (offset < 0)
			return false;
			
		if (!_fakePlayer.isInsideRadius(worldPosition.getX(), worldPosition.getY(), (int) (offset + _fakePlayer.getCollisionRadius()), false))
		{
			if (_fakePlayer.isMovementDisabled())
				return true;
			
			int x = _fakePlayer.getX();
			int y = _fakePlayer.getY();
			
			double dx = worldPosition.getX() - x;
			double dy = worldPosition.getY() - y;
			
			double dist = Math.sqrt(dx * dx + dy * dy);
			
			double sin = dy / dist;
			double cos = dx / dist;
			
			dist -= offset - 5;
			
			x += (int) (dist * cos);
			y += (int) (dist * sin);
			
			moveTo(x, y, worldPosition.getZ());
			return true;
		}

		return false;
	}	

	protected int getShotId() {
		int playerLevel = _fakePlayer.getLevel();
		if(playerLevel < 20)
			return getShotType() == ShotType.SOULSHOT ? 1835 : 3947;
		if(playerLevel >= 20 && playerLevel < 40)
			return getShotType() == ShotType.SOULSHOT ? 1463 : 3948;
		if(playerLevel >= 40 && playerLevel < 52)
			return getShotType() == ShotType.SOULSHOT ? 1464 : 3949;
		if(playerLevel >= 52 && playerLevel < 61)
			return getShotType() == ShotType.SOULSHOT ? 1465 : 3950;
		if(playerLevel >= 61 && playerLevel < 76)
			return getShotType() == ShotType.SOULSHOT ? 1466 : 3951;
		if(playerLevel >= 76)
			return getShotType() == ShotType.SOULSHOT ? 1467 : 3952;
		
		return 0;
	}
	
	protected int getArrowId() {
		int playerLevel = _fakePlayer.getLevel();
		if(playerLevel < 20)
			return 17; // wooden arrow
		if(playerLevel >= 20 && playerLevel < 40)
			return 1341; // bone arrow
		if(playerLevel >= 40 && playerLevel < 52)
			return 1342; // steel arrow
		if(playerLevel >= 52 && playerLevel < 61)
			return 1343; // Silver arrow
		if(playerLevel >= 61 && playerLevel < 76)
			return 1344; // Mithril Arrow
		if(playerLevel >= 76)
			return 1345; // shining
		
		return 0;
	}
	
	protected void moveToPawn(WorldObject pawn, int offset)
	{
		if (!_fakePlayer.isMovementDisabled())
		{
			if (offset < 10)
				offset = 10;
			
			boolean sendPacket = true;
			if (_clientMoving && (_fakePlayer.getTarget() == pawn))
			{
				if (_clientMovingToPawnOffset == offset)
				{
					if (System.currentTimeMillis() < _moveToPawnTimeout)
						return;
					
					sendPacket = false;
				}
				else if (_fakePlayer.isOnGeodataPath())
				{
					if (System.currentTimeMillis() < _moveToPawnTimeout + 1000)
						return;
				}
			}
			
			_clientMoving = true;
			_clientMovingToPawnOffset = offset;
			_fakePlayer.setTarget(pawn);
			_moveToPawnTimeout = System.currentTimeMillis() + 1000;
			
			if (pawn == null)
				return;
			
			_fakePlayer.moveToLocation(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
			
			if (!_fakePlayer.isMoving())
			{
				//clientActionFailed();
				return;
			}
			
			if (pawn instanceof Creature)
			{
				if (_fakePlayer.isOnGeodataPath())
				{
					_fakePlayer.broadcastPacket(new MoveToLocation(_fakePlayer));
					_clientMovingToPawnOffset = 0;
				}
				else if (sendPacket)
					_fakePlayer.broadcastPacket(new MoveToPawn(_fakePlayer, pawn, offset));
			}
			else
				_fakePlayer.broadcastPacket(new MoveToLocation(_fakePlayer));
		}
	}
	
	public void moveTo(int x, int y, int z)
	{
		if (!_fakePlayer.isMovementDisabled())
		{
			_clientMoving = true;
			_clientMovingToPawnOffset = 0;
			_fakePlayer.moveToLocation(x, y, z, 0);
			
			_fakePlayer.broadcastPacket(new MoveToLocation(_fakePlayer));
			
		}
		//else
		//	clientActionFailed();
	}
	
	protected boolean maybeMoveToPawn(WorldObject target, int offset)
	{
		if (target == null || offset < 0)
			return false;
		
		offset += _fakePlayer.getCollisionRadius();
		if (target instanceof Creature)
			offset += ((Creature) target).getCollisionRadius();
		
		if (!_fakePlayer.isInsideRadius(target, offset, false, false))
		{			
			if (_fakePlayer.isMovementDisabled())
			{
				if (_fakePlayer.getAI().getIntention() == CtrlIntention.ATTACK)
					_fakePlayer.getAI().setIntention(CtrlIntention.IDLE);				
				return true;
			}
			
			if (target instanceof Creature && !(target instanceof Door))
			{
				if (((Creature) target).isMoving())
					offset -= 30;
				
				if (offset < 5)
					offset = 5;
			}
			else
				moveToPawn(target, offset);
			return true;
		}
		
		return false;
	}
}
