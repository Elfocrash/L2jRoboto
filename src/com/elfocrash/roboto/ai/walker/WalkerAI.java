package com.elfocrash.roboto.ai.walker;

import java.util.LinkedList;
import java.util.Queue;

import com.elfocrash.roboto.FakePlayer;
import com.elfocrash.roboto.ai.FakePlayerAI;
import com.elfocrash.roboto.model.WalkNode;
import com.elfocrash.roboto.model.WalkerType;

import net.sf.l2j.commons.random.Rnd;

public abstract class WalkerAI extends FakePlayerAI {

	protected Queue<WalkNode> _walkNodes;
	private WalkNode _currentWalkNode;
	private int currentStayIterations = 0;
	protected boolean isWalking = false;
	
	public WalkerAI(FakePlayer character) {
		super(character);
	}
	
	public Queue<WalkNode> getWalkNodes(){
		return _walkNodes;
	}
	
	protected void addWalkNode(WalkNode walkNode) {
		_walkNodes.add(walkNode);
	}
	
	@Override
	public void setup() {
		super.setup();		
		_walkNodes = new LinkedList<>(); 
	}

	@Override
	public void thinkAndAct() {
		setBusyThinking(true);		
		handleDeath();
		
		if(_walkNodes.isEmpty())
			return;
		
		if(isWalking) {
			if(userReachedDestination(_currentWalkNode)) {
				if(currentStayIterations < _currentWalkNode.getStayIterations() ) {
					currentStayIterations++;
					setBusyThinking(false);
					return;
				}				
				_currentWalkNode = null;
				currentStayIterations = 0;
				isWalking = false;
			}			
		}
		
		if(!isWalking && _currentWalkNode == null) {
			switch(getWalkerType()) {
				case RANDOM:
					_currentWalkNode = (WalkNode) getWalkNodes().toArray()[Rnd.get(0, getWalkNodes().size() - 1)];
					break;
				case LINEAR:
					_currentWalkNode = getWalkNodes().poll();
					_walkNodes.add(_currentWalkNode);
					break;
			}
			_fakePlayer.getFakeAi().moveTo(_currentWalkNode.getX(), _currentWalkNode.getY(), _currentWalkNode.getZ());	
			isWalking = true;
		}
		
		setBusyThinking(false);
	}

	@Override
	protected int[][] getBuffs() {
		return new int[0][0]; 
	}

	protected boolean userReachedDestination(WalkNode targetWalkNode) {
		//TODO: Improve this with approximate equality and not strict
		if(_fakePlayer.getX() == targetWalkNode.getX()
			&& _fakePlayer.getY() == targetWalkNode.getY() 
			&& _fakePlayer.getZ() == targetWalkNode.getZ())
			return true;
		
		return false;
	}
	
	protected abstract WalkerType getWalkerType();
}
