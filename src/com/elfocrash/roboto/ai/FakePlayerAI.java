package com.elfocrash.roboto.ai;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.elfocrash.roboto.FakePlayer;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.SpawnLocation;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.Player;
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
	protected boolean _isBusyThinking = false;
	
	public FakePlayerAI(FakePlayer character)
	{
		_fakePlayer = character;
		setup();
		applyDefaultBuffs();
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
	
	public void setBusyThinking(boolean thinking) {
		_isBusyThinking = thinking;
	}
	
	public boolean isBusyThinking() {
		return _isBusyThinking;
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
				if (checkTargetLost(_fakePlayer.getTarget()))
				{
					if (skill.isOffensive() && _fakePlayer.getTarget() != null)
						_fakePlayer.setTarget(null);
					
					_fakePlayer.setIsCastingNow(false);
					return;
				}
				
				if (_fakePlayer.getTarget() != null)
				{
					if(maybeMoveToPawn(_fakePlayer.getTarget(), skill.getCastRange())) {
						return;
					}
				}
				
				if (_fakePlayer.isSkillDisabled(skill)) {
					return;
				}					
			}
			
			if (skill.getHitTime() > 50 && !skill.isSimultaneousCast())
				clientStopMoving(null);
			
			_fakePlayer.doCast(skill);
		}else {
			_fakePlayer.forceAutoAttack((Creature)_fakePlayer.getTarget());
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
	
	public void moveTo(int x, int y, int z)	{
		
		if (!_fakePlayer.isMovementDisabled())
		{
			_clientMoving = true;
			_clientMovingToPawnOffset = 0;
			_fakePlayer.moveToLocation(x, y, z, 0);
			
			_fakePlayer.broadcastPacket(new MoveToLocation(_fakePlayer));
			
		}
	}
	
	protected boolean maybeMoveToPawn(WorldObject target, int offset) {
		
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
			
			moveToPawn(target, offset);
			
			return true;
		}
		
		if(!GeoEngine.getInstance().canSeeTarget(_fakePlayer, _fakePlayer.getTarget())){
			_fakePlayer.setIsCastingNow(false);
			moveToPawn(target, 50);			
			return true;
		}
		
		
		return false;
	}	
	
	public abstract void thinkAndAct(); 
	protected abstract int[][] getBuffs();
}
